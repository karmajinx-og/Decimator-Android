# Supplementary and rollback files

This folder holds **key documents and copies** used for rollback and reference. Update this folder when you add or change important docs; you do **not** need to create a new zip for every backup—only when you want a single archive to store or share.

## Contents (update as needed)

- **Decimator_Design_Android_Control_App.md** — Copy of the design doc (source of truth for product/protocol).
- **README-repo.md** — Copy of the main repository README.
- **RULES.md** — Project rules (same as docs/RULES.md).
- **MASTER_CONTEXT.md** — Master context (same as root MASTER_CONTEXT.md).
- **PROJECT_LOG.md** — Project log (same as docs/PROJECT_LOG.md).
- **RECALL.md** — Recall (same as docs/RECALL.md): what’s done, what’s next, how to resume.

## How to use

1. **Update the folder:** When you change any of these files in the repo, copy the updated version here (or add new rollback-worthy files).
2. **Create/update the zip:** When you want one archive, generate **supplementary-and-rollback.zip** from this folder (e.g. from project root: `cd supplementary && zip -r ../supplementary-and-rollback.zip .`).
3. **Rollback:** Restore files from this folder (or from the zip) into the repo as needed.

Do **not** create a new zip for every small backup—update this folder and regenerate the zip when it’s useful.
