package com.example.helloworld


import android.app.Application
import androidx.room.Room
import com.example.helloworld.api.CbrApiService
import com.example.helloworld.database.AppDatabase
import com.example.helloworld.GameViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

val appModule = module {

    single {
        Retrofit.Builder()
            .baseUrl("https://www.cbr.ru/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(CbrApiService::class.java)
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    single { get<AppDatabase>().appDao() }

    viewModel { GameViewModel(get(), get()) }
}