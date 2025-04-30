package com.example.tomatoleafcare.model

import java.io.Serializable

data class History(
    val id: Long = 0,
    val diseaseName: String,
    val date: String,
    val imagePath: String
): Serializable