package com.example.mango_disease_detector.network

import com.example.mango_disease_detector.model.WeatherResponse
import com.google.gson.Gson
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import java.util.Locale

object WeatherService {
    private const val BASE_URL = "https://api.open-meteo.com/v1/forecast"
    private val gson = Gson()

    fun forecast(lat: Double, lon: Double): WeatherResponse {
        val url = BASE_URL.toHttpUrl().newBuilder()
            .addQueryParameter("latitude", String.format(Locale.US, "%.4f", lat))
            .addQueryParameter("longitude", String.format(Locale.US, "%.4f", lon))
            .addEncodedQueryParameter(
                "current",
                "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code," +
                        "wind_speed_10m,wind_gusts_10m,is_day"
            )
            .addEncodedQueryParameter(
                "hourly",
                "temperature_2m,relative_humidity_2m,precipitation_probability,precipitation,wind_gusts_10m"
            )
            .addEncodedQueryParameter(
                "daily",
                "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max," +
                        "precipitation_sum,wind_gusts_10m_max"
            )
            .addQueryParameter("timezone", "Asia/Dhaka")
            .addQueryParameter("forecast_days", "7")
            .build()

        val request = Request.Builder().url(url).get().build()

        ApiClient.client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Weather server error: ${response.code}")
            val body = response.body?.string() ?: throw Exception("Empty response")
            val parsed = gson.fromJson(body, WeatherResponse::class.java)
            if (parsed?.current == null) throw Exception("Invalid weather data")
            return parsed
        }
    }
}
