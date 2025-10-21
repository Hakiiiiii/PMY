package com.example.helloworld.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val name: String, // ФИО как ключ
    val difficulty: Int,
    val date: String  // Дата записи
)