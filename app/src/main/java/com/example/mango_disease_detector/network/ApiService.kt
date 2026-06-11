package com.example.mango_disease_detector.network

import com.example.mango_disease_detector.model.PredictionResponse
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

object ApiService {
    private val gson = Gson()

    fun predict(imageFile: File): PredictionResponse {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                imageFile.name,
                imageFile.readBytes().toRequestBody("image/*".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url("${ApiClient.BASE_URL}/predict")
            .post(requestBody)
            .build()

        ApiClient.client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Server error: ${response.code}")
            val body = response.body?.string() ?: throw Exception("Empty response")
            return gson.fromJson(body, PredictionResponse::class.java)
        }
    }
}
