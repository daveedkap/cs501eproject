# Pulsify

A context-aware music companion for Android, built with Jetpack Compose. Pulsify reads movement from the phone's accelerometer, combines it with the user's real Spotify library, and uses Google's Gemini API to suggest and explain music that fits the moment.

**Course:** CS-501 E1 — Mobile Application Development
**Team:** David Kaplansky, Parthiv Krishnan

---

## Features

- **Activity-aware home screen.** The accelerometer feeds a simple threshold-based classifier that tags the user as *sitting / walking / running*. The classification drives a chat-style assistant on the home tab.
- **Voice and text input.** The home assistant accepts spoken input through Android's `SpeechRecognizer` and falls back to text mode for quiet environments.
- **Spotify integration via PKCE OAuth.** The user signs in to Spotify in a Custom Tab; Pulsify exchanges the auth code for access and refresh tokens and pulls the user's top tracks and recently played items through the Web API.
- **Contextual playlist generation.** Pulsify sends the detected activity plus a track list to the Gemini 2.5 Flash-Lite endpoint; Gemini returns a conversational explanation of why the music fits the moment, and the playlist screen renders the real Spotify tracks underneath.
- **Persistent sessions.** Each context-music pairing is saved to a Room database with timestamp, activity, and track count. The Sessions screen surfaces the history; the underlying preferences can be reused on subsequent generations.
- **Location-aware map.** A Maps Compose view requests fine location and plots saved sessions as markers, so the user can see where each music context was recorded.
- **Settings screen.** Connect / disconnect Spotify, toggle text-only input, and clear the assistant thread.

## Architecture

The app is structured MVVM-style with a single repository sitting between the UI and the underlying data sources.

```
app/src/main/java/com/pulsify/android/
├── MainActivity.kt               # Single-activity host for Compose
├── PulsifyApplication.kt         # Application-level container init
├── di/
│   ├── AppContainer.kt           # Manual DI: wires Room, network, repository
│   └── PulsifyViewModelFactory.kt
├── navigation/
│   └── PulsifyDestinations.kt    # NavHost + bottom-bar destinations
├── domain/
│   └── Models.kt                 # UI/domain-facing data classes
├── data/
│   ├── local/                    # Room: database, DAO, entity
│   ├── remote/                   # Retrofit services + auth manager
│   │   ├── SpotifyAuthManager.kt # PKCE OAuth flow
│   │   ├── SpotifyWebService.kt  # /me/top/tracks, /me/player/recently-played
│   │   ├── GeminiPlaylistService.kt
│   │   └── NetworkModule.kt      # OkHttp + Moshi + Retrofit setup
│   ├── sensor/
│   │   └── ActivityClassifier.kt # Accelerometer → activity label
│   ├── location/
│   │   └── LocationReader.kt     # FusedLocationProviderClient wrapper
│   └── repository/
│       └── PulsifyRepository.kt  # Single source of truth
└── ui/
    ├── PulsifyApp.kt             # Scaffold + bottom bar
    ├── theme/                    # Material 3 theme + typography
    ├── home/                     # HomeScreen + HomeViewModel
    ├── playlist/                 # PlaylistScreen + PlaylistViewModel
    ├── sessions/                 # SessionsScreen + SessionsViewModel
    ├── map/                      # MapScreen + MapViewModel
    └── settings/                 # SettingsScreen + SettingsViewModel
```

Key choices:

- **Repository as the single source of truth.** All UI flows go through `PulsifyRepository`, which talks to Room, the Spotify Web API, and the Gemini API. ViewModels never touch Retrofit or the DAO directly; this keeps each screen testable and means new features extend the repository rather than rewiring UI.
- **Manual DI via `AppContainer`.** We didn't introduce Hilt for a project this size — `AppContainer` constructs the database, the network module, and the repository once at `Application` start, and `PulsifyViewModelFactory` hands typed ViewModels to the navigation graph.
- **Coroutines + Flow for async work.** Sensor reads, network calls, and Room queries are exposed as suspend functions or Flows; UI state is hoisted in ViewModels with `StateFlow`.
- **Graceful degradation.** Each remote call has a fallback: mock tracks when Spotify is not connected, activity-only messages when Gemini is unavailable, an empty session list when Room is empty. The app stays usable in all three cases.

## Setup

