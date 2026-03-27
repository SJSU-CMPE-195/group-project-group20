# Prototype / Demo: How Data From the Fitness Band Transmits to PC / Mobile Phone

---

## 1. Current Prototype Status

**We do not yet have an end-to-end working prototype checked into this repository.** As of today, the FitFuel Android app (`fitfuel_placeholder/`) accepts user data — weight, height, age, and calories burned — through **manual text input only** (see `MainActivity.kt`, line 55: `NumberField("Calories Burned Today", ...)`). The `AndroidManifest.xml` does not declare any Bluetooth permissions, no BLE client code exists in the codebase, and there is no PC-side companion script. The `src/` and `tests/` directories are empty placeholders.

What we **do** have is:

- A functioning Android app skeleton built with **Kotlin + Jetpack Compose + Material 3** that calculates a daily calorie target based on manually entered biometric data and a fitness goal selection (Lose Weight / Maintain / Gain Muscle).
- A clearly defined architecture and staged implementation plan (described in the rest of this document) for replacing that manual entry with **live sensor data streamed wirelessly from a fitness band over Bluetooth Low Energy (BLE)**.

The sections below describe, in full technical detail, exactly how data will move from the fitness band's sensors to both a mobile phone and a PC, what protocols and APIs are involved at each layer, and the concrete steps we will follow to build and demonstrate each stage of the prototype.

---

## 2. High-Level Data Flow Architecture

The transmission path from the fitness band to the phone or PC follows a layered architecture:

```
┌─────────────────────────────────────────────────────────────────────┐
│                        FITNESS BAND (Peripheral)                    │
│                                                                     │
│  ┌──────────┐    ┌──────────────┐    ┌───────────────────────────┐  │
│  │ Sensors  │───▶│     MCU      │───▶│    BLE Radio + Stack      │  │
│  │(HR, Accel│    │ (Data Proc.) │    │ (GATT Server, Advertiser) │  │
│  │ Gyro)    │    │              │    │                           │  │
│  └──────────┘    └──────────────┘    └─────────────┬─────────────┘  │
│                                                     │               │
└─────────────────────────────────────────────────────┼───────────────┘
                                                      │
                              BLE GATT Notifications   │  (2.4 GHz ISM)
                              (Wireless, ~10 m range)  │
                                                      │
     ┌────────────────────────────────────────────────┼──────────────┐
     │                                                ▼              │
     │  ┌──────────────────────────────────────────────────────┐     │
     │  │            BLE Central (GATT Client)                 │     │
     │  │                                                      │     │
     │  │  PHONE PATH:                                         │     │
     │  │    Android OS BLE Stack                              │     │
     │  │      → BluetoothGattCallback.onCharacteristicChanged │     │
     │  │        → FitFuel App (Jetpack Compose UI)            │     │
     │  │                                                      │     │
     │  │  PC PATH:                                            │     │
     │  │    OS BLE Driver + USB Adapter                       │     │
     │  │      → bleak (Python BLE library)                    │     │
     │  │        → Terminal / Debug Script                     │     │
     │  └──────────────────────────────────────────────────────┘     │
     │                    PHONE or PC (Central)                      │
     └───────────────────────────────────────────────────────────────┘
```

### What happens step by step

1. **Sensors sample data.** The fitness band's onboard sensors (heart rate via optical PPG, accelerometer, gyroscope) continuously sample biometric and motion data at a configured interval (e.g., heart rate every 1 second, accelerometer at 50 Hz).

2. **MCU processes and packages.** The microcontroller unit (MCU) on the band reads raw sensor registers over I2C or SPI, applies any necessary filtering or aggregation (e.g., computing calories burned from accelerometer data using a MET-based algorithm), and packs the result into a compact binary payload. A typical telemetry packet might look like:

   | Byte offset | Field              | Type     | Example       |
   |-------------|--------------------|----------|---------------|
   | 0           | Packet type        | uint8    | `0x01` (telem)|
   | 1–2         | Heart rate (bpm)   | uint16   | `0x0048` (72) |
   | 3–4         | Steps since reset  | uint16   | `0x0E10` (3600)|
   | 5–8         | Calories (kcal×10) | uint32   | `0x00001A2C`  |
   | 9           | Battery %          | uint8    | `0x5A` (90%)  |
   | 10–11       | Sequence number    | uint16   | `0x002F`      |

   Total: **12 bytes** — well within the BLE default ATT MTU of 23 bytes (20 bytes usable payload after ATT overhead).

