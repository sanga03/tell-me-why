// File: AppState.kt
package com.tellmewhy.data // Or your preferred package

import android.util.Log
import kotlin.jvm.Volatile

object AppState {
    private const val TAG = "AppState"

    @Volatile
    var lastBlockedAppPackageName: String? = null
        private set // Optional: Make setter private if only AppState methods should change it

    @Volatile
    var currentBlockerPackageName: String? = null
        private set
    var currentBlockerTimestamp: Long = 0L
        private  set
    @Volatile
    var lastBlockedAppTimestamp: Long = 0L
        private set // Optional: Make setter private

    fun updateLastBlockedApp(packageName: String?, timestamp: Long = System.currentTimeMillis()) {
        lastBlockedAppPackageName = packageName
        lastBlockedAppTimestamp = if (packageName != null) timestamp else 0L
    }

    fun updateCurtBlockedApp(packageName: String?, timestamp: Long = System.currentTimeMillis()) {
        currentBlockerPackageName = packageName
        currentBlockerTimestamp = if (packageName != null) timestamp else 0L

    }
    fun clearLastBlockedApp() {
        updateLastBlockedApp(null)
        Log.d(TAG, "Last blocked app cleared.")
    }

    // You can add other relevant methods here if needed, for example:
    // fun isAppCurrentlyBlocked(packageName: String): Boolean {
    // return lastBlockedAppPackageName == packageName
    // }
}
