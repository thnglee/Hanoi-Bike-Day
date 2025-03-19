// weather-related data the app fetches and processes
package com.example.HanoiBikeDay.data

data class WeatherResponse(
    val list: List<ForecastItem>,
    val city: City
)

data class City(
    val name: String,
    val country: String
)

data class ForecastItem(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val dt_txt: String
)

data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int
)

data class Wind(
    val speed: Double,
    val deg: Int
)

data class Weather(
    val main: String,
    val description: String,
    val icon: String
)

// Keep this for compatibility with existing code
data class DailyForecast(
    val dt: Long,
    val temp: Temperature,
    val humidity: Int,
    val wind_speed: Double,
    val weather: List<Weather>,
    val bikeRideScore: Int = 0 // Add bike ride score with default value
)

data class Temperature(
    val day: Double,
    val min: Double,
    val max: Double
)

// Add this to your existing data classes
data class HourlyForecast(
    val dt: Long,
    val temp: Double,
    val humidity: Int,
    val wind_speed: Double,
    val weather: List<Weather>,
    val bikeRideScore: Int = 0
) 