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

- `docs/PROJECT_LOG.md` — This log.
- `docs/RULES.md` — Project rules.
- `MASTER_CONTEXT.md` — Master context (root).
- `backups/README.md` — How to create build backup zips.
- `backups/Decimator-Android-build-2026-03-08.zip` — First build backup (excludes .git, build, .gradle, etc.).
- `supplementary/` — Design doc, README copy, RULES, MASTER_CONTEXT, PROJECT_LOG; update this folder for rollback, regenerate zip when needed.
- `supplementary-and-rollback.zip` — Zip of `supplementary/` (at project root); regenerate after updating supplementary/.
- `.gitignore` — Added `backups/*.zip` so large backup zips are not committed.

---

## 8. Recall and resume (before break)

- **docs/RECALL.md** — Recall document: what has been done (in detail), what is next to be done (immediate → short → medium → later), how to resume, key file reference. For use when returning from a break.
- **MASTER_CONTEXT.md** — Updated with “Resuming work / recall” section (points to RECALL.md), RECALL added to directory layout, and note to keep RECALL updated when resuming or after significant work.

---

## 9. First audit — critical fixes (C2–C4)

- **Audit:** 14 issues (4 critical, 4 medium, 6 low). Critical C2–C4 fixed so the project compiles (with FTDI AAR in app/libs/).
- **C2:** Replaced invalid `is Result.success` / `is Result.failure` with `result.isSuccess` and `getOrNull()` / `exceptionOrNull()` in DecimatorViewModel.
- **C3:** Moved `ACTION_USB_PERMISSION` inside `object UsbPermissionHelper` so `UsbPermissionHelper.ACTION_USB_PERMISSION` compiles.
- **C4:** DecimatorError: renamed FtdiError/IOError payload to `detail: String?`, fixed getter (was `msg` → `detail`), removed conflicting `override val message: String?` in subclasses.
- **C1 (missing AAR):** By design; see app/libs/README.md and docs/AUDIT_RESPONSE.md.
- **docs/AUDIT_RESPONSE.md** — Full audit response, why C2–C4 weren’t caught (no local compile, Result API, file-level const, Exception override).

---

## 10. Full audit fixes (medium + low)

- **C2/C4 aligned to audit:** DecimatorViewModel now uses `result.onSuccess { }.onFailure { }`; DecimatorError uses `ftdiMessage`/`ioMessage` and `data object DeviceDisconnected`.
- **M1:** MainActivity — usbDetachReceiver for ACTION_USB_DEVICE_DETACHED; register/unregister in onResume/onPause; calls viewModel.onDeviceDetached(device).
- **M2:** DecimatorFtdiDriver.clockRawBytes — throw IOError if read != chunk (short read).
- **M3:** DecimatorProtocol.rawResponseToBytes — Log.w on bit-phase mismatch, continue (no throw).
- **M4:** DecimatorProtocol — COMMAND_PREAMBLE; READ_PREAMBLE and WRITE_PREAMBLE delegate to it; comment references protocol.py.
- **L1:** DecimatorFtdiDriver — removed duplicate DecimatorUsbConstants; use usb.DecimatorUsbConstants only.
- **L2:** MainActivity — removed dead viewModel ?: return; use viewModel directly.
- **L3:** DecimatorViewModel — override onCleared(), call disconnect().
- **L4–L6 deferred:** Minification, Kotlin 2.x, AndroidViewModel documented in AUDIT_RESPONSE.
- **docs/FIXES_APPLIED.md** — Single reference list of every fix (critical, medium, low applied, low deferred).

---

## 11. FTDI library in place; break before testing

- **FTDI D2XX:** User had **Android Java D2xx** package (2.13) in the project folder. **d2xx.jar** was copied from `Android_Java_D2xx_2.13/d2xx.jar` into **app/libs/d2xx.jar** so the build can resolve the dependency.
- **Status:** Build dependency satisfied. User is taking a break.
- **When back:** Use **docs/RECALL.md** — next steps are run `./gradlew assembleDebug`, then install and test on a USB-host device (with or without a Decimator).
