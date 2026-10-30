# Hardware

- [`schematic/Interactive_FYP_Schematic.html`](schematic/Interactive_FYP_Schematic.html): open in a browser
- [`bom/`](bom): full hardware list with alternatives, 2-node shopping list, complete price list
- Detailed wiring guides are in [`../docs/guides`](../docs/guides)

## Pin map

### Final node: ESP32-S3-DevKitC-1 (N8R8) + SX1262

| SX1262 | ESP32-S3 GPIO |
|---|---|
| NSS | 10 |
| SCK | 12 |
| MOSI | 11 |
| MISO | 13 |
| RESET | 5 |
| BUSY | 4 |
| DIO1 | 8 |
| RXEN / TXEN (E22 / Core1262 only) | 6 / 7 |

Battery sense: GPIO 9 through a 100k/100k divider. Optional: OLED SDA 17 / SCL 18, GPS 15 / 16, button 14.
Power: 1S Li-ion → TP4056 (protected) → switch → TPS63802 buck-boost → 3V3 rail (100 µF + 100 nF).

### Mid-year prototype: Ai-Thinker Ra-02 (SX1278, 433 MHz) on breadboard

| Ra-02 | ESP32-S3 | ESP32 DevKit V1 |
|---|---|---|
| NSS | 10 | 5 |
| SCK | 12 | 18 |
| MOSI | 11 | 23 |
| MISO | 13 | 19 |
| RST | 5 | 14 |
| DIO0 | 4 | 26 |
| OLED SDA / SCL | 17 / 18 | 21 / 22 |
| LED TX / RX | 6 / 7 | 25 / 27 |
| Button | 14 | 32 |

RadioLib: `radio.begin(433.0, 125.0, 9, 7, 0x12, 10, 8, 0)` (433 MHz, BW 125 kHz, SF9, CR 4/7, sync 0x12, 10 dBm, preamble 8).
