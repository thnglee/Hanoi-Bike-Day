package com.example.HanoiBikeDay.network
// making networks request to fetch weather data - using Retrofit2

import com.example.HanoiBikeDay.data.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("data/2.5/forecast")
    suspend fun getWeatherForecastByCity(
        @Query("q") city: String = "Hanoi,vn",
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): WeatherResponse
} 