3. **BLE radio transmits via GATT NOTIFY.** The MCU writes the payload into a GATT characteristic value and triggers a **notification** to any connected Central device. The BLE stack handles the L2CAP framing, link-layer encryption (if paired), and RF transmission on the 2.4 GHz ISM band.

4. **Phone or PC receives the notification.** The Central's BLE stack delivers the raw bytes to the application layer through a platform-specific callback. The app parses the binary payload, extracts the fields, and either displays them in the UI or stores them for meal-planning calculations.

---

## 3. The Wireless Protocol: BLE GATT in Detail

### 3.1 Why BLE GATT (and not alternatives)

| Option | Phone support | PC support | Power draw | Why we chose / rejected it |
|--------|--------------|------------|------------|---------------------------|
| **BLE GATT** | Android + iOS natively | Via USB BLE dongle + OS driver | Very low (~15 mA peak TX) | **Our choice.** Industry standard for wearables. All modern phones have BLE. Low power means longer band battery life. Notification model fits our push-based telemetry pattern. |
| Classic Bluetooth SPP | Android easy; iOS requires MFi program | Serial port profile, easy on desktop | Higher (~40 mA) | Rejected. iOS support requires Apple MFi certification (expensive, slow). Higher power drain. Overkill bandwidth for our small packets. |
| USB Serial (UART over USB) | Requires OTG adapter, awkward UX | Trivial (plug in, open COM port) | N/A (wired) | **Lab/debug only.** Useful during early firmware bring-up when the BLE radio isn't stable yet, but not viable for real-world use — users won't tether a fitness band with a cable. |
| Wi-Fi (HTTP/MQTT) | Yes | Yes | Very high (~200 mA) | Rejected. Completely impractical for a battery-powered wearable. Order-of-magnitude more power than BLE. |

### 3.2 GATT Service and Characteristic Design

BLE organizes data into **Services** (logical groups) and **Characteristics** (individual data points within a service). Each is identified by a UUID.

**Our custom GATT service:**

```
Service UUID: TBD — will be a randomly generated 128-bit UUID
              (e.g., 0000FF01-0000-1000-8000-00805F9B34FB as placeholder)
```

Under this service, we define the following characteristics:

| Characteristic | UUID (placeholder) | Properties | Description |
|---------------|--------------------|------------|-------------|
| **Telemetry** | `0000FF02-...` | READ, NOTIFY | Primary data channel. Firmware writes the sensor payload here and issues a notification. The Central enables notifications by writing `0x0001` to the Client Characteristic Configuration Descriptor (CCCD, handle `0x2902`). Each notification delivers up to 20 bytes (default MTU) or more after MTU negotiation. |
| **Control** | `0000FF03-...` | WRITE | Command channel from phone/PC to band. Supports commands like: `0x01` = start streaming, `0x02` = stop streaming, `0x03` = request full sync, `0x04` = reset step counter. The firmware reads incoming writes and acts accordingly. |
| **Device Info** | `0000FF04-...` | READ | Returns firmware version, hardware revision, and build timestamp. Useful during integration debugging so we always know which firmware we're testing against. |
| **Battery** (standard) | `0x180F` / `0x2A19` | READ, NOTIFY | Standard Bluetooth SIG Battery Service. Reports battery percentage. Good for demos — proves we handle both custom and standard GATT services. |

### 3.3 Connection Lifecycle

```
Band (Peripheral)                          Phone/PC (Central)
     │                                           │
     │◄──────── BLE Scan ────────────────────────│  Central scans for devices
     │                                           │  filtering by service UUID
     │── Advertising Packet ────────────────────▶│  or device name "FitFuel-Band"
     │   (contains service UUID,                 │
     │    device name, TX power)                 │
     │                                           │
     │◄──────── Connection Request ──────────────│  Central initiates connection
     │                                           │
     │── Connection Response ───────────────────▶│  Link established
     │                                           │
     │◄──────── MTU Exchange Request ────────────│  Central requests larger MTU
     │── MTU Exchange Response ─────────────────▶│  (e.g., 128 bytes)
     │                                           │
     │◄──────── Service Discovery ───────────────│  Central enumerates services
     │── Service List ──────────────────────────▶│  and characteristics
     │                                           │
     │◄──────── Write CCCD (0x2902 = 0x0001) ───│  Central enables notifications
     │── Acknowledgment ────────────────────────▶│  on Telemetry characteristic
     │                                           │
     │   ┌──── Notification Loop ────────────┐   │
     │   │                                   │   │
     │── │─ NOTIFY (telemetry payload) ─────▶│───│  Band pushes data on timer
     │   │                                   │   │  (e.g., every 1 second)
     │── │─ NOTIFY (telemetry payload) ─────▶│───│
     │   │                                   │   │
     │   │         ... continues ...         │   │
     │   └───────────────────────────────────┘   │
     │                                           │
     │◄──────── Disconnect ──────────────────────│  Central disconnects
     │                                           │
     │── Resume Advertising ─────────────────────│  Band auto-restarts ads
     │                                           │  (no power cycle needed)
```

