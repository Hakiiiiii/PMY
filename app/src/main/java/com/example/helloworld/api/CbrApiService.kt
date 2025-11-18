package com.example.helloworld.api

import retrofit2.Call
import retrofit2.http.GET

interface CbrApiService {
    @GET("scripts/XML_daily.asp")
    fun getGoldRate(): Call<String>
}