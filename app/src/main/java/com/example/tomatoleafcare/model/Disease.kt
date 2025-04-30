package com.example.tomatoleafcare.model
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize

data class Disease(
    val name: String,
    val description: String,
    val cause: String,
    val symptoms: String,
    val impact: String,
    val solution: String,
    val imageResId: Int
): Parcelable
