# Sharing the build for audit

You can let others review the project in two main ways: **GitHub** (recommended) or a **zip snapshot**.

---

## Option 1: GitHub (recommended for audit)

**Why use GitHub**

- Auditors get the **same repo** you use: full history, branches, and tags.
- They can **clone** once and **pull** for updates.
- They can **browse** code and docs on the web, **open issues**, or suggest changes via pull requests.
- No need to regenerate or re-send a zip when you fix or add something.

**How to share**

1. **Push your latest work** to the repo:
   ```bash
   git add -A
   git commit -m "Your message"
   git push origin main
   ```
2. **Send auditors the repo URL:**  
   **https://github.com/karmajinx-og/Decimator-Android**
3. **Optional:** Create a **tag** for the version they should audit (e.g. `v0.1.0-audit` or `audit-2026-03-08`):
   ```bash
   git tag -a v0.1.0-audit -m "Snapshot for audit"
   git push origin v0.1.0-audit
   ```
   Then they can clone and checkout that tag for a fixed snapshot.

**What auditors can do**

- **Clone:** `git clone https://github.com/karmajinx-og/Decimator-Android.git`
- **Browse:** https://github.com/karmajinx-og/Decimator-Android (code, README, docs, history)
- **Download ZIP from GitHub:** Code → Download ZIP (no git needed on their side)

---

## Option 2: Zip snapshot (for a single, self-contained copy)

Use a zip when you need to send **one file** (e.g. email, USB, or a place that doesn’t use Git). The zip is a **point-in-time** snapshot; for updates you’ll need to generate and send a new zip or point them to GitHub.

**How to generate an audit zip**

From the **project root**, create a zip that matches your build backup (source + config, no .git, no build outputs):

```bash
zip -r Decimator-Android-audit-$(date +%Y-%m-%d).zip . \
  -x "*.git*" \
  -x "build/*" \
  -x ".gradle/*" \
  -x ".idea/*" \
  -x "*.iml" \
  -x "local.properties" \
  -x "backups/*.zip" \
  -x "supplementary-and-rollback.zip" \
  -x ".DS_Store"
```

This creates **Decimator-Android-audit-YYYY-MM-DD.zip** in the current directory. You can move it to **backups/** or another folder, or attach it where you need.

**What’s in the zip**

- All source code, Gradle files, manifests, and resources.
- **docs/** (including PROJECT_LOG, RULES, this file).
- **Decimator_Design_Android_Control_App.md**, **README.md**, **MASTER_CONTEXT.md**.
- **supplementary/** (design doc and doc copies).
- No **.git** (no history), no **build/** or **.gradle/** (no build artifacts).

**Note:** The app depends on the **FTDI D2XX** library in **app/libs/**; that folder may be empty in the repo (see **app/libs/README.md**). For a **code audit**, the zip is enough; for a **build audit**, auditors need to add the FTDI AAR themselves or you can add it to the zip if your license allows sharing it.

---

## Recommendation

- **Primary:** Share **https://github.com/karmajinx-og/Decimator-Android** and, if useful, a **tag** (e.g. `v0.1.0-audit`) so auditors know which version to review.
- **Secondary:** Use a **zip snapshot** when you must send a single file or when someone can’t use Git. Generate it with the command above and, if needed, store a copy in **backups/**.
