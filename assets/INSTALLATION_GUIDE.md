# Installation Guide for TellMeWhy (Developer/Testing Version)


**Why are these steps sometimes necessary for developer/testing versions?**

*   **Installing from "Unknown Sources":** Android devices, by default, are configured to only install apps from official app stores like Google Play. Installing an APK file directly is considered installing from an "unknown source." You'll need to grant permission for this.
*   **Google Play Protect:** Play Protect is a built-in malware scanner for Android. When you install an app from outside the Play Store, Play Protect might scan it. For new or unreleased apps that Play Protect hasn't seen before, it might show a warning or even block the installation as a precaution. Temporarily pausing it can help during testing, **but it should be re-enabled immediately.**
*   **Restricted Settings (for sensitive permissions like Accessibility Service):** Android has a security feature called "Restricted settings." If an app requests highly sensitive permissions (like Accessibility Services, which TellMeWhy uses for its core functionality) and is installed directly via an APK (sideloaded) rather than from an app store, Android may initially restrict access to these settings for that app. You'll need to explicitly allow the restricted setting for the app to grant these permissions. This is a security measure to protect users from potentially malicious sideloaded apps.

**IMPORTANT: These instructions are primarily for installing developer builds or testing versions. Always exercise caution when disabling security features or installing apps from outside official stores. Ensure you trust the source of the APK.**

## Steps to Install

### Step 1: Download the APK File
   - Ensure you have the latest apk file downloaded to your device from release section.

### Step 2: Enable "Install from Unknown Apps" (if not already enabled)
   This setting allows you to install APKs directly. The location of this setting can vary slightly depending on your Android version and device manufacturer.

   1.  Go to **Settings** on your Android device.
   2.  Navigate to **Apps**, then **Special app access** (or search for "unknown" or "install unknown apps").
   3.  Find the app you'll use to install the APK (e.g., your browser like Chrome, or your Files app).
   4.  Tap on that app and toggle **"Allow from this source"** or **"Install unknown apps"** to ON.

### Step 3: Temporarily Pause Google Play Protect (Recommended only if installation is blocked)
   **You should only do this if Play Protect actively blocks the installation of the trusted APK and you intend to re-enable it immediately after.**

   1.  Open the **Google Play Store** app.
   2.  Tap on your **Profile icon** in the top-right corner.
   3.  Tap on **Play Protect**.
   4.  Tap the **Settings gear icon** in the top-right corner of the Play Protect screen.
   5.  Turn **OFF** "Scan apps with Play Protect." You might see a warning; acknowledge it if you trust the APK source.
   
   

   **REMEMBER TO RE-ENABLE THIS AFTER INSTALLATION!**

### Step 4: Install the APK File
   1.  Open your **Files** app (or the app you used to download the APK).
   2.  Navigate to the folder where you saved `TellMeWhy.apk`.
   3.  Tap on the `TellMeWhy.apk` file.
   4.  You may see a prompt asking for confirmation. Tap **"Install."**
   5.  Wait for the installation to complete.

### Step 5: Re-Enable Google Play Protect (CRITICAL!)
   1.  Open the **Google Play Store** app.
   2.  Tap on your **Profile icon**.
   3.  Tap on **Play Protect**.
   4.  Tap the **Settings gear icon**.
   5.  Turn **ON** "Scan apps with Play Protect."

### Step 6: Grant Necessary Permissions and Allow Restricted Settings

   TellMeWhy requires certain permissions to function correctly, most notably the Accessibility Service permission.

   1.  **Open TellMeWhy** for the first time.
   2.  The app should guide you to enable the required permissions. This will likely involve:
      *   **Accessibility Service:** The app will explain why it needs this and provide a button to take you to the system settings.
         *   In the Accessibility settings, find **TellMeWhy** in the list of downloaded apps or services.
         *   Tap on it and toggle it **ON**. You will see a system warning about what this permission allows; please read it and confirm if you understand and trust the app.

   3.  **Allowing Restricted Setting (If Encountered):**
      *   When you try to enable a sensitive permission like the Accessibility Service for a sideloaded app, you might see a dialog saying "Restricted setting" or "For your security, this setting is currently unavailable."
      *   **Why this happens:** This is Android protecting you from sideloaded apps immediately gaining powerful permissions.
      *   **How to allow it:**
         1. Tap **"OK"** on the restricted setting dialog.
         2. Go to your phone's main **Settings** app.
         3. Go to **Apps**.
         4. Find and tap on **TellMeWhy** in the list of apps.
         5. On the "App info" page for TellMeWhy, tap the **three-dots menu icon** (⋮) in the top-right corner.
         6. Tap **"Allow restricted settings"** and authenticate if prompted (e.g., with your PIN or fingerprint).
         7. Now, go back into the TellMeWhy app and try enabling the Accessibility Service again. You should now be able to grant the permission.

## Why are these permissions necessary for TellMeWhy?

*   **Accessibility Service:**
    *   **To Show Prompts:** This service allows TellMeWhy to detect when you open an app you've selected for monitoring and display the justification prompt *over* that app. Without it, the overlay feature is not possible.
    *   **For Daily Reset:** The service helps detect when you start using your phone on a new day (by observing an app opening event), allowing the App to accurately reset your daily pass counter.
    *   *TellMeWhy is committed to privacy. This service is used ONLY for the above-stated functionalities. The app does not collect personal data through this service for other purposes. Please see our [Privacy Policy](LINK_TO_YOUR_PRIVACY_POLICY.md) for more details.*

---

Once these steps are completed, TellMeWhy should be installed and configured to run! If you encounter any issues, please refer to the project's issue tracker or contact the developer.

