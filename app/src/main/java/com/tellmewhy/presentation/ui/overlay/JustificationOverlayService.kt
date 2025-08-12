package com.tellmewhy.presentation.ui.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.tellmewhy.presentation.ui.theme.RistricotrTheme
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import com.tellmewhy.data.local.db.AppDatabase
import com.tellmewhy.domain.model.JustificationEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
//import androidx.compose.material.icons.filled.Error // Error Icon
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.animation.with
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.* // Keep existing layout imports
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Info // Or a more relevant icon
import androidx.compose.material3.* // Keep existing Material3 imports
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import com.tellmewhy.core.util.AppIcon // Import your AppIcon composable


import com.tellmewhy.core.util.JustifyAppContent;
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.remember


// Keep PreferencesKeys here if specific to HomeScreen, or move to a general Constants.kt
object PreferencesKeys {
    const val PREFS_NAME = "home_screen_prefs"
    const val PASS_COUNTER = "pass_counter"
    const val LAST_INCREMENT_TIMESTAMP = "last_increment_timestamp"
    const val AI_ENABLED = "ai_enabled"
}

class JustificationOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob) // Use IO dispatcher for DB
    private val database by lazy { AppDatabase.getDatabase(this) } // Get DB instance
    private val TAG = "JustificationOverlay"
    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null
    private var initialErrorMessage: String? by mutableStateOf(null) // observable state
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    private  var initialStateLoad : Boolean? by mutableStateOf(false)
    private val _viewModelStore by lazy { ViewModelStore() } // Use lazy initialization
    override val viewModelStore: ViewModelStore get() = _viewModelStore

    private val savedStateRegistryController by lazy { SavedStateRegistryController.create(this) } // Use lazy
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        Log.d(TAG, "Overlay Service Created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }

        val pkg = intent?.getStringExtra(EXTRA_PACKAGE_NAME) ?: return START_NOT_STICKY
        Log.d(TAG, "Overlay Service Started for $pkg")
        showOverlay(pkg)
        return START_NOT_STICKY // Changed from START_REDELIVER_INTENT if not critical to restart with same intent
    }

    private fun showOverlay(packageName: String) {
        removeOverlay() // Ensure any existing overlay is removed

        composeView = ComposeView(this).apply {
            // These are crucial for the ComposeView itself to be eligible for focus
            isFocusable = true
            isFocusableInTouchMode = true

            // Requesting focus here can sometimes help, but the internal FocusRequester is often better.
            // requestFocus() // You can try uncommenting this as a fallback.

            setViewTreeLifecycleOwner(this@JustificationOverlayService)
            setViewTreeViewModelStoreOwner(this@JustificationOverlayService)
            setViewTreeSavedStateRegistryOwner(this@JustificationOverlayService)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                RistricotrTheme {

                    JustificationPrompt(
                        packageName = packageName,
                        initialError = initialErrorMessage, // Pass it here
                        stateLoad = initialStateLoad,
                        onJustify = { justification ->
                            initialErrorMessage = null // Reset for next attempt if needed
                            Log.i(TAG, "Justified: $justification for $packageName")
                            var appNameFromPackage = packageName
                            try {
                                val appInfo =
                                    context.packageManager.getApplicationInfo(packageName, 0)
                                appNameFromPackage =
                                    context.packageManager.getApplicationLabel(appInfo).toString()
                            }catch (E: PackageManager.NameNotFoundException){

                        }
                                val entry = JustificationEntry(  appName = appNameFromPackage, packageName = packageName, justificationText = justification , timestamp = System.currentTimeMillis())
                            // For testing, you might want to see both values
                            serviceScope.launch {
                                try {
                                    val response: String? = JustifyAppContent(packageName, entry)
                                    Log.d("JustificationPrompt", "API Response: $response")

                                    val parts = response?.split(":", limit = 2)
                                    val code = parts?.getOrNull(0)
                                    val message = parts?.getOrNull(1)

                                    if (code == "ALLOW") {
                                        Log.i(TAG, "Access ALLOWED for $packageName. Message: $message")
                                        database.justificationDao().insertJustification(entry)
                                        Log.i(TAG, "Justification saved.")
                                        removeOverlay()
                                        stopSelf()
                                    } else if (code == "DENY") {
                                        Log.w(TAG, "Access DENIED for $packageName. Reason: $message")
                                        // RE-SHOW THE OVERLAY WITH THE ERROR MESSAGE
                                        // The current composeView will be recomposed due to initialErrorMessage change
                                        this@JustificationOverlayService.initialErrorMessage = message ?: "Justification denied by server."
                                        // No need to call showOverlay again explicitly if initialErrorMessage
                                        // is a mutableStateOf observed by the Composable.
                                        // However, if the composable isn't recomposing correctly,
                                        // you might need to force it by calling showOverlay(packageName, message)
                                        // but that would recreate the ComposeView.
                                        // A better approach is to ensure JustificationPrompt reacts to initialError.
                                        this@JustificationOverlayService.initialStateLoad = false
                                    } else {
                                        Log.e(TAG, "Unknown response from server: $response")
                                        this@JustificationOverlayService.initialErrorMessage = "Unexpected server response."
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error during justification or DB save: ${e.message}", e)
                                    this@JustificationOverlayService.initialErrorMessage = "Error: ${e.message}"
                                }
                            }
                            // TODO: Actually store the justification with the package name
//                            removeOverlay()
//                            stopSelf() // Stop the service after action
                        },
                        onCancel = {

                            Log.i(TAG, "Justification canceled for $packageName")
                            removeOverlay()
                            stopSelf() // Stop the service after action
                        }
                    )
                }
            }
        }

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // --- CRITICAL WindowManager.LayoutParams ---
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, // Or WindowManager.LayoutParams.WRAP_CONTENT
            WindowManager.LayoutParams.MATCH_PARENT, // Or WindowManager.LayoutParams.WRAP_CONTENT
            layoutFlag,
            // Ensure FLAG_NOT_FOCUSABLE is ABSENT.
            // FLAG_ALT_FOCUSABLE_IM is key for IME in non-activity windows.
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or         // Allows window to interact with IME when not primary focus
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or      // If you want to handle outside touches to dismiss
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,            // Window is not touch modal (touches can go to windows below IF this window doesn't handle them)
            // If you want it truly modal (blocking touches below), remove FLAG_NOT_TOUCH_MODAL
            // and possibly add FLAG_LAYOUT_INSET_DECOR if you want it to behave more like a dialog.
            // For a dialog-like prompt that needs input, you generally WANT it to be focusable and the primary interaction target.
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            // This is very important for the soft keyboard behavior:
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or // Adjusts window when keyboard appears
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE    // Tries to make keyboard visible when the window gets focus
            // title = "Justification Prompt" // Optional: for accessibility and debugging
        }
        // --- END CRITICAL WindowManager.LayoutParams ---

        try {
            Log.d(TAG, "Attempting to add overlay with flags: ${params.flags} and softInputMode: ${params.softInputMode}")
            windowManager.addView(composeView, params)
            Log.d(TAG, "Overlay added to WindowManager.")

            // After adding the view, explicitly try to request focus if the internal one isn't enough
            // This can sometimes be necessary due to timing.
            composeView?.post { // Post to the message queue to run after view is attached
                val focusResult = composeView?.requestFocus()
                Log.d(TAG, "composeView.post.requestFocus() result: $focusResult")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error adding overlay to WindowManager: ${e.message}", e)
            stopSelf() // Stop service if overlay cannot be shown
        }
    }

    private fun removeOverlay() {

        composeView?.let {
            if (it.parent != null) { // Check if it's actually added
                try {
                    windowManager.removeView(it)
                    Log.d(TAG, "Overlay removed from WindowManager.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error removing overlay: ${e.message}", e)
                }
            }
            // Dispose compositions
            it.disposeComposition()
        }
        composeView = null
        // Do not handle lifecycle events here for ON_PAUSE/ON_STOP,
        // ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed handles it better
        // when the view is detached.
    }

    override fun onDestroy() {
        Log.d(TAG, "Overlay Service Destroying...")
        removeOverlay() // Ensure cleanup
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
        Log.d(TAG, "Overlay Service Destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
    }
}
@Composable
fun PassPopAnimation(
    trigger: Boolean,
    onAnimationFinished: () -> Unit = {} // Default empty lambda
) {
    val particleCount = 8
    // Correct way to remember a list of Animatables.
    // Each particle needs its own Animatable instance.
    val particles = remember {
        List(particleCount) { Animatable(0f) }
    }

    // This LaunchedEffect will re-run when `trigger` changes.
    LaunchedEffect(key1 = trigger) {
        val animationJobs = particles.mapIndexed { index, animatable ->
            launch { // Launch a new coroutine for each particle's animation
                animatable.snapTo(0f) // Reset to initial state before starting
                delay(index * 30L)    // Stagger the start of each particle slightly

                // Animate to 1f (e.g., full expansion / visibility)
                animatable.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = LinearOutSlowInEasing
                    )
                )
                // Animate to 2f (e.g., to drive fade out and further shrinking)
                animatable.animateTo(
                    targetValue = 2.0f,
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutLinearInEasing
                    )
                )
            }
        }
        animationJobs.joinAll() // Wait for all particle animations to complete
        onAnimationFinished()   // Call the callback
    }

    Box(
        contentAlignment = Alignment.Center,
        // Ensure the Box itself doesn't clip the particles if they go outside
        // You might need to adjust padding or size of the parent container if clipping occurs
    ) {
        particles.forEachIndexed { index, anim ->
            // Current animation value (ranges from 0f to 2f based on the animation stages)
            val animationValue = anim.value

            // --- Calculate animation properties based on animationValue ---

            // Progress for outward motion (0f to 1f as animationValue goes from 0f to 1f)
            val outwardProgress = animationValue.coerceIn(0f, 1f)

            // Progress for fade-out and secondary effects (0f to 1f as animationValue goes from 1f to 2f)
            val fadeAndShrinkProgress = (animationValue - 1f).coerceIn(0f, 1f)

            // Angle for distributing particles in a circle
            val angleRad = Math.toRadians((360f / particleCount * index).toDouble())

            // Radius: Particles move further out as outwardProgress increases
            val currentRadius = 60.dp * outwardProgress // Max distance particles travel from center

            // Alpha: Fade in during the first phase, fade out during the second
            val alpha = if (animationValue <= 1f) {
                outwardProgress // Fade in
            } else {
                1f - fadeAndShrinkProgress // Fade out
            }.coerceIn(0f, 1f)

            // Scale: Start at a base size, potentially shrink slightly as they move out,
            // and shrink more significantly during the fade-out phase.
            val scale = (1f - outwardProgress * 0.3f) * (1f - fadeAndShrinkProgress * 0.7f)
            scale.coerceAtLeast(0.1f) // Ensure particles don't disappear entirely by scale

            // Particle size: Can also be dynamic
            val particleSize = (6.dp + (outwardProgress * 4).dp) * (1f - fadeAndShrinkProgress * 0.5f)
            particleSize.coerceAtLeast(2.dp)


            val offsetX = (currentRadius.value * cos(angleRad)).dp
            val offsetY = (currentRadius.value * sin(angleRad)).dp

            Box(
                modifier = Modifier
                    .offset(x = offsetX, y = offsetY)
                    .graphicsLayer( // Prefer graphicsLayer for performance on these properties
                        scaleX = scale,
                        scaleY = scale,
                        alpha = alpha
                    )
                    .size(particleSize)
                    .background(
                        color = MaterialTheme.colorScheme.tertiary, // Use a distinct color
                        shape = CircleShape
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class) // For FlowRow if needed
@Composable
fun JustificationPrompt(
    packageName: String, // Changed from appName to packageName
    onJustify: (String) -> Unit,
    initialError: String?,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    stateLoad: Boolean?
) {
    var outlinedText by remember { mutableStateOf("") }
    var outlinedTextError by remember(initialError) { mutableStateOf(initialError) }
    val outlinedFieldFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var isLoading by remember(stateLoad) { mutableStateOf(stateLoad ?: false) }

    val context = LocalContext.current
    var appNameFromPackage by remember(packageName) { mutableStateOf(packageName) }
    val prefs = remember {
        context.getSharedPreferences(PreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)
    }

    var showPassUsedAnimation by remember { mutableStateOf(false) }
    var passUsedTrigger by remember { mutableStateOf(0) } // To re-trigger animation


    var hourlyPassCount by remember { mutableStateOf(prefs.getInt(PreferencesKeys.PASS_COUNTER, 0)) }

    LaunchedEffect(packageName) {
        try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            appNameFromPackage = context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            // appNameFromPackage remains packageName if not found
        }
    }

    val validateClientSide: (String) -> String? = { text ->
        if (text.isBlank()) {
            "Justification cannot be empty."
        } else if (text.length < 5) {
            "Please provide a more detailed reason (min 5 characters)."
        } else {
            null // No client-side error
        }
    }

    LaunchedEffect(initialError, stateLoad) { // Also react to stateLoad changes
        if (initialError != null || stateLoad == false) { // If there's an error OR loading explicitly finished
            isLoading = false
        }
        // Only request focus if there isn't an initial error that might need user attention first
        if (initialError == null) {
            outlinedFieldFocusRequester.requestFocus()
        }
    }

    // Animation for the entire prompt appearance (optional)
    val animatedScale by animateFloatAsState(
        targetValue = if (stateLoad == null || stateLoad == true) 0.95f else 1f, // Initial small, then grow
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "PromptScale"
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (stateLoad == null || stateLoad == true) 0f else 1f, // Initial fade in
        animationSpec = tween(300), label = "PromptAlpha"
    )


    Surface(
        modifier = modifier
            .scale(animatedScale)
            .alpha(animatedAlpha)
            .widthIn(max = 380.dp) // Slightly wider
            .wrapContentHeight()
            .animateContentSize(), // Animates size changes smoothly
        shape = MaterialTheme.shapes.large, // Slightly larger rounding
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp, // More pronounced shadow
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 20.dp) // Adjusted padding
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Icon and Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                AppIcon(packageName = packageName, size = 48.dp) // Display App Icon
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Unlock $appNameFromPackage?", // Use resolved app name
                    style = MaterialTheme.typography.titleLarge, // Larger title
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "Please provide a reason to continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Animated TextField
            val textFieldInteractionSource = remember { MutableInteractionSource() }
            val isTextFieldPressed by textFieldInteractionSource.collectIsPressedAsState()
            val textFieldScale by animateFloatAsState(
                targetValue = if (isTextFieldPressed) 0.98f else 1f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "TextFieldScale"
            )

            OutlinedTextField(
                value = outlinedText,
                onValueChange = {
                    outlinedText = it
                    if (outlinedTextError != null) outlinedTextError = null
                },
                label = { Text("Your Justification") },
                placeholder = { Text("E.g., Finishing urgent work...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(textFieldScale)
                    .focusRequester(outlinedFieldFocusRequester)
                    .onFocusChanged { focusState ->
                        Log.d("JustificationPrompt", "TextField focus: ${focusState.isFocused}")
                        // Could add more focus-based animation here
                    }
                    .animateContentSize(), // Animates height changes (e.g. for multiline)
                interactionSource = textFieldInteractionSource,
                singleLine = false,
                minLines = 2,
                maxLines = 5,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Sentences
                ),
                isError = outlinedTextError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            // Expressive Error Message Area (same as before)
            androidx.compose.animation.AnimatedVisibility(
                visible = outlinedTextError != null,
                enter = slideInVertically { it / 2 } + fadeIn(), // Animate in from top
                exit = slideOutVertically { it / 2 } + fadeOut()  // Animate out to top
            ) {
                Surface( // Use Surface for background, shape, and elevation
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp), // Space between TextField and error message
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f), // Light, less intrusive error background
                    // Or use a more opaque color: MaterialTheme.colorScheme.errorContainer
                    // Or a custom color: Color(0xFFFFEBEE) // A very light red
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    ) // Subtle border
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Here is why",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = outlinedTextError
                                ?: "", // outlinedTextError will not be null here due to AnimatedVisibility
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer, // Good contrast with errorContainer
                            // Or directly: MaterialTheme.colorScheme.error
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }


                Spacer(Modifier.height(24.dp)) // More space before buttons

                // Animated Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        12.dp,
                        Alignment.End
                    ) // Spacing and alignment
                ) {
                    val passButtonInteractionSource = remember { MutableInteractionSource() }
                    val scope = rememberCoroutineScope()
                    val isPassButtonPressed by passButtonInteractionSource.collectIsPressedAsState()
                    val passButtonScale by animateFloatAsState(
                        targetValue = if (isPassButtonPressed) 0.95f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "PassButtonScale"
                    )

                    // For the particle animation, we'll draw directly on a Canvas or use multiple animated elements.
                    // Let's use a simpler approach with multiple small animated circles for a "pop" effect.
                    val particleAnimationTrigger = remember { mutableStateOf(false) }
                    val passAnimationInProgress = remember { mutableStateOf(false) }


                    Box(contentAlignment = Alignment.Center) { // Box to overlay particles
                        Button(
                            onClick = {
                                if (hourlyPassCount > 0 && !passAnimationInProgress.value) {
                                    outlinedTextError = null
                                    isLoading = false // Ensure other loading state is reset

                                    // Trigger animation
                                    particleAnimationTrigger.value = !particleAnimationTrigger.value
                                    passAnimationInProgress.value = true

                                    // Actual pass usage logic
                                    val newPassCount = hourlyPassCount - 1
                                    prefs.edit()
                                        .putInt(PreferencesKeys.PASS_COUNTER, newPassCount)
                                        .apply()
                                    // Update local state if it's not directly read from prefs or a ViewModel
                                    // hourlyPassCount = newPassCount // Assuming hourlyPassCount is a mutableStateOf from SharedPreferences

                                    // After a delay (for animation to finish), call onCancel
                                    // This allows the animation to be seen before the dialog closes.
                                    scope.launch {
                                        delay(600) // Animation duration + small buffer
                                        onCancel()
                                        passAnimationInProgress.value = false
                                    }
                                } else if (hourlyPassCount <= 0) {
                                    // Optionally, show a message that there are no passes
                                    // e.g., using a Snackbar or Toast
                                    Log.d("PassButton", "No passes left to avail.")
                                }
                            },
                            modifier = Modifier
                                .scale(passButtonScale)
                                .padding(horizontal = 8.dp), // Add some padding if needed
                            enabled = hourlyPassCount > 0 && !passAnimationInProgress.value,
                            interactionSource = passButtonInteractionSource,
                            shape = MaterialTheme.shapes.medium, // Or CircleShape for a round button
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Avail Pass ($hourlyPassCount)")
                            }
                        }

                        // Particle Pop Animation
                        if (passAnimationInProgress.value) {
                            PassPopAnimation(trigger = particleAnimationTrigger.value) {
                                // This callback could be used if the animation itself needs to signal completion
                                // For now, we use a fixed delay in the onClick.
                            }
                        }
                    }

                    val justifyInteractionSource = remember { MutableInteractionSource() }
                    val isJustifyPressed by justifyInteractionSource.collectIsPressedAsState()
                    val justifyScale by animateFloatAsState(
                        if (isJustifyPressed) 0.95f else 1f,
                        label = "JustifyScale"
                    )
                    val buttonElevation by animateDpAsState(
                        if (isJustifyPressed) 2.dp else 6.dp,
                        label = "JustifyElevation"
                    )


                    Button(
                        onClick = {
                            val clientError = validateClientSide(outlinedText)
                            if (clientError != null) {
                                outlinedTextError = clientError
                                isLoading = false // Ensure loading stops if client-side error
                            } else {
                                outlinedTextError = null // Clear any previous error
                                isLoading = true
                                onJustify(outlinedText)
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.scale(justifyScale),
                        interactionSource = justifyInteractionSource,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = buttonElevation,
                            pressedElevation = 2.dp,
                            disabledElevation = 0.dp
                        ),
                        shape = MaterialTheme.shapes.medium // Rounded corners
                    ) {
                        AnimatedContent(
                            targetState = isLoading,
                            transitionSpec = {
                                // CORRECTED AND MORE EXPLICIT APPROACH:
                                ContentTransform(
                                    targetContentEnter = fadeIn(animationSpec = tween(durationMillis = 150)),
                                    initialContentExit = fadeOut(
                                        animationSpec = tween(
                                            durationMillis = 150
                                        )
                                    )
                                )
                            },
                            label = "ButtonLoadAnim"

                        ) { loading ->
                            if (loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text("Unlock")
                            }
                        }
                    }

            }
        }

        // Request focus after the prompt has likely animated in
        LaunchedEffect(Unit) {
            delay(350) // Adjust delay to match your animation needs
            if (initialError == null) {
                outlinedFieldFocusRequester.requestFocus()
            }
        }
    }
}


