package com.example.HanoiBikeDay.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    
    var hourlyWeatherState by mutableStateOf<List<HourlyForecast>>(emptyList())
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
    }

    private fun calculateBikeRideScore(temp: Double, humidity: Int, windSpeed: Double, weatherMain: String, weatherDescription: String): Int {
        // Temperature score (0-40 points)
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
        
        // Rain chance score (0-40 points)
        val rainScore = when {
            weatherMain.contains("Rain", ignoreCase = true) -> 0
            weatherMain.contains("Drizzle", ignoreCase = true) -> 10
            weatherMain.contains("Thunderstorm", ignoreCase = true) -> 0
            weatherMain.contains("Snow", ignoreCase = true) -> 5
            weatherMain.contains("Mist", ignoreCase = true) || 
            weatherMain.contains("Fog", ignoreCase = true) -> 20
            weatherMain.contains("Clouds", ignoreCase = true) -> {
                when {
                    weatherDescription.contains("scattered", ignoreCase = true) -> 35
                    weatherDescription.contains("few", ignoreCase = true) -> 38
                    weatherDescription.contains("broken", ignoreCase = true) -> 30
                    weatherDescription.contains("overcast", ignoreCase = true) -> 25
                    else -> 30
                }
            }
            weatherMain.contains("Clear", ignoreCase = true) -> 40
            else -> 20
        }
        
        // Wind speed score (0-20 points)
        val windScore = when (windSpeed) {
            in 0.0..2.8 -> 20
            in 2.8..5.5 -> 15
            in 5.5..8.0 -> 10
            in 8.0..10.8 -> 5
            else -> 0
        }
        
        // Add humidity impact (-10 points if humidity is very high)
        val humidityPenalty = if (humidity > 85) -10 else 0
        
        return tempScore + rainScore + windScore + humidityPenalty
    }

    private fun fetchWeather() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = weatherService.getWeatherForecastByCity(
                    apiKey = "969774c06ad65b09487e1226b52b7ee0"
                )
                
                // Process hourly forecasts
                val hourlyForecasts = response.list.take(8).map { item ->
                    val weatherMain = item.weather.firstOrNull()?.main ?: ""
                    val weatherDescription = item.weather.firstOrNull()?.description ?: ""
                    
                    HourlyForecast(
                        dt = item.dt,
                        temp = item.main.temp,
                        humidity = item.main.humidity,
                        wind_speed = item.wind.speed,
                        weather = item.weather,
                        bikeRideScore = calculateBikeRideScore(
                            temp = item.main.temp,
                            humidity = item.main.humidity,
                            windSpeed = item.wind.speed,
                            weatherMain = weatherMain,
                            weatherDescription = weatherDescription
                        )
                    )
                }
                
                val dailyForecasts = response.list
                    .groupBy { item -> 
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = item.dt * 1000
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        calendar.timeInMillis
                    }
                    .map { (_, items) -> 
                        val item = items.first()
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
                                weatherDescription = item.weather.firstOrNull()?.description ?: ""
                            )
                        )
                    }
                
                hourlyWeatherState = hourlyForecasts
                weatherState = dailyForecasts
                error = null
            } catch (e: Exception) {
                error = "Failed to load weather data: ${e.message}"
            }
            isLoading = false
        }
    }
} 