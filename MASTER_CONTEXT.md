# Decimator-Android — master context

This is the **master context** for the project: one place for rules, structure, and how to work with the repo.

---

## Resuming work / recall

After a break, read **[docs/RECALL.md](docs/RECALL.md)** first. It gives:

- **When you come back — what is next?** — FTDI lib is already in **app/libs/d2xx.jar**. Next: run build, install on device, then test (see RECALL for testing steps).
- **What has been done** — in detail (scaffold, USB, FTDI layer, protocol, state/UI, GitHub, docs, backups, **and all audit fixes** C1–C4, M1–M4, L1–L3).
- **What is next to be done** — immediate (add AAR, build, test), short term (device type, register read), medium term (register map, control UI), later (device families).
- **Every fix applied** — [docs/FIXES_APPLIED.md](docs/FIXES_APPLIED.md) lists every fix (critical, medium, low) in one place.
- **Key file reference** — paths for design doc, rules, driver, protocol, UI, etc.

Keep RECALL.md updated when you finish major work or change priorities.

---

## What this project is

- **Decimator-Android** is an Android app that controls Decimator Design hardware (converters, cross converters, multi-viewers) via **USB**, using the reverse‑engineered protocol and **FTDI bit-bang** mode.
- There is **no official SDK**; the protocol comes from the open-source Python library [decimctl](https://github.com/quentinmit/decimctl) (Apache 2.0).
- **Design and feasibility:** See **[Decimator_Design_Android_Control_App.md](Decimator_Design_Android_Control_App.md)**.

---

## Repository

- **GitHub:** https://github.com/karmajinx-og/Decimator-Android  
- **Default branch:** `main`  
- **Remote:** `origin` → `https://github.com/karmajinx-og/Decimator-Android.git`

---

## Project rules

- **Full rules:** [docs/RULES.md](docs/RULES.md) — code style, USB/threading, protocol alignment, naming, dependencies, docs, backups, Git.
- **Summary:** Kotlin + Compose; all USB/FTDI I/O off main thread; protocol faithful to decimctl; errors via `DecimatorError`; docs and backups as below.

---

## What occurred (log)

- **Changelog / narrative:** [docs/PROJECT_LOG.md](docs/PROJECT_LOG.md) — what was done (project setup, FTDI layer, protocol port, USB permissions, GitHub update, rules/backup setup).

## Sharing for audit

- **Guide:** [docs/SHARING_FOR_AUDIT.md](docs/SHARING_FOR_AUDIT.md) — how to share the build for others to audit (GitHub vs zip snapshot).

---

## Directory layout (high level)

| Path | Purpose |
|------|--------|
| **app/** | Android app module (Kotlin, Compose, FTDI, USB). |
| **app/libs/** | FTDI D2XX AAR/JAR goes here; see `app/libs/README.md`. |
| **docs/** | PROJECT_LOG.md, RULES.md, RECALL.md, SHARING_FOR_AUDIT.md, AUDIT_RESPONSE.md, LESSONS_FROM_AUDIT.md, AUDIT_PROMPT.md, FIXES_APPLIED.md. |
| **gradle/** | Wrapper config, libs.versions.toml. |
| **backups/** | Timestamped zip backups of the build (see backups/README.md). |
| **supplementary/** | Supplementary and rollback files; update this folder and optionally zip as `supplementary-and-rollback.zip`. |
| **Decimator_Design_Android_Control_App.md** | Design doc (source of truth for product/protocol). |
| **README.md** | User-facing repo readme (build, run, next steps). |
| **MASTER_CONTEXT.md** | This file. |

---

## Build and run

1. Put the **FTDI D2XX** library in `app/libs/` (see `app/libs/README.md`).
2. Open the project in **Android Studio**, sync Gradle.
3. Build: `./gradlew assembleDebug`  
   Install: `./gradlew installDebug`
4. Device must have **USB host** and **OTG**; connect Decimator hardware and grant USB permission in the app.

---

## Backups and rollback

- **Build backup:** Create a **new** timestamped zip of the project (excluding `.git`, `build/`, `.gradle/`, etc.) and put it in **backups/**. Do this before major changes or releases. See **backups/README.md**.
- **Supplementary / rollback:**  
  - Keep key files (e.g. design doc, README copy, rules) in **supplementary/**.  
  - **Update** this folder when you add or change rollback-worthy files.  
  - When you want a single archive, (re)generate **supplementary-and-rollback.zip** from the contents of **supplementary/**.  
  - Do **not** create a new zip for every small backup—update the folder and zip when needed.

---

## Updating this document

- When you add or remove top-level folders, change the backup process, or change where rules live, update **MASTER_CONTEXT.md** and, if relevant, **docs/PROJECT_LOG.md**.
- When you return from a break or complete significant work, update **docs/RECALL.md** (“What has been done” / “What is next to be done” and “Last updated”).
- Before calling code “ready for audit” or “ready for review,” use **docs/LESSONS_FROM_AUDIT.md** (short checklist). For a **full, methodical audit**, use **docs/AUDIT_PROMPT.md** (phases, prompts, and output format).
