# Pulsify — demo script & talking points (Q1.9)

Use this as a **structured outline** for a **live** class demo or oral walkthrough. It is **not** a substitute for a video if your instructor explicitly requires one—check the syllabus.

**Suggested length:** about **3–4 minutes** (adjust to the time slot).

---

## Before you start (15 seconds)

- **One sentence hook:** “Pulsify is a music companion that reads **movement context** from the phone and suggests a playlist-shaped experience; **Spotify and cloud AI are stubbed** this milestone because of access and scope.”
- **Device:** real phone or emulator with **accelerometer**; for Map, **location** helps (emulator: set a mock location if needed).

---

## Demo flow (follow in order)

### 1. Home — context & conversation (~60s)

- Point out **Material 3** layout: **card**, **activity chip** (sitting / walking / running from the accelerometer).
- **Move the device** briefly so the chip updates—say you’re using **simple thresholds**, not ML yet (matches the proposal).
- Tap the **large mic target:** explain it **simulates** a voice prompt (no speech-to-text yet) and appends assistant text.
- If **text mode** is on in Settings, mention **quiet contexts** (library); otherwise say it’s available in Settings.

**Say:** “We kept **big touch targets** and a **clean** home screen for use while moving.”

### 2. Generate mix → Playlist (~45s)

- Tap **“Generate contextual mix (mock AI + Spotify).”**
- Mention **coroutines + delay** simulating latency; **Retrofit types** exist but **`USE_MOCK_APIS`** keeps calls local.
- Land on **Playlist:** **play / pause / skip** update **local state only**—**no Spotify App Remote** yet.

**Say:** “Playback is **deliberately fake** so we can prove UI and state flow before dealing with OAuth and SDK limits.”

### 3. Sessions — persistence (~30s)

- Bottom nav → **Sessions.**
- Show **Room**-backed list: timestamp, activity, optional **lat/lng** if location was granted.

**Say:** “This is our **persistence story** early: sessions and stub ‘association’ rows for later personalization.”

### 4. Map — location (~30s)

- **Map** tab → grant **fine location** if prompted.
- Show **coordinates** and **map + marker** (Maps API key in `local.properties`, not in Git).

**Say:** “Location tags **context** for the proposal; map proves **permissions + Google Maps Compose** wiring.”

### 5. Settings — UX & stubs (~30s)

- **Simulate Spotify link** toggle → UI state only.
- **Text mode**, **clear thread**, short **accessibility** note on screen.

**Say:** “We documented what’s **real vs simulated** so graders and future us aren’t surprised.”

### 6. Architecture close (~30s)

- **Bottom nav + playlist** as separate destination; **ViewModels** per feature; **repository** in the middle.

**Say:** “We aimed for **MVVM-shaped** separation so the next milestone is mostly **swapping stubs for real APIs**.”

---

## Decisions worth naming (if asked)

| Topic | One-line answer |
|--------|------------------|
| Why mock Spotify / Gemini? | Access, auth complexity, and milestone scope; UI and data flow first. |
| Why Room now? | Sessions and rules give a clear path to “memory” in the proposal. |
| Why sensors? | Core differentiator vs desktop—**movement context**. |
| Why Map + location? | Ties sessions to **place** without claiming full routing features. |

---

## Optional closing line

“We have a **small, coherent vertical slice**: sense → suggest → show tracks → save session → map context, with **honest stubs** where external APIs would go next.”
