package com.example.helloworld.database

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [User::class, Score::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}