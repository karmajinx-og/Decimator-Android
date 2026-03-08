# Audit prompt — how to run an extensive, comprehensive audit

**Purpose:** A dedicated, methodical guide for auditing this codebase (or similar). Use it as a prompt, playbook, or checklist so the audit is **extensive** and **comprehensive**, not shallow or ad hoc.

**Related:** [LESSONS_FROM_AUDIT.md](LESSONS_FROM_AUDIT.md) (what we learned), [AUDIT_RESPONSE.md](AUDIT_RESPONSE.md) (first audit response).

---

## How to approach the audit: logic and order

### 1. Work in phases, from “does it build?” to “is it correct and maintainable?”

Do not jump straight to “code style” or “architecture.” Follow this order so blockers are found first and nothing is skipped:

| Phase | Goal | Why this order |
|-------|------|-----------------|
| **1. Build & compile** | The project compiles and links. | If it doesn’t build, nothing else is testable. Fix or document every compile error. |
| **2. Language & API correctness** | Kotlin/Java and library APIs are used correctly. | Wrong API usage causes runtime crashes or wrong behavior even when the build succeeds. |
| **3. Threading & I/O** | No main-thread blocking; I/O and drivers on background. | Prevents ANRs and flaky behavior. |
| **4. Lifecycle & resources** | No leaks, no use-after-clear; receivers and subscriptions cleaned up. | Prevents leaks and crashes on config change or disconnect. |
| **5. Error handling & edge cases** | Disconnect, permission denied, short reads, protocol errors are handled. | Prevents silent failures and bad UX. |
| **6. Consistency & structure** | No duplicated magic values; clear layering; naming matches project rules. | Maintainability and fewer bugs from copy-paste. |
| **7. Security & permissions** | No hardcoded secrets; permissions requested and checked correctly. | Basic security and compliance. |
| **8. Documentation & intent** | Known limitations and “by design” choices are documented. | So future readers and auditors don’t treat them as bugs. |

Within each phase, go **file-by-file or layer-by-layer** (e.g. USB → FTDI → protocol → UI) so you don’t skip areas.

### 2. Verify against the source of truth, don’t guess

- For **Kotlin stdlib** (e.g. `Result`, `Flow`): look up the actual API (docs or “Go to declaration”). Do not assume “success/failure” or similar phrasing matches the type.
- For **Android** (e.g. `UsbManager`, `BroadcastReceiver`): confirm method signatures, constant scope (object vs file), and lifecycle.
- For **third-party** (e.g. FTDI D2XX): use the official API or descriptor; confirm init sequence, read/write signatures, and error returns.
- For **protocol** (e.g. decimctl): compare constants and control flow to the original source (e.g. `protocol.py`, `__init__.py`) so preambles, postambles, and bit encoding match.

### 3. Record every finding with severity and exact location

For each issue note:

- **ID** (e.g. C1, M1, L1).
- **Severity:** Critical (won’t compile / will crash) / Medium (wrong behavior or leak) / Low (quality or maintainability).
- **Location:** File path and, if useful, line or symbol.
- **What’s wrong:** One sentence.
- **Fix or rationale:** Suggested fix or “by design” and where it’s documented.

This makes the audit repeatable and the response traceable.

---

## Phase 1: Build and compile

**Objective:** The project builds with no unresolved references (or exactly one documented exception, e.g. optional AAR).

- [ ] **1.1** Environment has Java 17+ and Android SDK; any required native/libs (e.g. FTDI AAR in `app/libs/`) are present or their absence is documented.
- [ ] **1.2** Run `./gradlew assembleDebug` (or equivalent). Result is **BUILD SUCCESSFUL**.
- [ ] **1.3** There are no “unresolved reference” or “cannot find symbol” errors. If one exception exists (e.g. optional AAR), it is clearly documented (e.g. `app/libs/README.md`, AUDIT_RESPONSE).
- [ ] **1.4** Lint/static analysis: run the project’s lint task (if any) and fix or justify every reported error.

**Prompt to use:** “Run the full build and list every compile or link error. For each, either fix it or document why it is an intentional exception and where that is documented.”

