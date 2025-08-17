package com.tellmewhy.presentation.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    prefs: SharedPreferences,
    isOverlayGranted: Boolean = false,
    isAccessibilityEnabled: Boolean = false,
    onRequestOverlayPermission: (() -> Unit)? = null,
    onOpenAccessibilitySettings: (() -> Unit)? = null,
    onApiKeySaved: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var apiKey by remember { mutableStateOf(prefs.getString("openrouter_api_key", "") ?: "") }
    var showSaved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Settings & Permissions", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        // Overlay Permission
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Overlay Permission", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Needed to show justification prompts over other apps.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onRequestOverlayPermission?.invoke() },
                    enabled = !isOverlayGranted,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(if (isOverlayGranted) "Granted" else "Grant Overlay")
                }
                Text(
                    text = if (isOverlayGranted) "Status: Granted/Enabled" else "Status: Not Granted/Disabled",
                    color = if (isOverlayGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Accessibility Permission
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Accessibility Service", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Required to track app launches and identify apps.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onOpenAccessibilitySettings?.invoke() },
                    enabled = !isAccessibilityEnabled,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(if (isAccessibilityEnabled) "Enabled" else "Open Settings")
                }
                Text(
                    text = if (isAccessibilityEnabled) "Status: Granted/Enabled" else "Status: Not Granted/Disabled",
                    color = if (isAccessibilityEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // OpenRouter API Key
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("OpenRouter API Key") },
            placeholder = { Text("Paste your OpenRouter key here") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            prefs.edit().putString("openrouter_api_key", apiKey).apply()
            showSaved = true
            onApiKeySaved?.invoke()
        }) {
            Text("Save Key")
        }
        if (showSaved) {
            Spacer(Modifier.height(8.dp))
            Text("Key saved!", color = MaterialTheme.colorScheme.primary)
        }
        if(isOverlayGranted && isAccessibilityEnabled){
            Spacer(Modifier.height(24.dp))
            Text("All required permissions are granted!", color = MaterialTheme.colorScheme.primary)
        }
    }
}
