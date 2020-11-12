package com.android.android;

public final class MainPresenter {
    private static MainPresenter instance = null;
    private static final Object syncObj = new Object();

    // Это наш счетчик
    private String temperature;

    // Конструктор (вызывать извне его нельзя, поэтому он приватный)
    private MainPresenter(){
        temperature = "0";
    }

    // Увеличение счетчика
    public void setTemperature(String value){
        temperature = value;
    }

    public String getTemperature(){
        return temperature;
    }

    public static MainPresenter getInstance(){
        synchronized (syncObj) {
            if (instance == null) {
                instance = new MainPresenter();
            }
            return instance;
        }
    }
}
