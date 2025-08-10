package com.tellmewhy.presentation.ui.screen // Or com.example.ristricotr.ui.screens if you created a sub-package

// --- ALL THE IMPORTS needed for HomeScreen and MinuteLoopingBowlAnimation ---
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

// Keep PreferencesKeys here if specific to HomeScreen, or move to a general Constants.kt
object PreferencesKeys {
    const val PREFS_NAME = "home_screen_prefs"
    const val PASS_COUNTER = "pass_counter"
    const val LAST_INCREMENT_TIMESTAMP = "last_increment_timestamp"
    const val AI_ENABLED = "ai_enabled"
}

@Composable
fun MinuteLoopingJarAnimation(
    modifier: Modifier = Modifier,
    jarColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    liquidColor: Color = MaterialTheme.colorScheme.primary,
    animationDurationMillis: Int = 60 * 1000 // 1 minute
) {
    val infiniteTransition = rememberInfiniteTransition(label = "minuteJarFillTransition")

    val animatedFillLevel by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loopingJarFillLevel"
    )

    Canvas(modifier = modifier
        .size(150.dp) // You can adjust this
        .aspectRatio(0.7f)) { // Jars are often taller than they are wide
        val canvasWidth = size.width
        val canvasHeight = size.height

        // --- Define Jar Shape ---
        val rimHeight = canvasHeight * 0.05f
        val neckHeight = canvasHeight * 0.1f
        val shoulderHeight = canvasHeight * 0.1f
        val bodyHeight = canvasHeight - rimHeight - neckHeight - shoulderHeight

        val bodyWidth = canvasWidth * 0.9f
        val neckWidth = canvasWidth * 0.6f
        val rimWidth = canvasWidth * 0.65f // Rim slightly wider than neck

        // Start drawing from top-left of the rim
        val jarPath = Path().apply {
            // Top Rim
            moveTo( (canvasWidth - rimWidth) / 2, 0f)
            lineTo((canvasWidth + rimWidth) / 2, 0f)
            lineTo((canvasWidth + rimWidth) / 2, rimHeight)

            // Neck (right side)
            lineTo((canvasWidth + neckWidth) / 2, rimHeight + neckHeight)

            // Shoulder (right side) - using an arc for a rounded shoulder
            arcTo(
                rect = Rect(
                    left = (canvasWidth + neckWidth) / 2 - (bodyWidth - neckWidth), // control point to make it wide
                    top = rimHeight + neckHeight,
                    right = (canvasWidth + bodyWidth) / 2,
                    bottom = rimHeight + neckHeight + shoulderHeight * 2 // Double for full arc effect
                ),
                startAngleDegrees = -90f, // Start from top
                sweepAngleDegrees = 90f,  // Sweep to right
                forceMoveTo = false
            )

            // Body (right side)
            lineTo((canvasWidth + bodyWidth) / 2, rimHeight + neckHeight + shoulderHeight + bodyHeight)

            // Base (simple rounded corners with arcs or straight line)
            // For simplicity, a straight line for now, you can add arcs for rounded base corners
            arcTo(
                rect = Rect(
                    left = (canvasWidth - bodyWidth) / 2,
                    top = canvasHeight - (bodyWidth - ((canvasWidth - bodyWidth) / 2) )*0.2f - shoulderHeight*0.2f , //canvasHeight - shoulderHeight*0.5f,
                    right = (canvasWidth + bodyWidth) / 2,
                    bottom = canvasHeight + shoulderHeight*0.8f
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )


            // Body (left side)
            lineTo((canvasWidth - bodyWidth) / 2, rimHeight + neckHeight + shoulderHeight)


            // Shoulder (left side)
            arcTo(
                rect = Rect(
                    left = (canvasWidth - bodyWidth) / 2,
                    top = rimHeight + neckHeight,
                    right = (canvasWidth - neckWidth) / 2 + (bodyWidth - neckWidth) ,
                    bottom = rimHeight + neckHeight + shoulderHeight * 2
                ),
                startAngleDegrees = 180f, // Start from left horizontal
                sweepAngleDegrees = -90f,  // Sweep upwards
                forceMoveTo = false
            )


            // Neck (left side)
            lineTo((canvasWidth - neckWidth) / 2, rimHeight)
            lineTo((canvasWidth - rimWidth) / 2, rimHeight) // Connect back to rim start
            close() // Close the path
        }

        // Draw Jar outline
        drawPath(
            path = jarPath,
            color = jarColor,
            style = Stroke(width = 4f) // Adjust stroke width as needed
        )

        // --- Draw the Liquid ---
        // Determine the fillable area (inside the body, below the neck roughly)
        val fillableTopY = rimHeight + neckHeight + shoulderHeight * 0.5f // Start fill below shoulder curve
        val fillableBottomY = canvasHeight - ( (bodyWidth - ((canvasWidth - bodyWidth) / 2) )*0.2f - shoulderHeight*0.2f )*0.5f // End fill just above the base curve start
        val maxLiquidHeightInJar = fillableBottomY - fillableTopY

        val liquidHeight = maxLiquidHeightInJar * animatedFillLevel
        val liquidTopY = fillableBottomY - liquidHeight

        // Path for the liquid (rectangle clipped by the jar)
        val liquidRectPath = Path().apply {
            addRect(
                Rect(
                    left = 0f, // Cover full width, clipping will handle shape
                    top = liquidTopY,
                    right = canvasWidth,
                    bottom = fillableBottomY + 5f // Slightly overflow for clipping
                )
            )
        }

        // Optional: Gradient for the liquid
        val liquidBrush = Brush.verticalGradient(
            colors = listOf(liquidColor.copy(alpha = 0.7f), liquidColor),
            startY = liquidTopY,
            endY = fillableBottomY
        )

        // Clip drawing to the jar's shape before drawing the liquid
        clipPath(jarPath) {
            drawPath(
                path = liquidRectPath,
                brush = liquidBrush // Or use solid color: color = liquidColor
            )
        }
    }
}


