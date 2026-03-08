# Folder structure — single canonical copy

**Last updated:** 2026-03-09

---

## Main project folder

The project has **one canonical location** on this machine:

- **Path:** `developer/Decimator-Android` (relative to your home directory)
- **Full path (example):** `~/developer/Decimator-Android` or `$HOME/developer/Decimator-Android`  
  On this machine the parent folder is **DEVELOPER** (all caps): `~/DEVELOPER/Decimator-Android`.

All development, builds, and docs refer to this folder. Clone or open the repo here (or your preferred location); this is the only copy you need.

---

## What changed (2026-03-09)

Previously there were **two** local folders:

| Location | Role | Git state |
|----------|------|-----------|
| `~/Decimator-Android` | Audit snapshot (frozen at tag `v0.1.0-audit`) | Detached HEAD |
| `~/DEVELOPER/Decimator-Android` | Active development | `main` + local changes |

Both pointed at the same GitHub repo. The snapshot folder was created when preparing for audit (clone + `git checkout v0.1.0-audit`).

**Consolidation:**

- The redundant folder **`~/Decimator-Android`** was **removed**.
- **`developer/Decimator-Android`** (this repo) is the **single canonical copy**.

---

## Audit snapshots (no second folder)

For a fixed snapshot for auditors:

- **Use a tag:** Push a tag (e.g. `v0.1.0-audit`) and tell auditors to `git clone` then `git checkout <tag>`. See [SHARING_FOR_AUDIT.md](SHARING_FOR_AUDIT.md).
- **Or use a zip:** Generate a zip from this folder (see SHARING_FOR_AUDIT.md) and send that. Do **not** keep a second full clone in another path just for audit.

---

## Paths in this repo

Paths in MASTER_CONTEXT, RECALL, PROJECT_LOG, and other docs are **relative to the repo root** (the folder containing `app/`, `docs/`, `README.md`, etc.). No absolute paths or machine-specific locations are required for building or contributing.
