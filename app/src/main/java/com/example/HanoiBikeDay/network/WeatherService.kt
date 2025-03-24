package com.example.HanoiBikeDay.network
// making networks request to fetch weather data - using Retrofit2

import com.example.HanoiBikeDay.data.WeatherResponse
import com.example.HanoiBikeDay.data.AirQualityResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("data/2.5/forecast")
    suspend fun getWeatherForecastByCity(
        @Query("q") city: String = "Hanoi,vn",
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): WeatherResponse

    @GET("data/2.5/air_pollution")
    suspend fun getAirQuality(
        @Query("lat") lat: Double = 21.0285, // Hanoi latitude

        @Query("lon") lon: Double = 105.8542, // Hanoi longitude
        @Query("appid") apiKey: String
    ): AirQualityResponse
}