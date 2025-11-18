package com.example.helloworld.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AppDao {
    @Insert
    suspend fun insertScore(score: Score)

    @Query("SELECT * FROM scores ORDER BY score DESC")
    suspend fun getAllScores(): List<Score>

    @Query("UPDATE scores SET score = :newScore WHERE userName = :userName")
    suspend fun updateRecord(userName: String, newScore: Int)
}