---

## 4. Mobile Phone Path (Android — FitFuel App)

### 4.1 What needs to change in our codebase

**AndroidManifest.xml** — must add these permissions:

```xml
<!-- BLE permissions for Android 12+ (API 31+) -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN"
    android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- BLE permissions for Android 6–11 (API 23–30) — our minSdk is 24 -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- Declare that we use BLE hardware -->
<uses-feature android:name="android.hardware.bluetooth_le"
    android:required="true" />
```

**BLE Client Implementation** — new Kotlin class (e.g., `BleManager.kt`):

The Android BLE flow in code follows these steps:

1. **Check permissions at runtime.** On Android 12+, request `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT`. On older versions, request `ACCESS_FINE_LOCATION`.

2. **Get the BluetoothAdapter** via `BluetoothManager.getAdapter()`.

3. **Start a BLE scan** with a `ScanFilter` matching our service UUID:
   ```kotlin
   val filter = ScanFilter.Builder()
       .setServiceUuid(ParcelUuid(OUR_SERVICE_UUID))
       .build()
   bluetoothAdapter.bluetoothLeScanner.startScan(listOf(filter), settings, scanCallback)
   ```

4. **Connect to the discovered device** in the `ScanCallback.onScanResult`:
   ```kotlin
   device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
   ```

5. **Discover services** in `BluetoothGattCallback.onConnectionStateChange` (when `newState == CONNECTED`):
   ```kotlin
   gatt.discoverServices()
   ```

6. **Enable notifications** in `onServicesDiscovered`:
   ```kotlin
   val telemetryChar = gatt.getService(SERVICE_UUID)
       .getCharacteristic(TELEMETRY_CHAR_UUID)
   gatt.setCharacteristicNotification(telemetryChar, true)
   val cccd = telemetryChar.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
   cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
   gatt.writeDescriptor(cccd)
   ```

7. **Receive data** in `onCharacteristicChanged`:
   ```kotlin
   override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
       val data = characteristic.value  // raw byte array from the band
       val heartRate = (data[1].toInt() and 0xFF) or ((data[2].toInt() and 0xFF) shl 8)
       val steps = (data[3].toInt() and 0xFF) or ((data[4].toInt() and 0xFF) shl 8)
       val caloriesRaw = ByteBuffer.wrap(data, 5, 4).order(ByteOrder.LITTLE_ENDIAN).int
       val caloriesKcal = caloriesRaw / 10.0
       // Update UI state via ViewModel / StateFlow
   }
   ```

8. **Replace manual input.** Once BLE data is flowing, the `NumberField("Calories Burned Today", ...)` in `AppScreen()` can be replaced with a live-updating display driven by the BLE data stream, removing the need for the user to type anything for calories burned.

### 4.2 Data flow within the Android app

```
BLE Notification (raw bytes)
    │
    ▼
BluetoothGattCallback.onCharacteristicChanged()
    │
    ▼
BleManager parses binary payload → data class TelemetryPacket(hr, steps, calories, battery, seq)
    │
    ▼
Kotlin StateFlow / MutableState updated
    │
    ▼
Jetpack Compose UI recomposes automatically
    │
    ▼
User sees live heart rate, step count, and calories burned on screen
```

---

## 5. PC Path (Python + bleak)

### 5.1 Why bleak

Desktop operating systems do not expose a simple "open BLE and read" API. Each OS has its own BLE stack:
- **macOS**: CoreBluetooth framework
- **Windows**: WinRT Bluetooth APIs
- **Linux**: BlueZ D-Bus API

