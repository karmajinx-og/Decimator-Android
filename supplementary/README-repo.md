# Decimator-Android

Android app to control **Decimator Design** hardware (converters, cross converters, multi-viewers) via USB, using the reverse‑engineered protocol and FTDI bit-bang mode.

## Design & protocol

All product and protocol details are in:

- **[Decimator_Design_Android_Control_App.md](Decimator_Design_Android_Control_App.md)** — design doc, feasibility, recommended steps, risks, and links.

Summary:

- Decimator’s **USB Control Panel (UCP)** is desktop-only (Mac/Windows); there is no official mobile/SDK.
- Protocol was reverse‑engineered in the open-source Python library [decimctl](https://github.com/quentinmit/decimctl) (Apache 2.0).
- Devices use an **FTDI USB chip** (VID `0x215F`, PID `0x6000`) in **bit-bang mode** (not standard serial).
- This app will use **FTDI’s Android D2XX** library and port the decimctl protocol to Kotlin.

## Project structure

- **`app/`** — Android application (Kotlin, Jetpack Compose).
- **`app/src/main/java/com/decimator/android/`**
  - **`usb/`** — USB device detection and (future) FTDI D2XX integration.
  - **`protocol/`** — Placeholder for register map and read/write logic ported from decimctl.
  - **`ui/`** — Compose UI (settings by category, matching desktop UCP where applicable).

## Requirements

- Android 8.0+ (API 26+) with **USB host** support.
- **USB OTG** and a compatible cable to connect the Decimator device.
- FTDI Android D2XX library (to be added): [FTDI D2XX drivers](https://ftdichip.com/drivers/d2xx-drivers/).

## Git / GitHub

The project is a Git repo. To push to GitHub:

1. Create a new repository on GitHub (e.g. `Decimator-Android`) at [github.com/karmajinx-og](https://github.com/karmajinx-og).
2. Add the remote and push:
   ```bash
   git remote add origin https://github.com/karmajinx-og/Decimator-Android.git
   git branch -M main
   git push -u origin main
   ```

## Build

Open the project in **Android Studio** (Hedgehog or later recommended) to sync and generate the Gradle wrapper if needed. Then:

```bash
./gradlew assembleDebug
```

Install on a device with USB host:

```bash
./gradlew installDebug
```

## Next steps (from design doc)

1. Confirm exact FTDI chip model in Decimator hardware.
2. Integrate FTDI Android D2XX `.aar` into the project.
3. Port decimctl protocol (init sequence, `fpga_read_bytes` / `fpga_write_bytes`, register map).
4. Build control UI (Compose) grouped by category.
5. Handle USB attach/detach and permissions.
6. Test on each device family (CPA, VFA, KPA) as needed.

## License

App code: add your chosen license.  
Protocol knowledge and approach are based on [decimctl](https://github.com/quentinmit/decimctl) (Apache 2.0).  
Decimator Design hardware and UCP are their respective trademarks.
