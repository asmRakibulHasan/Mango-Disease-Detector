package com.example.mango_disease_detector.utils

import com.example.mango_disease_detector.model.WeatherResponse
import java.util.Calendar

/** Ordered by severity: the list shown to farmers is sorted by this order. */
enum class AdviceLevel { DANGER, CAUTION, GOOD, INFO }

/** One advice card: a short bold [title] and a one-or-two sentence [text]. */
data class FarmAdvice(val level: AdviceLevel, val title: String, val text: String)

/** Weather summary for the next 24 hours, starting at the current hour. */
data class Next24h(
    val maxRainChance: Double,
    val rainSum: Double,
    val avgHumidity: Double,
    val avgTemp: Double,
    val maxGust: Double
)

/**
 * Rule-based advice for mango farmers in Bangladesh.
 * Rules use the next 24h forecast plus the month (mango growth stage).
 */
object FarmAdvisor {
    const val STORM_GUST_KMH = 60.0
    const val WINDY_GUST_KMH = 40.0
    const val HEAVY_RAIN_MM = 50.0
    const val NO_SPRAY_RAIN_CHANCE = 60.0
    const val HEAT_C = 36.0

    fun next24h(w: WeatherResponse): Next24h {
        val current = w.current
        val hourly = w.hourly
        if (hourly == null || hourly.time.isEmpty()) {
            val d = w.daily
            return Next24h(
                maxRainChance = d?.rainChance?.firstOrNull() ?: 0.0,
                rainSum = d?.rainSum?.firstOrNull() ?: 0.0,
                avgHumidity = current?.humidity ?: 0.0,
                avgTemp = current?.temperature ?: 0.0,
                maxGust = maxOf(current?.windGusts ?: 0.0, d?.windGustsMax?.firstOrNull() ?: 0.0)
            )
        }

        val nowHour = current?.time?.take(13).orEmpty()
        val start = hourly.time.indexOfFirst { it.take(13) >= nowHour }.coerceAtLeast(0)
        val end = minOf(start + 24, hourly.time.size)

        fun window(values: List<Double?>): List<Double> =
            if (start >= values.size) emptyList()
            else values.subList(start, minOf(end, values.size)).filterNotNull()

        val humidity = window(hourly.humidity)
        val temps = window(hourly.temperature)
        return Next24h(
            maxRainChance = window(hourly.rainChance).maxOrNull() ?: 0.0,
            rainSum = window(hourly.rain).sum(),
            avgHumidity = if (humidity.isEmpty()) current?.humidity ?: 0.0 else humidity.average(),
            avgTemp = if (temps.isEmpty()) current?.temperature ?: 0.0 else temps.average(),
            maxGust = maxOf(current?.windGusts ?: 0.0, window(hourly.windGusts).maxOrNull() ?: 0.0)
        )
    }

    /** Index of today in the daily arrays (matched against the current local date). */
    fun todayIndex(w: WeatherResponse): Int {
        val today = w.current?.time?.take(10).orEmpty()
        return w.daily?.time?.indexOf(today)?.takeIf { it >= 0 } ?: 0
    }

