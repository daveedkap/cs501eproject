# Pulsify

A context-aware music companion for Android, built with Jetpack Compose. Pulsify estimates the user’s movement from the accelerometer, maps that to an activity label (_sitting / walking / running_), and builds short playlists from the user’s Spotify library. A Gemini model produces short conversational copy explaining how the current track list fits the moment. Sessions and lightweight “rules” metadata are persisted with Room; the Map tab plots saved sessions when coordinates are available.

**Course:** CS-501 E1 — Mobile Application Development  
**Team:** David Kaplansky, Parthiv Krishnan

---

## 1. Project overview

Pulsify is a single-activity Compose app. The user sees a home “assistant” surface driven by detected activity, optional voice or text input, and a primary action to generate a contextual mix. When Spotify is linked, the app loads the user’s **top tracks** (and falls back to **recently played** if needed), applies an **MVP bucketing strategy** that does **not** use Spotify’s deprecated Audio Features API, and randomly draws a 10-track subset per activity bucket so repeated “Generate” actions do not always return the same order. Voice input triggers an additional reshuffle from the same bucket so the playlist visibly updates. When Spotify is unavailable, the app uses built-in mock tracks and still calls Gemini for generic activity copy where configured.

---

## 2. Architecture

### 2.1 High-level pattern

The codebase follows **MVVM** with a **single repository** as the boundary between UI and data:

- **UI:** Jetpack Compose screens under `ui/`, each backed by a small `ViewModel` that exposes `StateFlow` values and forwards user actions.
- **Domain:** Plain Kotlin types in `domain/` (e.g. `DetectedActivity`, `Track`, `PlaybackUiState`, `ChatMessage`) decouple UI from Room and network DTOs.
- **Data:** `data/local` (Room), `data/remote` (Retrofit + Spotify OAuth), `data/sensor`, `data/location`, and `data/repository/PulsifyRepository.kt` as the **single source of truth** for chat messages, playback state, Spotify link status, and catalog prefetch.

ViewModels do **not** call Retrofit or the DAO directly; they depend on `PulsifyRepository`. That keeps navigation-friendly ViewModels thin and concentrates side effects (network, DB, token refresh) in one place.

### 2.2 Dependency injection

**Manual composition** is used instead of Hilt: `PulsifyApplication` builds an `AppContainer` once (database, `OkHttp`-based Retrofit clients, `SpotifyAuthManager`, `PulsifyRepository`, `LocationReader`). `PulsifyViewModelFactory` constructs screen ViewModels with the required dependencies. This matches the small scale of the project and avoids extra KSP modules.

### 2.3 Asynchrony and state

- **Kotlin coroutines** with `Dispatchers.IO` for repository work (Spotify, Room inserts, Gemini).
- **`StateFlow`** in the repository (`messages`, `playback`, `spotifyLinked`, `textModePreferred`) and in ViewModels for screen-local UI (e.g. loading, draft text, speech partials).
- **Room `Flow`** for sessions and rules tables, collected in ViewModels with lifecycle-aware collection in Compose where applicable.

### 2.4 Navigation

`PulsifyApp.kt` hosts a `NavHost` with a bottom bar for Home, Sessions, Map, and Settings. The playlist is a separate full-screen destination pushed on top when the user opens the current mix.

### 2.5 End-to-end data flow

On **playlist generation**, `HomeViewModel` calls `requestPlaylistForActivity` with the current `DetectedActivity`, optional user note, and last known coordinates. The repository ensures the **session catalog** is loaded (top 50 tracks + per-session fictional BPM buckets), selects 10 tracks for the activity bucket, updates `PlaybackUiState`, inserts an `ActivitySessionEntity`, upserts a `ContextMusicRuleEntity`, and may append Gemini- or fallback-generated chat lines.

### 2.6 Session catalog (context → tracks, without Audio Features)

Because Spotify’s **Audio Features** endpoint is deprecated for this MVP, track “energy” is approximated as follows (all implemented in `PulsifyRepository`):

