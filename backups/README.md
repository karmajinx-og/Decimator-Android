# Build backups

This folder holds **timestamped zip backups** of the full project (source and config, excluding `.git`, build outputs, and large generated files). Create a **new** backup before major changes or releases.

## How to create a build backup

From the **project root** (parent of `backups/`), run:

```bash
zip -r backups/Decimator-Android-build-$(date +%Y-%m-%d-%H%M).zip . \
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

Or on macOS with a simple date:

```bash
zip -r backups/Decimator-Android-build-2026-03-08.zip . \
  -x "*.git*" -x "build/*" -x ".gradle/*" -x ".idea/*" -x "*.iml" \
  -x "local.properties" -x "backups/*.zip" -x "supplementary-and-rollback.zip" -x ".DS_Store"
```

## What is excluded

- `.git` (full history stays in the repo)
- `build/`, `.gradle/` (Gradle build outputs)
- `.idea/`, `*.iml` (IDE files)
- `local.properties` (local paths)
- Other backup zips (to avoid huge archives)
- `.DS_Store` (macOS metadata)

## Testing the build locally

From the project root (with **Java 17** and **Android SDK** available):

```bash
./gradlew assembleDebug
```

- **If the build fails** with errors about `com.ftdi.j2xx` or missing FTDI classes: add the FTDI D2XX **AAR** (or JAR) to **app/libs/** (see **app/libs/README.md**), then run the command again.
- **If Java is not found:** install a JDK 17 (or use the one bundled with Android Studio) and ensure `JAVA_HOME` or `java` is on your PATH.

## Rollback

Unzip a backup into a **new** directory (or overwrite the project after committing or backing up the current state). Then run Gradle sync and build as usual.

## Supplementary / rollback files

For **documents and rollback copies** (design doc, README, rules, master context, project log), use the **supplementary/** folder and optionally **supplementary-and-rollback.zip**. Update that folder when you change key docs; regenerate the zip when you need a single archive. See **MASTER_CONTEXT.md** and **supplementary/README.md**.
