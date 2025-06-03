package com.example.tomatoleafcare.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class History(
    val id: Long = 0,
    val diseaseName: String,
    val date: String,
    val imagePath: String
) : Parcelable