package com.android.android.cards;

import android.content.res.Resources;

import com.android.android.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SocSource {
    private List<Soc> dataSource;   // строим этот источник данных
    private Resources resources;    // ресурсы приложения

    public SocSource(Resources resources) {
        dataSource = new ArrayList<>(6);
        this.resources = resources;
    }

    public SocSource build(){
        // строки описаний из ресурсов
        String[] days = resources.getStringArray(R.array.days);
        for (int i = 0; i < days.length; i++) {
            dataSource.add(new Soc(days[i], getRandomTemp()));
        }
        return this;
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

    public Soc getSoc(int position) {
        return dataSource.get(position);
    }

    public int size(){
        return dataSource.size();
    }

}
