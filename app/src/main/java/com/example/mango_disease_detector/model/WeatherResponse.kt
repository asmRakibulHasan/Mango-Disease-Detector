package com.example.mango_disease_detector.model

import com.google.gson.annotations.SerializedName

/** Response of the Open-Meteo forecast API (https://open-meteo.com). */
data class WeatherResponse(
    val current: CurrentWeather? = null,
    val hourly: HourlyWeather? = null,
    val daily: DailyWeather? = null
)

data class CurrentWeather(
    /** Local Dhaka time, e.g. "2026-11-04T21:45". */
    val time: String = "",
    @SerializedName("temperature_2m") val temperature: Double = 0.0,
    @SerializedName("relative_humidity_2m") val humidity: Double = 0.0,
    @SerializedName("apparent_temperature") val feelsLike: Double = 0.0,
    @SerializedName("weather_code") val weatherCode: Int = 0,
    @SerializedName("wind_speed_10m") val windSpeed: Double = 0.0,
    @SerializedName("wind_gusts_10m") val windGusts: Double = 0.0,
    @SerializedName("is_day") val isDay: Int = 1
)

data class HourlyWeather(
    val time: List<String> = emptyList(),
    @SerializedName("temperature_2m") val temperature: List<Double?> = emptyList(),
    @SerializedName("relative_humidity_2m") val humidity: List<Double?> = emptyList(),
    @SerializedName("precipitation_probability") val rainChance: List<Double?> = emptyList(),
    @SerializedName("precipitation") val rain: List<Double?> = emptyList(),
    @SerializedName("wind_gusts_10m") val windGusts: List<Double?> = emptyList()
)

data class DailyWeather(
    val time: List<String> = emptyList(),
    @SerializedName("weather_code") val weatherCode: List<Int?> = emptyList(),
    @SerializedName("temperature_2m_max") val tempMax: List<Double?> = emptyList(),
    @SerializedName("temperature_2m_min") val tempMin: List<Double?> = emptyList(),
    @SerializedName("precipitation_probability_max") val rainChance: List<Double?> = emptyList(),
    @SerializedName("precipitation_sum") val rainSum: List<Double?> = emptyList(),
    @SerializedName("wind_gusts_10m_max") val windGustsMax: List<Double?> = emptyList()
)
