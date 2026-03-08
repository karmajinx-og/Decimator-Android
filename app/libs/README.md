# FTDI D2XX library (required for USB communication)

Decimator hardware uses an FTDI chip in bit-bang mode. You must add FTDI's Android D2XX library here so the app can talk to the device.

1. Go to [FTDI D2XX drivers](https://ftdichip.com/drivers/d2xx-drivers/) (or [Android Java D2XX](https://www.ftdichip.com/old2020/Support/SoftwareExamples/Android_JAVA_D2XX_Projects.htm)).
2. Download the **Android Java D2XX** package (e.g. **Android_Java_D2XX_2.08** or later).
3. From the package, copy the **.aar** (or **.jar**) into this folder:
   - `app/libs/`
4. Ensure the file is named so it is included by the build (e.g. `D2xx.aar` or `ftd2xx.aar`). The project uses `fileTree("libs") { include("*.aar", "*.jar") }`.

The app uses:
- **VID 0x215F / PID 0x6000** (Decimator devices)
- **Bit-bang mode** (sync bit-bang, mask 0x48)
- **Baud 3,000,000**, **latency 1 ms**, **RTS/CTS flow control**

Without the library in `app/libs/`, the project will not compile.

**Distribution / licence:** FTDI’s binding Licence Terms may prohibit distributing the D2XX library as part of your app to end users. Before publishing (e.g. Google Play), see **docs/FTDI_LICENCE_AUDIT.md** and obtain FTDI clarification or legal advice.
