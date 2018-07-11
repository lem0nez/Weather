/*
 * Copyright © 2018 Nikita Dudko. All rights reserved.
 * Contacts: <nikita.dudko.95@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.a95gmail.dudko.nikita.weather;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.a95gmail.dudko.nikita.weather.dialog.EnterCityDialog;
import com.a95gmail.dudko.nikita.weather.dialog.ErrorDialog;
import com.a95gmail.dudko.nikita.weather.dialog.LoadingDialog;

import java.util.Objects;

public class StartActivity extends AppCompatActivity {
    private static final String TAG_DIALOG_LOADING = "FRAGMENT_LOADING";
    private static final String TAG_DIALOG_ERROR = "DIALOG_ERROR";
    private static final String TAG_DIALOG_ENTER_CITY = "DIALOG_ENTER_CITY";
    private static final int LOCATION_REQUEST_CODE = 1;

    private TextView mTextViewAction;
    private LocationManager mLocationManager;
    private LoadingDialog mLocateDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start);

        mTextViewAction = findViewById(R.id.text_view_action);
        Button mButtonLocate = findViewById(R.id.button_locate);
        Button mButtonEnterCity = findViewById(R.id.button_enter_city);

        mButtonLocate.setOnClickListener((view) -> requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE));
        mButtonEnterCity.setOnClickListener((view) -> {
            EnterCityDialog dialog = new EnterCityDialog((cityId) -> {
                SharedPreferences preferences =PreferenceManager
                        .getDefaultSharedPreferences(StartActivity.this);
                SharedPreferences.Editor editor = preferences.edit();

                editor.putInt(Preferences.PREF_CITY_ID, cityId);
                editor.commit();

                startActivity(new Intent(StartActivity.this, MainActivity.class));
                finish();
            });
            dialog.show(getFragmentManager(), TAG_DIALOG_ENTER_CITY);
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // If user clicked on the "Locate" button.
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE :
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    mTextViewAction.setText(R.string.action_need_location_permission);
                    mTextViewAction.setTextColor(getColor(R.color.color_red));
                } else {
                    mLocateDialog = new LoadingDialog(getString(R.string.loading_locate));
                    mLocateDialog.show(getFragmentManager(), TAG_DIALOG_LOADING);

                    mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    Objects.requireNonNull(mLocationManager).requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, 5000, 2000, locationListener);

                    mLocateDialog.setOnDismissListener(() -> mLocationManager.removeUpdates(locationListener));
                }
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            FragmentManager fragmentManager = getFragmentManager();
            // Only dismiss, because for the callback onDismiss() set the listener for remove updates.
            mLocateDialog.dismiss();

            LoadingDialog loadingDialog = new LoadingDialog(getString(R.string.loading));
            loadingDialog.show(fragmentManager, TAG_DIALOG_LOADING);

            new WeatherProvider(StartActivity.this).requestCityIdByCoordinates(
                    location.getLatitude(), location.getLongitude(), (cityId, responseStatusCode) -> {
                        if (cityId == WeatherProvider.INVALID_CITY_ID) {
                            ErrorDialog errorDialog =
                                    new ErrorDialog(getString(R.string.error_fail_locate));
                            errorDialog.show(fragmentManager, TAG_DIALOG_ERROR);
                            return;
                        }

                        SharedPreferences preferences =
                                PreferenceManager.getDefaultSharedPreferences(StartActivity.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt(Preferences.PREF_CITY_ID, cityId);
                        editor.commit();

                        startActivity(new Intent(StartActivity.this, MainActivity.class));
                        finish();
                    });
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };
}
