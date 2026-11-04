package com.example.mango_disease_detector

import com.example.mango_disease_detector.data.Districts
import com.example.mango_disease_detector.utils.BanglaFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class BanglaFormatTest {

    @Test
    fun digits_areConverted() {
        assertEquals("২০২৬-১১-০৪", BanglaFormat.digits("2026-11-04"))
    }

    @Test
    fun temp_isRoundedWithDegree() {
        assertEquals("২৮°", BanglaFormat.temp(27.6))
    }

    @Test
    fun dayLabel_usesTodayTomorrowThenWeekday() {
        assertEquals("আজ", BanglaFormat.dayLabel("2026-11-04", 0))
        assertEquals("আগামীকাল", BanglaFormat.dayLabel("2026-11-05", 1))
        // 2026-11-06 is a Friday
        assertEquals("শুক্র", BanglaFormat.dayLabel("2026-11-06", 2))
    }

    @Test
    fun updatedAt_formatsPeriodOfDay() {
        val cal = Calendar.getInstance().apply { set(2026, Calendar.NOVEMBER, 4, 15, 5) }
        assertEquals("আজ বিকেল ৩:০৫", BanglaFormat.updatedAt(cal.timeInMillis, cal.timeInMillis))
    }

    @Test
    fun districts_has64() {
        assertEquals(64, Districts.all.size)
    }

    @Test
    fun nearestDistrict_findsRajshahi() {
        assertEquals("রাজশাহী", Districts.nearest(24.37, 88.60).name)
        assertEquals("চাঁপাইনবাবগঞ্জ", Districts.nearest(24.60, 88.25).name)
    }

    @Test
    fun placeName_nullOutsideBangladesh() {
        assertNull(Districts.placeName(37.42, -122.08))
    }
}
