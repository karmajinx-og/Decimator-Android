# Recall — context for returning from a break

**Purpose:** When you (or an assistant) return to the project, read this for a detailed picture of what’s done and what’s next.  
**Last updated:** 2026-03-08  
*(Copy of docs/RECALL.md; paths assume repo root.)*

---

## What this project is

- **Decimator-Android** — Android app to control Decimator Design hardware (converters, cross converters, multi-viewers) via **USB**.
- Uses **FTDI chip** in **bit-bang mode** (VID `0x215F`, PID `0x6000`). No official SDK; protocol from [decimctl](https://github.com/quentinmit/decimctl) (Python, Apache 2.0).
- **Design doc (source of truth):** Decimator_Design_Android_Control_App.md at repo root.

---

## What has been done (in detail)

### 1. Project scaffold

- **Gradle:** Kotlin DSL, version catalog in `gradle/libs.versions.toml`. Root `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`.
- **App module:** `app/build.gradle.kts` — Kotlin 1.9, Compose, minSdk 26, targetSdk 35, namespace `com.decimator.android`.
- **Gradle wrapper:** `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar` (8.9). Build runs via `./gradlew assembleDebug`.
- **Launcher icons:** `app/src/main/res/drawable/ic_launcher.xml`, `ic_launcher_round.xml`. Strings and themes in `res/values/`.

### 2. USB and device filter

- **Manifest:** `app/src/main/AndroidManifest.xml` — `uses-feature android.hardware.usb.host`, activity with `USB_DEVICE_ATTACHED` intent filter.
- **Device filter:** `app/src/main/res/xml/device_filter.xml` — VID 8543 (0x215F), PID 24576 (0x6000).
- **Helpers:** `app/.../usb/DecimatorUsbManager.kt` (constants + `findDecimatorDevice()`); `app/.../usb/UsbPermissionHelper.kt` (hasPermission, requestPermission, broadcast action).

### 3. FTDI communication layer (core)

- **Dependency:** App depends on `fileTree("libs") { include("*.aar", "*.jar") }` in `app/build.gradle.kts`. User must add FTDI D2XX AAR to **app/libs/** — see **app/libs/README.md**. Without it, build fails on `com.ftdi.j2xx`.
- **Driver:** `app/.../ftdi/DecimatorFtdiDriver.kt` — open, init (3M baud, 1ms latency, RTS/CTS, bit-bang 0x48), clockRawBytes, DecimatorConnection, DecimatorError.
- **Connection:** `DecimatorConnection(ft)` — clockRawBytes(), fpgaWriteBytes(), fpgaReadBytes(), readRawRegisters(), close(); all on Dispatchers.IO.

### 4. Protocol (decimctl port)

- **File:** `app/.../protocol/DecimatorProtocol.kt` — REGISTER_SIZE, preambles/postamble, bytesToRawCommand(), rawResponseToBytes(), bitListToBytes(), ProtocolException.

### 5. Connection state and UI

- **State:** ConnectionState (NoDevice, PendingPermission, PermissionDenied, UnsupportedDevice, Connecting, Connected, Error). **ViewModel:** DecimatorViewModel. **Activity:** MainActivity + BroadcastReceiver. **UI:** DecimatorApp (Compose).

### 6. GitHub and sharing

- **Remote:** origin → https://github.com/karmajinx-og/Decimator-Android.git. **Audit tag:** v0.1.0-audit. **Sharing guide:** docs/SHARING_FOR_AUDIT.md.

### 7. Documentation and project discipline

- **Master context:** MASTER_CONTEXT.md. **Project log:** docs/PROJECT_LOG.md. **Rules:** docs/RULES.md. **Recall:** docs/RECALL.md.

### 8. Backups and rollback

- **Build backups:** backups/README.md; backups/*.zip in .gitignore. **Supplementary:** supplementary/ (this folder); regenerate supplementary-and-rollback.zip when needed.

---

## What is next to be done

### Immediate

1. Add FTDI D2XX AAR to app/libs/.
2. Run ./gradlew assembleDebug locally.
3. Install and test on device (USB host + OTG).

### Short term

4. Confirm FTDI chip model. 5. Test open/read with real hardware. 6. Device type from serial (first 3 chars → register map).

### Medium term

7. Port register map structs (CPA, VFA, KPA) from protocol.py. 8. Build control UI by category. 9. Handle USB detach in UI.

### Later

10. Test each device family. 11. No firmware update without official docs.

---

## How to resume

1. Read docs/RECALL.md and MASTER_CONTEXT.md.
2. Sync and build; add FTDI AAR if needed.
3. Pick next task from “What is next to be done”.
4. Update PROJECT_LOG and RECALL after significant work.

---

## Key file reference

| Purpose       | Path |
|--------------|------|
| Design doc   | Decimator_Design_Android_Control_App.md |
| Master context | MASTER_CONTEXT.md |
| Project log  | docs/PROJECT_LOG.md |
| Rules        | docs/RULES.md |
| Recall       | docs/RECALL.md |
| FTDI driver  | app/.../ftdi/DecimatorFtdiDriver.kt |
| Protocol     | app/.../protocol/DecimatorProtocol.kt |
| FTDI lib     | app/libs/README.md |
