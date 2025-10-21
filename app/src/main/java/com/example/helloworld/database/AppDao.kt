package com.example.helloworld.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Обновление при конфликте
    fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertScore(score: Score)

    @Query("SELECT * FROM scores ORDER BY score DESC")
    fun getAllScores(): List<Score>

    @Query("UPDATE scores SET score = :newScore WHERE userName = :name AND :newScore > score")
    fun updateRecord(name: String, newScore: Int) // Добавлен параметр newScore
}