1. Clone the repository and open it in Android Studio (Hedgehog or newer recommended).
2. Create a `local.properties` file at the project root with the following keys:

   ```properties
   sdk.dir=/path/to/Android/sdk
   MAPS_API_KEY=<Google Maps API key>
   SPOTIFY_CLIENT_ID=<Spotify Developer Dashboard client ID>
   SPOTIFY_CLIENT_SECRET=<Spotify Developer Dashboard client secret>
   GEMINI_API_KEY=<Google AI Studio API key>
   ```

3. In the Spotify Developer Dashboard, set the redirect URI to `com.pulsify.android://callback`. The manifest declares the matching intent filter on `MainActivity`.
4. In Google AI Studio, generate a Gemini API key with access to `gemini-2.5-flash-lite`. The free tier is sufficient.
5. Sync Gradle and run on a physical device (preferred — real accelerometer and location) or an emulator with a mock location set.

## Permissions

Declared in the manifest and requested at runtime where required:

- `INTERNET` — Spotify and Gemini APIs.
- `ACCESS_COARSE_LOCATION` / `ACCESS_FINE_LOCATION` — map markers and location-tagged sessions.
- `RECORD_AUDIO` — voice input via `SpeechRecognizer`.
- The accelerometer is declared as a required hardware feature.

## AI Usage Notes

Per the CS-501 E1 generative AI policy, this section discloses how AI tools were used while building Pulsify.

**Tool used:** Cursor (with Anthropic Claude / Composer agent), used as an IDE assistant alongside Android Studio. We also used Gemini at runtime as a *product feature* (the contextual playlist explanation) — that is separate from AI-assisted development and is documented above under Features.

**How we used it (matching the syllabus's "Encouraged" categories):**

- *Prototyping & boilerplate.* Initial Gradle setup, theme/typography scaffolding, the empty `Scaffold` + bottom-nav skeleton in `PulsifyApp.kt`, Room boilerplate (entity, DAO method signatures), and Retrofit service interface stubs. All of this was scaffolded with Cursor and then refined and tested by us.
- *Debugging support.* Compose recomposition issues (state hoisted at the wrong level), KSP/Room migration warnings, OkHttp interceptor ordering for Spotify token refresh, and a handful of Material 3 import / API mismatches. For each error we asked Cursor to explain the root cause, not just to apply a fix.
- *Documentation.* KDoc summaries on `PulsifyRepository`, parts of this README, and the structural outline of `PRESENTATION.md` were drafted with AI assistance and edited by us for accuracy.
- *Code review and learning.* We asked Cursor to walk us through Compose patterns we hadn't used before — `collectAsStateWithLifecycle`, the difference between `LaunchedEffect`, `DisposableEffect`, and `produceState`, and the recommended way to expose Room Flows to a ViewModel.

**Representative prompts:**

- "Generate a Retrofit interface for Spotify's `/me/top/tracks` endpoint with Moshi-Kotlin models."
- "Why is `collectAsState` causing a leak when the screen is offscreen, and what's the lifecycle-aware version?"
- "Explain PKCE for Spotify OAuth in an Android app and how the code verifier should be stored."
- "Suggest a minimal threshold-based activity classifier from a 3-axis accelerometer."

**Kept:**

- The Retrofit/Moshi service stubs for Spotify and Gemini, after we corrected types and added missing fields.
- The PKCE explanation, used to inform our `SpotifyAuthManager` implementation.
- README phrasing and KDoc comments after editing.
- Several lifecycle-aware Compose patterns (`collectAsStateWithLifecycle`, `produceState`).

**Discarded:**

- An initial AI suggestion to use Hilt for DI — we kept manual DI via `AppContainer` because the project is small enough not to justify Hilt's KSP overhead.
- An over-engineered "preference learning" Room schema with embedded JSON — replaced with the flatter `ActivitySessionEntity` we have now.
- Several full Composable bodies that read as generic templates rather than fitting our state model; we rewrote those screens against our actual ViewModels.

**What was *not* AI-generated:**

- The product direction and feature set (movement → music → conversational explanation) this came out of our team proposal.
- The architectural decisions: choosing a single repository, manual DI, the navigation graph layout, the fallback strategy, and where state is hoisted.
- The Spotify and Gemini integration logic past the initial Retrofit stubs — token refresh, error handling, request batching.
- The accelerometer classification thresholds, which we tuned empirically on a physical device.
- The session-persistence schema and the way the map and sessions screens share state.
- This README's structure and the architecture write-up.

---

## Team

**David Kaplansky** and **Parthiv Krishnan** pair-programmed on David's machine throughout the project, which is why the Git history shows primarily a single author. Both teammates contributed to design decisions, implementation, and the project update presentation.

---

CS-501 E1 — Spring 2026
