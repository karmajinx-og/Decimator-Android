# FTDI D2XX licence — audit summary

**Purpose:** Record the licence uncertainty and risk before distributing the app (e.g. Google Play). Tracks permission request to FTDI.  
**Last updated:** 2026-03-08

**Official reference:** [FTDI Driver Licence Terms – Details](https://ftdichip.com/driver-licence-terms-details/)

---

## Conclusion

**The FTDI licence does NOT clearly permit distributing the D2XX library as part of your application to end users.** The binding Licence Terms (clause 3.1.7) appear to **prohibit** making the Software available to any person. Before distributing the app (e.g. via Google Play), **contact FTDI for clarification or consult a lawyer.**

---

## What the audit found

### Informal note (D2XX drivers page)

On the drivers page, FTDI states:

> "FTDI drivers may be distributed in any form as long as license information is not modified."

This is a brief summary only — **not** the binding legal text.

### Binding Licence Terms (ftdichip.com)

The full terms are published at:

- **Driver Licence Terms – Details:** [https://ftdichip.com/driver-licence-terms-details/](https://ftdichip.com/driver-licence-terms-details/)

Relevant clauses:

- **Clause 1.3** grants narrow permissions:
  - **Manufacturer** of a device with a Genuine FTDI Component → may install the Software onto that device.
  - **Seller/distributor** of a Device → may distribute the Software *with the Device*.
  - **User** of a Device → may install the Software on the Device or on a computer to use the Device.

- **Clause 3.1.7** explicitly prohibits:
  - **Not** to provide, or otherwise make available, the Software in any form, in whole or in part, **to any person**.

That prohibition would directly affect **bundling the Android Java D2XX library** (e.g. `.jar`/`.aar`) inside your app and distributing it to end users via an app store or otherwise.

### Conflict

| Source | What it says |
|--------|----------------|
| D2XX drivers page (informal) | "FTDI drivers may be distributed in any form as long as license information is not modified" |
| Full Licence Terms, §3.1.7 | Licensee must **not** "provide, or otherwise make available, the Software in any form … to any person" |

FTDI’s licence page states that **in the event of conflict, the Licence Terms prevail.** So the binding terms (3.1.7) take precedence over the informal note.

---

## Recommendation

1. **Do not assume** you may distribute the D2XX library to end users as part of your app without explicit permission.
2. **Contact FTDI** (e.g. via [ftdichip.com](https://ftdichip.com) contact / support) to ask whether distributing the Android Java D2XX library *as part of your compiled application* to end users (e.g. via Google Play) is permitted under their current licence.
3. **If you plan to publish:** Get written clarification or legal advice before distributing the app.

---

## Impact on this project

- **Development and local testing:** Using the D2XX library locally to build and test the app is a separate question; the audit focused on **distribution to end users**.
- **Public repo:** We do **not** commit `d2xx.jar` or the full FTDI package to the repo (see `.gitignore`), to avoid redistributing the library in source form.
- **Publishing (e.g. Play Store):** Resolve the licence question with FTDI or legal counsel before publishing any build that includes the D2XX library.

---

## Permission request sent

- **Date sent:** 2026-03-08  
- **Status:** Pending — awaiting FTDI response.  
- **Request:** Permission to bundle the Android Java D2XX library in our app and distribute the app to end users (e.g. Google Play), with use only for devices containing a genuine FTDI component; we will acknowledge permission in the app (e.g. About/Licences) and in documentation.  
- **When response received:**  
  - If **granted** — document the grant (date, contact, wording) here or in a short `FTDI_PERMISSION_GRANTED.md`; add acknowledgement in the app (About/Licences screen) and in README or docs as agreed.  
  - If **denied or separate licence required** — document the outcome; do not distribute the app with D2XX until resolved (e.g. separate licence or legal advice). See **Alternatives if permission is rejected** below.

---

## Alternatives if permission is rejected

If FTDI denies the request or says a separate licence is required, options include:

| Option | Description |
|--------|-------------|
| **1. Ask for a commercial / distribution licence** | Reply asking whether FTDI offers a paid or custom licence that allows app-store distribution. Some vendors allow this under a separate agreement. |
| **2. Decimator Design** | Contact **Decimator Design** (the hardware manufacturer). They may have an existing agreement with FTDI that allows software to be distributed *with* their devices (clause 1.3), or they might provide or license an alternative way to control their hardware. |
| **3. Don’t publish to stores** | Use the app only for **personal use or sideload**: build and install the APK yourself; do not distribute via Google Play or other stores. No redistribution of the driver = no breach of 3.1.7. You can still share **source code** (without `d2xx.jar` in the repo) and document that users must obtain the D2XX library from FTDI and add it locally to build. |
| **4. Legal advice** | A lawyer may advise whether your use fits within clause 1.3 (e.g. “distribution with the Device”) or whether the informal drivers-page note supports a defence. Do not rely on this without written advice. |
| **5. Alternative tech (only if available)** | If Decimator hardware ever supports another interface (e.g. network, Bluetooth, or USB VCP/CDC) that doesn’t require D2XX, the app could be adapted. Current protocol is D2XX/bit-bang only; no such alternative is known today. |

**Summary:** If rejected, first try (1) commercial licence and (2) Decimator; if neither works, (3) personal/sideload only and keep the repo without the JAR, or (4) get legal advice before any store distribution.

---

## Decimator Design — do you need permission?

**Short answer:** There is **no clear requirement** to get formal permission from Decimator to build or distribute this app, but **trademark and branding** are the main things to be careful about.

- **No official SDK** — Decimator Design does not provide an official SDK or protocol document (confirmed in design doc; they’ve engaged on community projects). The app uses a **reverse‑engineered** protocol from the open-source [decimctl](https://github.com/quentinmit/decimctl) project (Apache 2.0). You are not using Decimator’s code or a Decimator licence.
- **Interoperability** — In many jurisdictions, making a third‑party app that works with existing hardware (interoperability) does not by itself require the manufacturer’s permission, as long as you don’t infringe patents, copy protected code, or misuse their trademarks.
- **Trademark / branding** — Using the name **“Decimator”** in the app name (e.g. “Decimator-Android”) or store listing can be acceptable as **nominative fair use** (describing compatibility with Decimator hardware). To reduce risk:
  - Avoid implying **endorsement** or that the app is official (e.g. “Compatible with Decimator Design hardware” rather than “Decimator Design app”).
  - If Decimator ever asks you to change the name or branding, consider complying to avoid dispute.
- **Optional: give them a heads-up** — You are not obliged to ask permission, but you *could* send a short, polite email to Decimator (e.g. tech@decimator.com) saying you’ve built an open-source Android app that works with their hardware via the community reverse‑engineered protocol, and that you’re not claiming to be official. That can improve relations and sometimes leads to useful feedback. It’s optional, not a legal requirement.

**Summary:** No formal permission from Decimator is required to build or distribute the app. Be careful with branding (no implied endorsement); optionally inform them.
