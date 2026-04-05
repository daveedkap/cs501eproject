# Pulsify — Project Update 1 demonstration outline

**Team:** David Kaplansky, Parthiv Krishnan · **Course:** CS501E  

This document is our **planned structure** for the Project Update 1 walkthrough (approximately **3–4 minutes**). We will demonstrate the app **live** on a device or emulator.

---

## Opening

We will introduce **Pulsify** as a music companion that uses **movement context** from the phone to drive suggestions. For this milestone, **Spotify and cloud AI are intentionally stubbed**—Spotify’s access model and API setup are a poor fit for a short milestone, and we wanted a **working UI and data path** first. Our proposal describes the full vision; this build proves the **foundation**.

We will use hardware with an **accelerometer** (physical device or emulator). For the Map screen, **location** improves the story; on an emulator we can set a **mock location** if needed.

---

## 1. Home — context and conversation (~1 min)

We will show the **Material 3** home experience: the **card**, the **activity chip** (sitting / walking / running) driven by the **accelerometer**, and **simple threshold-based** classification—not ML yet, which matches our scoped proposal.

We will **move the device** so the chip updates. We will tap the **large mic control** and explain that it **simulates** a voice prompt: we have **not** integrated speech-to-text yet, but the interaction pattern matches how we want the product to feel during motion. If **text mode** is enabled in Settings, we will note that it supports **quiet environments** (e.g. a library); otherwise we will mention that the toggle lives in Settings.

**Planned wording:** *“We prioritized **large touch targets** and a **minimal** home screen so the app stays usable while someone is moving.”*

---

## 2. Generate mix → Playlist (~45 s)

We will tap **“Generate contextual mix (mock AI + Spotify).”** We will explain that **coroutines** and a short **delay** stand in for network latency, that **Retrofit** and DTOs are in place for a future Gemini path, and that **`USE_MOCK_APIS`** keeps everything **local** for now.

On the **Playlist** screen we will use **play, pause, and skip** and clarify that state is **entirely local**—there is **no Spotify App Remote** or Web API playback in this build.

**Planned wording:** *“Playback is **simulated on purpose** so we could nail **state and navigation** before investing in OAuth and SDK constraints.”*

---

## 3. Sessions — persistence (~30 s)

We will open **Sessions** from the bottom navigation and show the **Room**-backed list: **timestamps**, **detected activity**, and **optional coordinates** when location permission was granted.

**Planned wording:** *“This is our early **persistence** layer: saved sessions plus placeholder rows for **context–music associations** we describe in the proposal.”*

---

## 4. Map — location (~30 s)

On the **Map** tab we will request **fine location** if the system prompts us. We will show **coordinates** and the **map with a marker**. Our **Maps API key** lives in **`local.properties`** and is **not** committed to the repository.

**Planned wording:** *“Location supports the **context** story in our proposal; this screen shows **permissions** and **Maps Compose** wired end-to-end.”*

---

## 5. Settings — UX and transparency (~30 s)

We will briefly show **Simulate Spotify link** (UI-only), **text mode**, **clear assistant thread**, and the short **accessibility** note on the screen.

**Planned wording:** *“We tried to make it obvious what is **real** versus **stubbed**, so the milestone stays honest about scope.”*

---

## 6. Architecture (~30 s)

We will summarize **navigation** (tabs plus a separate **playlist** destination), **ViewModels** per area, and a **repository** between the UI and **Room / remote-shaped** code.

**Planned wording:** *“We structured the app in an **MVVM-style** way so the next step is largely **replacing mocks with real integrations** rather than rewriting the UI.”*

---

## Anticipated questions

| Topic | Our response |
|--------|----------------|
| Why mock Spotify and Gemini? | **Access and auth complexity**, plus milestone time; we focused on **flow and architecture** first. |
| Why Room this early? | **Sessions** and association stubs match the proposal’s **memory** direction. |
| Why sensors? | **Movement context** is the main reason the product is **mobile-first**, not a desktop app. |
| Why map and location? | **Place** enriches context; we are **not** claiming full mapping or routing features. |

---

## Closing

We will end with a single summary line:

*“In this update we deliver a **small vertical slice**: **sense** movement → **suggest** a mix → **show** tracks → **persist** a session → **tie** optional location on a map, with **clear stubs** where external services will plug in next.”*
