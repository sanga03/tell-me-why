# Privacy Policy for TellMeWhy

**Last Updated: [16-08-2025]**

Welcome to TellMeWhy! This Privacy Policy explains how we ("TellMeWhy," "we," "us," or "our") handle information in connection with your use of the TellMeWhy mobile application (the "App").

We are committed to transparency and protecting your privacy. This App is designed with user privacy as a core principle.

## 1. Information We Do Not Collect

TellMeWhy is designed to function without collecting or storing any personally identifiable information (PII) from you.

*   **No User Accounts:** The App does not require you to create an account.
*   **No Server-Side Storage of Personal Data:** We do not have servers that store your personal data or app usage patterns linked to you. All data processing relevant to the App's core features happens locally on your device.
*   **No Sharing of Personal Data:** We do not sell, rent, trade, or otherwise share any of your personal data with third parties because we do not collect it in the first place.

## 2. Information Processed Locally on Your Device

To provide its features, TellMeWhy processes certain information strictly locally on your device:

*   **App Usage for Justification Prompts (via Accessibility Service):**
    *   **What it does:** To enable the core feature of displaying a justification prompt when you open specific applications you choose to monitor, TellMeWhy utilizes Android's Accessibility Service.
    *   **Data Processed:** When enabled, the Accessibility Service detects `TYPE_WINDOW_STATE_CHANGED` events. This means it can identify the package name (i.e., which app) is currently in the foreground on your device.
    *   **Purpose:** This information is used *solely* to:
        1.  Determine if the opened app is one you have configured in TellMeWhy to trigger a justification prompt.
        2.  Show the justification overlay over that specific app.
        3.  Detect the first app opened on a new calendar day to reset your daily "pass counter" within the App.
    *   **Local Storage:** Information about which apps you've chosen to monitor and your justifications are stored locally in the App's private storage on your device. The pass counter and related timestamps are also stored locally.
    *   **No Transmission:** This app opening information is **not transmitted off your device** by TellMeWhy. It is processed locally for the App's functionality.

*   **Justification Logs:**
    *   If you choose to use the justification feature, the text of your justifications, the associated app, and the timestamp are stored locally on your device for your review. This data is not sent to us or any third party.

*   **Pass Counter Data:**
    *   The App maintains a local counter for "passes" and timestamps related to their accrual and daily reset. This data is stored locally on your device.

## 3. Accessibility Service Usage – Prominent Disclosure

TellMeWhy uses Android's Accessibility Service to provide its core features: showing justification prompts over selected apps and resetting your daily pass counter.

*   **Why we need it:**
    1.  **To Show Prompts:** The service allows TellMeWhy to detect when you open an app you've selected for monitoring and display the justification prompt over it.
    2.  **For Daily Reset:** The service helps detect when you start using your phone on a new day (by observing an app opening event), allowing the App to reset your daily pass counter.
*   **What it can access:** The Accessibility Service, when enabled, can observe app window changes (which app is on screen).
*   **What we do with this access:** We only use this to identify the foreground app for the purposes stated above. **TellMeWhy does not use the Accessibility Service to collect any personal or sensitive data beyond the app package name for its intended features, nor does it track your browsing history or text input in other apps.**
*   **Your Control:** You have full control over enabling or disabling the Accessibility Service for TellMeWhy at any time through your device's settings. The App will clearly guide you on how to do this and will request your explicit consent before taking you to the system settings to enable it.

## 4. Open Source Commitment

We believe in transparency. The source code for TellMeWhy is publicly available on GitHub at:
**[https://github.com/sanga03/tell-me-why/]**

You are welcome to review the code to understand exactly how the App functions and how your data (or lack thereof) is handled. This commitment to open source is part of our dedication to user trust and privacy.

## 5. Data Security

While we do not collect personal data on servers, we take reasonable measures to protect the data stored locally on your device by utilizing standard Android application security practices. The App's local data is protected by Android's app sandboxing mechanisms.

## 6. Children's Privacy

TellMeWhy is not intended for use by children under the age of 13 (or the relevant age of consent in your jurisdiction). We do not knowingly collect any information from children. If you believe a child has used the app and you are concerned, please contact us.

## 7. Changes to This Privacy Policy

We may update this Privacy Policy from time to time. We will notify you of any significant changes by posting the new Privacy Policy within the App or through other appropriate means. We encourage you to review this Privacy Policy periodically for any changes. Your continued use of the App after any modifications to this Privacy Policy will constitute your acknowledgment of the modifications and your consent to abide and be bound by the modified Privacy Policy.

## 8. Contact Us

If you have any questions or concerns about this Privacy Policy or our privacy practices, please contact us at:
**[sangamesh_biradar@outlook.com]**

