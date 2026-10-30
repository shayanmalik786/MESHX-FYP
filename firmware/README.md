# Node firmware (coming next)

Planned structure (PlatformIO, Arduino framework, RadioLib + NimBLE):

```
firmware/
  platformio.ini
  src/
    main.cpp          setup/loop, wires everything together
    radio.cpp/.h      LoRa (RadioLib) send/receive, RSSI/SNR
    ble_uart.cpp/.h   Nordic UART Service, speaks docs/protocol.md
    mesh.cpp/.h       packet header, TTL, duplicate cache, forwarding, ACK/retry
    routing.cpp/.h    neighbour table, link score, adaptive / shortest-hop / flooding
    crypto.cpp/.h     AES-256-GCM (mbedTLS) + node identity
  test/
```

Milestone 1: two nodes, each with a phone, exchanging `MSG` / `ACK` over LoRa with the app's protocol.
