package com.example.mango_disease_detector.data

import android.content.Context
import com.example.mango_disease_detector.model.WeatherResponse
import com.google.gson.Gson

data class SavedPlace(val name: String, val lat: Double, val lon: Double, val isGps: Boolean)

data class CachedWeather(val response: WeatherResponse, val placeName: String, val savedAt: Long)

/** Stores the chosen location and the last weather response for offline use. */
object WeatherPrefs {
    private const val PREFS = "weather_prefs"
    private const val KEY_PLACE_NAME = "place_name"
    private const val KEY_LAT = "place_lat"
    private const val KEY_LON = "place_lon"
    private const val KEY_IS_GPS = "place_is_gps"
    private const val KEY_CACHE = "weather_cache"

    /** Cached forecasts older than this are not shown. */
    private const val MAX_CACHE_AGE_MS = 3L * 24 * 60 * 60 * 1000

    private val gson = Gson()

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun savePlace(context: Context, place: SavedPlace) {
        prefs(context).edit()
            .putString(KEY_PLACE_NAME, place.name)
            .putString(KEY_LAT, place.lat.toString())
            .putString(KEY_LON, place.lon.toString())
            .putBoolean(KEY_IS_GPS, place.isGps)
            .apply()
    }

    fun loadPlace(context: Context): SavedPlace? {
        val p = prefs(context)
        val name = p.getString(KEY_PLACE_NAME, null) ?: return null
        val lat = p.getString(KEY_LAT, null)?.toDoubleOrNull() ?: return null
        val lon = p.getString(KEY_LON, null)?.toDoubleOrNull() ?: return null
        return SavedPlace(name, lat, lon, p.getBoolean(KEY_IS_GPS, false))
    }

    fun saveCache(context: Context, cached: CachedWeather) {
        prefs(context).edit().putString(KEY_CACHE, gson.toJson(cached)).apply()
    }

    fun loadCache(context: Context, now: Long = System.currentTimeMillis()): CachedWeather? {
        val json = prefs(context).getString(KEY_CACHE, null) ?: return null
        val cached = try {
            gson.fromJson(json, CachedWeather::class.java)
        } catch (e: Exception) {
            null
        } ?: return null
        if (cached.response.current == null || now - cached.savedAt > MAX_CACHE_AGE_MS) return null
        return cached
    }
}
