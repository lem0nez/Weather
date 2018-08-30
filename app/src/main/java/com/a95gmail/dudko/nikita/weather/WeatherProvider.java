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

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.Log;

import com.a95gmail.dudko.nikita.weather.db.AppDatabase;
import com.a95gmail.dudko.nikita.weather.db.dao.WeatherDao;
import com.a95gmail.dudko.nikita.weather.db.entitie.Weather;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class WeatherProvider {
    // Three hours, because the OpenWeatherMap API
    // using interval as three hours between the forecast data on day.
    public static final int PARTS_OF_DAY_AS_THREE_HOURS_UNIT = 8;
    private static final long MILLIS_IN_DAY = 3600 * 24 * 1000;

    private static final float HPA_IN_MMHG = 1.33322387415f;
    public static final int INVALID_CITY_ID = -1;
    @DrawableRes
    public static final int INVALID_RESOURCE_ID = 0;
    public static final int INVALID_TIMESTAMP = -1;

    private static final String API_KEY = "0ec658aba227f6b090eb728831aceece";
    private static final String KEY_LOW_TIMESTAMP = "LOW_TIMESTAMP";
    private static final String KEY_HIGH_TIMESTAMP = "HIGH_TIMESTAMP";

    /** @noinspection unchecked*/
    private static final HashMap<String, Integer> iconsMap = new HashMap() {{
        put("01d", R.drawable.ic_day_sunny);
        put("01n", R.drawable.ic_night_clear);
        put("02d", R.drawable.ic_day_cloudy);
        put("02n", R.drawable.ic_night_cloudy);
        put("03", R.drawable.ic_scattered_clouds);
        put("04", R.drawable.ic_broken_clouds);
        put("09", R.drawable.ic_shower_rain);
        put("10d", R.drawable.ic_day_rain);
        put("10n", R.drawable.ic_night_rain);
        put("11", R.drawable.ic_thunderstorm);
        put("13", R.drawable.ic_snow);
        put("50", R.drawable.ic_mist);
    }};

    private final RequestQueue mRequestQueue;
    private final WeatherDao mWeatherDao;
    private final SharedPreferences mPreferences;

    public WeatherProvider(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);

        AppDatabase db =
                Room.databaseBuilder(context, AppDatabase.class, AppDatabase.DB_NAME).build();
        mWeatherDao = db.getWeatherDao();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public interface OnResponseCityIdListener {
        void onResponse(int cityId, int responseStatusCode);
    }

    public interface OnResponseWeatherListener {
        void onResponseWeatherToday(Weather weather, int responseStatusCode);
        void onResponseWeatherList(List<Weather> weathers, int responseStatusCode);
    }

    public interface OnResponseWeatherFromDbListener {
        void onResponse(List<Weather> weathers);
    }

    public interface OnResponseNeedToUpdateResult {
        void onResponse(boolean needToUpdate);
    }

    public void requestCityIdByCoordinates(double latitude, double longitude,
                                           @NonNull OnResponseCityIdListener listener) {
        StringRequest request = new StringRequest(Request.Method.GET,
                "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon="
                        + longitude + "&appid=" + API_KEY,
                (String response) -> {
                    int cityId = INVALID_CITY_ID;
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        cityId = jsonResponse.getInt("id");
                    } catch (JSONException e) {
                        Log.e(MainActivity.LOG_TAG, e.getMessage());
                    }
                    listener.onResponse(cityId, HttpsURLConnection.HTTP_OK);
                }, (VolleyError error) -> {
                    if (error.networkResponse == null) {
                        listener.onResponse(INVALID_CITY_ID,
                                HttpURLConnection.HTTP_UNAUTHORIZED);
                    } else {
                        listener.onResponse(INVALID_CITY_ID, error.networkResponse.statusCode);
                    }
                });
        mRequestQueue.add(request);
    }

    public void requestCityIdByName(String city, @NonNull OnResponseCityIdListener responseListener) {
        StringRequest request = new StringRequest(Request.Method.GET,
                "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY,
                (String response) -> {
                    int cityId = INVALID_CITY_ID;
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        cityId = jsonResponse.getInt("id");
                    } catch (JSONException e) {
                        Log.e(MainActivity.LOG_TAG, e.getMessage());
                    }
                    responseListener.onResponse(cityId, HttpsURLConnection.HTTP_OK);
                }, (VolleyError error) -> {
                    if (error.networkResponse == null) {
                        responseListener.onResponse(INVALID_CITY_ID,
                                HttpURLConnection.HTTP_UNAUTHORIZED);
                    } else {
                        responseListener.onResponse(INVALID_CITY_ID, error.networkResponse.statusCode);
                    }
                });
        mRequestQueue.add(request);
    }

    public void requestWeatherToday(int cityId, @NonNull OnResponseWeatherListener weatherTodayListener) {
        StringRequest request = new StringRequest(Request.Method.GET,
                "https://api.openweathermap.org/data/2.5/weather?id=" + String.valueOf(cityId)
                        + "&units=metric&appid=" + API_KEY,
                response -> weatherTodayListener.onResponseWeatherToday(parseResponse(response),
                        HttpURLConnection.HTTP_OK),
                (VolleyError error) -> {
                    if (error.networkResponse == null) {
                        weatherTodayListener.onResponseWeatherToday(
                                null, HttpURLConnection.HTTP_UNAUTHORIZED);
                    } else {
                        weatherTodayListener.onResponseWeatherToday(
                                null, error.networkResponse.statusCode);
                    }
                });
        mRequestQueue.add(request);
    }

    public void requestWeatherList(int cityId, @NonNull OnResponseWeatherListener responseListener) {
        StringRequest request = new StringRequest(Request.Method.GET,
                "https://api.openweathermap.org/data/2.5/forecast?id=" + String.valueOf(cityId)
                        + "&units=metric&appid=" + API_KEY,
                response -> responseListener.onResponseWeatherList(parseArrayResponse(response),
                        HttpURLConnection.HTTP_OK),
                (VolleyError error) -> {
                    if (error.networkResponse == null) {
                        responseListener.onResponseWeatherList(
                                null, HttpURLConnection.HTTP_UNAUTHORIZED);
                    } else {
                        responseListener.onResponseWeatherList(
                                null, error.networkResponse.statusCode);
                    }
                });
        mRequestQueue.add(request);
    }

    @DrawableRes
    public static int getDrawableWeatherIcon(String icon) {
        @DrawableRes int resId = INVALID_RESOURCE_ID;

        for (String key : iconsMap.keySet()) {
            if (icon.contains(key)) {
                resId = iconsMap.get(key);
                break;
            }
        }
        return resId;
    }

    private Weather parseResponse(String response) {
        Weather weather = new Weather();

        try {
            JSONObject responseJson = new JSONObject(response);
            weather.setTimestamp(responseJson.getLong("dt"));
            weather.setCityId(responseJson.getInt("id"));
            weather.setCity(responseJson.getString("name"));

            JSONObject weatherJson = responseJson.getJSONArray("weather").getJSONObject(0);
            weather.setWeatherId((short) weatherJson.getInt("id"));
            weather.setIcon(weatherJson.getString("icon"));

            JSONObject mainJson = responseJson.getJSONObject("main");
            weather.setTemperature((float) mainJson.getDouble("temp"));
            weather.setTempMin((float) mainJson.getDouble("temp_min"));
            weather.setTempMax((float) mainJson.getDouble("temp_max"));
            weather.setHumidity((byte) mainJson.getInt("humidity"));
            weather.setPressure((float) mainJson.getDouble("pressure") / HPA_IN_MMHG);

            JSONObject windJson = responseJson.getJSONObject("wind");
            weather.setWindSpeed((float) windJson.getDouble("speed"));
            weather.setWindDegree((short) windJson.getInt("deg"));

            JSONObject cloudsJson = responseJson.getJSONObject("clouds");
            weather.setCloudiness((byte) cloudsJson.getInt("all"));

            JSONObject sysJson = responseJson.getJSONObject("sys");
            weather.setSunrise(sysJson.getInt("sunrise"));
            weather.setSunset(sysJson.getInt("sunset"));
        } catch (JSONException e) {
            Log.e(MainActivity.LOG_TAG, e.getMessage());
        }

        return weather;
    }

    @NonNull
    private List<Weather> parseArrayResponse(String response) {
        ArrayList<Weather> weathers = new ArrayList<>();

        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONObject cityJson = jsonResponse.getJSONObject("city");
            JSONArray arrayJson = jsonResponse.getJSONArray("list");

            int cityId = cityJson.getInt("id");
            String cityName = cityJson.getString("name");

            for (int i = 0; i < arrayJson.length(); ++i) {
                JSONObject jsonArrayItem = arrayJson.getJSONObject(i);
                Weather weather = new Weather();

                weather.setCityId(cityId);
                weather.setCity(cityName);
                weather.setTimestamp(jsonArrayItem.getLong("dt"));

                JSONObject weatherJson = jsonArrayItem.getJSONArray("weather").getJSONObject(0);
                weather.setWeatherId((short) weatherJson.getInt("id"));
                weather.setIcon(weatherJson.getString("icon"));

                JSONObject mainJson = jsonArrayItem.getJSONObject("main");
                weather.setTemperature((float) mainJson.getDouble("temp"));
                weather.setTempMin((float) mainJson.getDouble("temp_min"));
                weather.setTempMax((float) mainJson.getDouble("temp_max"));

                weather.setHumidity((byte) mainJson.getInt("humidity"));
                weather.setPressure((float) mainJson.getDouble("pressure") / HPA_IN_MMHG);

                JSONObject windJson = jsonArrayItem.getJSONObject("wind");
                weather.setWindSpeed((float) windJson.getDouble("speed"));
                weather.setWindDegree((short) windJson.getInt("deg"));

                JSONObject cloudsJson = jsonArrayItem.getJSONObject("clouds");
                weather.setCloudiness((byte) cloudsJson.getInt("all"));

                weathers.add(weather);
            }
        } catch (JSONException e) {
            Log.e(MainActivity.LOG_TAG, e.getMessage());
        }

        return weathers;
    }

    public void requestRelevantWeathersOnDay(long timestampMillis, int limit,
                                             @NonNull OnResponseWeatherFromDbListener responseListener) {
        Bundle bundle = getLowAndHighTimestampMillis(timestampMillis);

        new Thread(() -> {
            List<Weather> weathers = mWeatherDao.getWeathersBetweenTimeValues(
                    bundle.getLong(KEY_LOW_TIMESTAMP), bundle.getLong(KEY_HIGH_TIMESTAMP));

            if (weathers.size() == 0) {
                responseListener.onResponse(Collections.emptyList());
            } else {
                weathers.sort((o1, o2) -> Long.compare(o1.getTimestamp(), o2.getTimestamp()));
                responseListener.onResponse(weathers.subList(0, limit));
            }
        }).start();
    }

    // For getting the weather data on a one day.
    private static Bundle getLowAndHighTimestampMillis(long timestampMillis) {
        final Date date = new Date(timestampMillis);
        final String lowDate = (String) DateFormat.format("dd.MM.yyyy 00:00:00", date);
        final String highDate = (String) DateFormat.format("dd.MM.yyyy 23:59:59", date);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        long lowTimestamp = 0, highTimestamp = 0;
        try {
            lowTimestamp = simpleDateFormat.parse(lowDate).getTime() / 1000;
            highTimestamp = simpleDateFormat.parse(highDate).getTime() / 1000;
        } catch (ParseException e) {
            Log.e(MainActivity.LOG_TAG, e.getMessage());
        }

        Bundle bundle = new Bundle(2);
        bundle.putLong(KEY_LOW_TIMESTAMP, lowTimestamp);
        bundle.putLong(KEY_HIGH_TIMESTAMP, highTimestamp);
        return bundle;
    }

    public void isNeedToUpdateForecast(OnResponseNeedToUpdateResult responseResult) {
        long lastUpdateTime = mPreferences.getLong(Preferences.PREF_LAST_UPDATE, 0);
        int updateInterval = Integer.valueOf(mPreferences.getString(
                Preferences.PREF_UPDATE_INTERVAL, Preferences.DEFAULT_PREF_UPDATE_INTERVAL));
        long needToUpdateTime = lastUpdateTime + updateInterval;

        if (System.currentTimeMillis() / 1000 >= needToUpdateTime) {
            responseResult.onResponse(true);
        } else {
            new Thread(() -> {
                long needTimestamp = getStartTimestampForForecastList();
                if (needTimestamp == INVALID_TIMESTAMP) {
                    responseResult.onResponse(true);
                    return;
                }

                List<Weather> weathers = mWeatherDao.getAll();
                for (/* Nothing */; weathers.size() != 0; weathers.remove(0)) {
                    if (weathers.get(0).getTimestamp() == needTimestamp) {
                        break;
                    }
                }

                if (weathers.size() < PARTS_OF_DAY_AS_THREE_HOURS_UNIT) {
                    responseResult.onResponse(true);
                } else {
                    responseResult.onResponse(false);
                }
            }).start();
        }
    }

    public long getStartTimestampForForecastList() {
        final String DATE_FORMAT = "dd.MM.yyyy 00:00:00";

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        String needDate = (String) DateFormat.format(DATE_FORMAT,
                new Date(System.currentTimeMillis() + MILLIS_IN_DAY));
        long needTimestamp = INVALID_TIMESTAMP;

        try {
            needTimestamp = simpleDateFormat.parse(needDate).getTime() / 1000;
        } catch (ParseException e) {
            Log.e(MainActivity.LOG_TAG, e.getMessage());
        }
        return needTimestamp;
    }

    public Weather[] getWeatherArrayForForecastList(List<Weather> weathers) {
        long needTimestamp = getStartTimestampForForecastList();

        for (/* Nothing */; weathers.size() != 0; weathers.remove(0)) {
            Weather weather = weathers.get(0);
            if (weather.getTimestamp() == needTimestamp) {
                break;
            }
        }
        return weathers.toArray(new Weather[weathers.size()]);
    }
}