The **[bleak](https://github.com/hbldh/bleak)** Python library wraps all three behind a single async API, so one script works on any platform. It requires a BLE-capable adapter — most modern laptops have one built in; desktops typically need a USB BLE 4.0+ dongle.

### 5.2 PC demo script outline

```python
import asyncio
import struct
from bleak import BleakClient, BleakScanner

SERVICE_UUID = "0000ff01-0000-1000-8000-00805f9b34fb"       # same as Android
TELEMETRY_UUID = "0000ff02-0000-1000-8000-00805f9b34fb"     # same as Android

def parse_telemetry(data: bytearray):
    """Parse the binary payload from the fitness band."""
    packet_type = data[0]
    heart_rate = struct.unpack_from("<H", data, 1)[0]
    steps = struct.unpack_from("<H", data, 3)[0]
    calories_raw = struct.unpack_from("<I", data, 5)[0]
    battery = data[9]
    seq = struct.unpack_from("<H", data, 10)[0]
    return {
        "heart_rate_bpm": heart_rate,
        "steps": steps,
        "calories_kcal": calories_raw / 10.0,
        "battery_pct": battery,
        "sequence": seq,
    }

def notification_handler(characteristic, data: bytearray):
    """Callback invoked each time the band sends a NOTIFY."""
    parsed = parse_telemetry(data)
    print(f"[seq={parsed['sequence']:04d}] HR={parsed['heart_rate_bpm']} bpm | "
          f"Steps={parsed['steps']} | Cal={parsed['calories_kcal']:.1f} kcal | "
          f"Batt={parsed['battery_pct']}%")

async def main():
    print("Scanning for FitFuel band...")
    device = await BleakScanner.find_device_by_filter(
        lambda d, adv: SERVICE_UUID.lower() in [s.lower() for s in (adv.service_uuids or [])]
    )
    if not device:
        print("Band not found. Is it advertising?")
        return

    print(f"Found: {device.name} [{device.address}]")

    async with BleakClient(device) as client:
        print(f"Connected. MTU = {client.mtu_size}")
        await client.start_notify(TELEMETRY_UUID, notification_handler)
        print("Receiving telemetry (Ctrl+C to stop)...")
        while True:
            await asyncio.sleep(1)

asyncio.run(main())
```

### 5.3 PC data flow

```
Fitness Band (BLE Peripheral)
    │
    │  BLE GATT Notification (2.4 GHz)
    ▼
USB BLE Adapter / Built-in BLE Radio
    │
    │  OS BLE Driver (CoreBluetooth / WinRT / BlueZ)
    ▼
bleak Python Library (async wrapper)
    │
    │  notification_handler callback with bytearray
    ▼
parse_telemetry() → Python dict
    │
    ▼
Print to terminal / log to file / feed into analysis pipeline
```

---

## 6. Staged Demo Plan

We are building the prototype incrementally in four stages. Each stage produces a demonstrable result.

### Stage A — Prove the Wireless Link (Band → Generic BLE Tool)

**Goal:** Confirm that bytes physically travel from the band's BLE radio to a phone over the air.

**What we build:**
- Firmware on the band: advertise with our service UUID, push a **dummy counter pattern** (incrementing byte sequence) into the Telemetry characteristic every 1 second via NOTIFY.
- No custom app needed yet.

**How we demo it:**
- Install **nRF Connect** (free, from Nordic Semiconductor) on an Android or iOS phone.
- Open nRF Connect → Scan → find our device by name or UUID → Connect → navigate to our custom service → tap the notification icon on the Telemetry characteristic.
- Watch the hex values update live on screen. The counter incrementing proves data is flowing from band to phone.

**What this proves:** The entire wireless stack works — BLE advertising, connection establishment, GATT service discovery, CCCD notification enable, and continuous data push. This alone counts as a **prototype demo** for design checkpoints.

**Evidence to capture:** Screenshot or short screen recording of nRF Connect showing the characteristic value changing in real time.

### Stage B — FitFuel Android App Receives Live Data

**Goal:** Our own app (not a third-party tool) receives and displays the band's data.

**What we build:**
- Add BLE permissions to `AndroidManifest.xml` (as detailed in Section 4.1).
- Implement `BleManager.kt` with the scan → connect → discover → notify flow.
- Add a debug screen or modify `AppScreen()` to show incoming telemetry values instead of (or alongside) the manual input fields.
- Replace the hardcoded `NumberField("Calories Burned Today", ...)` with a live-updating value from the BLE stream.

**How we demo it:**
- Power on the fitness band (running Stage A firmware or upgraded firmware with real sensor data).
- Open FitFuel on an Android phone → grant Bluetooth permissions → the app scans, finds the band, connects, and starts displaying live heart rate, steps, and calories.
- Move the band / simulate activity → watch the values change on screen.

**What this proves:** End-to-end integration from physical sensor to user-facing mobile UI. The transmission path is: sensors → MCU → BLE radio → Android BLE stack → FitFuel app → Compose UI.

### Stage C — PC Receives the Same Data

**Goal:** Demonstrate platform independence — the band isn't phone-only.

**What we build:**
- Install a BLE USB adapter on the PC (if no built-in BLE).
- Write the Python `bleak` script (as shown in Section 5.2) using the **exact same UUIDs** as the Android app.
- Run the script, see telemetry printed to terminal.

**How we demo it:**
- Band advertising → run `python fitfuel_ble_demo.py` on a laptop → terminal shows live telemetry lines updating every second.

**What this proves:** The GATT design is client-agnostic. Any BLE Central — phone, laptop, Raspberry Pi — can receive the band's data using the same protocol. This is important for lab analysis, debugging, and future web dashboard features.

### Stage D — Presentation Fallback Plan

If the live PC BLE demo fails during a presentation (driver issues, USB adapter not recognized, etc.):

1. **Fallback 1:** Switch to the **nRF Connect live demo on a phone** (Stage A) — this is extremely reliable since nRF Connect is a mature, well-tested app.
2. **Fallback 2:** Show a **pre-recorded screen capture** of the Stage B or Stage C demo, and explain that the PC path uses identical UUIDs and protocol.
3. **Fallback 3:** Show **Logcat output** from the Android app receiving BLE data, which provides raw proof of bytes arriving even if the UI isn't rendering correctly.

---

## 7. USB Serial — Lab/Debug Only Path

During early hardware bring-up, before the BLE radio firmware is stable, we can use **USB serial (UART)** to validate that the MCU is reading sensors correctly:

```
Band MCU ──── USB cable ──── PC
         UART (115200 baud)
```

- On the PC, open a serial terminal (e.g., PuTTY, `screen /dev/ttyUSB0 115200`, or Python `pyserial`) and read the same telemetry payload format.
- This is **not** our demo path — it's a development tool. Users will never connect a cable to a fitness band in real life.
- Once BLE is working, USB serial becomes optional for firmware debugging only.

---

## 8. Security Considerations

| Concern | Our approach |
|---------|-------------|
| **Pairing for class demo** | **Just Works** pairing (no PIN, no passkey). Acceptable because we're not transmitting secrets or stable personal identifiers in a demo environment. |
| **Future production use** | If the band ever carries real health data tied to a user identity, we would upgrade to **LE Secure Connections** with **Numeric Comparison** pairing and enable **bonding** (key storage) so the phone doesn't need to re-pair every time. This is a firmware + app decision made together. |
| **Data integrity** | The BLE link layer includes CRC-24 error checking on every packet. Corrupted packets are automatically retransmitted. Our sequence number field in the payload lets the app detect if a notification was missed (gap in sequence). |
| **Encryption** | BLE 4.2+ supports AES-128-CCM encryption at the link layer. For demos, encryption is optional. For production, it should be mandatory. |

---

## 9. Checklist Before Transport Is "Done"

- [ ] Publish our **128-bit service and characteristic UUIDs** in a shared constants file that both firmware and app code reference.
- [ ] Document **advertising settings**: device name (`FitFuel-Band`), service UUID in advertising data, connectable mode, advertising interval.
- [ ] Agree on and test **MTU behavior**: what MTU we request, what the fallback is, and what our maximum notification payload size is.
- [ ] Verify **disconnect → re-advertise** works smoothly (no power cycle needed between demo attempts).
- [ ] Capture **Stage A proof**: screenshot or short video of nRF Connect showing live NOTIFY data from the band.
- [ ] Verify **Stage B** works on at least one physical Android device (not just emulator — emulators don't have BLE).
- [ ] Test the **Python bleak script** on at least one PC platform (macOS or Windows).

---

## 10. Summary

| Question | Answer |
|----------|--------|
| Do we have a working prototype in the repo today? | **No.** The Android app uses manual input. No BLE code, no Bluetooth permissions, and no PC script exist yet. |
| What protocol will we use? | **BLE GATT.** The fitness band is a Peripheral (GATT Server) that pushes sensor data via NOTIFY. The phone or PC is a Central (GATT Client) that subscribes to notifications. |
| What data gets transmitted? | A compact binary packet (~12 bytes) containing heart rate, step count, calories burned, battery level, and a sequence number. |
| How does the phone receive it? | Android's `BluetoothGattCallback.onCharacteristicChanged()` delivers raw bytes. Our app parses them and updates the Jetpack Compose UI via StateFlow. |
| How does the PC receive it? | A Python script using the `bleak` library connects to the same GATT service/characteristic and receives the same notifications. |
| What's the fastest path to a demo? | Stage A: firmware sends dummy data → verify with nRF Connect on a phone. This can be demoed without writing any app code. |
| What are the concrete next steps? | (1) Define and publish UUIDs, (2) add BLE permissions to AndroidManifest.xml, (3) implement BLE client in Kotlin, (4) write the Python bleak script, (5) integrate live data into the FitFuel UI. |

---

*CMPE 195A/B — Senior Design Project | San Jose State University | Spring 2026*
