# Every fix applied — single reference

**Purpose:** One place to see every fix that has been done (critical, medium, low).  
**Source:** Build Audit Report #1 (14 issues) + earlier critical fixes.  
**Last updated:** 2026-03-08

---

## Critical (compile-blocking)

| ID | File(s) | Fix |
|----|--------|-----|
| **C1** | app/libs/, app/build.gradle.kts | **By design:** FTDI AAR not in repo. Documented in `app/libs/README.md` and `docs/AUDIT_RESPONSE.md`. Add AAR to `app/libs/` to build. |
| **C2** | DecimatorViewModel.kt | Replaced invalid `is Result.success` / `is Result.failure` with `DecimatorFtdiDriver.open(...).onSuccess { }.onFailure { }`. |
| **C3** | UsbPermissionHelper.kt | Moved `const val ACTION_USB_PERMISSION` **inside** `object UsbPermissionHelper` so `UsbPermissionHelper.ACTION_USB_PERMISSION` compiles from MainActivity. |
| **C4** | DecimatorFtdiDriver.kt (DecimatorError) | (1) FtdiError/IOError use `ftdiMessage: String?` and `ioMessage: String?`; getter uses those, not `msg`. (2) `DeviceDisconnected` is `data object`. Single `override val message: String` getter in sealed class. |

---

## Medium (runtime / logic)

| ID | File(s) | Fix |
|----|--------|-----|
| **M1** | MainActivity.kt | Added `usbDetachReceiver` for `UsbManager.ACTION_USB_DEVICE_DETACHED`; register in onResume, unregister in onPause; calls `viewModel.onDeviceDetached(device)`. |
| **M2** | DecimatorFtdiDriver.kt (clockRawBytes) | After `ft.read(chunkBuf, chunk)`, throw `DecimatorError.IOError("Short read: expected $chunk, got $read")` if `read != chunk`. |
| **M3** | DecimatorProtocol.kt (rawResponseToBytes) | Replaced throw on bit-phase mismatch with `Log.w(TAG, "Bit phase mismatch at bit … — continuing")` and continue (match decimctl). |
| **M4** | DecimatorProtocol.kt | Added `COMMAND_PREAMBLE`; `READ_PREAMBLE` and `WRITE_PREAMBLE` are getters returning it. Comment: decimctl protocol.py has identical preambles. |

---

## Low (applied)

| ID | File(s) | Fix |
|----|--------|-----|
| **L1** | DecimatorFtdiDriver.kt, DecimatorUsbManager.kt | Removed private `DecimatorUsbConstants` from DecimatorFtdiDriver; use `com.decimator.android.usb.DecimatorUsbConstants` only. |
| **L2** | MainActivity.kt (usbPermissionReceiver) | Removed dead `val vm = viewModel ?: return`; use `viewModel` directly. |
| **L3** | DecimatorViewModel.kt | Overrode `onCleared()`; call `super.onCleared()` then `disconnect()` so FTDI connection is closed when ViewModel is cleared. |

---

## Low (deferred — documented)

| ID | Intent | Where documented |
|----|--------|------------------|
| **L4** | Enable minification for release + ProGuard keep rules for FTDI | docs/AUDIT_RESPONSE.md — do before release. |
| **L5** | Upgrade Kotlin to 2.x and Compose Compiler plugin | docs/AUDIT_RESPONSE.md — plan when ready. |
| **L6** | Refactor to AndroidViewModel + Application instead of passing Context into ViewModel | docs/AUDIT_RESPONSE.md — with DI/context cleanup. |

---

## Other (project setup, not from audit)

- Gradle wrapper added (gradlew, gradlew.bat, gradle-wrapper.jar).
- Project rules, master context, recall, backups, supplementary, sharing-for-audit, lessons-from-audit, audit prompt docs added.
- GitHub: pushed to main; audit tag `v0.1.0-audit` created and pushed.
