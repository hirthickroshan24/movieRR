package com.example.movierr

import androidx.room.*

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries ORDER BY id DESC")
    suspend fun getAllEntries(): List<DiaryEntry>

    @Query("SELECT * FROM diary_entries WHERE movieTitle = :title LIMIT 1")
    suspend fun getEntryByTitle(title: String): DiaryEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DiaryEntry)

    @Update
    suspend fun updateEntry(entry: DiaryEntry)

    @Delete
    suspend fun deleteEntry(entry: DiaryEntry)
}
