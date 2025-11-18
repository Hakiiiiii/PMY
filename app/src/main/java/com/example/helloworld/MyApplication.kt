package com.example.helloworld

import android.app.Application
import androidx.room.Room
import com.example.helloworld.api.CbrApiService
import com.example.helloworld.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class MyApplication : Application() {

    companion object {
        lateinit var database: AppDatabase
        lateinit var cbrApi: CbrApiService
    }

    override fun onCreate() {
        super.onCreate()

        // Room
        database = Room.databaseBuilder(this, AppDatabase::class.java, "app_db").build()

        // Retrofit (для текста XML)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.cbr.ru/")
            .addConverterFactory(ScalarsConverterFactory.create()) // ← ВАЖНО: Scalars!
            .build()

        cbrApi = retrofit.create(CbrApiService::class.java)

        // Koin
        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}

