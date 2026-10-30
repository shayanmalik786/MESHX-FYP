# MeshX: 2-Node Shopping List (until the mid-year evaluation)

**Already have:** 2 × ESP32, 2 × Ra-02 SX1278 433 MHz (on the blue adapter board), 2 × breadboard, jumper wires.
**Build:** everything on breadboards, no soldering. Soldering comes after mid-year.
**Phone:** your own Android phones work for BLE, so nothing to buy for that.

The **"Dukaan pe kya bolein"** column has the name the part is sold under in Pakistan. Show it to the shopkeeper as it is.
Prices are rough. Ones marked * are my estimates; the rest come from your price list.

---

## ⚠️ The one thing to get done at the shop

The **back of your Ra-02 adapter board has 2 rows of 8 empty holes** (J1, J2). There are no pins in them yet, so it can't be connected to the breadboard or to jumpers.
👉 Tell the shopkeeper: **"In 2 LoRa boards par male header pin laga kar solder kar dein."**
This takes 5 minutes, about Rs. 50–100 per board*. If header pins are already in the bag, give those; otherwise buy item 3.
After that, nothing in the project needs soldering.

---

## A. Radio and antenna (required)

| # | Dukaan pe kya bolein | Qty | Spec / what to check | Price (Rs.) |
|---|---------------------|-----|----------------------|-------------|
| 1 | **"433 MHz SMA antenna"** (LoRa antenna) | 3 (1 spare) | **433 MHz** must be written on it, **SMA male** connector, 2–5 dBi. No spring antennas. | 300–1,000 each |
| 2 | **"IPEX to SMA cable"** (u.FL pigtail) | 3 (1 spare) | IPEX-1 (u.FL) on one end, **SMA female** on the other, 10–15 cm. The IPEX end snaps onto the small round socket on the Ra-02, and the antenna screws onto the SMA end. | 150–300 each |
| 3 | **"Male header pin strip 2.54 mm"** | 1 strip (40 pin) | Only if pins are not in the bag | 50–100 |

## B. Wiring and small parts (required)

| # | Dukaan pe kya bolein | Qty | Spec / what to check | Price (Rs.) |
|---|---------------------|-----|----------------------|-------------|
| 4 | **"Jumper wire male to female 20 cm"** | 1 pack (40) | Female end on the LoRa pins, male end in the breadboard. Skip if you already have M-F. | 150 |
| 5 | **"Jumper wire male to male 20 cm"** | 1 pack (40) | For the breadboard rails. Skip if you already have M-M. | 150 |
| 6 | **"Capacitor 100uF 16V"** (electrolytic) | 4 | 16 V or higher. Long leg is +. | 10–20 each |
| 7 | **"104 ceramic capacitor"** (100 nF) | 4 | "104" printed on it | 5–10 each |
| 8 | **"Resistor 220 ohm 1/4 watt"** | 6 | For the LEDs | 2–5 each |
| 9 | **"Resistor 10K ohm 1/4 watt"** | 4 | Pull-ups / spare | 2–5 each |
| 10 | **"5mm LED"** (green and red) | 4 each | One LED for TX, one for RX on each node | 5–10 each |
| 11 | **"Tactile push button 4 pin 6×6"** | 4 | Test-send button on each node | 10–20 each |

## C. Display (recommended, makes the demo look good)

| # | Dukaan pe kya bolein | Qty | Spec / what to check | Price (Rs.) |
|---|---------------------|-----|----------------------|-------------|
| 12 | **"OLED 0.96 inch I2C display"** (SSD1306) | 2 | **4 pins** (VCC, GND, SCL, SDA), I2C, 128×64. Not the 7-pin SPI one. Pins must already be soldered. | 450 each |

Shows node ID, RSSI/SNR and the last message on each node without needing a laptop.

## D. Power (required)

| # | Dukaan pe kya bolein | Qty | Spec / what to check | Price (Rs.) |
|---|---------------------|-----|----------------------|-------------|
| 13 | **"Data cable"** matching your ESP32 port (Type-C or Micro-USB) | 2 | Must say **data cable**. A charging-only cable won't upload code. | ~100 each |
| 14 | **"18650 battery shield V3"** (single cell) | 2 | Has a USB-A output, a charging port and an **on/off switch**. Charges the battery and powers the ESP32 through a USB cable. | 300–600 each* |
| 15 | **"18650 cell"** (Li-ion 3.7 V) | 2 | **Brand-name**: Samsung, LG, Sony or Panasonic, 2500–3500 mAh. Don't buy "9900 mAh" cells, they are fake. | 600–1,100 each* |
| 16 | **"USB cable"** to connect the shield to the ESP32 | 2 | USB-A to your ESP32 port | ~100 each |

Instead of items 14–16 you can also use a phone power bank plus a data cable for each node.

## E. Tools

| # | Dukaan pe kya bolein | Qty | Spec / what to check | Price (Rs.) |
|---|---------------------|-----|----------------------|-------------|
| 17 | **"Digital multimeter DT9205A"** | 1 | Has a **continuity buzzer**. Skip if you already have one. | 1,320 |
| 18 | **"Logic analyzer 24MHz 8 channel"** (optional) | 1 | Shows the SPI signals when the LoRa isn't responding. Works with the free PulseView software. | 1,750 |

---

## Total (2 nodes)

| | Rs. |
|---|---|
| A + B (radio + wiring) | ~2,500–4,500 |
| C (OLED) | ~900 |
| D (power, battery shield) | ~2,200–3,800 |
| E (multimeter) | ~1,320 |
| **Everything (without logic analyzer)** | **~7,000–10,500** |

---

## Check before you leave the shop

- [ ] All antennas say **433 MHz** and the connector is SMA
- [ ] The IPEX end snaps onto the Ra-02 socket (try it at the shop)
- [ ] Header pins are soldered on both LoRa boards
- [ ] Cables are **data** cables and fit your ESP32 port
- [ ] OLED has 4 pins and says I2C
- [ ] Battery shield has a switch and USB output

## Important

- **Never power the LoRa without its antenna.** It can burn out the radio.
- Ra-02 runs on **3.3 V**. Connect it to the ESP32's **3V3** pin. 5V will damage it.
- Ra-02 (SX1278) wiring: **NSS, SCK, MOSI, MISO, RST, DIO0, 3.3V, GND**. There is no BUSY pin. The SX1262 wiring in the hardware guide won't work as is.
- Don't plug in the laptop USB and the battery shield at the same time.
- Battery % reading (for adaptive routing) comes after mid-year, together with soldering.