1. After link (or on first need), fetch up to **50 top tracks** (`time_range=medium_term`), ordered as returned by Spotify; if empty, fall back to **recently played** (distinct tracks, capped at 50).
2. Assign each track a **random fictional BPM** once per app session, by **index** in that list: ranks 1–17 → 70–94 BPM, 18–34 → 95–118, 35–50 → 119–142. These values are stored in memory for the session only (see `CatalogEntry` and `sessionCatalog`).
3. Map **Sitting →** slow bucket, **Walking →** medium, **Running →** fast, **Unknown →** prefer medium then slow/fast as available.
4. **Randomly sample 10 tracks without replacement** from the bucket; the repository remembers the last set of track IDs per activity and **reshuffles** when possible so back-to-back generations are not identical when the pool allows.
5. **Voice:** after recognition, the repository runs a similar draw for the same activity bucket and refreshes playback so the list changes even though selection is not semantically tied to speech content.

`SpotifyAuthManager` clears the in-memory catalog on disconnect (`clearSpotifySessionCatalog`); `MainActivity` and `HomeViewModel` prefetch the catalog when appropriate.

---

## 3. Database usage and schema

### 3.1 Technology and file

- **Room** (`androidx.room`), version **1**, database file name **`pulsify.db`** (`PulsifyDatabase.kt`).
- **`exportSchema = false`** for simplicity; **`fallbackToDestructiveMigration()`** is enabled so schema upgrades during development replace the file (acceptable for a course prototype; a production app would ship migrations).

### 3.2 Tables and columns

| Table                 | Entity class             | Purpose                                                                                                            |
| --------------------- | ------------------------ | ------------------------------------------------------------------------------------------------------------------ |
| `activity_sessions`   | `ActivitySessionEntity`  | One row each time the user generates a contextual playlist (or mock equivalent path that still records a session). |
| `context_music_rules` | `ContextMusicRuleEntity` | Lightweight association between activity type and a human-readable “rule” line; surfaced on Sessions / rules UI.   |

**`activity_sessions`**

| Column            | Type              | Description                                                            |
| ----------------- | ----------------- | ---------------------------------------------------------------------- |
| `id`              | `Long` (PK, auto) | Surrogate primary key.                                                 |
| `timestampMillis` | `Long`            | When the session was recorded.                                         |
| `activityType`    | `String`          | Enum name of `DetectedActivity` at generation time.                    |
| `latitude`        | `Double?`         | Optional; from `LocationReader` when permission granted.               |
| `longitude`       | `Double?`         | Optional.                                                              |
| `playlistSummary` | `String?`         | Short summary line (track count, activity label, rough BPM/mood text). |
| `trackCount`      | `Int`             | Number of tracks in that playlist.                                     |

**`context_music_rules`**

| Column            | Type              | Description                                                       |
| ----------------- | ----------------- | ----------------------------------------------------------------- |
| `id`              | `Long` (PK, auto) | Surrogate primary key.                                            |
| `activityType`    | `String`          | Activity key for the rule.                                        |
| `associationNote` | `String`          | Descriptive label built from the mix (e.g. mood / BPM phrasing).  |
| `useCount`        | `Int`             | Incremented/updated when the repository upserts after generation. |

### 3.3 DAO operations

`PulsifyDao`:

- **`observeSessions()`** — `Flow` of all rows from `activity_sessions` ordered by `timestampMillis` descending (Sessions screen).
- **`insertSession(session)`** — suspend insert; returns row id.
- **`observeRules()`** — `Flow` of `context_music_rules` ordered by `useCount` descending.
- **`upsertRule(rule)`** — `@Insert(OnConflictStrategy.REPLACE)` for upsert semantics.

The repository is the only writer; ViewModels observe flows through the repository’s exposed `sessions` and `contextRules` flows.

---

## 4. External APIs, sensors, and platform usage

### 4.1 Spotify Web API (Retrofit)

