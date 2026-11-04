package com.example.mango_disease_detector.ui.weather

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.mango_disease_detector.R
import com.example.mango_disease_detector.utils.AdviceLevel

/** Colours, icon and label for one advice level (matches the design's advice cards). */
data class AdviceStyle(
    @StringRes val label: Int,
    @DrawableRes val drawable: Int,
    @ColorRes val icon: Int,
    @ColorRes val bg: Int,
    @ColorRes val labelColor: Int,
    @ColorRes val title: Int,
    @ColorRes val body: Int
) {
    companion object {
        fun of(level: AdviceLevel): AdviceStyle = when (level) {
            AdviceLevel.DANGER -> AdviceStyle(
                R.string.advice_label_danger, R.drawable.ic_warning_line,
                R.color.danger_icon, R.color.danger_bg, R.color.danger_label, R.color.danger_title, R.color.danger_body
            )
            AdviceLevel.CAUTION -> AdviceStyle(
                R.string.advice_label_caution, R.drawable.ic_clock_line,
                R.color.caution_icon, R.color.caution_bg, R.color.caution_label, R.color.caution_title, R.color.caution_body
            )
            AdviceLevel.GOOD -> AdviceStyle(
                R.string.advice_label_good, R.drawable.ic_check,
                R.color.good_icon, R.color.good_bg, R.color.good_label, R.color.good_title, R.color.good_body
            )
            AdviceLevel.INFO -> AdviceStyle(
                R.string.advice_label_info, R.drawable.ic_sprout_line,
                R.color.info_icon, R.color.info_bg, R.color.info_label, R.color.info_title, R.color.info_body
            )
        }
    }
}
