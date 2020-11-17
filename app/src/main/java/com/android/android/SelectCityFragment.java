package com.android.android;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SelectCityFragment extends Fragment implements Constants {

    private Button btnSelectCity;
    private EditText etSelectCity;
    private CheckBox cbHumidity;
    private MainPresenter presenter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_select_city, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        btnSelectCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = etSelectCity.getText().toString();
                boolean isHumidity = cbHumidity.isChecked();

                presenter.setCity(city);
                presenter.setRandomTemperature();
                presenter.setHumidity(isHumidity);

                saveToPreference(getActivity().getPreferences(Context.MODE_PRIVATE));

                Snackbar.make(v, "Применить изменения?", Snackbar.LENGTH_LONG).setAction("ОК", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideKeyboard(getContext(), view);
                        getFragmentManager().
                                beginTransaction()
                                .replace(R.id.fragment_container, new MainActivityFragment())
                                .addToBackStack("")
                                .commit();
                    }
                }).show();
            }
        });

        readIntent();
    }

    private void saveToPreference(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();

        String city = presenter.getCity();
        editor.putString(CITY, city);
        editor.apply();
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        presenter.setCity(etSelectCity.getText().toString());
        presenter.setHumidity(cbHumidity.isChecked());
        super.onSaveInstanceState(outState);
    }

    private void initViews(View view) {
        presenter = MainPresenter.getInstance();
        btnSelectCity = view.findViewById(R.id.btn_select_city);
        cbHumidity = view.findViewById(R.id.cb_humidity);
        etSelectCity = view.findViewById(R.id.et_select_city);
    }

    private void readIntent() {
        String city = presenter.getCity();
        boolean isHumidity = presenter.isHumidity();

        if (city != null && city.equals(getResources().getString(R.string.city))) {
            etSelectCity.setHint(getResources().getString(R.string.enter_city));
        } else {
            etSelectCity.setText(city);
        }

        cbHumidity.setChecked(isHumidity);
    }
}
