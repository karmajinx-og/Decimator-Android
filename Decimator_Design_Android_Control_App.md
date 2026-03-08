# Project: Decimator Design Android Control App
**Date:** 8 March 2026 | **Status:** Research Phase

---

## 1. What is the Decimator USB Control Panel (UCP)?

Decimator Design's **USB Control Panel (UCP)** is a native desktop application for Mac and Windows that controls their range of hardware devices (converters, cross converters, multi-viewers) via USB. Current version is **3.0.4**.

- **Mac:** Distributed as `.dmg` — Intel and Apple Silicon ARM builds, supports macOS Catalina through Sequoia 15.4.1
- **Windows:** Distributed as `.msi` — 64-bit, Windows 7 or later
- **Supported devices:** 12G-CROSS, MD-LX, MD-HX, DMON-QUAD, DMON-6S, DMON-12S, DMON-16S, DMON-16SL, DMON-4S, MD-CROSS, MD-CROSS V2, MD-QUAD, MC-DMON-QUAD, MC-DMON-9S
- **No mobile version exists** — iOS or Android

---

## 2. Does Decimator Provide an Official SDK or USB Protocol Document?

**No.** Confirmed directly from Decimator Design staff (Edward Wright, tech@decimator.com) in a January 2019 GitHub thread:

> *"Currently there is no API for our products."*
> — Edward Wright, Decimator Design

The issue is tagged **"Blocked externally"** on GitHub (Bitfocus Companion module request #83) and remains open.

**Contact:** tech@decimator.com | Phone: +61 (0)2 4577-8725

---

## 3. Community Reverse-Engineering Work (Key Finding)

Despite no official SDK, a developer named **Quentin Smith (@quentinmit, MIT)** successfully reverse-engineered the USB protocol and published a working open-source Python library:

- **Repository:** https://github.com/quentinmit/decimctl
- **License:** Apache 2.0 (open source — commercially friendly)
- **Language:** Python 100%
- **Last updated:** December 2021

### What was discovered about the USB protocol

- The devices use an **FTDI USB chip** (USB VID: `0x215F`, PID: `0x6000`)
- The FTDI chip is used in **bit-bang mode** (raw GPIO control), not as a standard serial/UART bridge
- Communication is via **SPI-like raw bit clocking** into an FPGA on the hardware
- The library uses `pylibftdi` (Python wrapper for `libftdi`) to drive the FTDI chip
- The FPGA has a **register map** (512 bytes / 0x200 registers) that is read/written to control all settings
- Device type is identified by the **first 3 characters of the serial number** (e.g. CLA = MD-HX v1, MQD = DMON-QUAD)
- Connection parameters: Baud 3,000,000, latency timer 1ms, RTS/CTS flow control, bit-bang mode 0x48

### Key protocol files in the repo

- `decimctl/__init__.py` — Device connection, FTDI init, register read/write
- `decimctl/protocol.py` — Full register map definitions for all device types (CPA, VFA, KPA register structs)
- `bin/decimctl` — Command-line tool

### Additional community tools

- **DeciConn** (https://github.com/Toshiba01/DeciConn/) — Windows precompiled version
- A **Facebook post** (Companion User Group) mentions someone built an app that exposes Decimator USB to an HTTP API — potential bridge approach

---

## 4. Android Re-Engineering Feasibility Assessment

### The Core Challenge

The Decimator hardware uses an **FTDI FT232H/similar chip** in raw bit-bang mode. This is **not standard USB serial** — it requires low-level FTDI driver access.

### Android USB Host Support

| Factor | Detail |
|---|---|
| USB Host API | Available on Android 3.1+ via `android.hardware.usb` |
| FTDI support on Android | **Yes — FTDI officially provides an Android D2XX driver (Java library)** |
| libftdi on Android | Possible via NDK (C/C++ native layer) |
| Protocol knowledge | Available via quentinmit's reverse-engineered Python library |

### FTDI's Official Android Support

FTDI provides a **free Android D2XX library** (`.aar` / `.jar`) that supports their chips natively on Android without requiring root.

URL: https://ftdichip.com/drivers/d2xx-drivers/

---

## 5. Recommended Development Path for Android App

**Step 1 — Confirm FTDI chip model**
Identify the exact FTDI chip used in Decimator devices (likely FT232H or FT2232). USB VID `0x215F` / PID `0x6000` is confirmed.

**Step 2 — Integrate FTDI Android D2XX library**
Download FTDI's official Android D2XX `.aar` library and add it to an Android Studio project. This replaces the need for `pylibftdi`.

**Step 3 — Port the protocol**
Translate `quentinmit/decimctl`'s Python protocol logic into Kotlin/Java:
- Device open/init sequence (baud rate, latency, flow control, bit-bang mode)
- `fpga_read_bytes()` and `fpga_write_bytes()` functions
- Register map structs from `protocol.py`

**Step 4 — Build the Android UI**
Replicate the UCP interface in Android using Kotlin + Jetpack Compose or XML layouts. Settings grouped by category, matching the desktop UCP.

**Step 5 — USB OTG handling**
Add USB device detection (intent filter for VID `0x215F` / PID `0x6000`), permission request dialog, and graceful disconnect handling.

**Step 6 — Test on each device family**
CPA (MD-HX, MD-LX, MD-CROSS), VFA (DMON series), KPA — each uses different register maps.

---

## 6. Risks & Limitations

- The `decimctl` library is **experimental** — only tested on MD-HX; other devices may behave differently
- FTDI bit-bang mode on Android requires careful timing — performance may vary by Android device
- Firmware updates via USB are **not recommended** without official documentation (bricking risk)
- No official Decimator support — any issues require community troubleshooting
- The GitHub issue is tagged "Stale" (May 2025) — community effort has slowed

---

## 7. Key Resources

| Resource | URL |
|---|---|
| Decimator Downloads | https://decimator.com/DOWNLOADS/DOWNLOADS.html |
| decimctl (Python library) | https://github.com/quentinmit/decimctl |
| DeciConn (Windows tool) | https://github.com/Toshiba01/DeciConn/ |
| Bitfocus Companion issue #83 | https://github.com/bitfocus/companion-module-requests/issues/83 |
| FTDI Android D2XX driver | https://ftdichip.com/drivers/d2xx-drivers/ |
| Decimator tech contact | tech@decimator.com |
