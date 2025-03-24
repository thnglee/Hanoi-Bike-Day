package com.example.HanoiBikeDay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.HanoiBikeDay.data.DailyForecast
import com.example.HanoiBikeDay.data.Weather
import com.example.HanoiBikeDay.data.AirQualityData
import com.example.HanoiBikeDay.data.AirComponents
import com.example.HanoiBikeDay.ui.theme.HanoiBikeDayTheme
import com.example.HanoiBikeDay.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.border
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HanoiBikeDayTheme {
                WeatherScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1A1A1A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Hanoi",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            when {
                viewModel.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                viewModel.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = Color.Red,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = viewModel.error ?: "",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Button(onClick = { viewModel.retryLoading() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.weatherState) { forecast ->
                            WeatherCard(
                                forecast = forecast,
                                airQuality = viewModel.airQualityState
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherCard(
    forecast: DailyForecast,
    airQuality: AirQualityData? = null
) {
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    val date = Date(forecast.dt * 1000)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D2D2D)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = dateFormat.format(date),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Temperature and details
                WeatherDetails(
                    temp = forecast.temp.day,
                    humidity = forecast.humidity,
                    windSpeed = forecast.wind_speed
                )

                // Weather icon and description
                forecast.weather.firstOrNull()?.let { weather ->
                    WeatherIcon(weather = weather)
                }
                
                // Bike score
                BikeScoreIndicator(forecast.bikeRideScore)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Air Quality Bar
            airQuality?.let { AirQualityBar(it) }
        }
    }
}

@Composable
fun WeatherDetails(temp: Double, humidity: Int, windSpeed: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "${temp.toInt()}°C",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Humid ${humidity}%",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Wind ${windSpeed.toInt()}km/h",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun WeatherIcon(weather: Weather) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.width(80.dp)
    ) {
        val iconRes = when (weather.main.lowercase()) {
            "clear" -> R.drawable.ic_clear
            "clouds" -> R.drawable.ic_clouds
            "rain" -> R.drawable.ic_rain
            "thunderstorm" -> R.drawable.ic_thunderstorm
            else -> R.drawable.ic_unknown
        }
        
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = weather.description,
            modifier = Modifier.size(48.dp)
        )
        
        Text(
            text = weather.description.capitalize(),
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BikeScoreIndicator(score: Int) {
    val (gradient, description) = when (score) {
        in 0..20 -> Pair(
            Brush.linearGradient(
                colors = listOf(Color(0xFFFF1744), Color(0xFFD50000))
            ),
            "Poor"
        )
        in 21..40 -> Pair(
            Brush.linearGradient(
                colors = listOf(Color(0xFFFF9100), Color(0xFFF57C00))
            ),
            "Fair"
        )
        in 41..60 -> Pair(
            Brush.linearGradient(
                colors = listOf(Color(0xFFFFD600), Color(0xFFFBC02D))
            ),
            "Moderate"
        )
        in 61..80 -> Pair(
            Brush.linearGradient(
                colors = listOf(Color(0xFF00E676), Color(0xFF00C853))
            ),
            "Good"
        )
        else -> Pair(
            Brush.linearGradient(
                colors = listOf(Color(0xFF00E676), Color(0xFF00B248))
            ),
            "Perfect"
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.width(80.dp)  // Match weather icon width
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)  // Match weather icon size
                .clip(CircleShape)
                .background(gradient)
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {


            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = "$score",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge  // Smaller text size
                )
                Text(
                    text = "%",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 1.dp)
                )
            }
        }
        
        Text(
            text = description,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AirQualityBar(airQuality: AirQualityData) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Air Quality Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Air Quality Index",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${airQuality.aqi} - ${airQuality.qualityLevel}",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Gradient Bar with Marker
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)  // Increased height
        ) {
            // Gradient Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4CAF50),    // Good (Green)
                                Color(0xFFFDD835),    // Moderate (Yellow)
                                Color(0xFFFF9800),    // Unhealthy for Sensitive Groups (Orange)
                                Color(0xFFE53935),    // Unhealthy (Red)
                                Color(0xFF8E24AA)     // Very Unhealthy (Purple)
                            )
                        )
                    )
            )
            
            // Marker position calculation
            val markerPosition = remember(airQuality.aqi) {
                when (airQuality.aqi) {
                    in 0..50 -> 0.1f      // Good (left)
                    in 51..100 -> 0.3f    // Moderate
                    in 101..150 -> 0.5f   // Unhealthy for Sensitive Groups
                    in 151..200 -> 0.7f   // Unhealthy
                    else -> 0.9f          // Very Unhealthy (right)
                }
            }
            
            // Enhanced marker
            Box(
                modifier = Modifier
                    .offset(
                        x = with(LocalDensity.current) {
                            (markerPosition * (LocalConfiguration.current.screenWidthDp - 32)).dp - 8.dp
                        }
                    )
            ) {
                // Outer circle (white border)
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
                ) {
                    // Inner circle (colored based on AQI)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                when (airQuality.aqi) {
                                    in 0..50 -> Color(0xFF4CAF50)      // Good
                                    in 51..100 -> Color(0xFFFDD835)    // Moderate
                                    in 101..150 -> Color(0xFFFF9800)   // Unhealthy for Sensitive
                                    in 151..200 -> Color(0xFFE53935)   // Unhealthy
                                    else -> Color(0xFF8E24AA)          // Very Unhealthy
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun AirQualityCard(airQuality: AirQualityData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Air Quality",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                AirQualityIndicator(aqi = airQuality.aqi)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Level: ${airQuality.qualityLevel}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            PollutantLevels(components = airQuality.components)
        }
    }
}

@Composable
fun AirQualityIndicator(aqi: Int) {
    val color = when(aqi) {
        in 0..50 -> Color(0xFF4CAF50)     // Green
        in 51..100 -> Color(0xFFFDD835)    // Yellow
        in 101..150 -> Color(0xFFFF9800)   // Orange
        in 151..200 -> Color(0xFFE53935)   // Red
        in 201..300 -> Color(0xFF8E24AA)   // Purple
        else -> Color(0xFF880E4F)          // Dark Purple
    }
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$aqi",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PollutantLevels(components: AirComponents) {
    Column {
        PollutantItem("PM2.5", components.pm2_5, "μg/m³")
        PollutantItem("PM10", components.pm10, "μg/m³")
        PollutantItem("O₃", components.o3, "μg/m³")
        PollutantItem("NO₂", components.no2, "μg/m³")
        PollutantItem("CO", components.co, "μg/m³")
    }
}

@Composable
fun PollutantItem(name: String, value: Double, unit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = name, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "%.1f %s".format(value, unit),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}