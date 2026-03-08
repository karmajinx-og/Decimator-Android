# What we learned from the audit — and how to audit next time

**Source:** First-attempt build audit (14 issues; 4 critical).  
**Purpose:** So the same mistakes aren’t repeated and the next “audit” (internal or external) is done right.  
**For a full, methodical audit:** Use [AUDIT_PROMPT.md](AUDIT_PROMPT.md) (phases, prompts, and output format).

---

## What we learned

### 1. Run the build whenever possible

- **Lesson:** The compiler finds things that code review can miss. C2, C3, and C4 would have been reported immediately by a successful compile (or its failure).
- **Practice:** Before calling anything “done” or “ready for audit,” run `./gradlew assembleDebug` in an environment that has Java 17+ and the Android SDK (and the FTDI AAR in `app/libs/` if the project depends on it). Fix every compile error. Treat “it doesn’t build” as a blocker.

### 2. When the build can’t run, do a language/API check

- **Lesson:** The auditor caught bugs without necessarily running the build by checking the code against the **Kotlin language** and **standard/Android APIs**. We could have done the same.
- **Practice:** If the build isn’t run (e.g. no Java in the environment), do an explicit **API and language pass**:
  - **Types and APIs:** Look up the real API for any type you use (e.g. `Result`: use `isSuccess`, `getOrNull()`, `exceptionOrNull()`, not `is Result.success`).
  - **Scoping:** For every reference like `Foo.BAR`, confirm that `BAR` is actually a member of `Foo` (not a top-level in the same file).
  - **Overrides:** For classes extending framework types (e.g. `Exception`), check override rules (return types, nullability) and that every property name in getters exists (no typos like `msg` vs `message`).

### 3. Don’t assume “it looks right”

- **Lesson:** Idiomatic-looking code (e.g. “success/failure” for `Result`) can still be wrong if it doesn’t match the actual API.
- **Practice:** Prefer **looking up the type** (Kotlin stdlib, Android, FTDI) over guessing. One minute with the docs or IDE “Go to declaration” avoids invalid syntax and wrong usage.

### 4. Treat “first attempt” as draft until verified

- **Lesson:** We shipped a “first attempt” that had critical compile errors. The first version should be treated as unverified until the build and a minimal review pass are done.
- **Practice:** Before sharing for audit (or merging), **verify**: build succeeds, and a short checklist (build + Result/scope/override checks above) is done. Document that in RECALL or PROJECT_LOG so the bar is clear next time.

---

## How to audit next time (checklist)

Use this **before** calling the code “ready for audit” or “ready for review.”

### A. Build

- [ ] Environment has Java 17+ and Android SDK (and any required libs, e.g. FTDI AAR in `app/libs/`).
- [ ] `./gradlew assembleDebug` runs and finishes with **BUILD SUCCESSFUL**.
- [ ] No unresolved references or “cannot find symbol” (fix or document the only exception, e.g. C1 AAR by design).

### B. Code / API pass (even if build passed)

- [ ] **Result / optional types:** No `is Result.success` or `is Result.failure`. Use `isSuccess`, `getOrNull()`, `exceptionOrNull()` (or `fold`). Same idea for other stdlib types.
- [ ] **Constants and scope:** Every `SomeObject.CONSTANT` or `SomeObject.METHOD` — confirm the constant/method is defined **inside** that object/class, not only in the same file.
- [ ] **Exceptions and overrides:** Any custom `Exception` subclass: a single, correct `override val message: String` (or `String?` if you really need it). No typos in getters (e.g. `msg` vs `message`). Subclasses don’t widen return type in a way that breaks the contract.
- [ ] **Threading:** USB/FTDI and any heavy I/O are off the main thread (e.g. `Dispatchers.IO`, `withContext`). No blocking the main thread.

### C. Docs and context

- [ ] RECALL / PROJECT_LOG updated if you changed “what’s done” or “what’s next.”
- [ ] Any known limitation (e.g. “won’t compile without FTDI AAR”) is documented (e.g. `app/libs/README.md`, AUDIT_RESPONSE).

---

## One-line takeaway

**Run the build; if you can’t, do a deliberate pass over Kotlin/API usage (Result, scope, overrides). Don’t treat “first attempt” as ready until both build and that pass are done.**
