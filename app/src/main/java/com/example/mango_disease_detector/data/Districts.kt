package com.example.mango_disease_detector.data

import java.text.Collator
import java.util.Locale
import kotlin.math.cos

data class District(val name: String, val lat: Double, val lon: Double)

/** All 64 districts of Bangladesh (district headquarters coordinates). */
object Districts {
    val all: List<District> = listOf(
        // Dhaka division
        District("ঢাকা", 23.8103, 90.4125),
        District("গাজীপুর", 23.9999, 90.4203),
        District("নারায়ণগঞ্জ", 23.6238, 90.5000),
        District("নরসিংদী", 23.9322, 90.7151),
        District("মুন্সিগঞ্জ", 23.5422, 90.5305),
        District("মানিকগঞ্জ", 23.8617, 90.0003),
        District("টাঙ্গাইল", 24.2513, 89.9167),
        District("কিশোরগঞ্জ", 24.4449, 90.7766),
        District("ফরিদপুর", 23.6070, 89.8429),
        District("গোপালগঞ্জ", 23.0050, 89.8266),
        District("মাদারীপুর", 23.1641, 90.1897),
        District("শরীয়তপুর", 23.2423, 90.4348),
        District("রাজবাড়ী", 23.7574, 89.6445),
        // Mymensingh division
        District("ময়মনসিংহ", 24.7471, 90.4203),
        District("জামালপুর", 24.9375, 89.9372),
        District("শেরপুর", 25.0205, 90.0153),
        District("নেত্রকোণা", 24.8709, 90.7279),
        // Chattogram division
        District("চট্টগ্রাম", 22.3569, 91.7832),
        District("কক্সবাজার", 21.4272, 92.0058),
        District("কুমিল্লা", 23.4607, 91.1809),
        District("ফেনী", 23.0159, 91.3976),
        District("নোয়াখালী", 22.8696, 91.0995),
        District("লক্ষ্মীপুর", 22.9425, 90.8412),
        District("চাঁদপুর", 23.2333, 90.6713),
        District("ব্রাহ্মণবাড়িয়া", 23.9571, 91.1119),
        District("রাঙ্গামাটি", 22.6533, 92.1789),
        District("খাগড়াছড়ি", 23.1193, 91.9847),
        District("বান্দরবান", 22.1953, 92.2184),
        // Rajshahi division
        District("রাজশাহী", 24.3745, 88.6042),
        District("চাঁপাইনবাবগঞ্জ", 24.5965, 88.2776),
        District("নওগাঁ", 24.7936, 88.9318),
        District("নাটোর", 24.4206, 89.0003),
        District("বগুড়া", 24.8465, 89.3773),
        District("জয়পুরহাট", 25.0968, 89.0227),
        District("সিরাজগঞ্জ", 24.4534, 89.7007),
        District("পাবনা", 24.0064, 89.2372),
        // Rangpur division
        District("রংপুর", 25.7439, 89.2752),
        District("দিনাজপুর", 25.6217, 88.6354),
        District("ঠাকুরগাঁও", 26.0337, 88.4617),
        District("পঞ্চগড়", 26.3411, 88.5542),
        District("নীলফামারী", 25.9318, 88.8560),
        District("লালমনিরহাট", 25.9923, 89.2847),
        District("কুড়িগ্রাম", 25.8072, 89.6295),
        District("গাইবান্ধা", 25.3287, 89.5281),
        // Khulna division
        District("খুলনা", 22.8456, 89.5403),
        District("যশোর", 23.1664, 89.2081),
        District("সাতক্ষীরা", 22.7185, 89.0705),
        District("বাগেরহাট", 22.6516, 89.7859),
        District("ঝিনাইদহ", 23.5450, 89.1726),
        District("কুষ্টিয়া", 23.9013, 89.1204),
        District("চুয়াডাঙ্গা", 23.6402, 88.8418),
        District("মেহেরপুর", 23.7622, 88.6318),
        District("মাগুরা", 23.4855, 89.4198),
        District("নড়াইল", 23.1725, 89.5127),
        // Barishal division
        District("বরিশাল", 22.7010, 90.3535),
        District("পটুয়াখালী", 22.3596, 90.3299),
        District("ভোলা", 22.6859, 90.6482),
        District("পিরোজপুর", 22.5791, 89.9759),
        District("বরগুনা", 22.1591, 90.1262),
        District("ঝালকাঠি", 22.6406, 90.1987),
        // Sylhet division
        District("সিলেট", 24.8949, 91.8687),
        District("মৌলভীবাজার", 24.4829, 91.7774),
        District("হবিগঞ্জ", 24.3745, 91.4155),
        District("সুনামগঞ্জ", 25.0658, 91.3950)
    ).sortedWith(compareBy(Collator.getInstance(Locale("bn", "BD"))) { it.name })

    fun nearest(lat: Double, lon: Double): District {
        val k = cos(Math.toRadians(lat))
        return all.minBy { d ->
            val dLat = d.lat - lat
            val dLon = (d.lon - lon) * k
            dLat * dLat + dLon * dLon
        }
    }

    fun isInBangladesh(lat: Double, lon: Double): Boolean =
        lat in 20.5..26.7 && lon in 88.0..92.7

    /** Nearest district name for a GPS fix, or null when outside Bangladesh. */
    fun placeName(lat: Double, lon: Double): String? =
        if (isInBangladesh(lat, lon)) nearest(lat, lon).name else null
}
