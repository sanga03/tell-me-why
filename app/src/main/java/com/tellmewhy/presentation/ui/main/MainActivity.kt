package com.tellmewhy.presentation.ui.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.rememberAsyncImagePainter
import com.tellmewhy.presentation.ui.theme.RistricotrTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch
import com.tellmewhy.presentation.ui.screen.HomeScreen
// Keep this for app tracking status
private const val TRACKED_APPS_PREFS_NAME = "TrackedAppsPrefs"

// Define navigation destinations
sealed class Screen {
    object AppList : Screen()
    object PermissionSetup : Screen()
    object HomeScreen : Screen()
    // Add LogScreen here if you want to navigate to it within MainActivity
    // object LogView : Screen()
}

class MainActivity : ComponentActivity() {

    private lateinit var trackedAppsPrefs: SharedPreferences

    // Activity Result Launchers for permissions
    private lateinit var overlayPermissionLauncher: ActivityResultLauncher<Intent>
    private lateinit var accessibilitySettingsLauncher: ActivityResultLauncher<Intent>

    // State to trigger recomposition when permissions change
    private var permissionsStateTrigger by mutableStateOf(0)
        private set

    private fun refreshPermissionsState() {
        permissionsStateTrigger++
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trackedAppsPrefs = getSharedPreferences(TRACKED_APPS_PREFS_NAME, Context.MODE_PRIVATE)

        overlayPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                refreshPermissionsState() // Refresh UI
            }
        accessibilitySettingsLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                refreshPermissionsState() // Refresh UI
            }

        setContent {
            RistricotrTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.HomeScreen) }

                // Check permissions here to potentially guide user or disable features
                val isOverlayGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(this)
                } else true
                val isAccessibilitySvcEnabled = isAccessibilityServiceEnabled(this, permissionsStateTrigger) // Pass trigger

                MainScaffold(
                    currentScreen = currentScreen,
                    onScreenChange = { currentScreen = it },
                    trackedAppsPrefs = trackedAppsPrefs,
                    onViewLogsClicked = {
                        startActivity(Intent(this, com.tellmewhy.presentation.ui.log.LogActivity::class.java))
                    },
                    isOverlayPermissionGranted = isOverlayGranted,
                    isAccessibilityServiceEnabled = isAccessibilitySvcEnabled,
                    onRequestOverlayPermission = ::requestOverlayPermission,
                    onOpenAccessibilitySettings = ::openAccessibilitySettings
                )
            }
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                overlayPermissionLauncher.launch(intent)
            }
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        accessibilitySettingsLauncher.launch(intent)
        Toast.makeText(this, "Please enable 'App Usage Tracker' service.", Toast.LENGTH_LONG).show()
    }
}

// Composable function to check accessibility status and react to the trigger
@Composable
fun isAccessibilityServiceEnabled(context: Context, trigger: Int): Boolean {
    // This 'key' will cause recomposition when 'trigger' changes
    return remember(trigger) {
        val expectedComponentName = "${context.packageName}/${com.tellmewhy.presentation.ui.overlay.AppUsageAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        enabledServices?.contains(expectedComponentName, ignoreCase = true) ?: false
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    currentScreen: Screen,
    onScreenChange: (Screen) -> Unit,
    trackedAppsPrefs: SharedPreferences,
    onViewLogsClicked: () -> Unit,
    isOverlayPermissionGranted: Boolean,
    isAccessibilityServiceEnabled: Boolean,
    onRequestOverlayPermission: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentScreen) {
                            is Screen.AppList -> "Track Apps"
                            is Screen.PermissionSetup -> "Setup Permissions"
                            Screen.HomeScreen -> "Tell Me Why..!"
                        }
                    )
                },
                actions = {
                    if (currentScreen is Screen.AppList) { // Show these only on app list
                        IconButton(onClick = onViewLogsClicked) {
                            Icon(Icons.Filled.Face, "View Logs")
                        }
                    }
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Filled.MoreVert, "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (currentScreen !is Screen.PermissionSetup) {
                            DropdownMenuItem(
                                text = { Text("Setup Permissions") },
                                onClick = {
                                    onScreenChange(Screen.PermissionSetup)
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = "Setup") }
                            )
                        }
                        if (currentScreen !is Screen.AppList) {
                            DropdownMenuItem(
                                text = { Text("App List") },
                                onClick = {
                                    onScreenChange(Screen.AppList)
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Rounded.Menu, contentDescription = "list") }
                            )
                        }
                        if (currentScreen !is Screen.HomeScreen) {
                            DropdownMenuItem(
                                text = { Text("Home") },
                                onClick = {
                                    onScreenChange(Screen.HomeScreen)
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Rounded.Home, contentDescription = "Home") }
                            )
                        }
                        // Add other menu items if needed
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                is Screen.AppList -> AppListScreen(
                    trackedAppsPrefs = trackedAppsPrefs,
                    isOverlayGranted = isOverlayPermissionGranted,
                    isAccessibilityEnabled = isAccessibilityServiceEnabled
                    // Removed onViewLogsClicked from here, handled by TopAppBar
                )
                is Screen.PermissionSetup -> PermissionSetupScreen(
                    isOverlayGranted = isOverlayPermissionGranted,
                    isAccessibilityEnabled = isAccessibilityServiceEnabled,
                    onRequestOverlayPermission = onRequestOverlayPermission,
                    onOpenAccessibilitySettings = onOpenAccessibilitySettings
                )

                Screen.HomeScreen -> HomeScreen()
            }
        }
    }
}

// Constants for SharedPreferences (as defined above)



