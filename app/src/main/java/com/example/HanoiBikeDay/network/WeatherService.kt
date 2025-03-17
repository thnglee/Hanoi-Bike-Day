package com.example.HanoiBikeDay.network
// making networks request to fetch weather data - using Retrofit2

import com.example.HanoiBikeDay.data.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("data/2.5/onecall")
    suspend fun getWeatherForecast(
        @Query("lat") lat: Double = 21.0285,  // Hanoi latitude
        @Query("lon") lon: Double = 105.8542, // Hanoi longitude
        @Query("exclude") exclude: String = "current,minutely,hourly,alerts",
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): WeatherResponse
    
    // Add a new method that uses the weather endpoint
    @GET("data/2.5/forecast")
    suspend fun getWeatherForecastByCity(
        @Query("q") city: String = "Hanoi,vn",
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): WeatherResponse
} 