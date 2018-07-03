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

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

public class WeatherProvider {
    /** @noinspection SpellCheckingInspection*/
    private static final String API_KEY = "0ec658aba227f6b090eb728831aceece";
    public static final int INVALID_CITY_ID = -1;

    private final RequestQueue requestQueue;

    public WeatherProvider(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public interface OnResponseCityIdListener {
        void onResponse(int cityId, int responseStatusCode);
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
                        Log.e(MainActivity.TAG, e.getMessage());
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
        requestQueue.add(request);
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
                        Log.e(MainActivity.TAG, e.getMessage());
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
        requestQueue.add(request);
    }
}