// --- AppListScreen Composable (Modified) ---
@Composable
fun AppListScreen(
    trackedAppsPrefs: SharedPreferences,
    isOverlayGranted: Boolean,
    isAccessibilityEnabled: Boolean
) {
    val context = LocalContext.current
    var showSystemApps by remember { mutableStateOf(false) }

    val priorityPackageNames = remember {
        listOf(
            "com.instagram.android",
            "com.google.android.youtube",
            "com.android.chrome",
            "com.instagram.lite",
            "app.revanced.android.youtube",
            "com.reddit.frontpage",
            "com.instagram.barcelona"
        )
    }

    val installedApps by remember { // Removed showSystemApps from remember key for now
        derivedStateOf {
            val pm = context.packageManager
            val allApplications = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            Log.d("MainActivity", "Total applications found: ${allApplications.size}")

            val (priorityApps, otherApps) = allApplications
                .filter { appInfo ->
                    // Your existing filter logic for user-launchable apps
                    val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isUpdatedSystemApp = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                    val hasLaunchIntent = pm.getLaunchIntentForPackage(appInfo.packageName) != null

                    if (!hasLaunchIntent) {
                        return@filter false
                    }
                    !isSystemApp || isUpdatedSystemApp
                }
                .mapNotNull { appInfo ->
                    try {
                        AppDetail(
                            appName = appInfo.loadLabel(pm).toString(),
                            packageName = appInfo.packageName,
                            icon = appInfo.loadIcon(pm)
                        )
                    } catch (e: Exception) {
                        Log.e("AppListDebug", "Failed to map app: ${appInfo.packageName}", e)
                        // Fallback with a placeholder or skip if icon loading fails critically
                        AppDetail(
                            appName = appInfo.loadLabel(pm).toString(),
                            packageName = appInfo.packageName,
                            icon = null // Or some default icon
                        )
                    }
                }
                .partition { appDetail -> // Partition the list
                    appDetail.packageName in priorityPackageNames
                }

            // Sort each list alphabetically by appName before combining
            val sortedPriorityApps = priorityApps.sortedBy { it.appName.lowercase() }
            val sortedOtherApps = otherApps.sortedBy { it.appName.lowercase() }

            // Combine the lists: priority apps first, then other apps
            val finalList = sortedPriorityApps + sortedOtherApps
            Log.d("MainActivity", "Priority apps: ${sortedPriorityApps.joinToString { it.packageName }}")
            Log.d("MainActivity", "Final list size: ${finalList.size}")
            finalList
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (!isOverlayGranted || !isAccessibilityEnabled) {
            Surface(color = MaterialTheme.colorScheme.errorContainer, tonalElevation = 4.dp) {
                Text(
                    text = "Core permissions are not granted. Some features might not work. Please go to 'Setup Permissions' in the menu.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                )
            }
        }

        if (installedApps.isEmpty()) {
            Box(modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Loading apps or no filterable apps found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f) // Ensure LazyColumn takes available space
            ) {
                items(installedApps, key = { it.packageName }) { appDetail ->
                    AppListItem(
                        appDetail = appDetail,
                        initialTrackState = trackedAppsPrefs.getBoolean(appDetail.packageName, false),
                        onTrackStateChanged = { packageName, isTracked ->
                            trackedAppsPrefs.edit().putBoolean(packageName, isTracked).apply()
                        },
                        // Disable switch if core permissions are missing
                        enabled = isOverlayGranted && isAccessibilityEnabled
                    )
                    Divider()
                }
            }
        }
    }
}

// --- AppListItem Composable (Modified) ---
@Composable
fun AppListItem(
    appDetail: AppDetail,
    initialTrackState: Boolean,
    onTrackStateChanged: (String, Boolean) -> Unit,
    enabled: Boolean // New parameter
) {
    var isTracked by remember { mutableStateOf(initialTrackState) }

    // Update local state if initialTrackState changes due to external preference update (though not strictly necessary here)
    LaunchedEffect(initialTrackState) {
        isTracked = initialTrackState
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = appDetail.icon),
            contentDescription = "${appDetail.appName} icon",
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = appDetail.appName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = isTracked,
            onCheckedChange = {
                isTracked = it
                onTrackStateChanged(appDetail.packageName, it)
            },
            enabled = enabled // Use the enabled parameter
        )
    }
}

// --- PermissionSetupScreen Composable (Similar to SetupScreen in previous example) ---
@Composable
fun PermissionSetupScreen(
    isOverlayGranted: Boolean,
    isAccessibilityEnabled: Boolean,
    onRequestOverlayPermission: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("App Permissions", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Grant the following permissions for the app to function correctly.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        PermissionCard(
            title = "Overlay Permission",
            description = "Needed to show justification prompts over other apps.",
            isGranted = isOverlayGranted,
            onRequestPermission = onRequestOverlayPermission,
            buttonText = if (isOverlayGranted) "Granted" else "Grant Overlay"
        )
        Spacer(modifier = Modifier.height(16.dp))
        PermissionCard(
            title = "Accessibility Service",
            description = "Required to track app launches and identify apps.",
            isGranted = isAccessibilityEnabled,
            onRequestPermission = onOpenAccessibilitySettings,
            buttonText = if (isAccessibilityEnabled) "Enabled" else "Open Settings"
        )
        Spacer(modifier = Modifier.height(24.dp))
        if(isOverlayGranted && isAccessibilityEnabled){
            Text("All required permissions are granted!", color = MaterialTheme.colorScheme.primary)
        }
    }
}

// --- PermissionCard Composable (Re-use from previous SetupActivity example) ---
@Composable
fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onRequestPermission: () -> Unit,
    buttonText: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRequestPermission,
                enabled = !isGranted,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(buttonText)
            }
            Text(
                text = if (isGranted) "Status: Granted/Enabled" else "Status: Not Granted/Disabled",
                color = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// AppDetail data class (reuse)
data class AppDetail(
    val appName: String,
    val packageName: String,
    val icon: Drawable?
)

