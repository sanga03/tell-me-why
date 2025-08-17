package com.tellmewhy.presentation.ui.screen

import android.content.Context
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Correct import for LazyColumn items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tellmewhy.domain.model.JustificationEntry // Your Log Data Class
import com.tellmewhy.presentation.ui.screen.*
import com.tellmewhy.presentation.ui.log.JustificationLogViewModel // Your ViewModel
import com.tellmewhy.presentation.ui.overlay.PreferencesKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Place this function in a utility file or at the top level of your screen file if it's only used there.
fun formatTimestamp(timestamp: Long, pattern: String = "MMM dd, yyyy 'at' hh:mm a"): String {
    if (timestamp == 0L) return "N/A" // Or some other placeholder for invalid timestamps
    return try {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        val date = Date(timestamp)
        sdf.format(date)
    } catch (e: Exception) {
        // Log error or return a fallback string
        "Invalid Date"
    }
}

@OptIn(ExperimentalMaterial3Api::class) // For TopAppBar and Scaffold
@Composable
fun HomeScreen(
    onNavigateToAppList: () -> Unit,
    onNavigateToAllLogs: () -> Unit,
    logViewModel: JustificationLogViewModel = viewModel() // Obtain ViewModel instance
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val passCounterPrefs = remember {
        context.getSharedPreferences(PreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- State for Pass Counter ---
    var hourlyPassCount by remember { mutableStateOf(passCounterPrefs.getInt(PreferencesKeys.PASS_COUNTER, 0)) }
    var lastHourlyIncrementTimestamp by remember {
        mutableStateOf(passCounterPrefs.getLong(PreferencesKeys.LAST_INCREMENT_TIMESTAMP, System.currentTimeMillis()))
    }
    val animatedHourlyPassCount by animateIntAsState(
        targetValue = hourlyPassCount,
        label = "hourlyPassCountAnimation"
    )

    // --- Collect Justification Logs from ViewModel ---
    val justificationLogs by logViewModel.allJustifications.collectAsState(initial = emptyList())

    // --- Lifecycle and Coroutine Effects for Pass Counter ---
    DisposableEffect(key1 = lifecycleOwner, key2 = lastHourlyIncrementTimestamp) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val currentTime = System.currentTimeMillis()
                val hoursPassed = TimeUnit.MILLISECONDS.toHours(currentTime - lastHourlyIncrementTimestamp)
                if (hoursPassed > 0) {
                    val newCount = hourlyPassCount + hoursPassed.toInt()
                    hourlyPassCount = newCount
                    val newTimestamp = lastHourlyIncrementTimestamp + TimeUnit.HOURS.toMillis(hoursPassed)
                    lastHourlyIncrementTimestamp = newTimestamp
                    passCounterPrefs.edit()
                        .putInt(PreferencesKeys.PASS_COUNTER, newCount)
                        .putLong(PreferencesKeys.LAST_INCREMENT_TIMESTAMP, newTimestamp)
                        .apply()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        val effectScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        effectScope.launch {
            while (true) { // Use isActive for better cancellation handling in a real app
                val currentLastTimestamp = lastHourlyIncrementTimestamp // Use in-memory value
                val currentTime = System.currentTimeMillis()
                val hoursDifference = TimeUnit.MILLISECONDS.toHours(currentTime - currentLastTimestamp)

                if (hoursDifference >= 1) {
                    val hoursToCatchUp = hoursDifference
                    val passesToAdd = hoursToCatchUp.toInt()
                    val newPassCount = hourlyPassCount + passesToAdd // Update from current state
                    val newLastTimestamp = currentLastTimestamp + TimeUnit.HOURS.toMillis(hoursToCatchUp)

                    // Update state variables (will trigger recomposition)
                    hourlyPassCount = newPassCount
                    lastHourlyIncrementTimestamp = newLastTimestamp

                    // Save to SharedPreferences
                    passCounterPrefs.edit()
                        .putInt(PreferencesKeys.PASS_COUNTER, newPassCount)
                        .putLong(PreferencesKeys.LAST_INCREMENT_TIMESTAMP, newLastTimestamp)
                        .apply()
                }
                delay(TimeUnit.MINUTES.toMillis(10)) // Check interval
            }
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            effectScope.cancel()
        }
    }

    // --- UI Structure ---
    Scaffold(
        topBar = {
//            TopAppBar(
//                title = { Text("Dashboard") },
//                actions = {
//                    IconButton(onClick = onNavigateToAllLogs) {
//                        Icon(Icons.Filled.Info, contentDescription = "View All Logs")
//                    }
//                    IconButton(onClick = onNavigateToAppList) {
//                        Icon(Icons.Filled.Menu, contentDescription = "App List")
//                    }
//                }
//            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(16.dp), // Additional overall padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Spacing between direct children
        ) {
            // Passes Earned Card
            PassesEarnedCard(count = animatedHourlyPassCount)

            // Recent Justifications Section
            RecentJustificationsSection(
                logs = justificationLogs.take(5), // Show only the first 5 recent logs
                modifier = Modifier.weight(1f) // Fills the remaining space
            )

            // You can add more sections/cards here if needed
        }
    }
}

@Composable
fun PassesEarnedCard(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Passes Earned",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "$count",
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                if (count == 1) "Pass" else "Passes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecentJustificationsSection(
    logs: List<JustificationEntry>,
    modifier: Modifier = Modifier // <--- Add modifier parameter
) {
    Column(
        modifier = modifier.fillMaxWidth() // Apply the passed modifier (which includes weight)
    ) {
        Text(
            "Recent Justifications",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (logs.isEmpty()) {
            Text(
                "No justifications logged yet.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            LazyColumn( // This LazyColumn will now fill the height given to its parent Column by weight
                modifier = Modifier.fillMaxSize() // Fills the space given by the parent Column
            ) {
                items(logs, key = { it.id }) { logEntry ->
                    JustificationLogItem(logEntry)
                    if (logs.last() != logEntry) {
                        Divider(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        }
    }
}
@Composable
fun JustificationLogItem(log: JustificationEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Display appName
                Text(
                    text = log.appName, // Use appName here
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f) // Allow app name to take space and wrap if long
                )
                Spacer(modifier = Modifier.width(8.dp)) // Add some space if appName is long
                // Display formatted timestamp
                Text(
                    text = formatTimestamp(log.timestamp), // Format the Long timestamp
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                "Justification:", // Clearer label
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                log.justificationText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
        }
    }
}
