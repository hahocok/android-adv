package com.android.android;

import android.view.View;

import java.util.Random;

public final class MainPresenter {
    private static MainPresenter instance = null;
    private static final Object syncObj = new Object();

    private String city;
    private String temperature;
    private boolean isHumidity;

    private MainPresenter(){
        temperature = "0";
    }

    public void setTemperature(String value){
        temperature = value;
    }

    public void setRandomTemperature() {
        temperature = getRandomTemp();
    }

    public String getTemperature(){
        return temperature;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isHumidity() {
        return isHumidity;
    }

    public void setHumidity(boolean humidity) {
        isHumidity = humidity;
    }

    public void setHumidity(View view) {
        isHumidity = isVisible(view);
    }

    public static MainPresenter getInstance(){
        synchronized (syncObj) {
            if (instance == null) {
                instance = new MainPresenter();
            }
            return instance;
        }
    }

    private String getRandomTemp() {
        int min = -30;
        int max = 30;
        int diff = max - min;
        Random random = new Random();
        int i = random.nextInt(diff + 1);
        i += min;
        return String.valueOf(i);
    }

    private boolean isVisible(View view) {
        int isVisible = view.getVisibility();
        return isVisible == View.VISIBLE;
    }
}