    /** Month 1..12 in Dhaka local time. */
    fun monthOf(w: WeatherResponse): Int =
        w.current?.time?.substring(5, 7)?.toIntOrNull()
            ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)

    fun advise(w: WeatherResponse): List<FarmAdvice> {
        val n = next24h(w)
        val month = monthOf(w)
        val daily = w.daily
        val today = todayIndex(w)
        val todayMax = daily?.tempMax?.getOrNull(today) ?: w.current?.temperature ?: 0.0
        val todayMin = daily?.tempMin?.getOrNull(today) ?: todayMax
        val next3DaysRain = (today until today + 3).sumOf { daily?.rainSum?.getOrNull(it) ?: 0.0 }

        val out = mutableListOf<FarmAdvice>()

        // Storm / strong wind (Kalbaishakhi season: March-May)
        if (n.maxGust >= STORM_GUST_KMH) {
            out += FarmAdvice(
                AdviceLevel.DANGER,
                if (month in 3..5) "কালবৈশাখী ঝড়ের আশঙ্কা" else "ঝড়ো বাতাসের আশঙ্কা",
                "বাতাস ঘণ্টায় ${BanglaFormat.number(n.maxGust)} কিমি পর্যন্ত হতে পারে। " +
                        "আম ঝরে পড়তে পারে, দুর্বল ডাল খুঁটি দিয়ে বেঁধে দিন।"
            )
        } else if (n.maxGust >= WINDY_GUST_KMH) {
            out += FarmAdvice(
                AdviceLevel.CAUTION, "জোরে বাতাসের সম্ভাবনা",
                "আজ স্প্রে করবেন না, বাতাসে ওষুধ উড়ে যাবে।"
            )
        }

        // Heavy rain / waterlogging
        if (n.rainSum >= HEAVY_RAIN_MM) {
            out += FarmAdvice(
                AdviceLevel.DANGER, "ভারী বৃষ্টির সম্ভাবনা",
                "বাগানের নালা পরিষ্কার রাখুন, গাছের গোড়ায় পানি জমতে দেবেন না।"
            )
        }

        // Spraying window
        if (n.maxRainChance >= NO_SPRAY_RAIN_CHANCE) {
            out += FarmAdvice(
                AdviceLevel.CAUTION, "আজ স্প্রে করবেন না",
                "বৃষ্টির সম্ভাবনা ${BanglaFormat.number(n.maxRainChance)}%। " +
                        "ছত্রাকনাশক বা কীটনাশক বৃষ্টিতে ধুয়ে যাবে।"
            )
        }

        // Anthracnose: warm, humid, wet
        val wet = n.maxRainChance >= 50 || n.rainSum >= 2
        if (n.avgHumidity >= 80 && n.avgTemp in 20.0..32.0 && wet) {
            out += FarmAdvice(
                AdviceLevel.DANGER, "অ্যানথ্রাকনোজের ঝুঁকি",
                "আবহাওয়া ভেজা ও আর্দ্র। পাতায় কালো দাগ খুঁজুন, " +
                        "বৃষ্টি থামলে ম্যানকোজেব বা কার্বেন্ডাজিম স্প্রে করুন।"
            )
        }

        // Powdery mildew: flowering season, cool nights, humid air
        if (month in 1..3 && n.avgHumidity >= 60 && todayMin in 10.0..20.0) {
            out += FarmAdvice(
                AdviceLevel.CAUTION, "পাউডারি মিলডিউর ঝুঁকি",
                "রাত ঠান্ডা, বাতাস আর্দ্র। মুকুলে সাদা গুঁড়া দেখলে " +
                        "সালফার জাতীয় ছত্রাকনাশক স্প্রে করুন।"
            )
        }

        // Heat stress with no rain coming
        if (todayMax >= HEAT_C && next3DaysRain < 2) {
            out += FarmAdvice(
                AdviceLevel.CAUTION, "তীব্র গরম (${BanglaFormat.temp(todayMax)})",
                "বিকেলে গাছের গোড়ায় পানি দিন, এতে আমের গুটি ঝরা কমবে।"
            )
        }

        // No warnings: say whether today is a good day to work
        if (out.isEmpty()) {
            out += if (n.maxRainChance < 30 && n.maxGust < 30) {
                FarmAdvice(
                    AdviceLevel.GOOD, "আজ কাজের ভালো দিন",
                    if (month in 5..7) "আবহাওয়া ভালো। আম পাড়া ও স্প্রে করার জন্য উপযুক্ত দিন।"
                    else "আবহাওয়া ভালো। প্রয়োজন হলে আজ স্প্রে করার জন্য উপযুক্ত দিন।"
                )
            } else {
                FarmAdvice(AdviceLevel.GOOD, "আবহাওয়া স্বাভাবিক", "নিয়মিত বাগান পরিচর্যা চালিয়ে যান।")
            }
        }

        out += seasonalTip(month)
        return out.sortedBy { it.level.ordinal }
    }

    /** Mango growth-stage tip for Bangladesh, by month. */
    fun seasonalTip(month: Int): FarmAdvice = when (month) {
        12, 1 -> FarmAdvice(
            AdviceLevel.INFO, "মুকুল আসার আগের সময়",
            "এখন গাছে সেচ দেবেন না, এতে মুকুল ভালো আসে।"
        )
        2, 3 -> FarmAdvice(
            AdviceLevel.INFO, "মুকুল ও গুটি আসার সময়",
            "হপার পোকা ও পাউডারি মিলডিউর দিকে নজর রাখুন।"
        )
        4, 5 -> FarmAdvice(
            AdviceLevel.INFO, "গুটি বড় হওয়ার সময়",
            "১৫ দিন পরপর সেচ দিন, এতে গুটি ঝরা কমবে।"
        )
        6, 7 -> FarmAdvice(
            AdviceLevel.INFO, "আম পাকা ও সংগ্রহের সময়",
            "শুকনো দিনে, সকালের দিকে আম পাড়ুন।"
        )
        8, 9 -> FarmAdvice(
            AdviceLevel.INFO, "ডাল ছাঁটার সময়",
            "আম সংগ্রহের পর মরা, রোগা ও ঘন ডাল ছেঁটে দিন।"
        )
        else -> FarmAdvice(
            AdviceLevel.INFO, "সার দেওয়ার সময়",
            "গাছের গোড়ার চারপাশে সার দিয়ে হালকা সেচ দিন।"
        )
    }
}
