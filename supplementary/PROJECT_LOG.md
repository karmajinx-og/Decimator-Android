# Project log — what occurred

**Project:** Decimator-Android  
**Repo:** https://github.com/karmajinx-og/Decimator-Android  
**Last updated:** 2026-03-08

---

## 1. Project start (from design doc)

- **Source:** `Decimator_Design_Android_Control_App.md` (research/design document).
- **Goal:** Android app to control Decimator Design hardware via USB (FTDI bit-bang), with no official SDK.
- **Approach:** Use FTDI Android D2XX library and port the reverse‑engineered Python protocol from [quentinmit/decimctl](https://github.com/quentinmit/decimctl).

---

## 2. Initial project setup

- Created Android project: **Kotlin**, **Jetpack Compose**, **Gradle** (Kotlin DSL, version catalog).
- **Namespace:** `com.decimator.android`  
- **minSdk 26**, **targetSdk 35**, **compileSdk 35**.
- **USB:** Intent filter for Decimator devices (VID `0x215F`, PID `0x6000`) in `AndroidManifest.xml` and `res/xml/device_filter.xml`.
- **Git:** Repo initialized; `.gitignore` added for Android/Gradle/Studio.

---

## 3. FTDI communication layer (core)

- **FTDI D2XX:** Project configured to use FTDI AAR/JAR from `app/libs/` (see `app/libs/README.md`).
- **Init sequence (matches decimctl):**
  - Baud **3,000,000**, latency **1 ms**, **RTS/CTS** flow control.
  - Bit-bang: `setBitMode(0, RESET)` → `setBitMode(0, SYNC_BITBANG)` → clock `0x48` → `setBitMode(0x48, SYNC_BITBANG)`.
- **Protocol port (Kotlin):**
  - `DecimatorProtocol`: `READ_PREAMBLE`, `WRITE_PREAMBLE`, `WRITE_POSTAMBLE`, `bytesToRawCommand()`, `rawResponseToBytes()`, `bitListToBytes()`.
  - `DecimatorConnection`: `fpgaWriteBytes()`, `fpgaReadBytes()`, `readRawRegisters()`, `clockRawBytes()`, `close()`.
- **Thread safety:** All USB/FTDI I/O on `Dispatchers.IO` (coroutines); no USB on main thread.
- **Errors:** `DecimatorError` (UnsupportedDevice, OpenFailed, FtdiError, IOError, DeviceDisconnected).

---

## 4. USB permissions and UI

- **Runtime permission:** `UsbPermissionHelper` + `BroadcastReceiver` for `USB_DEVICE_ATTACHED` and permission result.
- **State:** `ConnectionState` (NoDevice, PendingPermission, PermissionDenied, UnsupportedDevice, Connecting, Connected, Error).
- **ViewModel:** `DecimatorViewModel` — refresh device, request permission, open/close on IO, disconnect.
- **UI:** `DecimatorApp` (Compose) shows connection state, “Grant USB permission”, “Disconnect”, and errors.

---

## 5. GitHub

- **Remote:** `origin` → `https://github.com/karmajinx-og/Decimator-Android.git`
- **Previous repo content:** Only `Decimator_Design_Android_Control_App.md` and a short `README.md`.
- **Update:** Full local build (single commit) was force-pushed to `main`, so the repo now contains the complete Android project and updated README.

---

## 6. Project rules and context (this session)

- **docs/PROJECT_LOG.md** — This log (what occurred).
- **MASTER_CONTEXT.md** — Master context and entry point for rules, structure, and process.
- **docs/RULES.md** — Project rules and conventions.
- **backups/** — Timestamped zip backups of the build.
- **supplementary/** — Supplementary and rollback files (single folder to update; can be zipped as `supplementary-and-rollback.zip` when needed).

---

## 7. Files and folders added in this session (rules/backup)

- `docs/PROJECT_LOG.md`
- `docs/RULES.md`
- `MASTER_CONTEXT.md`
- `backups/` (build backup zip + README)
- `supplementary/` (design doc, README copy, etc.) and optional `supplementary-and-rollback.zip`
