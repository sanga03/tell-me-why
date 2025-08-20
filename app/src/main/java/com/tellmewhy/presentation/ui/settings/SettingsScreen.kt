// SettingsScreen.kt
package com.tellmewhy.presentation.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close // Added for dismissing messages
import androidx.compose.material.icons.filled.Done // For success indication
//import androidx.compose.material.icons.filled.Error // For error icon
//import androidx.compose.material.icons.filled.Key // API Key Icon
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
//import androidx.compose.material.icons.filled.LockClock // Cooldown Icon
import androidx.compose.material.icons.filled.Settings
//import androidx.compose.material.icons.filled.SettingsAccessibility // Accessibility Icon
//import androidx.compose.material.icons.filled.Visibility // Overlay Icon
//import androidx.compose.material.icons.filled.VisibilityOff // For API key hiding
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.tellmewhy.data.datastore.AppSettingsDataStore
import kotlinx.coroutines.launch
import kotlin.text.toIntOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isOverlayGranted: Boolean,
    isAccessibilityEnabled: Boolean,
    onRequestOverlayPermission: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // --- State for API Key ---
    var openRouterApiKey by remember { mutableStateOf("") }
    var apiKeyVisible by remember { mutableStateOf(false) }
    var apiKeySavedMessage by remember { mutableStateOf<String?>(null) }

    // --- State for Secondary Cooldown ---
    var secondaryCooldownInput by remember { mutableStateOf("") }
    var secondaryCooldownError by remember { mutableStateOf<String?>(null) }
    val maxCooldownMinutes = 10
    var cooldownSavedMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        launch {
            AppSettingsDataStore.getOpenRouterApiKeyFlow(context).collect { storedApiKey ->
                openRouterApiKey = storedApiKey ?: ""
            }
        }
        launch {
            AppSettingsDataStore.getCooldownTimeFlow(context).collect { storedCooldown ->
                val initialCooldown = storedCooldown.toString()
                secondaryCooldownInput = initialCooldown
                val currentVal = initialCooldown.toIntOrNull()
                if (currentVal != null && currentVal > maxCooldownMinutes) {
                    secondaryCooldownError = "Max cooldown is $maxCooldownMinutes minutes"
                } else if (currentVal != null && currentVal < 0) {
                    secondaryCooldownError = "Cooldown cannot be negative"
                }
                else {
                    secondaryCooldownError = null
                }
            }
        }
    }

    fun validateAndSaveCooldown() {
        cooldownSavedMessage = null
        val minutes = secondaryCooldownInput.toIntOrNull()
        if (minutes == null) {
            secondaryCooldownError = "Please enter a valid number"
            return
        }
        if (minutes > maxCooldownMinutes) {
            secondaryCooldownError = "Max cooldown is $maxCooldownMinutes minutes"
            return
        }
        if (minutes < 0) {
            secondaryCooldownError = "Cooldown cannot be negative"
            return
        }
        secondaryCooldownError = null
        coroutineScope.launch {
            AppSettingsDataStore.saveCooldownTime(context, minutes)
            focusManager.clearFocus()
            cooldownSavedMessage = "Cooldown saved!"
        }
    }

    fun saveApiKey() {
        apiKeySavedMessage = null
        coroutineScope.launch {
            AppSettingsDataStore.saveOpenRouterApiKey(context, openRouterApiKey)
            focusManager.clearFocus()
            apiKeySavedMessage = "API Key saved!"
        }
    }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp) // Slightly less horizontal padding for cards to stand out
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- Cooldown Settings Card ---
            SettingCard(
                title = "App Lock Cooldown",
                icon = Icons.Filled.Settings,
                savedMessage = cooldownSavedMessage,
                onDismissSavedMessage = { cooldownSavedMessage = null }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = secondaryCooldownInput,
                        onValueChange = { newValue ->
                            cooldownSavedMessage = null // Clear save message on change
                            if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.length <= 3)) {
                                secondaryCooldownInput = newValue
                                val currentVal = newValue.toIntOrNull()
                                if (currentVal != null) {
                                    if (currentVal > maxCooldownMinutes) {
                                        secondaryCooldownError = "Max is $maxCooldownMinutes min"
                                    } else if (currentVal < 0) {
                                        secondaryCooldownError = "Cannot be negative"
                                    } else {
                                        secondaryCooldownError = null
                                    }
                                } else if (newValue.isNotEmpty()) {
                                    secondaryCooldownError = "Invalid number"
                                } else {
                                    secondaryCooldownError = null
                                }
                            }
                        },
                        label = { Text("Duration (minutes)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { validateAndSaveCooldown() }),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = secondaryCooldownError != null,
                        supportingText = {
                            if (secondaryCooldownError != null) {
                                Text(secondaryCooldownError!!, color = MaterialTheme.colorScheme.error)
                            } else {
//                                Text("App lock after repeated invalid justifications (max $maxCooldownMinutes min).", style = MaterialTheme.typography.bodySmall)
                            }
                        },
                        trailingIcon = {
                            if (secondaryCooldownError != null) {
                                Icon(Icons.Filled.Warning, "Error", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { validateAndSaveCooldown() },
                        enabled = secondaryCooldownInput.isNotBlank() && secondaryCooldownError == null
                    ) {
                        Text("Save")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- API Key Settings Card ---
            SettingCard(
                title = "OpenRouter API Key",
                icon = Icons.Filled.Build,
                savedMessage = apiKeySavedMessage,
                onDismissSavedMessage = { apiKeySavedMessage = null }
            ) {
                OutlinedTextField(
                    value = openRouterApiKey,
                    onValueChange = {
                        apiKeySavedMessage = null // Clear save message
                        openRouterApiKey = it
                    },
                    label = { Text("Your API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { saveApiKey() }),
                    trailingIcon = {
                        val image = if (apiKeyVisible) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
                        IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                            Icon(imageVector = image, contentDescription = if (apiKeyVisible) "Hide API Key" else "Show API Key")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { saveApiKey() },
                    modifier = Modifier.align(Alignment.End),
                    enabled = openRouterApiKey.isNotBlank() // Consider more robust validation if needed
                ) {
                    Text("Save Key")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Needed to validate justifications. Get your key from openrouter.ai. A fallback key is used if this is not set.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Permissions Card ---
            SettingCard(title = "App Permissions", icon = Icons.Filled.Settings) {
                PermissionItem(
                    title = "Overlay Permission",
                    description = "Needed to show justification prompts over other apps.",
                    isGranted = isOverlayGranted,
                    onRequestPermission = onRequestOverlayPermission,
                    icon = Icons.Filled.Settings
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                PermissionItem(
                    title = "Accessibility Service",
                    description = "Required to track app launches and identify apps.",
                    isGranted = isAccessibilityEnabled,
                    onRequestPermission = onOpenAccessibilitySettings,
                    icon = Icons.Filled.Settings
                )
            }
            Spacer(modifier = Modifier.height(24.dp)) // More space at the very bottom

    }
}

@Composable
fun SettingCard(
    title: String,
    icon: ImageVector,
    savedMessage: String? = null,
    onDismissSavedMessage: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Standard surface color for card
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = "$title Icon",
                    tint = MaterialTheme.colorScheme.primary, // Use primary color for icon
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge, // Slightly larger title
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface // Standard text color on surface
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Divider(modifier = Modifier.padding(bottom = 12.dp))

            if (savedMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp), // Space below the message
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween // Pushes text and button apart
                ) {
                    Text(
                        text = savedMessage,
                        color = MaterialTheme.colorScheme.primary, // Highlight saved message
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f) // Text takes available space
                    )
                    IconButton(
                        onClick = { onDismissSavedMessage?.invoke() },
                        modifier = Modifier.size(32.dp) // Smaller dismiss button
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Dismiss save message", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            content()
        }
    }
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onRequestPermission: () -> Unit,
    icon: ImageVector
) {
    Column { // Wrap in column to place status text below
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null, // Decorative
                tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Subdued color for description
                )
            }
            Spacer(modifier = Modifier.width(16.dp)) // Space before button
            Button(
                onClick = onRequestPermission,
                enabled = !isGranted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGranted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                    contentColor = if (isGranted) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(if (isGranted) "Granted" else "Grant")
            }
        }
        Text(
            text = if (isGranted) "Status: Enabled" else "Status: Not Granted / Disabled",
            color = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall, // Smaller text for status
            modifier = Modifier
                .padding(start = 40.dp, top = 4.dp) // Align with text content above, add little top padding
        )
    }
}

