# Pulsify — Project Update demonstration outline

**Team:** David Kaplansky, Parthiv Krishnan · **Course:** CS501E  

This document is our **planned structure** for the Project Update walkthrough (approximately **3–4 minutes**). We will demonstrate the app **live** on a device or emulator.

---

## Opening

We will introduce **Pulsify** as a context-aware music companion that reads **movement** from the phone's accelerometer and combines it with the user's **real Spotify library** and **Gemini AI** to suggest music that fits the moment.

We will use hardware with an **accelerometer** (physical device or emulator). For the Map screen, **location** improves the story; on an emulator we can set a **mock location** if needed.

---

## 1. Home — context and conversation (~1 min)

We will show the **Material 3** home experience: the **card**, the **activity chip** (sitting / walking / running) driven by the **accelerometer**, and **simple threshold-based** classification.

We will **move the device** so the chip updates. We will tap the **mic control** to demonstrate **real voice-to-text** via Android's SpeechRecognizer — what the user says is displayed in the chat, and the bot responds contextually. We will also show **text mode** for quiet environments.

**Planned wording:** *"We prioritized **large touch targets** and a **minimal** home screen so the app stays usable while someone is moving."*

---

## 2. Spotify integration & generate mix (~45 s)

We will show the **Settings** screen where we connect to **Spotify** via **OAuth PKCE** — the real authorization flow opens in a browser, the user signs in, and Pulsify receives an access token. We will show it says **"Connected as [name]"** after auth.

We will tap **"Generate contextual mix."** Behind the scenes, Pulsify:
1. Fetches the user's **top tracks** from the Spotify API
2. Sends the track list plus the **detected activity** to **Gemini AI** (free tier)
3. Gemini returns a **conversational explanation** of why the music fits
4. The playlist screen shows **real tracks from Spotify**

**Planned wording:** *"We pull real data from Spotify and use Gemini to add intelligence — the AI explains how your music fits your current context."*

---

## 3. Sessions — persistence (~30 s)

We will open **Sessions** from the bottom navigation and show the **Room**-backed list: **timestamps**, **detected activity**, **track count**, and **learned preferences** that the app builds over time.

**Planned wording:** *"This is our **persistence** layer: saved sessions plus context–music associations that improve over time."*

---

## 4. Map — location (~30 s)

On the **Map** tab we will request **fine location** if the system prompts us. We will show **coordinates**, the **map with session markers**, and the session list below.

**Planned wording:** *"Location adds another dimension of context — a jog in the park and a walk across campus are different vibes. Sessions are plotted on the map."*

---

## 5. Settings — UX and transparency (~30 s)

We will briefly show **Spotify connect/disconnect**, **text mode**, and **clear assistant thread**.

**Planned wording:** *"Settings keeps the user in control of their Spotify connection and chat preferences."*

---

## 6. Architecture (~30 s)

We will summarize **navigation** (tabs plus a separate **playlist** destination), **ViewModels** per area, and a **repository** that sits between the UI and **Room / Spotify / Gemini**.

**Planned wording:** *"We structured the app MVVM-style — the repository handles all API + DB logic, ViewModels expose state, and Compose renders it. Adding new features means extending the repository, not rewriting UI."*

---

## Anticipated questions

| Topic | Our response |
|--------|----------------|
| How does Spotify work? | **PKCE OAuth** in a Custom Tab → access + refresh tokens → fetch top tracks and recently played via Retrofit. |
| How does Gemini work? | **Free-tier Gemini 2.5 Flash-Lite** via Retrofit. We send activity + track list, get conversational reasoning back. |
| What if APIs are down? | Graceful fallback: mock tracks if no Spotify, activity-based messages if no Gemini. |
| Why Room? | **Sessions** and **learned preferences** persist on-device for cross-session learning. |
| Why sensors + location? | **Movement** is the core context; **location** enriches it. Both are rubric items and proposal features. |

---

## Closing

We will end with a single summary line:

*"Pulsify senses your movement, pulls music from your Spotify library, and uses Gemini to explain the connection — all persisted locally so the app learns your preferences over time."*
