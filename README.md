# Pulsify

Context-aware music companion for Android (Jetpack Compose).

## Setup

Copy `local.properties.example` values into `local.properties` at the project root:

```properties
sdk.dir=/path/to/Android/sdk
MAPS_API_KEY=<Google Maps API key>
SPOTIFY_CLIENT_ID=<Spotify Developer Dashboard client ID>
SPOTIFY_CLIENT_SECRET=<Spotify Developer Dashboard client secret>
GEMINI_API_KEY=<Google AI Studio API key>
```

Spotify redirect URI must be set to `com.pulsify.android://callback` in the Spotify Developer Dashboard.

## Responsible use of AI

Generative AI tools were used as a supporting assistant while building this project — mainly for boilerplate, structure suggestions, and common Android API patterns. Design decisions and product direction were reviewed and owned by the team. Suggestions that were wrong or over-scoped were revised or discarded.

## Team

**David Kaplansky** and **Parthiv Krishnan** — pair programmed on David's machine, which is why Git history shows a single author. Both teammates contributed throughout.

---

CS501E
