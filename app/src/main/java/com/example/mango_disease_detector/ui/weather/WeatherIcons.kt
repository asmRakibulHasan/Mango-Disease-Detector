package com.example.mango_disease_detector.ui.weather

import androidx.annotation.DrawableRes
import com.example.mango_disease_detector.R

/** WMO weather code to a colour weather icon (drawable/wx_*). */
@DrawableRes
fun weatherIconRes(code: Int, isDay: Boolean = true): Int = when (code) {
    0 -> if (isDay) R.drawable.wx_sun else R.drawable.wx_moon
    1, 2 -> if (isDay) R.drawable.wx_partly else R.drawable.wx_cloud
    3 -> R.drawable.wx_cloud
    45, 48 -> R.drawable.wx_fog
    51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81 -> R.drawable.wx_rain
    82, 95, 96, 99 -> R.drawable.wx_storm
    else -> R.drawable.wx_cloud
}