---

## Phase 2: Language and API correctness

**Objective:** Kotlin and all libraries are used according to their real API; no invalid syntax or misuse that the compiler might not catch.

- [ ] **2.1 Result / optional types**  
  Search for `Result`, `is success`, `is failure`, and similar. Confirm: no `is Result.success` or `is Result.failure` (Kotlin’s `Result` is a value class). Use `isSuccess`, `getOrNull()`, `exceptionOrNull()`, or `fold`. Same for other optional-like types.
- [ ] **2.2 Constants and scope**  
  For every reference of the form `SomeObject.CONSTANT` or `SomeObject.METHOD`, confirm that `CONSTANT` or `METHOD` is defined **inside** `SomeObject` (class/object), not only in the same file as a top-level. Fix or document.
- [ ] **2.3 Overrides (Exception, etc.)**  
  For every class extending `Exception` or other framework base: (a) there is a single, correct override of `message` (type and nullability consistent with base); (b) getters do not reference non-existent properties (e.g. `msg` vs `message`); (c) subclasses do not override in a way that widens or contradicts the base contract.
- [ ] **2.4 External API usage**  
  For every use of a third-party or Android API (e.g. FTDI `openByUsbDevice`, `setBitMode`, `read`/`write`): confirm method names, parameter order and types, and return types against the official API. Flag any mismatch.
- [ ] **2.5 Protocol constants and encoding**  
  If the app ports a known protocol (e.g. decimctl): compare preambles, postambles, and encoding/decoding (e.g. `bytesToRawCommand`, `rawResponseToBytes`) to the original source line-by-line. Flag any difference unless it’s an intentional, documented deviation.

**Prompt to use:** “Go through the codebase and verify: (1) Result and optional types use the actual Kotlin API, not assumed subtypes. (2) Every Foo.BAR reference: BAR is a member of Foo. (3) Exception subclasses: correct message override, no typos in getters, no illegal override. (4) FTDI and protocol usage match the official/upstream API and source.”

---

## Phase 3: Threading and I/O

**Objective:** No blocking of the main thread; all USB/driver/network/disk I/O runs on a background dispatcher or thread.

- [ ] **3.1** Identify every call that opens a device, reads, writes, or does heavy I/O. Confirm each is invoked from a coroutine on `Dispatchers.IO` (or equivalent) or from a background thread, and never from the main thread or a main-thread callback without `withContext`/equivalent.
- [ ] **3.2** Confirm that UI updates (e.g. `StateFlow.value =`, Compose state) are not performed from a background thread unless they use the proper main-safe mechanism (e.g. `withContext(Dispatchers.Main)` or library support).

**Prompt to use:** “List every place that performs USB, FTDI, or file/network I/O. For each, state whether it runs on the main thread or a background thread. Flag any that run on the main thread.”

---

## Phase 4: Lifecycle and resources

**Objective:** No connection or receiver leaks; cleanup on disconnect and when the ViewModel/Activity is destroyed.

- [ ] **4.1** When a USB device is disconnected (unplugged), is there a receiver or callback that clears the connection and updates UI state? If not, flag it.
- [ ] **4.2** When the user disconnects or the screen is destroyed, is the FTDI connection closed (e.g. `connection.close()`)? Is the ViewModel or Activity clearing state and not holding references that prevent GC?
- [ ] **4.3** BroadcastReceivers (e.g. for USB permission): are they unregistered in `onPause` or equivalent so they are not leaked?

**Prompt to use:** “Trace what happens when (a) the USB cable is unplugged, (b) the user taps Disconnect, (c) the Activity is destroyed. For each, confirm the connection is closed and receivers unregistered. List any leak or missing cleanup.”

---

## Phase 5: Error handling and edge cases

**Objective:** Disconnect, permission denied, short reads, and protocol errors are handled; the app does not crash or silently fail.

