# Hanoi Bike Day

A modern Android application that helps cyclists in Hanoi plan their rides based on weather conditions and air quality. The app provides a comprehensive view of daily weather forecasts along with biking suitability scores and air quality information.

## Features

- **Daily Weather Forecast**: Detailed weather information including temperature, humidity, and wind speed
- **Biking Score**: Custom scoring system indicating how suitable the weather is for cycling
- **Air Quality Index**: Real-time air quality measurements with visual indicators
- **Modern UI**: Material Design 3 implementation with a clean, intuitive interface
- **Visual Weather Indicators**: Custom icons for different weather conditions

## Screenshots

[Add screenshots here]

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependencies**:
  - AndroidX Compose
  - Material Design 3
  - Kotlin Coroutines
  - ViewModel
  - LiveData

## Weather Icons

The app uses custom weather icons for the following conditions:
- Clear weather (`ic_clear.png`)
- Cloudy conditions (`ic_clouds.png`)
- Rain (`ic_rain.png`)
- Thunderstorm (`ic_thunderstorm.png`)
- Unknown/Default (`ic_unknown.png`)

## Biking Score System

The biking score is calculated based on various weather parameters and is displayed with color-coded indicators:
- 81-100: Perfect (Green)
- 61-80: Good (Light Green)
- 41-60: Moderate (Yellow)
- 21-40: Fair (Orange)
- 0-20: Poor (Red)

## Air Quality Index

The app displays real-time air quality information with a gradient indicator showing:
- Good (Green)
- Moderate (Yellow)
- Unhealthy for Sensitive Groups (Orange)
- Unhealthy (Red)
- Very Unhealthy (Purple)

## Getting Started

### Prerequisites
- Android Studio Arctic Fox or newer
- Android SDK 21 or higher
- Kotlin 1.9.0 or higher

### Installation
1. Clone the repository:
```bash
git clone https://github.com/yourusername/hanoi-bike-day.git
```

2. Open the project in Android Studio

3. Sync project with Gradle files

4. Run the app on an emulator or physical device

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Weather data provided by [Add your weather data provider]
- Air quality data provided by [Add your AQI data provider]
- Icons and design inspiration [Add credits if applicable] 