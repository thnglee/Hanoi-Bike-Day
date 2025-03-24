package com.example.HanoiBikeDay.viewmodel

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.HanoiBikeDay.data.*
import com.example.HanoiBikeDay.network.WeatherService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class WeatherViewModel : ViewModel() {
    var weatherState by mutableStateOf<List<DailyForecast>>(emptyList())
        private set
    var airQualityState by mutableStateOf<AirQualityData?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private val weatherService = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherService::class.java)

    init {
        fetchWeather()
        fetchAirQuality()
    }

    private fun calculateBikeRideScore(
        temp: Double,
        humidity: Int,
        windSpeed: Double,
        weatherMain: String,
        weatherDescription: String,
        airQuality: Int?
    ): Int {
        val tempScore = when (temp) {
            in Double.NEGATIVE_INFINITY..5.0 -> 0
            in 5.0..10.0 -> 10
            in 10.0..15.0 -> 20
            in 15.0..18.0 -> 30
            in 18.0..25.0 -> 40
            in 25.0..30.0 -> 30
            in 30.0..35.0 -> 15
            else -> 0
        }

        val rainScore = when {
            weatherMain.contains("Rain", ignoreCase = true) -> 0
            weatherMain.contains("Drizzle", ignoreCase = true) -> 10
            weatherMain.contains("Thunderstorm", ignoreCase = true) -> 0
            weatherMain.contains("Snow", ignoreCase = true) -> 5
            weatherMain.contains("Mist", ignoreCase = true) || 
            weatherMain.contains("Fog", ignoreCase = true) -> 20
            weatherMain.contains("Clouds", ignoreCase = true) -> when {
                weatherDescription.contains("scattered", ignoreCase = true) -> 35
                weatherDescription.contains("few", ignoreCase = true) -> 38
                weatherDescription.contains("broken", ignoreCase = true) -> 30
                weatherDescription.contains("overcast", ignoreCase = true) -> 25
                else -> 30
            }
            weatherMain.contains("Clear", ignoreCase = true) -> 40
            else -> 20
        }

        val windScore = when (windSpeed) {
            in 0.0..2.8 -> 20
            in 2.8..5.5 -> 15
            in 5.5..8.0 -> 10
            in 8.0..10.8 -> 5
            else -> 0
        }

        val humidityPenalty = if (humidity > 85) -10 else 0
        val aqiPenalty = when(airQuality) {
            null -> 0
            in 0..50 -> 0
            in 51..100 -> -5
            in 101..150 -> -10
            in 151..200 -> -15
            else -> -20
        }

        return (tempScore + rainScore + windScore + humidityPenalty + aqiPenalty)
            .coerceIn(0, 100)
    }

    private fun fetchWeather() {
        viewModelScope.launch {
            isLoading = true
            try {
                Log.d("WeatherViewModel", "Fetching weather data...")
                val response = weatherService.getWeatherForecastByCity(
                    city = "Hanoi,vn",
                    units = "metric",
                    apiKey = "969774c06ad65b09487e1226b52b7ee0"
                )
                Log.d("WeatherViewModel", "Weather data fetched successfully: ${response.list.size} items")
                if (response.list.isEmpty()) {
                    error = "No weather data available"
                    return@launch
                }
                processWeatherResponse(response)
                error = null
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error fetching weather data: ${e.message}", e)
                error = "Failed to load weather data: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun fetchAirQuality() {
        viewModelScope.launch {
            try {
                val response = weatherService.getAirQuality(
                    apiKey = "969774c06ad65b09487e1226b52b7ee0"
                )
                processAirQualityResponse(response)
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error fetching air quality: ${e.message}", e)
                error = "Failed to load air quality data: ${e.message}"
            }
        }
    }

    private fun processWeatherResponse(response: WeatherResponse) {
        try {
            Log.d("WeatherViewModel", "Processing weather response: ${response.list.size} items")
            val groupedForecasts = response.list.groupBy { getDayTimestamp(it.dt) }
            Log.d("WeatherViewModel", "Grouped into ${groupedForecasts.size} days")
            
            weatherState = groupedForecasts.map { (timestamp, items) -> 
                val item = items.first()
                Log.d("WeatherViewModel", "Processing forecast for timestamp: ${Date(timestamp)}")
                
                DailyForecast(
                    dt = item.dt,
                    temp = Temperature(
                        day = item.main.temp,
                        min = item.main.temp_min,
                        max = item.main.temp_max
                    ),
                    humidity = item.main.humidity,
                    wind_speed = item.wind.speed,
                    weather = item.weather,
                    bikeRideScore = calculateBikeRideScore(
                        temp = item.main.temp,
                        humidity = item.main.humidity,
                        windSpeed = item.wind.speed,
                        weatherMain = item.weather.firstOrNull()?.main ?: "",
                        weatherDescription = item.weather.firstOrNull()?.description ?: "",
                        airQuality = airQualityState?.aqi
                    )
                )
            }
            Log.d("WeatherViewModel", "Processed weather state: ${weatherState.size} forecasts")
        } catch (e: Exception) {
            Log.e("WeatherViewModel", "Error processing weather response", e)
            error = "Error processing weather data: ${e.message}"
        }
    }

    private fun processAirQualityResponse(response: AirQualityResponse) {
        response.list.firstOrNull()?.let { item ->
            Log.d("WeatherViewModel", "Air Quality AQI: ${item.main.aqi}")
            airQualityState = AirQualityData(
                aqi = item.main.aqi,
                components = item.components
            )
        }
    }

    private fun getDayTimestamp(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp * 1000
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun retryLoading() {
        fetchWeather()
        fetchAirQuality()
    }
} 