- [ ] **5.1** If the device is unplugged during a read/write, does the code handle the error (e.g. catch, close, and transition state) instead of crashing?
- [ ] **5.2** If a read returns fewer bytes than requested (short read), does the code handle it (retry, fail explicitly, or document why it’s safe to ignore)?
- [ ] **5.3** Protocol or decode errors (e.g. bit-phase mismatch): does the code throw, log, or recover? Is that choice documented and consistent?
- [ ] **5.4** Permission denied: does the UI show a clear state (e.g. PermissionDenied) and not treat it as a generic error?

**Prompt to use:** “For disconnect, permission denied, short read, and protocol/decode failure: describe how each is handled (or not). List any crash path or silent failure.”

---

## Phase 6: Consistency and structure

**Objective:** No duplicated magic numbers/strings; layering and naming match project rules (e.g. RULES.md).

- [ ] **6.1** Constants (e.g. VID/PID, baud rate, latency): defined once and reused, or duplicated? If duplicated, flag and suggest a single source.
- [ ] **6.2** Naming and packages: do they match the project’s stated layout (e.g. `usb`, `ftdi`, `protocol`, `connection`, `ui`)?
- [ ] **6.3** Dead code: any null checks or branches that are always true/false? Remove or document.
- [ ] **6.4** Release build: is minification/R8 configured if desired? (Flag if completely off and the project intends to ship.)

**Prompt to use:** “List every literal constant (numbers, important strings) used in USB/FTDI/protocol code. For each, state whether it is defined in one place and reused. List any dead or redundant check.”

---

## Phase 7: Security and permissions

**Objective:** No hardcoded secrets; USB (and other) permissions are requested and checked correctly.

- [ ] **7.1** No API keys, passwords, or tokens in source or in default config that get committed.
- [ ] **7.2** USB permission: requested before use; result (granted/denied) is handled and reflected in UI/state.
- [ ] **7.3** Intent filters and manifest: only the minimum required permissions and components are exported.

**Prompt to use:** “Confirm USB permission is requested and the result is handled. Confirm no secrets are hardcoded. List any permission or export that seems unnecessary.”

---

## Phase 8: Documentation and intent

**Objective:** Known limitations and “by design” choices are documented so they are not mistaken for bugs.

- [ ] **8.1** If the project does not compile without an external artifact (e.g. FTDI AAR), that is documented (e.g. `app/libs/README.md`) and, if applicable, in the audit response.
- [ ] **8.2** Protocol or behavioral differences from the reference implementation (e.g. decimctl) are documented with rationale.
- [ ] **8.3** RECALL, PROJECT_LOG, or MASTER_CONTEXT are updated if the audit finds that “what’s done” or “what’s next” has changed.

**Prompt to use:** “List every known limitation or by-design choice that could be mistaken for a bug. For each, state where it is documented in the repo.”

---

## What we learned (incorporated)

- **Run the build first.** The compiler catches many issues; don’t skip Phase 1.
- **When the build can’t run,** Phase 2 (language/API) is even more important: verify Result, scope, overrides, and external/protocol API against the source of truth.
- **Don’t assume “it looks right.”** Check the actual type and API; one wrong assumption can cause a critical bug.
- **Treat “first attempt” as unverified** until Phase 1 and at least Phase 2 are done. Use this prompt before calling anything “ready for audit.”

---

## Output format for the auditor

Produce a single report with:

1. **Summary:** Total counts by severity (Critical / Medium / Low).
2. **Per issue:** ID, severity, file (and line/symbol if helpful), one-sentence description, suggested fix or pointer to documentation.
3. **Phases:** For each phase, a short “Pass” / “Fail” and the list of issue IDs in that phase.
4. **Recommendations:** Ordered list of what to fix first (e.g. all Critical, then Medium, then Low).

Example:

```text
## Summary
- Critical: 4
- Medium: 4
- Low: 6

## Critical
- [C1] app/libs/ missing FTDI AAR — entire driver layer unresolved. See app/libs/README.md (by design).
- [C2] DecimatorViewModel.kt — invalid Result check. Use isSuccess/getOrNull/exceptionOrNull.
…
```

---

## One-line summary

**Work in phases (build → API → threading → lifecycle → errors → structure → security → docs); verify every API and constant against the source of truth; record every finding with severity and location; produce a structured report so the audit is extensive and comprehensive.**
