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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.HanoiBikeDay.data.DailyForecast
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
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow

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
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.background
        )
    )
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Hanoi Bike Day",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(brush = backgroundGradient)
        ) {
            when {
                viewModel.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                viewModel.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_dialog_alert),
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Best Days for Biking in Hanoi",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(viewModel.weatherState) { forecast ->
                                WeatherCard(forecast = forecast)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherCard(forecast: DailyForecast) {
    val score = forecast.bikeRideScore
    
    // Define gradient colors based on score with improved contrast
    val gradientColors = when (score) {
        in 0..20 -> listOf(Color(0xFFFF5252), Color(0xFFFF1744)) // Deeper red
        in 21..40 -> listOf(Color(0xFFFF9800), Color(0xFFFF6D00)) // Deeper orange
        in 41..60 -> listOf(Color(0xFFFFEB3B), Color(0xFFFFD600)) // Deeper yellow
        in 61..80 -> listOf(Color(0xFF8BC34A), Color(0xFF689F38)) // Deeper light green
        else -> listOf(Color(0xFF4CAF50), Color(0xFF2E7D32)) // Deeper green
    }
    
    val cardBrush = Brush.linearGradient(gradientColors)
    val animatedElevation by animateFloatAsState(
        targetValue = 4f,
        animationSpec = tween(durationMillis = 500),
        label = "elevation"
    )
    
    val weatherIcon = remember(forecast.weather.firstOrNull()?.main) {
        when (forecast.weather.firstOrNull()?.main?.lowercase()) {
            "clear" -> android.R.drawable.ic_menu_day
            "clouds" -> android.R.drawable.ic_menu_compass
            "rain" -> android.R.drawable.ic_menu_info_details
            "snow" -> android.R.drawable.ic_menu_help
            else -> android.R.drawable.ic_menu_report_image
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = animatedElevation.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBrush)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f))
                                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = weatherIcon),
                                contentDescription = forecast.weather.firstOrNull()?.main,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = formatDate(forecast.dt),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = forecast.weather.firstOrNull()?.description?.capitalize() ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    BikeRideScoreIndicator(score = score)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeatherInfoItem(
                        title = "Temperature",
                        value = "${forecast.temp.day.toInt()}°C",
                        subtitle = "Min: ${forecast.temp.min.toInt()}°C / Max: ${forecast.temp.max.toInt()}°C"
                    )
                    
                    WeatherInfoItem(
                        title = "Humidity",
                        value = "${forecast.humidity}%",
                        subtitle = "Relative humidity"
                    )
                    
                    WeatherInfoItem(
                        title = "Wind",
                        value = "${forecast.wind_speed} m/s",
                        subtitle = "${(forecast.wind_speed * 3.6).toInt()} km/h"
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherInfoItem(title: String, value: String, subtitle: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BikeRideScoreIndicator(score: Int) {
    val scoreColor = when (score) {
        in 0..20 -> Color(0xFFFF1744)
        in 21..40 -> Color(0xFFFF6D00)
        in 41..60 -> Color(0xFFFFD600)
        in 61..80 -> Color(0xFF8BC34A)
        else -> Color(0xFF2E7D32)
    }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .border(2.dp, Color.White, CircleShape)
            .padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(scoreColor)
                .border(1.dp, Color.White, CircleShape)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(2.dp)
            ) {
                Text(
                    text = "$score%",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "FOR BIKING",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 7.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
    return sdf.format(Date(timestamp * 1000))
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { 
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
    }
}