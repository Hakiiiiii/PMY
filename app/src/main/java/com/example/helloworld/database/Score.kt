package com.example.helloworld.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class Score(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userName: String, // ФИО
    val score: Int, // Очки
    val difficulty: Int, // Сложность
    val date: String // Дата
)