// --- Preview ---
@Preview(showBackground = true)
@Composable
fun DefaultJarAnimationPreview() {
    MaterialTheme {
        MinuteLoopingJarAnimation(modifier = Modifier.size(150.dp, 214.dp)) // Approximate 0.7 aspect ratio
    }
}

@Preview(showBackground = true)
@Composable
fun FastJarAnimationPreview() {
    MaterialTheme {
        MinuteLoopingJarAnimation(
            modifier = Modifier.size(150.dp, 214.dp),
            animationDurationMillis = 5000 // 5 seconds for fast preview
        )
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val prefs = remember {
        context.getSharedPreferences(PreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)
    }

    var hourlyPassCount by remember { mutableStateOf(prefs.getInt(PreferencesKeys.PASS_COUNTER, 0)) }
    var lastHourlyIncrementTimestamp by remember {
        mutableStateOf(prefs.getLong(PreferencesKeys.LAST_INCREMENT_TIMESTAMP, System.currentTimeMillis()))
    }
    val animatedHourlyPassCount by animateIntAsState(
        targetValue = hourlyPassCount,
        label = "hourlyPassCountAnimation"
    )
    var isAiEnabled by remember { mutableStateOf(prefs.getBoolean(PreferencesKeys.AI_ENABLED, false)) }

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
                    prefs.edit()
                        .putInt(PreferencesKeys.PASS_COUNTER, newCount)
                        .putLong(PreferencesKeys.LAST_INCREMENT_TIMESTAMP, newTimestamp)
                        .apply()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        val effectScope = CoroutineScope(SupervisorJob())
        effectScope.launch {
            while (true) {
                delay(TimeUnit.MINUTES.toMillis(15))
                val currentTime = System.currentTimeMillis()
                if (TimeUnit.MILLISECONDS.toHours(currentTime - lastHourlyIncrementTimestamp) >= 1) {
                    val newCount = hourlyPassCount + 1
                    hourlyPassCount = newCount
                    val newTimestamp = lastHourlyIncrementTimestamp + TimeUnit.HOURS.toMillis(1)
                    lastHourlyIncrementTimestamp = newTimestamp
                    prefs.edit()
                        .putInt(PreferencesKeys.PASS_COUNTER, newCount)
                        .putLong(PreferencesKeys.LAST_INCREMENT_TIMESTAMP, newTimestamp)
                        .apply()
                }
            }
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            effectScope.cancel()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Passes Earned", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Text("$animatedHourlyPassCount", fontSize = 60.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))
                Text(if (animatedHourlyPassCount == 1) "Pass" else "Passes", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 16.dp)) {
            MinuteLoopingJarAnimation()
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Enable AI Features", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = isAiEnabled,
                    onCheckedChange = { newValue ->
                        isAiEnabled = newValue
                        prefs.edit().putBoolean(PreferencesKeys.AI_ENABLED, newValue).apply()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun DefaultHomeScreenWithBowlPreview() {
    MaterialTheme {
        HomeScreen()
    }
}
