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