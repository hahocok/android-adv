package com.android.android.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.android.Constants;
import com.android.android.model.WeatherRequest;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import androidx.annotation.Nullable;

public class ServiceReadWeather extends IntentService implements Constants {

    public ServiceReadWeather() {
        super("ServiceReadWeather");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String city = getCity(intent);
            final WeatherRequest wr = getWeather(city);

            Intent broadcastIntent = new Intent(BROADCAST_RESPONSE_WEATHER);
            broadcastIntent.putExtra(INTENT_RESPONSE, wr);
            sendBroadcast(broadcastIntent);
        }
    }

    public static void startService(Context context, String city) {
        if (city != null && !city.isEmpty()) {
            Intent intent = new Intent(context, ServiceReadWeather.class);
            intent.putExtra(CITY, city);
            context.startService(intent);
        }
    }

    public String  getCity(Intent intent) {
        return intent.getStringExtra(CITY);
    }

    private WeatherRequest getWeather(String city) {
        final URL uri = getUrl(city);
        return getDataFromServer(uri);
    }

    private WeatherRequest getDataFromServer(URL uri) {
        HttpsURLConnection urlConnection = null;
        try {
            urlConnection = (HttpsURLConnection) uri.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String result = getLines(in);
            Gson gson = new Gson();
            final WeatherRequest weatherRequest = gson.fromJson(result, WeatherRequest.class);
            Log.e(TAG, "weatherRequest.getName = " + weatherRequest.getName());
            return weatherRequest;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != urlConnection) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    private URL getUrl(String city) {
        String sb = WEATHER_URL_START +
                city +
                WEATHER_URL_END;

        URL uri = null;
        try {
            uri = new URL(sb + WEATHER_API_KEY);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return uri;
    }

    private String getLines(BufferedReader in) {
        return in.lines().collect(Collectors.joining("\n"));
    }
}