Base URL: `https://api.spotify.com/` (`NetworkModule.spotifyRetrofit()`). Endpoints used in `SpotifyWebService`:

| Method | Endpoint                         | Usage                                                                                    |
| ------ | -------------------------------- | ---------------------------------------------------------------------------------------- |
| GET    | `v1/me`                          | Profile display name after login (`fetchUserProfileName`).                               |
| GET    | `v1/me/top/tracks`               | Primary seed for the session catalog (`limit=50`, `time_range=medium_term`).             |
| GET    | `v1/me/player/recently-played`   | Fallback when top tracks are empty; also used historically for smaller limits elsewhere. |
| GET    | `v1/search`                      | Available for search (wired in API layer).                                               |
| GET    | `v1/me/player/devices`           | Device enumeration for playback scenarios.                                               |
| PUT    | `v1/me/player/play`              | Start playback with a list of track URIs and an offset index.                            |
| PUT    | `v1/me/player/pause`             | Pause playback.                                                                          |
| POST   | `v1/me/player/next` / `previous` | Skip controls (API present; UI may use repository paths).                                |

**OAuth 2.0 with PKCE** is implemented in `SpotifyAuthManager` (Custom Tabs redirect to `com.pulsify.android://callback` on `MainActivity`). Scopes include top read, recently played, and playback modification as declared in code. Tokens are stored in app-private `SharedPreferences`; refresh is handled inside `getValidAccessToken()`.

**Note:** The app intentionally does **not** call the deprecated **Audio Features** API; tempo/energy for bucketing uses the fictional BPM scheme in §2.6.

### 4.2 Google Gemini API (Retrofit)

Base URL: `https://generativelanguage.googleapis.com/` (`NetworkModule.geminiRetrofit()`).

- **`GeminiPlaylistService`** posts to **`v1beta/models/gemini-2.0-flash-lite:generateContent`** with a JSON body (`GeminiGenerateRequest` / Moshi models) and the API key as query parameter `key`.
- Used for short assistant messages: **with** a concrete track list when Spotify data exists, or **generic** activity/location copy when using mocks or missing keys.

If `GEMINI_API_KEY` is blank, the repository skips network calls and uses fixed fallback strings so the UI still advances.

### 4.3 Google Maps SDK

The manifest declares `com.google.android.geo.API_KEY` from Gradle’s `MAPS_API_KEY` placeholder. The Map screen uses **Maps Compose** to show session markers when lat/lon exist.

### 4.4 Sensors

- **Accelerometer (`Sensor.TYPE_ACCELEROMETER`)** — `HomeViewModel` registers as a `SensorEventListener`, forwarding `x,y,z` to **`ActivityClassifier`**. The classifier computes magnitude, subtracts an approximate gravity term, smooths the signal, and applies thresholds: below `SITTING_MAX` → Sitting, below `WALKING_MAX` → Walking, else Running (`ActivityClassifier.kt`). The manifest marks **`android.hardware.sensor.accelerometer`** as **required**.

### 4.5 Location

**`LocationReader`** wraps **`FusedLocationProviderClient.getCurrentLocation`** (balanced priority) with **`lastLocation`** fallback. Coordinates are passed into playlist generation for Gemini context and stored on the session row when available. Runtime permissions: **fine** (and coarse) location as declared in the manifest.

### 4.6 Voice input

**`SpeechRecognizer`** on the home screen (after **`RECORD_AUDIO`** permission). Partial results update UI text; final transcript is appended as a user chat line. The repository may **refresh** the current Spotify-based playlist from the same activity bucket so the track list changes after speech (MVP “connected” behavior).

### 4.7 Package map (reference)

```
app/src/main/java/com/pulsify/android/
├── MainActivity.kt              # Compose host, Spotify redirect handling
├── PulsifyApplication.kt
├── di/                          # AppContainer, ViewModel factory
├── navigation/
├── domain/                      # DetectedActivity, Track, chat, playback models
├── data/
│   ├── local/                   # Room entities, DAO, database
│   ├── remote/                  # Retrofit, Moshi, Spotify auth, Gemini
│   ├── sensor/                  # ActivityClassifier
│   ├── location/                # LocationReader
│   └── repository/              # PulsifyRepository
└── ui/                          # Compose screens + ViewModels + theme
```

