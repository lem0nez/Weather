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
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.a95gmail.dudko.nikita.weather.dialog.EnterCityDialog;
import com.a95gmail.dudko.nikita.weather.dialog.LoadingDialog;

import java.util.Objects;

public class PreferencesFragment extends PreferenceFragment {
    private static final String TAG_DIALOG_ENTER_CITY = "DIALOG_ENTER_CITY";
    private static final String TAG_DIALOG_LOADING = "DIALOG_LOADING";
    private static final int REQUEST_CODE_LOCATION = 1;

    private SharedPreferences mPreferences;
    private LocationManager mLocationManager;
    private LoadingDialog mLoadingDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        initCustomPreferences();
        updateUpdateIntervalSummary();
    }

    @Override
    public void onStart() {
        super.onStart();
        mPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // If user clicked on the "Locate" preferences item.
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(locationListener);
        }
        mPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener =
            (SharedPreferences sharedPreferences, String key) -> {
        if (key.equals(Preferences.PREF_UPDATE_INTERVAL)) {
            updateUpdateIntervalSummary();
        }
    };


    private void initCustomPreferences() {
        Preference cityPref = findPreference(Preferences.PREF_CITY_ID);
        cityPref.setOnPreferenceClickListener(preference -> {
            new EnterCityDialog((cityId) -> {
                SharedPreferences.Editor editor = mPreferences.edit();

                editor.putInt(Preferences.PREF_CITY_ID, cityId);
                editor.commit();
            }).show(getFragmentManager(), TAG_DIALOG_ENTER_CITY);
            return true;
        });

        Preference locatePref = findPreference(Preferences.PREF_LOCATE);
        locatePref.setOnPreferenceClickListener(preference -> {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION);
            return true;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOCATION :
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mLocationManager = (LocationManager)
                            getActivity().getSystemService(Context.LOCATION_SERVICE);

                    Objects.requireNonNull(mLocationManager).requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, 5000, 2000, locationListener);

                    mLoadingDialog = new LoadingDialog(getActivity().getString(R.string.loading_locate));
                    mLoadingDialog.show(getFragmentManager(), TAG_DIALOG_LOADING);
                    mLoadingDialog.setOnDismissListener(
                            () -> mLocationManager.removeUpdates(locationListener));
                } else {
                    Toast.makeText(getActivity(), R.string.need_location_permission, Toast.LENGTH_LONG)
                            .show();
                }
            default :
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            new WeatherProvider(getActivity()).requestCityIdByCoordinates(
                    location.getLatitude(), location.getLongitude(), cityIdListener);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    private final WeatherProvider.OnResponseCityIdListener cityIdListener =
            (cityId, responseStatusCode) -> {
        if (cityId != WeatherProvider.INVALID_CITY_ID) {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putInt(Preferences.PREF_CITY_ID, cityId);
            editor.commit();
            mLoadingDialog.dismiss();
            Toast.makeText(getActivity(), R.string.city_determined, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), R.string.error_while_locate, Toast.LENGTH_LONG).show();
        }
    };

    private void updateUpdateIntervalSummary() {
        Preference preference = findPreference(Preferences.PREF_UPDATE_INTERVAL);
        String value = mPreferences
                .getString(Preferences.PREF_UPDATE_INTERVAL, Preferences.DEFAULT_PREF_UPDATE_INTERVAL);
        Resources resources = getActivity().getResources();

        String[] entries = resources.getStringArray(R.array.update_interval_entries);
        String[] entryValues = resources.getStringArray(R.array.update_interval_entry_values);

        for (int i = 0; i != entries.length; ++i) {
            if (value.equals(entryValues[i])) {
                preference.setSummary(entries[i]);
                break;
            }
        }
    }
}
