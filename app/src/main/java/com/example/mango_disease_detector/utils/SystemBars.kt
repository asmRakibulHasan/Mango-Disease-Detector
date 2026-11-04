package com.example.mango_disease_detector.utils

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge

/**
 * Edge-to-edge with fixed icon colours: the app is always light-themed, so it must not
 * follow the system dark mode (white icons on the cream background were unreadable).
 */
fun ComponentActivity.edgeToEdge(lightIcons: Boolean = false) {
    val style = if (lightIcons) {
        SystemBarStyle.dark(Color.TRANSPARENT)
    } else {
        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    }
    enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
}
