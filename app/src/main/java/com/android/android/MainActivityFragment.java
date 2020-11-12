package com.android.android;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.android.cards.SocSource;
import com.android.android.model.WeatherRequest;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivityFragment extends Fragment implements Constants {

    private static final String TAG = "WEATHER";
    private static final String WEATHER_URL_START = "https://api.openweathermap.org/data/2.5/weather?q=";
    private static final String WEATHER_URL_END = ",RU&appid=";
    private static final String WEATHER_API_KEY = "68c65e3c4c42de33f8a67466a1719a08";

    private MainPresenter presenter;

    private View mainCityContainer;
    private View mainHumidityContainer;

    private TextView mainCity;
    private TextView mainTemperature;
    private TextView mainPressure;
    private TextView mainHumidity;
    private TextView mainWindSpeed;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity_main, container, false);

        presenter = MainPresenter.getInstance();

        initViews(view);

        mainCityContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectCityFragment selectCityFragment = new SelectCityFragment();

                Bundle bundle = new Bundle();
                bundle.putString(CITY, mainCity.getText().toString());
                bundle.putBoolean(HUMIDITY, isVisible(mainHumidityContainer));

                selectCityFragment.setArguments(bundle);

                getFragmentManager().
                        beginTransaction()
                        .replace(R.id.fragment_container, selectCityFragment)
                        .commit();

            }
        });


        mainTemperature.setText(presenter.getTemperature());

        SocSource sourceData = new SocSource(getResources());
        initRecyclerView(view, sourceData.build());


        readIntent();
        return view;
    }

    private void initViews(View view) {
        mainCityContainer = view.findViewById(R.id.main_city_container);
        mainHumidityContainer = view.findViewById(R.id.main_humidity_container);

        mainCity = view.findViewById(R.id.main_city);
        mainTemperature = view.findViewById(R.id.main_temperature);
        mainPressure = view.findViewById(R.id.main_pressure);
        mainHumidity = view.findViewById(R.id.main_humidity);
        mainWindSpeed = view.findViewById(R.id.main_wind_speed);
    }

    private void getWeather() {
        final URL uri = getUrl();
        final Handler handler = new Handler(); // Запоминаем основной поток
        new Thread(new Runnable() {
            public void run() {
                HttpsURLConnection urlConnection = null;
                try {
                    urlConnection = (HttpsURLConnection) uri.openConnection();
                    urlConnection.setRequestMethod("GET"); // установка метода получения данных -GET
                    urlConnection.setReadTimeout(10000); // установка таймаута - 10 000 миллисекунд
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream())); // читаем  данные в поток
                    String result = getLines(in);
                    // преобразование данных запроса в модель
                    Gson gson = new Gson();
                    final WeatherRequest weatherRequest = gson.fromJson(result, WeatherRequest.class);
                    // Возвращаемся к основному потоку
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            displayWeather(weatherRequest);
                        }
                    });
                } catch (FileNotFoundException e) {
                    Snackbar.make(getView(), getResources().getString(R.string.error_city_not_found), Snackbar.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e(TAG, getResources().getString(R.string.error_fail_connection), e);
                    e.printStackTrace();
                } finally {
                    if (null != urlConnection) {
                        urlConnection.disconnect();
                    }
                }
            }
        }).start();
    }

    private URL getUrl() {
        String city;
        if (getArguments() != null) {
            city = getArguments().getString(CITY);
        } else {
            city = "";
        }
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

    private void displayWeather(WeatherRequest weatherRequest){
        mainCity.setText(weatherRequest.getName());
        mainTemperature.setText(String.format("%f2", weatherRequest.getMain().getTemp()));
        mainPressure.setText(String.format("%d", weatherRequest.getMain().getPressure()));
        mainHumidity.setText(String.format("%d", weatherRequest.getMain().getHumidity()));
        mainWindSpeed.setText(String.format("%d", weatherRequest.getWind().getSpeed()));
    }

    private void initRecyclerView(View view, SocSource data){
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);

        // Эта установка служит для повышения производительности системы
        recyclerView.setHasFixedSize(true);

        // Будем работать со встроенным менеджером
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        // Установим адаптер
        SocnetAdapter adapter = new SocnetAdapter(data);
        recyclerView.setAdapter(adapter);

        // Добавим разделитель карточек
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(),  LinearLayoutManager.HORIZONTAL);
        itemDecoration.setDrawable(getActivity().getDrawable(R.drawable.separator));
        recyclerView.addItemDecoration(itemDecoration);

    }


    private boolean isVisible(View view) {
        int isVisible = view.getVisibility();
        if (isVisible == View.VISIBLE) {
            return true;
        } else {
            return false;
        }
    }

    private void readIntent() {
        Bundle args = getArguments();
        String city = null;
        boolean isCloudiness = false;
        boolean isHumidity = false;
        if (args != null) {
            city = args.getString(CITY);
            isCloudiness = args.getBoolean(CLOUDINESS, false);
            isHumidity = args.getBoolean(HUMIDITY, false);

            if (city != null && city.equals(getResources().getString(R.string.city))) {
                mainCity.setHint(getResources().getString(R.string.enter_city));
            } else {
                mainCity.setText(city);
                mainCity.setGravity(Gravity.CENTER_HORIZONTAL);
            }

            mainTemperature.setText(presenter.getTemperature());

            if (isHumidity) {
                mainHumidityContainer.setVisibility(View.VISIBLE);
            } else {
                mainHumidityContainer.setVisibility(View.GONE);
            }
        } else {
            mainHumidityContainer.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        readIntent();
        getWeather();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        presenter.setTemperature(mainTemperature.getText().toString());
        Toast.makeText(getContext(), "onSaveInstanceState()", Toast.LENGTH_SHORT).show();
        Log.d(LOG_TAG, "onSaveInstanceState()");
    }
}
