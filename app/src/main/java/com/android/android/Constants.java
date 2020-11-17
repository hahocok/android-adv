package com.android.android;

public interface Constants {
    final String TAG = "WEATHER";

    final String WEATHER_URL_START = "https://api.openweathermap.org/data/2.5/weather?q=";
    final String WEATHER_URL_END = ",RU&appid=";
    final String WEATHER_API_KEY = "68c65e3c4c42de33f8a67466a1719a08";

    final String INTENT_RESPONSE = "com.android.android.intent_response";
    final String BROADCAST_RESPONSE_WEATHER = "com.android.android.broadcast_response_weather";

    final String CITY = "CITY";
    final String CLOUDINESS = "CLOUDINESS";
    final String HUMIDITY = "HUMIDITY";

    final float ABSOLUTE_ZERO = -273.15f;
}
