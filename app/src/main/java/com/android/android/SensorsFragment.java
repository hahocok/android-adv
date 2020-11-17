package com.android.android;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.content.Context.SENSOR_SERVICE;

public class SensorsFragment extends Fragment {

    private TextView textTemperature;
    private TextView textHumidity;
    private SensorManager sensorManager;
    private Sensor sensorTemperature;
    private Sensor sensorHumidity;


    SensorEventListener listenerTemperature = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            showTemperatureSensors(event);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    SensorEventListener listenerHumidity = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            showHumiditySensors(event);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_sensors, container, false);
        initViews(view);
        getSensors();
        return view;
    }

    private void initViews(View view) {
        textHumidity = view.findViewById(R.id.sensor2View);
        textTemperature = view.findViewById(R.id.sensor1View);
    }

    private void getSensors() {
        sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        sensorTemperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        sensorHumidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
    }

    private void showTemperatureSensors(SensorEvent event) {
        showSensor(event, textTemperature, R.string.sensor_temperature_text);
    }

    private void showHumiditySensors(SensorEvent event) {
        showSensor(event, textHumidity, R.string.sensor_humidity_text);
    }

    private void showSensor(SensorEvent event, TextView view, int resId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getString(resId))
                .append(event.values[0])
                .append("\n")
                .append("==================")
                .append("\n");

        view.setText(stringBuilder);
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(listenerTemperature, sensorTemperature, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listenerHumidity, sensorHumidity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(listenerTemperature, sensorTemperature);
        sensorManager.unregisterListener(listenerHumidity, sensorHumidity);
    }
}
