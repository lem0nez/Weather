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

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.a95gmail.dudko.nikita.weather.db.entitie.Weather;

import java.util.Date;

public class TodayFragment extends Fragment {
    private final Weather mWeather;

    private TextView mTextTitle;

    private ImageView mImageWeatherIcon;
    private TextView mTextTemperature;
    private TextView mTextWeatherInfo;

    private ImageView mImageCompass;
    private TextView mTextWindSpeed;

    private TextView mTextHumidity;
    private TextView mTextPressure;

    private TextView mTextCloudiness;

    private TextView mTextTemperatureMax;
    private TextView mTextTemperatureMin;

    private TextView mTextSunrise;
    private TextView mTextSunset;

    public TodayFragment(Weather weather) {
        mWeather = weather;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        inflater.inflate(R.layout.fragment_weather_today, container, true);

        Activity activity = getActivity();
        Resources resources = activity.getResources();
        Date date;
        String temperatureSymbol;

        mTextTitle = activity.findViewById(R.id.text_view_weather_title);
        mImageWeatherIcon = activity.findViewById(R.id.image_view_forecast_weather_icon);
        mTextTemperature = activity.findViewById(R.id.text_view_temperature);
        mTextWeatherInfo = activity.findViewById(R.id.text_view_weather_info);

        mImageCompass = activity.findViewById(R.id.image_view_forecast_compass);
        mTextWindSpeed = activity.findViewById(R.id.text_view_forecast_wind_speed);

        mTextHumidity = activity.findViewById(R.id.text_view_humidity);
        mTextPressure = activity.findViewById(R.id.text_view_pressure);

        mTextCloudiness = activity.findViewById(R.id.text_view_cloudiness);

        mTextTemperatureMax = activity.findViewById(R.id.text_view_temperature_max);
        mTextTemperatureMin = activity.findViewById(R.id.text_view_temperature_min);

        mTextSunrise = activity.findViewById(R.id.text_view_sunrise);
        mTextSunset = activity.findViewById(R.id.text_view_sunset);

        date = new Date(System.currentTimeMillis());
        String dateString = (String) DateFormat.format("MMM d", date);
        String title = mWeather.getCity() + ", " + dateString;
        mTextTitle.setText(title);

        mImageWeatherIcon.setImageResource(
                WeatherProvider.getDrawableWeatherIcon(mWeather.getIcon()));

        temperatureSymbol = (mWeather.getTemperature() > 0.0f) ? "+" : "";
        mTextTemperature.setText(temperatureSymbol + (int) mWeather.getTemperature()
                + ' ' + resources.getString(R.string.unit_celsius));

        String weatherInfo = new Util(activity)
                .getResValueByKey(R.array.weather_conditions, String.valueOf(mWeather.getWeatherId()));
        mTextWeatherInfo.setText(weatherInfo);

        mImageCompass.setRotation((float) mWeather.getWindDegree());
        mTextWindSpeed.setText(String.format("%.1f", mWeather.getWindSpeed())
                + ' ' + resources.getString(R.string.unit_meters_per_second));

        mTextHumidity.setText(String.valueOf(mWeather.getHumidity()) + " %");
        mTextPressure.setText(String.valueOf((int) mWeather.getPressure())
                + ' ' + resources.getString(R.string.unit_mmhg));

        mTextCloudiness.setText(String.valueOf(mWeather.getCloudiness()) + " %");

        temperatureSymbol = (mWeather.getTempMax() > 0.0f) ? "+" : "";
        mTextTemperatureMax.setText(String.valueOf(temperatureSymbol + (int) mWeather.getTempMax())
                + ' ' + resources.getString(R.string.unit_celsius));

        temperatureSymbol = (mWeather.getTempMin() > 0.0f) ? "+" : "";
        mTextTemperatureMin.setText(String.valueOf(temperatureSymbol + (int) mWeather.getTempMin())
                + ' ' + resources.getString(R.string.unit_celsius));

        date = new Date(mWeather.getSunrise() * 1000);
        mTextSunrise.setText(DateFormat.format("HH:mm", date));
        date = new Date(mWeather.getSunset() * 1000);
        mTextSunset.setText(DateFormat.format("HH:mm", date));
        return null;
    }
}
