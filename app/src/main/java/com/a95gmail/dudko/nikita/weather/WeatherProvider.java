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
import android.os.Bundle;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class WeatherProvider {
    private static final float HPA_IN_MMHG = 1.33322387415f;
    /** @noinspection SpellCheckingInspection*/
    private static final String API_KEY = "0ec658aba227f6b090eb728831aceece";
    public static final int INVALID_CITY_ID = -1;
    /** @noinspection WeakerAccess*/
    @DrawableRes
    public static final int INVALID_RESOURCE_ID = 0;

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

    public WeatherProvider(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);

        AppDatabase db =
                Room.databaseBuilder(context, AppDatabase.class, AppDatabase.DB_NAME).build();
        mWeatherDao = db.getWeatherDao();
    }

    public interface OnResponseCityIdListener {
        void onResponse(int cityId, int responseStatusCode);
    }

    public interface OnResponseWeatherTodayListener {
        void onResponse(Weather weather, int responseStatusCode);
    }

    public interface OnResponseWeatherFromDbListener {
        void onResponse(List<Weather> weathers);
    }

    public void requestCityIdByCoordinates(double latitude, double longitude,
                                           OnResponseCityIdListener listener) {
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

    public void requestCityIdByName(String city, OnResponseCityIdListener responseListener) {
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

    public void requestWeatherToday(int cityId, OnResponseWeatherTodayListener weatherTodayListener) {
        StringRequest request = new StringRequest(Request.Method.GET,
                "https://api.openweathermap.org/data/2.5/weather?id=" + String.valueOf(cityId)
                        + "&units=metric&appid=" + API_KEY,
                response -> weatherTodayListener.onResponse(parseResponse(response),
                        HttpURLConnection.HTTP_OK),
                (VolleyError error) -> {
                    if (error.networkResponse == null) {
                        weatherTodayListener.onResponse(null, HttpURLConnection.HTTP_UNAUTHORIZED);
                    } else {
                        weatherTodayListener.onResponse(null, error.networkResponse.statusCode);
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

    public void requestRelevantWeathersOnDay(long timestampMillis, int limit,
                                             @NonNull OnResponseWeatherFromDbListener responseListener) {
        Bundle bundle = getLowAndHighTimestampMillis(timestampMillis);

        new Thread(() -> {
            List<Weather> weathers = mWeatherDao.getThatValueBetween(
                    bundle.getLong(KEY_LOW_TIMESTAMP), bundle.getLong(KEY_HIGH_TIMESTAMP));

            if (weathers.size() == 0) {
                responseListener.onResponse(Collections.emptyList());
            } else {
                weathers.sort((o1, o2) -> Long.compare(o1.getTimestamp(), o2.getTimestamp()));
                responseListener.onResponse(weathers.subList(0, limit));
            }
        }).start();
    }

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
}
