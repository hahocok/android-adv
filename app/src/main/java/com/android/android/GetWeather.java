package com.android.android;

import com.android.android.model.WeatherRequest;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetWeather {
    @GET("data/2.5/weather")
    Call<WeatherRequest> loadWeather(@Query("q") String cityCountry, @Query("appid") String keyApi);
    @GET("data/2.5/weather")
    Call<WeatherRequest> loadWeatherLoc(@Query("lat") String lat, @Query("lon") String lon, @Query("appid") String keyApi);
}
