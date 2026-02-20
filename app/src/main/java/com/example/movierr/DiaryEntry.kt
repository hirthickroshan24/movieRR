package com.example.movierr

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val movieTitle: String,
    val rating: Float,
    val review: String,
    val date: String // We can store it as a String for simplicity
)
