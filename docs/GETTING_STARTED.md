# Getting started: step-by-step guide for Team MeshX

## What was done so far

| Done | Where |
|---|---|
| Proposal (approved) | `docs/proposal/` |
| Hardware list, shopping list, prices | `hardware/bom/` |
| Interactive schematic + pin map | `hardware/schematic/`, `hardware/README.md` |
| Node hardware guide, Ra-02 build guide, mid-year pack | `docs/guides/` (open in a browser) |
| **New:** Android app UI/UX design | Figma links in `docs/design/README.md` |
| **New:** Android app starter (7 screens, demo mode, Bluetooth) | `android-app/` |
| **New:** Phone ↔ node protocol | `docs/protocol.md` |

## Task 1: UI/UX schematic of the app (for your supervisor)

1. Open the Figma design: https://www.figma.com/design/AcwXg0kupgabqYOPDhirem (log in with the same Gmail).
2. You'll see: a header, the **Style Guide** (colours + fonts), the **Components** board, and **7 phone screens** in a row.
3. Open the user-flow diagram (FigJam): https://www.figma.com/board/8ZKwC6RBukY7y6MXb6kNz0.
   If Figma asks you to "claim" or "save" the file, do it so it stays in your account.
4. To present: in the design file select a screen and press **Shift+2** to zoom to it. Or turn on Prototype mode
   and link the buttons (Get started → Connect → Chats ...) to make a clickable demo. This is a good 30-minute task to do yourself.
5. To export images for a report: select the screens → right panel **Export** → PNG 2x.

What to tell your supervisor: the app has 7 screens; each one maps to a proposal objective
(send/receive, hop count, signal strength, delivery status, live topology). See the table in `docs/design/README.md`.

## Task 2: Run the Android app

1. Install Android Studio. Open the `android-app` folder (not the whole repo).
2. Wait for Gradle sync. Press ▶ Run with a phone connected (Settings → About phone → tap Build number 7 times →
   Developer options → USB debugging ON).
3. Try: Get started → Connect → open a chat → send a message → watch ✓ then ✓✓.
   Network tab → tap a node → Node details. Settings → Demo mode switch.

## Task 3: Learn the code (one screen per day)

Day 1 `ui/theme/Theme.kt` + `WelcomeScreen.kt` (how Compose layouts work)
Day 2 `ChatsScreen.kt` + `ChatScreen.kt` (lists, text input)
Day 3 `MeshViewModel.kt` + `DemoMeshRepository.kt` (state and fake data)
Day 4 `Protocol.kt` + `docs/protocol.md` (what the phone sends to the node)
Day 5 `BleMeshRepository.kt` (real Bluetooth)

Good free resource: the "Jetpack Compose for Android Developers" course on developer.android.com.

## Working with Git (team)

- Install Git and GitHub Desktop (easiest), clone the repo, and make a branch per task (for example `app/chat-screen`).
- Commit small changes with clear messages. Open a pull request and let a teammate review before merging.
- Never commit passwords or keys.

## What comes next

1. **Firmware v0.1** (`firmware/`): two nodes + two phones exchanging messages with ACKs, speaking `docs/protocol.md`.
2. Connect the real app to the real node (turn Demo mode off).
3. Extend to 4 nodes: discovery, forwarding, TTL, duplicate detection.
