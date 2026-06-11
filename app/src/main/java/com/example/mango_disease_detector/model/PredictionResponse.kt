package com.example.mango_disease_detector.model

data class PredictionResponse(
    val disease: String,
    val confidence: Float
)
