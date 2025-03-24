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
    val temp_min: Double,
    val temp_max: Double,
    val humidity: Int
)

data class Wind(
    val speed: Double
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

data class AirQualityData(
    val aqi: Int,                    // Air Quality Index
    val components: AirComponents,
    val qualityLevel: String = calculateAirQualityLevel(aqi)
)

data class AirComponents(
    val co: Double,    // Carbon monoxide (μg/m3)
    val no2: Double,   // Nitrogen dioxide (μg/m3)
    val o3: Double,    // Ozone (μg/m3)
    val pm2_5: Double, // Fine particles (μg/m3)
    val pm10: Double   // Coarse particles (μg/m3)
)

private fun calculateAirQualityLevel(aqi: Int) = when(aqi) {
    in 0..50 -> "Good"
    in 51..100 -> "Moderate"
    in 101..150 -> "Unhealthy for Sensitive Groups"
    in 151..200 -> "Unhealthy"
    in 201..300 -> "Very Unhealthy"
    else -> "Hazardous"
}

data class AirQualityResponse(
    val list: List<AirQualityItem>
)

data class AirQualityItem(
    val main: AirQualityMain,
    val components: AirComponents
)

data class AirQualityMain(
    val aqi: Int
) 