package com.example.HanoiBikeDay.viewmodel

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context
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

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun calculateBikeRideScore(forecast: DailyForecast): Int {
        // Temperature score (0-40 points)
        // Ideal temperature range: 18-25°C
        val tempScore = when (forecast.temp.day) {
            in Double.NEGATIVE_INFINITY..5.0 -> 0 // Too cold
            in 5.0..10.0 -> 10
            in 10.0..15.0 -> 20
            in 15.0..18.0 -> 30
            in 18.0..25.0 -> 40 // Ideal
            in 25.0..30.0 -> 30
            in 30.0..35.0 -> 15
            else -> 0 // Too hot
        }
        
        // Rain chance score (0-40 points)
        // Based on weather condition
        val weatherMain = forecast.weather.firstOrNull()?.main ?: ""
        val rainScore = when {
            weatherMain.contains("Rain", ignoreCase = true) -> 0
            weatherMain.contains("Drizzle", ignoreCase = true) -> 10
            weatherMain.contains("Thunderstorm", ignoreCase = true) -> 0
            weatherMain.contains("Snow", ignoreCase = true) -> 5
            weatherMain.contains("Mist", ignoreCase = true) || 
            weatherMain.contains("Fog", ignoreCase = true) -> 20
            weatherMain.contains("Clouds", ignoreCase = true) -> {
                // Check description for cloud coverage
                val description = forecast.weather.firstOrNull()?.description ?: ""
                when {
                    description.contains("scattered", ignoreCase = true) -> 35
                    description.contains("few", ignoreCase = true) -> 38
                    description.contains("broken", ignoreCase = true) -> 30
                    description.contains("overcast", ignoreCase = true) -> 25
                    else -> 30
                }
            }
            weatherMain.contains("Clear", ignoreCase = true) -> 40
            else -> 20 // Default for unknown conditions
        }
        
        // Wind speed score (0-20 points)
        // Ideal wind speed: 0-10 km/h (0-2.8 m/s)
        val windScore = when (forecast.wind_speed) {
            in 0.0..2.8 -> 20 // Ideal (0-10 km/h)
            in 2.8..5.5 -> 15 // Light breeze (10-20 km/h)
            in 5.5..8.0 -> 10 // Moderate breeze (20-29 km/h)
            in 8.0..10.8 -> 5 // Fresh breeze (29-39 km/h)
            else -> 0 // Strong wind, not good for biking
        }
        
        // Calculate total score (0-100)
        return tempScore + rainScore + windScore
    }

    private fun fetchWeather() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = weatherService.getWeatherForecastByCity(
                    apiKey = "969774c06ad65b09487e1226b52b7ee0" // Using your API key
                )
                
                // Convert forecast items to daily forecasts
                // Group forecast items by day and take the first item for each day
                val dailyForecasts = response.list
                    .groupBy { item -> 
                        // Group by day (truncate timestamp to day)
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = item.dt * 1000
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        calendar.timeInMillis
                    }
                    .map { (_, items) -> 
                        // Convert the first item of each day to DailyForecast
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
                            weather = item.weather
                        )
                    }
                    .take(7) // Take only 7 days
                    .map { forecast ->
                        // Calculate bike ride score for each forecast
                        forecast.copy(bikeRideScore = calculateBikeRideScore(forecast))
                    }
                
                weatherState = dailyForecasts
                error = null
            } catch (e: Exception) {
                error = "Failed to load weather data: ${e.message}"
                e.printStackTrace()
            }
            isLoading = false
        }
    }
} 