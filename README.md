# MeshX: Adaptive Off-Grid Communication System using LoRa

Final Year Design Project, Department of Computer and Information Systems Engineering (CS-406).

MeshX is a low-cost LoRa mesh of 5–6 battery-powered nodes (ESP32-S3 + SX1262 / SX1278) that lets people
exchange text messages over several kilometres with no cellular network, Wi-Fi or internet. Each phone talks
to its nearest node over Bluetooth Low Energy, and nodes forward messages for each other using a
link-quality-aware routing algorithm (RSSI, SNR, packet loss, battery) instead of blind hop count.

**Team:** Shayan Malik (CS-23053), Syyeda Fatima Shoaib (CS-23075), Umaiza Asif (CS-23080), Hamna Rizvi (CS-23082)
**Supervisor:** Dr. Sumayya Zafar · **Co-supervisor:** Sheikh Abdul Basit (NCCS)

## Repository layout

| Folder | What is in it | Status |
|---|---|---|
| [`docs/proposal`](docs/proposal) | Approved FYDP proposal (PDF + Word) | Done |
| [`docs/guides`](docs/guides) | Node hardware guide, Ra-02 breadboard build guide, mid-year readiness pack (open the `.html` files in a browser) | Done |
| [`docs/design`](docs/design) | Android app UI/UX: Figma links, screen previews, user flow | v0.1 |
| [`docs/protocol.md`](docs/protocol.md) | Phone ↔ node BLE message format | v0.1 |
| [`hardware`](hardware) | Interactive schematic, pin map, bill of materials and price lists | Done for prototype |
| [`android-app`](android-app) | Android companion app (Kotlin + Jetpack Compose) | v0.1 starter, runs in demo mode |
| [`firmware`](firmware) | ESP32 node firmware (Arduino / PlatformIO) | Next |
| [`analysis`](analysis) | Python scripts for logs, routing comparison and graphs | Later |

New to the repo? Start with **[docs/GETTING_STARTED.md](docs/GETTING_STARTED.md)**.

## Project plan (from the proposal Gantt chart)

1. Sept–Oct: LoRa / RF / mesh research, first point-to-point ESP32 ↔ LoRa link
2. Nov: BLE bridge to Android, phone-to-phone message through two nodes
3. Dec: 4-node chain with discovery, forwarding, TTL and duplicate detection
4. Jan: reliability layer (ACKs, retransmission, store-and-forward, route recovery)
5. Feb: security layer (AES-256-GCM, node authentication)
6. Mar: adaptive routing using RSSI / SNR / loss / battery
7. Apr: comparison of adaptive vs shortest-hop vs flooding, campus field tests
8. May: results, thesis, poster, final demo
