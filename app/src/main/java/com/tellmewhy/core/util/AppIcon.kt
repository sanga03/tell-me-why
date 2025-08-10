package com.tellmewhy.core.util // Or your preferred package

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter // Accompanist library for Drawable

// You'll need to add this dependency to your app's build.gradle:
// implementation "com.google.accompanist:accompanist-drawablepainter:<latest_version>"
// Check latest version: https://google.github.io/accompanist/drawablepainter/

@Composable
fun AppIcon(
    packageName: String,
    modifier: Modifier = Modifier,
    size: Dp
) {
    val context = LocalContext.current
    var appIconDrawable by remember(packageName) { mutableStateOf<Drawable?>(null) }

    LaunchedEffect(packageName) {
        try {
            appIconDrawable = context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            // Handle error: app not found, maybe show a default icon
            appIconDrawable = null // Or a placeholder drawable
            // Log.e("AppIcon", "App not found: $packageName", e)
        }
    }

    if (appIconDrawable != null) {
        Image(
            painter = rememberDrawablePainter(drawable = appIconDrawable),
            contentDescription = "$packageName icon",
            modifier = modifier.size(size)
        )
    } else {
        // Optional: Placeholder if icon is null or loading
        // For example, a generic icon or a simple colored box
        // Box(modifier = modifier.size(size).background(MaterialTheme.colorScheme.surfaceVariant))
    }
}
