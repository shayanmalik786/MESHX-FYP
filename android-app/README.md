# MeshX Android app (v0.1)

Kotlin + Jetpack Compose (Material 3), min Android 8.0 (API 26), target API 35.

## Run it

1. Install **Android Studio** (latest stable) from developer.android.com/studio.
2. File → Open → select this `android-app` folder. Let Gradle sync finish (first time takes a few minutes).
3. Plug in an Android phone with USB debugging on (or start an emulator) and press ▶ Run.
4. The app starts in **Demo mode**: tap *Get started* → *Connect* on MeshX-03 → you get a fake 6-node mesh.
   Sending a message shows ✓ then ✓✓ and an automatic reply. Turn Demo mode off in Settings to use real nodes.

Real Bluetooth needs a physical phone (emulators have no BLE) and a node running the MeshX firmware.

## Code map

```
app/src/main/java/com/meshx/app/
  MainActivity.kt           entry point
  MeshViewModel.kt          holds app state, switches demo / real Bluetooth
  model/Models.kt           Message, MeshNode, Link, MeshState ...
  data/Protocol.kt          text frames exchanged with the node (docs/protocol.md)
  data/BleMeshRepository.kt real BLE (Nordic UART Service)
  data/DemoMeshRepository.kt fake mesh for demos
  ui/MeshXApp.kt            navigation + bottom bar
  ui/screens/*.kt           one file per Figma screen (01–07)
  ui/components/            bubbles, avatar, status bar
  ui/theme/Theme.kt         colours from the Figma style guide
app/src/test/               unit tests (run: ./gradlew test)
```

## Status

- Done: all 7 screens, navigation, demo mesh, BLE scan/connect/notify/write, protocol parser + tests.
- Next: test BLE against the first firmware, save messages in a Room database, network key / encryption settings.
- Note: written outside Android Studio. The protocol and demo logic are unit tested, but the full app has not been
  compiled yet. If the first Gradle sync shows an error, paste it to Claude.
