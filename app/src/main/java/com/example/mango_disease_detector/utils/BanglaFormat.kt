package com.example.mango_disease_detector.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

/** Bengali text helpers: digits, day names, weather descriptions. */
object BanglaFormat {
    private const val DIGITS = "০১২৩৪৫৬৭৮৯"

    private val SHORT_DAYS = arrayOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহস্পতি", "শুক্র", "শনি")
    private val FULL_DAYS = arrayOf("রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার")

    fun digits(text: String): String =
        text.map { if (it in '0'..'9') DIGITS[it - '0'] else it }.joinToString("")

    fun number(value: Double): String = digits(value.roundToInt().toString())

    fun temp(value: Double): String = number(value) + "°"

    /** "আজ", "আগামীকাল", or the short weekday name for later days. */
    fun dayLabel(date: String, offsetFromToday: Int): String = when (offsetFromToday) {
        0 -> "আজ"
        1 -> "আগামীকাল"
        else -> weekdayIndex(date)?.let { SHORT_DAYS[it] } ?: digits(date)
    }

    fun fullDayName(date: String): String =
        weekdayIndex(date)?.let { FULL_DAYS[it] } ?: digits(date)

    /** 0 = Sunday ... 6 = Saturday, or null if the date cannot be parsed. */
    fun weekdayIndex(date: String): Int? {
        val parsed = try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date)
        } catch (e: Exception) {
            null
        } ?: return null
        val cal = Calendar.getInstance().apply { time = parsed }
        return cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
    }

    /** WMO weather code to Bengali description. */
    fun weatherText(code: Int): String = when (code) {
        0 -> "পরিষ্কার আকাশ"
        1, 2 -> "আংশিক মেঘলা"
        3 -> "মেঘলা আকাশ"
        45, 48 -> "কুয়াশা"
        51, 53, 55, 56, 57 -> "গুঁড়ি গুঁড়ি বৃষ্টি"
        61, 66, 80 -> "হালকা বৃষ্টি"
        63, 81 -> "বৃষ্টি"
        65, 67, 82 -> "ভারী বৃষ্টি"
        95 -> "বজ্রসহ বৃষ্টি"
        96, 99 -> "শিলাবৃষ্টি ও ঝড়"
        else -> "মেঘলা আকাশ"
    }

    fun weatherEmoji(code: Int, isDay: Boolean = true): String = when (code) {
        0 -> if (isDay) "☀️" else "🌙"
        1, 2 -> if (isDay) "⛅" else "☁️"
        3 -> "☁️"
        45, 48 -> "🌫️"
        51, 53, 55, 56, 57, 61, 66, 80 -> "🌦️"
        63, 65, 67, 81 -> "🌧️"
        82, 95, 96, 99 -> "⛈️"
        else -> "☁️"
    }

    /** e.g. "আজ বিকেল ৩:২০" or "০৩/১১ সকাল ৯:০৫". */
    fun updatedAt(millis: Long, now: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val today = Calendar.getInstance().apply { timeInMillis = now }
        val sameDay = cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val period = when (hour) {
            in 4..11 -> "সকাল"
            in 12..14 -> "দুপুর"
            in 15..17 -> "বিকেল"
            in 18..19 -> "সন্ধ্যা"
            else -> "রাত"
        }
        val hour12 = if (hour % 12 == 0) 12 else hour % 12
        val time = digits(String.format(Locale.US, "%d:%02d", hour12, cal.get(Calendar.MINUTE)))
        val day = if (sameDay) "আজ" else digits(
            String.format(Locale.US, "%02d/%02d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1)
        )
        return "$day $period $time"
    }
}
