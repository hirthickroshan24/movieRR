package com.example.movierr

data class ReviewModel(
    val id: String = "",
    val movieName: String = "",
    val reviewText: String = "",
    val rating: Float = 0f,
    val timestamp: Long = 0
)