---

## 5. Setup and local configuration

1. Clone the repository and open it in **Android Studio** (Hedgehog or newer recommended).
2. Create **`local.properties`** at the project root (Android Studio usually creates this) with at least:

   ```properties
   sdk.dir=/path/to/Android/sdk
   MAPS_API_KEY=<Google Maps SDK key>
   SPOTIFY_CLIENT_ID=<Spotify Dashboard client ID>
   SPOTIFY_CLIENT_SECRET=<Spotify Dashboard client secret>
   GEMINI_API_KEY=<Google AI Studio / Gemini API key>
   ```

3. In the **Spotify Developer Dashboard**, add redirect URI **`com.pulsify.android://callback`** to match `MainActivity`’s intent filter and `BuildConfig.SPOTIFY_REDIRECT_URI`.
4. Ensure the Gemini project allows the model named in **`GeminiPlaylistService`** (currently **`gemini-2.0-flash-lite`** in code; adjust if your key targets a different model name).
5. Run on a **physical device** when possible (accelerometer + real location + microphone).

---

## 6. Permissions (runtime and manifest)

| Permission                                        | Purpose                                    |
| ------------------------------------------------- | ------------------------------------------ |
| `INTERNET`                                        | Spotify, Gemini, Maps tiles.               |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Single-shot location for sessions and map. |
| `RECORD_AUDIO`                                    | `SpeechRecognizer` on Home.                |

Hardware feature: **accelerometer** required.

---

## 7. Current limitations

For this MVP, two external-platform constraints affect behavior:

1. **Spotify Audio Features endpoint is no longer usable for development-mode apps.**  
   Pulsify cannot rely on Spotify-provided BPM/energy values through that endpoint, so contextual track selection uses a session-scoped fallback: top tracks are bucketed with fictional BPM ranges (slow / medium / fast) and then sampled randomly per activity.

2. **Gemini API rate limits can throttle or fail suggestion calls.**  
   When quota or rate limits are reached, Gemini reasoning text may be delayed or unavailable. The app is designed to degrade gracefully by still generating a playlist (or mock mix) and showing fallback assistant messaging so core flow remains usable.

---

## 8. Team responsibilities and contributions

We did a lot of pair programming at the beginning of this project, helping us build most of our initial UI and our most basic API integrations with Spotify. After this point, David continued to work on making UI improvements, google Gemini API integration, while I (Parthiv) worked on the spotify API integration, specifically a bulk of the code behind the generate contextual playlist button.

---

## 9. AI reflection (course deliverable)

1. In the beginning of the project we used AI to help us build a skeleton for us to work off of, and also to help divide tasks between us. Beyond that we also used AI for debugging and coming up with ideas for getting around different constraints that we were facing (limitations with spotify API endpoints, rate limits, etc)

2. AI helped with the architecture and testing of our application especially. It helped us plan how we were going to build this application starting from scratch. Moreover, it gave us implementation suggestions like having fallback behaviors and user facing error states to ensure that our app doesn't fail completely when certain parameters are not available, and also to ensure that the user has some understanding of what is happening.

3. AI helped us accelerate our workflows, by allowing us to troubleshoot and debug problems a lot faster than we otherwise would have been able too.

4. There were a fair number of times when AI would be getting things wrong, it wouldn't understand exactly what we were asking for and would give us answers that didn't really help. When we were debugging the situation with audio-features endpoint it got into a rabbit hole of continually trying to use it despite the fact that we told the AI it was deprecated.

---

## 10. Final Presentation Link

[Final presentation](https://docs.google.com/presentation/d/12P45zprH_PN8M80EbWcQCKcjZ8gpRUP4OAyN8CpYwvU/edit?usp=sharing)

CS-501 E1 — Spring 2026
