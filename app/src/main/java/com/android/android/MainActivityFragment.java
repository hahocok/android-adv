package com.android.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.android.cards.SocSource;
import com.android.android.model.WeatherRequest;
import com.android.android.services.ServiceReadWeather;
import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivityFragment extends Fragment implements Constants {

    private MainPresenter presenter;

    private View mainCityContainer;
    private View mainHumidityContainer;

    private TextView mainCity;
    private TextView mainTemperature;
    private TextView mainPressure;
    private TextView mainHumidity;
    private TextView mainWindSpeed;

    private ServiceFinishedReceiver serviceFinishedReceiver = new ServiceFinishedReceiver();

    private class ServiceFinishedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayWeather(intent);
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity_main, container, false);

        presenter = MainPresenter.getInstance();

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

    private void saveToPreference(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();

        String city = presenter.getCity();
        editor.putString(CITY, city);
        editor.apply();
    }

    private void readFromPreference(SharedPreferences preferences) {
        String city = preferences.getString(CITY, "");
        presenter.setCity(city);
        mainCity.setText(city);
        getWeather();
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
        ServiceReadWeather.startService(getContext(), presenter.getCity());
    }

    private void displayWeather(Intent intent){
        final WeatherRequest weatherRequest = (WeatherRequest) intent.getSerializableExtra(INTENT_RESPONSE);
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
        getActivity().registerReceiver(serviceFinishedReceiver, new IntentFilter(BROADCAST_RESPONSE_WEATHER));
    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(serviceFinishedReceiver);
        super.onStop();
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
