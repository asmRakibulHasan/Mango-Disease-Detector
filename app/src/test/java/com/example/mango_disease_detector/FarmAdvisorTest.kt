package com.example.mango_disease_detector

import com.example.mango_disease_detector.model.CurrentWeather
import com.example.mango_disease_detector.model.DailyWeather
import com.example.mango_disease_detector.model.HourlyWeather
import com.example.mango_disease_detector.model.WeatherResponse
import com.example.mango_disease_detector.utils.AdviceLevel
import com.example.mango_disease_detector.utils.FarmAdvisor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FarmAdvisorTest {

    /** Builds 48 hourly slots starting at 00:00 on [date], with constant values. */
    private fun weather(
        date: String = "2026-11-04",
        nowHour: Int = 10,
        temp: Double = 28.0,
        humidity: Double = 60.0,
        rainChance: Double = 0.0,
        rainPerHour: Double = 0.0,
        gust: Double = 10.0,
        dailyMax: Double = 30.0,
        dailyMin: Double = 22.0,
        dailyRain: Double = 0.0
    ): WeatherResponse {
        val nextDate = "2026-11-05"
        val times = (0 until 48).map { h ->
            val d = if (h < 24) date else nextDate
            "%sT%02d:00".format(d, h % 24)
        }
        return WeatherResponse(
            current = CurrentWeather(
                time = "%sT%02d:15".format(date, nowHour),
                temperature = temp,
                humidity = humidity,
                windGusts = gust
            ),
            hourly = HourlyWeather(
                time = times,
                temperature = List(48) { temp },
                humidity = List(48) { humidity },
                rainChance = List(48) { rainChance },
                rain = List(48) { rainPerHour },
                windGusts = List(48) { gust }
            ),
            daily = DailyWeather(
                time = listOf(date, nextDate, "2026-11-06"),
                weatherCode = listOf(0, 0, 0),
                tempMax = List(3) { dailyMax },
                tempMin = List(3) { dailyMin },
                rainChance = List(3) { rainChance },
                rainSum = List(3) { dailyRain },
                windGustsMax = List(3) { gust }
            )
        )
    }

    private fun texts(w: WeatherResponse) = FarmAdvisor.advise(w).map { "${it.title} ${it.text}" }

    @Test
    fun next24h_startsAtCurrentHour_andCoversOneDay() {
        val w = weather(nowHour = 10, rainPerHour = 1.0)
        val n = FarmAdvisor.next24h(w)
        assertEquals(24.0, n.rainSum, 0.001)
    }

    @Test
    fun calmDryDay_isGoodDayToSpray() {
        val advice = FarmAdvisor.advise(weather())
        assertEquals(AdviceLevel.GOOD, advice.first().level)
        assertTrue(advice.first().text.contains("স্প্রে করার জন্য উপযুক্ত"))
        assertEquals(AdviceLevel.INFO, advice.last().level)
    }

    @Test
    fun rainLikely_saysDoNotSpray() {
        assertTrue(texts(weather(rainChance = 70.0)).any { it.contains("স্প্রে করবেন না") })
    }

    @Test
    fun strongGustInKalbaishakhiSeason_isStormDanger() {
        val advice = FarmAdvisor.advise(weather(date = "2026-04-15", gust = 75.0))
        assertEquals(AdviceLevel.DANGER, advice.first().level)
        assertTrue(advice.first().title.contains("কালবৈশাখী"))
    }

    @Test
    fun humidWarmWet_flagsAnthracnose() {
        val w = weather(humidity = 90.0, temp = 27.0, rainChance = 80.0, rainPerHour = 0.5)
        assertTrue(texts(w).any { it.contains("অ্যানথ্রাকনোজ") })
    }

    @Test
    fun anthracnose_notFlaggedWhenDry() {
        assertFalse(texts(weather(humidity = 90.0, temp = 27.0)).any { it.contains("অ্যানথ্রাকনোজ") })
    }

    @Test
    fun heavyRain_warnsAboutDrainage() {
        assertTrue(texts(weather(rainChance = 90.0, rainPerHour = 3.0)).any { it.contains("নালা") })
    }

    @Test
    fun coolHumidFloweringSeason_flagsPowderyMildew() {
        val w = weather(date = "2026-02-10", humidity = 75.0, temp = 20.0, dailyMin = 14.0)
        assertTrue(texts(w).any { it.contains("পাউডারি মিলডিউ") })
    }

    @Test
    fun heatWithNoRain_saysIrrigate() {
        val w = weather(date = "2026-05-10", temp = 37.0, dailyMax = 39.0)
        assertTrue(texts(w).any { it.contains("তীব্র গরম") })
    }

    @Test
    fun adviceIsSortedBySeverity() {
        val w = weather(date = "2026-04-15", gust = 75.0, rainChance = 90.0, rainPerHour = 3.0, humidity = 90.0)
        val levels = FarmAdvisor.advise(w).map { it.level.ordinal }
        assertEquals(levels.sorted(), levels)
    }

    @Test
    fun todayIndex_matchesCurrentDate() {
        val w = weather().copy(
            daily = weather().daily!!.copy(time = listOf("2026-11-03", "2026-11-04", "2026-11-05"))
        )
        assertEquals(1, FarmAdvisor.todayIndex(w))
    }
}
