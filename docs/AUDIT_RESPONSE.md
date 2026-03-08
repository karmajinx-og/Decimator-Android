# Audit response — first-attempt build

**Audit:** 14 issues (4 critical, 4 medium, 6 low).  
**Response date:** 2026-03-08

---

## Critical fixes applied (C2–C4)

### [C2] `Result.success` / `Result.failure` — FIXED

- **Issue:** `is kotlin.Result.success` / `is kotlin.Result.failure` is invalid Kotlin; `Result` is a value class, not a sealed class with those subtypes.
- **Fix:** Replaced with `result.isSuccess` and `result.getOrNull()` / `result.exceptionOrNull()` in `DecimatorViewModel.openDevice()`.

### [C3] `UsbPermissionHelper.ACTION_USB_PERMISSION` — FIXED

- **Issue:** The constant was at file scope, so `UsbPermissionHelper.ACTION_USB_PERMISSION` from MainActivity was a compile error.
- **Fix:** Moved `const val ACTION_USB_PERMISSION` inside `object UsbPermissionHelper` in `UsbPermissionHelper.kt`. References in the same file (Intent, isPermissionIntent) use the member constant.

### [C4] `DecimatorError` message/override — FIXED

- **Issue:** (1) Getter referenced `msg` which doesn’t exist (should use the subclass property). (2) Subclasses overrode `message` with `String?`, conflicting with the sealed class’s `message: String` and Exception’s contract.
- **Fix:** Renamed the nullable payload to `detail: String?` in `FtdiError` and `IOError`. The sealed class’s single `override val message: String` getter now returns `detail ?: "FTDI error"` / `detail ?: "I/O error"` for those subtypes. All construction sites already pass a string (e.g. `FtdiError(err?.message)`, `IOError("write failed: $written")`).

---

## [C1] FTDI D2XX `.aar` missing — by design

- **Issue:** The driver layer has unresolved imports for `com.ftdi.j2xx` because no AAR is in `app/libs/`.
- **Status:** Intentional. The FTDI D2XX library is not bundled in the repo (licensing/redistribution). **app/libs/README.md** instructs adding the AAR for a full build.
- **For auditors / CI:** To get a compilable build without the real AAR you would need either: (1) add the AAR to `app/libs/` (download from FTDI), or (2) introduce an interface + no-op implementation and make the FTDI dependency optional (larger refactor). Current policy is (1).

---

## Why the audit caught these and the original implementation didn’t

The auditor had at least one of:

- **A working compile:** They ran the build (Java + Android SDK available), so the compiler reported C2, C3, and C4.
- **Strict code review:** They checked the code against the Kotlin language and standard library (e.g. `Result` API, where constants live, `Exception` overrides) and spotted the mistakes without running anything.

The original implementation didn’t catch them because:

1. **No compile was run here** — This environment had no Java runtime, so `./gradlew assembleDebug` was never executed and the compiler never reported the errors.
2. **No equivalent code review was done** — C2, C3, and C4 could have been found without running the build by checking:
   - **C2:** Kotlin’s `Result` API (no sealed `success`/`failure`; use `isSuccess`, `getOrNull()`, `exceptionOrNull()`).
   - **C3:** That `UsbPermissionHelper.ACTION_USB_PERMISSION` is a real member (the constant was at file scope, not inside the object).
   - **C4:** That the `message` getter used an existing property and that subclasses didn’t illegally override `message` with `String?`.

So the gap isn’t only “no Java” — it’s that a **systematic check of the code against the language/spec** would have found these. The audit did that (and/or ran the build); the original pass didn’t.

**Going forward:** (1) Run `./gradlew assembleDebug` when the environment allows. (2) When it doesn’t, explicitly verify Kotlin/API usage (Result, const/object scope, overrides) so compile-time bugs are caught before audit.

---

## Medium issues — applied

- **[M1] USB detach receiver:** Registered `usbDetachReceiver` for `UsbManager.ACTION_USB_DEVICE_DETACHED` in MainActivity; register in onResume, unregister in onPause; calls `viewModel.onDeviceDetached(device)`.
- **[M2] Short read in clockRawBytes:** After `ft.read(chunkBuf, chunk)`, now throw `DecimatorError.IOError("Short read: expected $chunk, got $read")` if `read != chunk`.
- **[M3] Protocol bit-phase mismatch:** In `DecimatorProtocol.rawResponseToBytes`, replaced throw with `Log.w(TAG, "Bit phase mismatch at bit … — continuing")` to match decimctl behaviour.
- **[M4] READ/WRITE preamble:** Confirmed identical in decimctl protocol.py; added `COMMAND_PREAMBLE` and made `READ_PREAMBLE` / `WRITE_PREAMBLE` delegate to it, with comment.

## Low issues — applied

- **[L1] Duplicate DecimatorUsbConstants:** Removed private copy from DecimatorFtdiDriver.kt; use `com.decimator.android.usb.DecimatorUsbConstants` only.
- **[L2] Dead null check:** Removed `val vm = viewModel ?: return` in MainActivity; use `viewModel` directly.
- **[L3] ViewModel connection leak:** Overrode `onCleared()` in DecimatorViewModel and call `disconnect()` so the FTDI connection is closed when the ViewModel is cleared.

## Low issues — deferred (documented)

- **[L4] isMinifyEnabled = false:** Left as-is for now; enable minification and add ProGuard keep rules for FTDI before release.
- **[L5] Kotlin 1.9.24:** Stale version; plan upgrade to Kotlin 2.x and Compose Compiler plugin when ready.
- **[L6] Context in ViewModel:** Passing Context into ViewModel at runtime is a known anti-pattern; refactor to AndroidViewModel + Application when doing a larger DI/context cleanup.
