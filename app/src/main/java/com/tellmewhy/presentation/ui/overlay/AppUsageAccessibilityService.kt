package com.tellmewhy.presentation.ui.overlay

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.tellmewhy.presentation.ui.overlay.JustificationOverlayService
private const val TRACKED_APPS_PREFS_NAME = "TrackedAppsPrefs"
class AppUsageAccessibilityService : AccessibilityService() {

    private val TAG = "AppUsageAccessibility"
    private val TARGET_APPS = listOf(
        "com.instagram.android",
        "com.google.android.youtube",
        "com.android.chrome",
        "com.instagram.lite",
        "app.revanced.android.youtube",
        "com.reddit.frontpage",
        "com.instagram.barcelona"
        // Add other package names you want to track
    )


    // Keep track of the last app that triggered the overlay to avoid re-triggering immediately
    private var lastBlockedApp: String? = null
    private val COOLDOWN_PERIOD_MS = 2000 * 60 // 2 seconds
    private lateinit var trackedAppsPrefs: SharedPreferences
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {


        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
//            || event?.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED // Sometimes useful for more reliable detection
        ) {
            trackedAppsPrefs = getSharedPreferences(TRACKED_APPS_PREFS_NAME, Context.MODE_PRIVATE);
            val initialTrackState = trackedAppsPrefs.getBoolean(event.packageName?.toString(), false)
            val packageName = event.packageName?.toString()
            val className = event.className?.toString()
            Log.d(TAG, "Event: ${event.eventType}, Pkg: $packageName, Class: $className")

            if (packageName != null && initialTrackState) {
                // Basic cooldown to prevent rapid re-triggering for the same app
                if (packageName == lastBlockedApp &&
                    (System.currentTimeMillis() - (lastOverlayTimeMillis[packageName]
                        ?: 0) < COOLDOWN_PERIOD_MS)
                ) {
                    Log.d(TAG, "Cooldown active for $packageName, skipping overlay.")
                    return
                }

                Log.i(TAG, "Target app opened: $packageName")
                // Here, you would trigger your overlay
                // For example, start the JustificationOverlayService
                val intent = Intent(this, JustificationOverlayService::class.java).apply {
                    putExtra(JustificationOverlayService.EXTRA_PACKAGE_NAME, packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Important when starting activity/service from service
                }
                // Inside AppUsageAccessibilityService.onAccessibilityEvent, before starting JustificationOverlayService
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                    Log.e(TAG, "Overlay permission is NOT granted. Cannot start JustificationOverlayService.")
                    // Optionally, send a user-visible notification to instruct them to open MainActivity to grant it.
                    // For example:
                    // showPermissionNeededNotification("Overlay permission required", "Tap to grant permission in app settings.")
                    return // Do NOT proceed to start JustificationOverlayService
                }

                // If permission is granted (or pre-M), proceed to start the service:
                Log.i(TAG, "Target app opened: $packageName. Overlay permission appears granted. Attempting to show overlay.")
//                val intent = Intent(this, JustificationOverlayService::class.java).apply { /* ... */ }

                startService(intent)
                lastBlockedApp = packageName
                lastOverlayTimeMillis[packageName] = System.currentTimeMillis()
            } else if (packageName != null && !initialTrackState  &&  packageName != "com.tellmewhy") {
//                Log.i(TAG,"package $packageName and $initialTrackState")
                // If a non-target app is opened, reset the lastBlockedApp for that specific app
                // This allows the overlay to show again if the user quickly switches back
                if (lastBlockedApp == packageName) {
                    lastBlockedApp = null
                }
                // This is a NON-TRACKED app OR potentially the keyboard
                val className = event.className?.toString()

                // List of known IME window class names (add more if needed for other keyboards)
                val isKeyboardWindow = className == "android.inputmethodservice.SoftInputWindow" ||
                        className?.startsWith("com.android.internal.view.InputMethod") == true // Another common pattern
                // Add other specific class names you observe for keyboards

                if (isKeyboardWindow) {
                    Log.d(TAG, "Keyboard window detected ($packageName, $className). Not stopping overlay.")
                } else {
                    // It's a non-tracked app, AND NOT the keyboard
                    Log.d(TAG, "Non-tracked app ($packageName, $className) came to foreground. Stopping JustificationOverlayService.")
                    val serviceIntent = Intent(this, JustificationOverlayService::class.java)
                    stopService(serviceIntent)
                }

            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility Service Interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes =
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            // Optionally, specify packageNames here if not done in XML or if you want dynamic configuration
            // packageNames = TARGET_APPS.toTypedArray()
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        this.serviceInfo = info
        Log.i(TAG, "Accessibility Service Connected")
        // Request Usage Stats permission when the service starts, if not already granted
        // checkAndRequestUsageStatsPermission() // Implement this in your MainActivity or a helper
    }

    companion object {
        // To keep track of the last time an overlay was shown for each app to avoid rapid re-triggering
        private val lastOverlayTimeMillis = mutableMapOf<String, Long>()
    }
}