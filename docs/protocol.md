# Phone ↔ node protocol (v0.1)

The Android app talks to its nearest MeshX node over **Bluetooth Low Energy** using the
**Nordic UART Service (NUS)**, so the ESP32 can use any standard "BLE UART" example.

| | UUID |
|---|---|
| Service | `6e400001-b5a3-f393-e0a9-e50e24dcca9e` |
| RX (phone writes → node) | `6e400002-b5a3-f393-e0a9-e50e24dcca9e` |
| TX (node notifies → phone) | `6e400003-b5a3-f393-e0a9-e50e24dcca9e` |

The node must advertise a name starting with **`MeshX`** (for example `MeshX-03`), because the app only lists those.

## Frames

One frame per line (`\n`), fields separated by `|`. The text field is always last, so it may contain `|`.
A newline inside a message is sent as `\n` and a backslash as `\\`. Messages are limited to 200 characters.
Long frames are split into BLE chunks by the phone; the node must buffer until it sees `\n`.

### Phone → node

| Frame | Meaning |
|---|---|
| `HELLO` | Sent right after connecting. Node answers with `ME`, then its `NODE` and `LINK` table. |
| `MSG|<msgId>|<toNode>|<text>` | Send text to a node. `toNode = 255` means broadcast (flood to all). |
| `MODE|ADAPTIVE` / `MODE|SHORTEST_HOP` / `MODE|FLOODING` | Switch routing algorithm (for the comparison experiments). |

### Node → phone

| Frame | Meaning |
|---|---|
| `ME|<nodeId>|<battery%>` | Who the phone is connected to. |
| `SENT|<msgId>` | Packet left this node's radio. App shows ✓. |
| `ACK|<msgId>|<hops>` | Destination acknowledged. App shows ✓✓ and hop count. |
| `FAIL|<msgId>` | No ACK after all retries. App shows ⚠. |
| `RX|<msgId>|<fromNode>|<hops>|<rssi>|<snr>|<text>` | Message received for this phone. |
| `NODE|<id>|<name>|<rssi>|<snr>|<battery>|<loss>|<score>|<hops>|<lastSeenSec>` | One row of the neighbour / routing table. `loss` and `score` are 0–1. |
| `LINK|<a>|<b>|<score>` | A link between two nodes, used to draw the network map. |

Example session:

```
phone → HELLO
node  → ME|3|82
node  → NODE|2|Fatima|-92|7.5|64|0.03|0.86|2|5
node  → LINK|3|2|0.86
phone → MSG|1001|2|Are you near the main gate?
node  → SENT|1001
node  → ACK|1001|2
node  → RX|77|2|2|-92|7.5|Yes, 2 min away
```

## Link score (first draft)

`score = 0.35·RSSI_n + 0.25·SNR_n + 0.25·(1 − loss) + 0.15·battery_n`, where
`RSSI_n = (rssi + 120) / 70`, `SNR_n = (snr + 10) / 20`, `battery_n = battery / 100`, each clamped to 0–1.
These weights are placeholders to be tuned during the adaptive-routing phase; the app's Node details screen
uses the same numbers, so update both together.

The over-the-air LoRa packet format (header, TTL, sequence numbers, encryption) is defined separately in `firmware/`.
