package com.android.android;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.android.cards.SocSource;
import com.android.android.database.DBHelper;
import com.android.android.database.WeatherTable;
import com.android.android.model.WeatherRequest;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.LOCATION_SERVICE;

public class MainActivityFragment extends Fragment implements Constants {

    private MainPresenter presenter;

    private View mainCityContainer;
    private View mainHumidityContainer;

    private TextView mainCity;
    private TextView mainTemperature;
    private TextView mainPressure;
    private TextView mainHumidity;
    private TextView mainWindSpeed;

    private GetWeather getWeather;
    private SQLiteDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity_main, container, false);

        presenter = MainPresenter.getInstance();

        initDB();
        initRetrofit();
        initViews(view);

        mainCityContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.setCity(mainCity.getText().toString());
                presenter.setHumidity(mainHumidityContainer);

                getFragmentManager().
                        beginTransaction()
                        .replace(R.id.fragment_container, new SelectCityFragment())
                        .addToBackStack("")
                        .commit();

            }
        });


        mainTemperature.setText(presenter.getTemperature());

        SocSource sourceData = new SocSource(getResources());
        initRecyclerView(view, sourceData.build());


        readIntent();
        return view;
    }

    private void requestPermissions() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            requestLocation();
        } else {
            requestLocationPermissions();
        }
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);

        String provider = locationManager.getBestProvider(criteria, true);
        if (provider != null) {
            final int TIME_TO_UPDATE = 10000;   // 10 секунд
            final int DISTANCE_TO_UPDATE = 10;  // 10 метров

            locationManager.getLastKnownLocation(provider);
            locationManager.requestLocationUpdates(provider, TIME_TO_UPDATE, DISTANCE_TO_UPDATE, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    final double lat = location.getLatitude(); // Широта
                    final String latitude = String.format(Locale.getDefault(),"%.2f", lat).replace(',', '.');

                    final double lng = location.getLongitude(); // Долгота
                    final String longitude = String.format(Locale.getDefault(),"%.2f", lng).replace(',', '.');

                    requestRetrofit(latitude, longitude);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            });
        }
    }

    // Запрашиваем Permission’ы для геолокации
    private void requestLocationPermissions() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CALL_PHONE)) {
            // Запрашиваем эти два Permission’а у пользователя
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void readFromPreference(SharedPreferences preferences) {
        String city = preferences.getString(CITY, "");
        presenter.setCity(city);
        mainCity.setText(city);
        getWeather();
    }

    private void initRetrofit() {
        Retrofit retrofit;
        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        getWeather = retrofit.create(GetWeather.class);
    }

    private void requestRetrofit(String city) {
        getWeather.loadWeather(city, WEATHER_API_KEY)
                .enqueue(new Callback<WeatherRequest>() {
                    @Override
                    public void onResponse(Call<WeatherRequest> call, Response<WeatherRequest> response) {
                        if (response.body() != null) {
                            WeatherRequest wr = response.body();
                            saveDataToDB(wr);
                            displayWeather(wr);
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherRequest> call, Throwable t) {
                        Log.e(TAG, "onFailure error : " + t.getMessage());
                    }
                });
    }

    private void requestRetrofit(String lat, String lon) {
        getWeather.loadWeatherLoc(lat, lon, WEATHER_API_KEY)
                .enqueue(new Callback<WeatherRequest>() {
                    @Override
                    public void onResponse(Call<WeatherRequest> call, Response<WeatherRequest> response) {
                        if (response.body() != null) {
                            WeatherRequest wr = response.body();
                            saveDataToDB(wr);
                            displayWeather(wr);
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherRequest> call, Throwable t) {
                        Log.e(TAG, "onFailure error : " + t.getMessage());
                    }
                });
    }

    private void saveDataToDB(WeatherRequest weatherRequest) {
        String city = weatherRequest.getName();
        String data = new Gson().toJson(weatherRequest, WeatherRequest.class);
        List<String> dataFromDB = WeatherTable.getDataFromCity(city, database);

        if (dataFromDB.isEmpty()) {
            WeatherTable.addData(city, data, database);
        } else {
            WeatherTable.editData(city, data, database);
        }

        WeatherTable.addData(weatherRequest.getName(), data, database);
    }

    private void initDB() {
        database = new DBHelper(getContext()).getWritableDatabase();
    }

    private void initViews(View view) {
        mainCityContainer = view.findViewById(R.id.main_city_container);
        mainHumidityContainer = view.findViewById(R.id.main_humidity_container);

        mainCity = view.findViewById(R.id.main_city);
        mainTemperature = view.findViewById(R.id.main_temperature);
        mainPressure = view.findViewById(R.id.main_pressure);
        mainHumidity = view.findViewById(R.id.main_humidity);
        mainWindSpeed = view.findViewById(R.id.main_wind_speed);

        MaterialButton btn = view.findViewById(R.id.btn_sensors);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().
                        beginTransaction()
                        .replace(R.id.fragment_container, new SensorsFragment())
                        .addToBackStack("")
                        .commit();
            }
        });

        readFromPreference(getActivity().getPreferences(Context.MODE_PRIVATE));
    }

    private void getWeather() {
        requestRetrofit(presenter.getCity());
    }

    private void displayWeather(WeatherRequest weatherRequest){
        if (weatherRequest != null) {
            mainCity.setText(weatherRequest.getName());
            mainTemperature.setText(String.format("%f2", weatherRequest.getMain().getTemp()));
            mainPressure.setText(String.format("%d", weatherRequest.getMain().getPressure()));
            mainHumidity.setText(String.format("%d", weatherRequest.getMain().getHumidity()));
            mainWindSpeed.setText(String.format("%f2", weatherRequest.getWind().getSpeed()));
        }
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

    private void readIntent() {
        String city = presenter.getCity();
        boolean isHumidity = presenter.isHumidity();

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


    }

    @Override
    public void onStart() {
        super.onStart();
        requestPermissions();
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
        Log.d(TAG, "onSaveInstanceState()");
    }
}
