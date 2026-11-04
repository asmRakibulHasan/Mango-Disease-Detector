package com.example.mango_disease_detector.ui.result

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.example.mango_disease_detector.R

/** Icon tile for one treatment step: line icon + its tint and tile colour. */
enum class StepIcon(@DrawableRes val icon: Int, @ColorRes val tint: Int, @ColorRes val tile: Int) {
    CUT(R.drawable.ic_scissors, R.color.green_primary, R.color.mint),
    BURN(R.drawable.ic_fire, R.color.peach_icon, R.color.peach),
    SPRAY(R.drawable.ic_spray, R.color.sky, R.color.sky_light),
    REPEAT(R.drawable.ic_calendar, R.color.amber_icon, R.color.butter),
    WATER(R.drawable.ic_drop_line, R.color.sky, R.color.sky_light),
    PASTE(R.drawable.ic_brush, R.color.amber_icon, R.color.butter),
    DISPOSE(R.drawable.ic_trash, R.color.peach_icon, R.color.peach),
    BUG(R.drawable.ic_bug, R.color.peach_icon, R.color.peach),
    CLEAN(R.drawable.ic_broom, R.color.green_primary, R.color.mint),
    INSPECT(R.drawable.ic_eye, R.color.sky, R.color.sky_light),
    GROW(R.drawable.ic_sprout_line, R.color.green_primary, R.color.mint),
    HAPPY(R.drawable.ic_smile, R.color.green_primary, R.color.mint),
    AIR(R.drawable.ic_wind_line, R.color.sky, R.color.sky_light),
    SUN(R.drawable.ic_sun_line, R.color.amber_icon, R.color.butter)
}

data class AdviceStep(val icon: StepIcon, val text: String)

/** Bengali name, disease type and the five treatment steps for one class. */
data class DiseaseInfo(val name: String, val type: String, val steps: List<AdviceStep>) {
    val isHealthy: Boolean get() = type == HEALTHY_TYPE

