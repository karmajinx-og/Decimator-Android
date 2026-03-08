# Recall — context for returning from a break

**Purpose:** When you (or an assistant) return to the project, read this for a detailed picture of what’s done and what’s next.  
**Last updated:** 2026-03-08

---

## When you come back — what is next? (Testing)

1. **Read this file** and [MASTER_CONTEXT.md](../MASTER_CONTEXT.md).
2. **FTDI library:** Already in place — **app/libs/d2xx.jar** (from Android Java D2XX 2.13). No need to add it again.
3. **Build:** Run **`./gradlew assembleDebug`**. Fix any compile errors if they appear.
4. **Test on device:** Install the APK on a device with USB host + OTG. Test **with** a Decimator (connect, permission, open, disconnect) or **without** (confirm “Connect a Decimator device” / permission flow).
5. **After testing:** Pick from “What is next to be done” below (e.g. device type from serial, register map, control UI).
6. **Full list of every fix:** [docs/FIXES_APPLIED.md](FIXES_APPLIED.md).

---

## What this project is

- **Decimator-Android** — Android app to control Decimator Design hardware (converters, cross converters, multi-viewers) via **USB**.
- Uses **FTDI chip** in **bit-bang mode** (VID `0x215F`, PID `0x6000`). No official SDK; protocol from [decimctl](https://github.com/quentinmit/decimctl) (Python, Apache 2.0).
- **Design doc (source of truth):** [Decimator_Design_Android_Control_App.md](../Decimator_Design_Android_Control_App.md) at repo root.

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

- **Dependency:** App depends on `fileTree("libs") { include("*.aar", "*.jar") }` in `app/build.gradle.kts`. **FTDI library is in place:** **app/libs/d2xx.jar** (from Android Java D2XX 2.13). See **app/libs/README.md** if you need to replace or upgrade it.
- **Driver:** `app/.../ftdi/DecimatorFtdiDriver.kt`
  - `open(context, usbDevice)` → `Result<DecimatorConnection>`; all work on `Dispatchers.IO`.
  - Init: `resetDevice()`, `setBaudRate(3_000_000)`, `setFlowControl(FT_FLOW_RTS_CTS, 0, 0)`, `setChars(0,0,0,0)`, `setLatencyTimer(1)`, `setBitMode(0, RESET)` → `setBitMode(0, SYNC_BITBANG)` → clock `0x48` → `setBitMode(0x48, SYNC_BITBANG)`.
  - `clockRawBytes(ft, dataIn)` in chunks of 256; read/write via FT_Device.
- **Connection:** `DecimatorConnection(ft)` — `clockRawBytes()`, `fpgaWriteBytes(register, value)`, `fpgaReadBytes(start, length)`, `readRawRegisters()`, `close()`. All public APIs use `withContext(Dispatchers.IO)`.
- **Errors:** `DecimatorError` sealed class: UnsupportedDevice, OpenFailed, FtdiError, IOError, DeviceDisconnected.

### 4. Protocol (decimctl port)

- **File:** `app/.../protocol/DecimatorProtocol.kt`
  - Constants: `REGISTER_SIZE = 0x200`, `READ_PREAMBLE`, `WRITE_PREAMBLE`, `WRITE_POSTAMBLE` (byte arrays).
  - `bytesToRawCommand(ByteArray)`, `rawResponseToBytes(ByteArray)`, `bitListToBytes(List<Boolean>)`. `ProtocolException` for decode errors.

### 5. Connection state and UI

- **State:** `app/.../connection/ConnectionState.kt` — sealed: NoDevice, PendingPermission(device), PermissionDenied(device), UnsupportedDevice(device), Connecting, Connected(connection, device), Error(DecimatorError).
- **ViewModel:** `app/.../connection/DecimatorViewModel.kt` — `state: StateFlow<ConnectionState>`, `refreshDeviceState()`, `requestUsbPermission()`, `onPermissionGranted()`, `onPermissionDenied()`, `disconnect()`, `onDeviceDetached()`. Opens device via `DecimatorFtdiDriver.open()` on IO.
- **Activity:** `app/.../MainActivity.kt` — `BroadcastReceiver` for USB permission result, `onResume`/`onPause` register/unregister, `onNewIntent` for attach; holds `DecimatorViewModel` by `viewModels()`.
- **UI:** `app/.../ui/DecimatorApp.kt` — Composable that takes ViewModel, collects `state`, shows message/button per state (NoDevice, PendingPermission with “Grant USB permission”, PermissionDenied, Connecting, Connected with “Disconnect”, Error). Theme: `app/.../ui/theme/Theme.kt`.

### 6. GitHub and sharing

- **Remote:** `origin` → `https://github.com/karmajinx-og/Decimator-Android.git`. Owner: **karmajinx-og**.
- **Pushed:** Latest code on `main` (includes rules, docs, wrapper, supplementary, SHARING_FOR_AUDIT).
- **Audit tag:** `v0.1.0-audit` created and pushed. Auditors can clone and `git checkout v0.1.0-audit`.
- **Sharing guide:** [docs/SHARING_FOR_AUDIT.md](SHARING_FOR_AUDIT.md) — GitHub (primary) vs zip snapshot.

### 7. Documentation and project discipline

- **Master context:** [MASTER_CONTEXT.md](../MASTER_CONTEXT.md) — entry point: what the project is, repo, rules, log, directory layout, build, backups, sharing.
- **Project log:** [docs/PROJECT_LOG.md](PROJECT_LOG.md) — narrative of what occurred (setup → FTDI → protocol → USB/UI → GitHub → rules/backup).
- **Rules:** [docs/RULES.md](RULES.md) — Kotlin/Compose, USB off main thread, protocol alignment, naming, dependencies, docs, backups, Git, Markdown.
- **Recall:** This file — done vs next, resume steps.

### 8. Backups and rollback

- **Build backups:** `backups/README.md` — how to create timestamped zips (exclude .git, build, .gradle, etc.). First backup: `backups/Decimator-Android-build-2026-03-08.zip` (not in Git; `backups/*.zip` in .gitignore).
- **Supplementary:** Folder `supplementary/` — copies of design doc, README, RULES, MASTER_CONTEXT, PROJECT_LOG. Update folder when docs change; regenerate `supplementary-and-rollback.zip` from it when needed (zip in .gitignore).

### 9. Build audit — all fixes applied

- **Audit report:** 14 issues (4 critical, 4 medium, 6 low). All critical and medium fixes applied; low L1–L3 applied; L4–L6 deferred (see [docs/FIXES_APPLIED.md](FIXES_APPLIED.md)).
- **Critical:** C1 by design (AAR); C2 Result → onSuccess/onFailure; C3 ACTION_USB_PERMISSION inside object; C4 DecimatorError ftdiMessage/ioMessage, data object DeviceDisconnected.
- **Medium:** M1 USB detach receiver in MainActivity; M2 short-read guard in clockRawBytes; M3 protocol bit-phase → Log.w not throw; M4 COMMAND_PREAMBLE, READ/WRITE delegate.
- **Low applied:** L1 single DecimatorUsbConstants; L2 remove dead viewModel check; L3 onCleared() → disconnect().
- **Low deferred:** L4 minification, L5 Kotlin 2.x, L6 AndroidViewModel (documented in AUDIT_RESPONSE).

---

## What is next to be done

### Immediate (when you’re back — testing)

1. ~~**Add FTDI D2XX** to app/libs/~~ — **Done.** `app/libs/d2xx.jar` is in place (Android Java D2XX 2.13).
2. **Run build locally:** `./gradlew assembleDebug` (Java 17 + Android SDK required, e.g. via Android Studio).
3. **Install and test on device:** USB host + OTG. Test with Decimator (connect, grant permission, open, disconnect) or without (confirm “Connect a Decimator device” / permission flow).

### Short term (from design doc and current gaps)

4. **Confirm FTDI chip model** in Decimator hardware (design doc Step 1).
5. **Test open/read** with real hardware: confirm `DecimatorConnection.readRawRegisters()` or a single register read works; handle any timing/errors.
6. **Device type from serial:** Implement mapping from first 3 chars of serial (e.g. CLA, MQD) to device type / register map (decimctl `_SERIAL_PREFIX_TO_TYPE` and `registers` property).

### Medium term (control UI and device support)

7. **Port register map structs** from decimctl `protocol.py` (CPA_Registers, VFA_Registers, KPA_Registers) to Kotlin data classes or similar; expose typed access (e.g. CPA.SO_Source) and writes that call `fpgaWriteBytes`.
8. **Build control UI** by category (matching desktop UCP): settings screens per device family, bound to register state.
9. **USB detach** is already handled (M1: usbDetachReceiver calls `viewModel.onDeviceDetached`).

### Later

10. **Test each device family** (CPA, VFA, KPA) as needed; document any protocol differences.
11. **Firmware:** Do not attempt firmware update without official docs (bricking risk per design doc).

---

## How to resume

1. **Read this file** (RECALL.md) and [MASTER_CONTEXT.md](../MASTER_CONTEXT.md).
2. **Sync and build:** Open project in Android Studio, add FTDI AAR to `app/libs/` if not done, run `./gradlew assembleDebug`.
3. **Pick the next task** from “What is next to be done” (e.g. add AAR → build → test on device → device type from serial → register map port).
4. **After making changes:** Update [docs/PROJECT_LOG.md](PROJECT_LOG.md) if significant; update this RECALL’s “What has been done” / “What is next” if the roadmap changes.
5. **Before calling anything “ready for audit”:** Use [docs/LESSONS_FROM_AUDIT.md](LESSONS_FROM_AUDIT.md) (short checklist). For a **full audit**, follow [docs/AUDIT_PROMPT.md](AUDIT_PROMPT.md) (phases, prompts, output format).

---

## Key file reference

| Purpose | Path |
|--------|------|
| Design doc | `Decimator_Design_Android_Control_App.md` |
| Master context | `MASTER_CONTEXT.md` |
| Project log | `docs/PROJECT_LOG.md` |
| Rules | `docs/RULES.md` |
| Recall (this) | `docs/RECALL.md` |
| Sharing for audit | `docs/SHARING_FOR_AUDIT.md` |
| Every fix applied | `docs/FIXES_APPLIED.md` |
| FTDI driver | `app/.../ftdi/DecimatorFtdiDriver.kt` |
| Protocol | `app/.../protocol/DecimatorProtocol.kt` |
| Connection state | `app/.../connection/ConnectionState.kt`, `DecimatorViewModel.kt` |
| Main UI | `app/.../ui/DecimatorApp.kt` |
| USB filter | `app/src/main/res/xml/device_filter.xml` |
| FTDI lib instructions | `app/libs/README.md` |

---

**When you take another break:** Update the “What has been done” and “What is next to be done” sections above, and bump “Last updated” at the top.
