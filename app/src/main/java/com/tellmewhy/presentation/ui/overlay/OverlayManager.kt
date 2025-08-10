package com.tellmewhy.presentation.ui.overlay


import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.tellmewhy.R


object OverlayManager {
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null

    fun showOverlay(context: Context, packageName: String) {
        if (overlayView != null) return // Already shown

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        val inflater = LayoutInflater.from(context)
        overlayView = inflater.inflate(R.layout.overlay_view, null)

        val text = overlayView!!.findViewById<TextView>(R.id.overlayText)
        val input = overlayView!!.findViewById<EditText>(R.id.inputJustification)
        val btn = overlayView!!.findViewById<Button>(R.id.submitBtn)

        text.text = "You're trying to open $packageName. Please justify:"
        btn.setOnClickListener {
            val reason = input.text.toString()
            if (reason.length > 5) {
                removeOverlay(context)
                Toast.makeText(context, "Access granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Justification too short", Toast.LENGTH_SHORT).show()
            }
        }

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager?.addView(overlayView, layoutParams)
    }

    fun removeOverlay(context: Context) {
        if (overlayView != null) {
            windowManager?.removeView(overlayView)
            overlayView = null
        }
    }
}
