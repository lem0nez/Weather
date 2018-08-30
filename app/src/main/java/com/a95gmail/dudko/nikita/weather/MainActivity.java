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

import android.app.FragmentTransaction;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.a95gmail.dudko.nikita.weather.db.AppDatabase;
import com.a95gmail.dudko.nikita.weather.db.dao.WeatherDao;
import com.a95gmail.dudko.nikita.weather.db.entitie.Weather;

import java.net.HttpURLConnection;
import java.util.List;

import static com.a95gmail.dudko.nikita.weather.Preferences.DEFAULT_PREF_CITY_ID;
import static com.a95gmail.dudko.nikita.weather.Preferences.PREF_CITY_ID;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = "Weather";
    private static final String KEY_SELECTED_ITEM_ID = "SELECTED_ITEM_ID";

    private Toolbar mToolbar;
    private ProgressBar mProgressBar;
    private FrameLayout mMainLayout;
    private BottomNavigationView mBottomNavigation;

    private SharedPreferences mPreferences;
    private WeatherDao mWeatherDao;

    // For fast restoring data.
    private Weather mWeatherToday;
    private Weather[] mWeatherArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_main);

        initFields();
        initBottomNavigation();
        setSupportActionBar(mToolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        if (mPreferences.getInt(PREF_CITY_ID, DEFAULT_PREF_CITY_ID) == DEFAULT_PREF_CITY_ID) {
            startActivity(new Intent(this, StartActivity.class));
            finish();
            return;
        }

        // For adding previous fragment on a current screen (for example, when the screen rotate).
        if (savedInstanceState != null) {
            addFragmentToLayout(savedInstanceState.getInt(KEY_SELECTED_ITEM_ID));
        } else {
            initWeatherToday();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_SELECTED_ITEM_ID, mBottomNavigation.getSelectedItemId());
        super.onSaveInstanceState(outState);
    }

    private void initFields() {
        mToolbar = findViewById(R.id.toolbar_main);
        mProgressBar = findViewById(R.id.progress_bar_main);
        mMainLayout = findViewById(R.id.layout_main);
        mBottomNavigation = findViewById(R.id.bottom_navigation_main);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, AppDatabase.DB_NAME).build();
        mWeatherDao = db.getWeatherDao();
    }

    private void initBottomNavigation() {
        mBottomNavigation.setOnNavigationItemSelectedListener((item) -> {
            addFragmentToLayout(item.getItemId());
            return true;
        });
    }

    private void addFragmentToLayout(int itemId) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        mMainLayout.removeAllViews();

        switch (itemId) {
            case R.id.menu_today :
                initWeatherToday();
                break;
            case R.id.menu_five_days:
                displayForecast();
                break;
            case R.id.menu_settings :
                mProgressBar.setVisibility(View.GONE);
                fragmentTransaction.add(R.id.layout_main, new PreferencesFragment());
                fragmentTransaction.commit();
                break;
            default :
                break;
        }
    }

    private void initWeatherToday() {
        // If before this moment the today weather screen showed forecast...
        if (mWeatherToday != null) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.layout_main, new TodayFragment(mWeatherToday));
            fragmentTransaction.commit();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);

        long lastUpdateTime = mPreferences.getLong(Preferences.PREF_LAST_UPDATE, 0);
        long needForUpdateTime = lastUpdateTime
                + Long.valueOf(mPreferences.getString(Preferences.PREF_UPDATE_INTERVAL, ""));
        long currentTimestampMillis = System.currentTimeMillis();

        if (currentTimestampMillis / 1000 < needForUpdateTime) {
            WeatherProvider weatherProvider = new WeatherProvider(this);
            weatherProvider.requestRelevantWeathersOnDay(currentTimestampMillis, 1, weathers -> {
                if (weathers.size() != 0) {
                    runOnUiThread(() -> displayWeatherUsingLocalData(weathers));
                } else {
                    loadAndDisplayWeatherToday();
                }
            });
        } else {
            loadAndDisplayWeatherToday();
        }
    }

    private void loadAndDisplayWeatherToday() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Util util = new Util(this);

        if (util.isConnected()) {
            WeatherProvider weatherProvider = new WeatherProvider(this);
            int cityId =
                    mPreferences.getInt(Preferences.PREF_CITY_ID, Preferences.DEFAULT_PREF_CITY_ID);

            weatherProvider.requestWeatherToday(cityId,
                    new WeatherProvider.OnResponseWeatherListener() {
                @Override
                public void onResponseWeatherToday(Weather weather, int responseStatusCode) {
                    if (responseStatusCode != HttpURLConnection.HTTP_OK) {
                        displayWeatherUsingDatabaseData();
                        return;
                    }

                    SharedPreferences.Editor prefEditor = mPreferences.edit();
                    prefEditor.putLong(Preferences.PREF_LAST_UPDATE, System.currentTimeMillis() / 1000);
                    prefEditor.commit();

                    mWeatherToday = weather;
                    fragmentTransaction.add(R.id.layout_main, new TodayFragment(weather));
                    fragmentTransaction.commit();
                    mProgressBar.setVisibility(View.GONE);

                    new Thread(() -> {
                        mWeatherDao.clear();
                        mWeatherDao.insert(weather);
                    }).start();
                }

                @Override
                public void onResponseWeatherList(List<Weather> weathers, int responseStatusCode) {}
            });
        } else {
            displayWeatherUsingDatabaseData();
        }
    }

    private void displayWeatherUsingLocalData(List<Weather> weathers) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        mWeatherToday = weathers.get(0);
        fragmentTransaction.add(R.id.layout_main, new TodayFragment(mWeatherToday));
        fragmentTransaction.commit();
        mProgressBar.setVisibility(View.GONE);
    }

    private void displayWeatherUsingDatabaseData() {
        WeatherProvider weatherProvider = new WeatherProvider(this);
        weatherProvider.requestRelevantWeathersOnDay(System.currentTimeMillis(), 1, weathers -> {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

            if (weathers.size() == 0) {
                fragmentTransaction.add(R.id.layout_main, new ErrorFragment(
                        getResources().getString(R.string.need_network_for_update)));
            } else {
                mWeatherToday = weathers.get(0);
                fragmentTransaction.add(R.id.layout_main, new TodayFragment(mWeatherToday));
            }
            fragmentTransaction.commit();
            MainActivity.this.runOnUiThread(() -> mProgressBar.setVisibility(View.GONE));
        });
    }

    private void displayForecast() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        if (mWeatherArray != null) {
            fragmentTransaction.add(R.id.layout_main, new ForecastListFragment(mWeatherArray));
            fragmentTransaction.commit();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);

        WeatherProvider weatherProvider = new WeatherProvider(this);
        weatherProvider.isNeedToUpdateForecast(needToUpdate -> {
            if (needToUpdate) {
                Util util = new Util(MainActivity.this);

                if (!util.isConnected()) {
                    runOnUiThread(() -> mProgressBar.setVisibility(View.GONE));

                    fragmentTransaction.add(R.id.layout_main, new ErrorFragment(
                            getResources().getString(R.string.need_network_for_update)));
                    fragmentTransaction.commit();
                    return;
                }

                int cityId = mPreferences.getInt(Preferences.PREF_CITY_ID,
                        Preferences.DEFAULT_PREF_CITY_ID);

                weatherProvider.requestWeatherList(cityId,
                        new WeatherProvider.OnResponseWeatherListener() {
                    @Override
                    public void onResponseWeatherToday(Weather weather, int responseStatusCode) {}

                    @Override
                    public void onResponseWeatherList(List<Weather> weathers, int responseStatusCode) {
                        runOnUiThread(() -> mProgressBar.setVisibility(View.GONE));

                        if (responseStatusCode != HttpURLConnection.HTTP_OK) {
                            String error = getResources().getString(R.string.error_response_code)
                                    + responseStatusCode;
                            fragmentTransaction.add(R.id.layout_main, new ErrorFragment(error));
                            fragmentTransaction.commit();
                            return;
                        }

                        mWeatherArray = weatherProvider.getWeatherArrayForForecastList(weathers);
                        fragmentTransaction.add(R.id.layout_main,
                                new ForecastListFragment(mWeatherArray));
                        fragmentTransaction.commit();
                    }
                });
            } else {
                new Thread(() -> {
                    List<Weather> weathers = mWeatherDao.getAll();
                    mWeatherArray = weatherProvider.getWeatherArrayForForecastList(weathers);

                    fragmentTransaction.add(R.id.layout_main,
                            new ForecastListFragment(mWeatherArray));
                    fragmentTransaction.commit();
                }).start();
            }
        });
    }
}
