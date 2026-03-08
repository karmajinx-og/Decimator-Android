# Decimator-Android — project rules

Use these rules when editing or extending the project.

---

## 1. Code and structure

- **Language:** Kotlin. Use Kotlin idioms; avoid unnecessary Java-style code.
- **UI:** Jetpack Compose only (no XML layouts for new screens).
- **USB / FTDI:** All device open, read, and write must run off the main thread (e.g. `Dispatchers.IO` or `withContext(Dispatchers.IO)`). Never block the main thread with USB I/O.
- **Protocol:** Keep protocol logic aligned with [decimctl](https://github.com/quentinmit/decimctl) (init sequence, preambles, `bytesToRawCommand` / `rawResponseToBytes`). Document any intentional deviation.
- **Errors:** Use `DecimatorError` (or existing sealed types) for device/permission/IO failures; surface user-facing messages from these, don’t swallow errors.

---

## 2. Naming and layout

- **Packages:** `com.decimator.android` with subpackages `ftdi`, `protocol`, `usb`, `connection`, `ui`, `ui.theme`.
- **New features:** Prefer adding to existing modules; new modules only when clearly separate (e.g. a dedicated “settings” or “device list” feature).
- **Resources:** `strings.xml` for user-facing text; use `@string/` in XML/Compose where applicable.

---

## 3. Dependencies and build

- **FTDI:** The app depends on the FTDI D2XX AAR/JAR in `app/libs/`. Document any version or API assumptions in `app/libs/README.md`.
- **Versions:** Use the version catalog in `gradle/libs.versions.toml` for library and plugin versions; avoid hardcoding versions in `build.gradle.kts` where possible.
- **Min/target SDK:** Change only with a note in `docs/PROJECT_LOG.md` (and in MASTER_CONTEXT if it affects “Requirements”).

---

## 4. Documentation and context

- **Design and protocol:** The single source of truth for product/protocol is **Decimator_Design_Android_Control_App.md**. Reference it from code comments or README when relevant.
- **What happened:** Log significant changes (features, refactors, breaking changes) in **docs/PROJECT_LOG.md**.
- **Master context:** **MASTER_CONTEXT.md** is the main entry point for project context, rules, and process; keep it updated when structure or process changes.

---

## 5. Backups and rollback

- **Build backups:** Timestamped zips go in **backups/** (see `backups/README.md`). Generate a new backup before major changes or releases.
- **Supplementary / rollback:** Key docs and copies for rollback live in **supplementary/**. Update that folder when you add or change rollback-worthy files; regenerate **supplementary-and-rollback.zip** when you want a single archive to store or share. Do not create a new zip for every small backup—update the folder and zip when it’s useful.

---

## 6. Git and GitHub

- **Default branch:** `main`.
- **Remote:** `origin` → `https://github.com/karmajinx-og/Decimator-Android.git`.
- **Commits:** Prefer clear, single-purpose messages; reference design doc or issue when relevant.
- **Force push:** Only when intentionally syncing the repo with a local rewrite (e.g. replacing doc-only content with full build); document in PROJECT_LOG.

---

## 7. Markdown (docs and MD files)

- Use **Markdown** for all docs (`.md`).
- **Headings:** One `#` for document title; use `##`, `###` for sections.
- **Links:** Prefer relative links for in-repo files (e.g. `[Design doc](Decimator_Design_Android_Control_App.md)`).
- **Lists and code:** Use fenced code blocks with language where it helps (e.g. `bash`, `kotlin`).
