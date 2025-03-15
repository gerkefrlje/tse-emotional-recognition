package com.example.tse_emotionalrecognition.model

data class DataRow(
    val meanHeartRateNormalized: Double,
    val rmssdNormalized: Double,
    val meanSkinTemperatureNormalized: Double,
    val affect: Long? = null
)