    companion object {
        private const val FUNGAL = "ছত্রাকজনিত রোগ"
        private const val BACTERIAL = "ব্যাকটেরিয়াজনিত রোগ"
        private const val PEST = "পোকার আক্রমণ"
        private const val HEALTHY_TYPE = "কোনো রোগ নেই"

        fun of(label: String): DiseaseInfo? = all[label]

        val all: Map<String, DiseaseInfo> = mapOf(
            "Anthracnose" to DiseaseInfo(
                "অ্যানথ্রাকনোজ", FUNGAL, listOf(
                    AdviceStep(StepIcon.CUT, "আক্রান্ত পাতা ও ফল গাছ থেকে তুলে ফেলুন"),
                    AdviceStep(StepIcon.BURN, "তুলে ফেলা পাতা মাটিতে পুঁতে বা পুড়িয়ে দিন"),
                    AdviceStep(StepIcon.SPRAY, "ম্যানকোজেব বা কপার অক্সিক্লোরাইড স্প্রে করুন"),
                    AdviceStep(StepIcon.REPEAT, "প্রতি ১৫ দিনে একবার স্প্রে করুন"),
                    AdviceStep(StepIcon.WATER, "গাছের গোড়ায় পানি জমতে দেবেন না")
                )
            ),
            "Bacterial Canker" to DiseaseInfo(
                "ব্যাকটেরিয়াল ক্যাংকার", BACTERIAL, listOf(
                    AdviceStep(StepIcon.CUT, "আক্রান্ত ডাল ও পাতা কেটে সরিয়ে দিন"),
                    AdviceStep(StepIcon.PASTE, "কাটা জায়গায় বোর্দো পেস্ট লাগান"),
                    AdviceStep(StepIcon.SPRAY, "কপার হাইড্রোক্সাইড স্প্রে করুন"),
                    AdviceStep(StepIcon.REPEAT, "প্রতি ১০ দিনে একবার স্প্রে করুন"),
                    AdviceStep(StepIcon.DISPOSE, "রোগাক্রান্ত অংশ মাটিতে পুঁতে ফেলুন")
                )
            ),
            "Cutting Weevil" to DiseaseInfo(
                "কাটিং উইভিল", PEST, listOf(
                    AdviceStep(StepIcon.CUT, "আক্রান্ত ডালপালা কেটে ফেলুন"),
                    AdviceStep(StepIcon.SPRAY, "ক্লোরপাইরিফস কীটনাশক স্প্রে করুন"),
                    AdviceStep(StepIcon.BUG, "পোকা হাতে বেছে মেরে ফেলুন"),
                    AdviceStep(StepIcon.CLEAN, "গাছের চারপাশ পরিষ্কার রাখুন"),
                    AdviceStep(StepIcon.INSPECT, "প্রতি সপ্তাহে গাছ পরীক্ষা করুন")
                )
            ),
            "Die Back" to DiseaseInfo(
                "ডাই ব্যাক", FUNGAL, listOf(
                    AdviceStep(StepIcon.CUT, "মরা ও শুকনো ডাল কেটে ফেলুন"),
                    AdviceStep(StepIcon.PASTE, "কাটা জায়গায় বোর্দো পেস্ট লাগান"),
                    AdviceStep(StepIcon.SPRAY, "কার্বেন্ডাজিম স্প্রে করুন"),
                    AdviceStep(StepIcon.GROW, "গাছে পর্যাপ্ত সার ও পানি দিন"),
                    AdviceStep(StepIcon.INSPECT, "প্রতি ১৫ দিনে গাছ পরীক্ষা করুন")
                )
            ),
            "Gall Midge" to DiseaseInfo(
                "গল মিজ", PEST, listOf(
                    AdviceStep(StepIcon.BURN, "আক্রান্ত কুঁড়ি ও পাতা তুলে পুড়িয়ে দিন"),
                    AdviceStep(StepIcon.SPRAY, "ইমিডাক্লোপ্রিড কীটনাশক স্প্রে করুন"),
                    AdviceStep(StepIcon.REPEAT, "ফুল আসার আগেই স্প্রে দিন"),
                    AdviceStep(StepIcon.CLEAN, "বাগান সবসময় পরিষ্কার রাখুন"),
                    AdviceStep(StepIcon.REPEAT, "প্রতি ১০ দিনে একবার স্প্রে করুন")
                )
            ),
            "Healthy" to DiseaseInfo(
                "সুস্থ", HEALTHY_TYPE, listOf(
                    AdviceStep(StepIcon.HAPPY, "গাছ ভালো আছে, চিন্তার কিছু নেই!"),
                    AdviceStep(StepIcon.WATER, "নিয়মিত সঠিক পরিমাণে পানি দিন"),
                    AdviceStep(StepIcon.GROW, "সময়মতো সার দিন"),
                    AdviceStep(StepIcon.CLEAN, "গাছের চারপাশ পরিষ্কার রাখুন"),
                    AdviceStep(StepIcon.INSPECT, "প্রতি সপ্তাহে গাছ পরীক্ষা করুন")
                )
            ),
            "Powdery Mildew" to DiseaseInfo(
                "পাউডারি মিলডিউ", FUNGAL, listOf(
                    AdviceStep(StepIcon.CUT, "আক্রান্ত পাতা ও ফুল সরিয়ে ফেলুন"),
                    AdviceStep(StepIcon.SPRAY, "সালফার বা হেক্সাকোনাজল স্প্রে করুন"),
                    AdviceStep(StepIcon.REPEAT, "প্রতি ১০ দিনে একবার স্প্রে দিন"),
                    AdviceStep(StepIcon.WATER, "গাছে অতিরিক্ত পানি দেবেন না"),
                    AdviceStep(StepIcon.AIR, "গাছের পাশে বায়ু চলাচল ভালো রাখুন")
                )
            ),
            "Sooty Mould" to DiseaseInfo(
                "সুটি মোল্ড", FUNGAL, listOf(
                    AdviceStep(StepIcon.WATER, "সাবান-পানি মিশিয়ে পাতা মুছে দিন"),
                    AdviceStep(StepIcon.SPRAY, "কীটনাশক স্প্রে করে পোকামাকড় দূর করুন"),
                    AdviceStep(StepIcon.CUT, "আক্রান্ত পাতা কেটে সরিয়ে দিন"),
                    AdviceStep(StepIcon.SUN, "গাছে পর্যাপ্ত আলো ও বায়ু নিশ্চিত করুন"),
                    AdviceStep(StepIcon.INSPECT, "প্রতি ১৫ দিনে গাছ পরীক্ষা করুন")
                )
            )
        )
    }
}
