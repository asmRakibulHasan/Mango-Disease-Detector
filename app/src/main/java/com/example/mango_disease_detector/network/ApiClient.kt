package com.example.mango_disease_detector.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object ApiClient {
    const val BASE_URL = "https://rakibmist-mango-disease-detector.hf.space"

    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
}
