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

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.a95gmail.dudko.nikita.weather.db.entitie.Weather;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ForecastListFragment extends Fragment {
    private final Weather[] mWeathers;

    public ForecastListFragment(Weather[] weathers) {
        mWeathers = weathers;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast_list, container, true);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_forecast_list);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new RecyclerViewAdapter(mWeathers));
        return null;
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        private ArrayList<ArrayList<Weather>> mDataSet;

        public class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mTextViewDay;
            final TextView mTextViewWindSpeed;
            final TextView mTextViewTemperature;
            final TextView mTextViewWeatherInfo;
            final ImageView mImageViewIcon;

            ViewHolder(CardView forecastCardView) {
                super(forecastCardView);
                mTextViewDay = forecastCardView.findViewById(R.id.text_view_forecast_day);
                mTextViewWindSpeed = forecastCardView.findViewById(R.id.text_view_forecast_wind_speed);
                mTextViewTemperature = forecastCardView.findViewById(R.id.text_view_forecat_temp);
                mTextViewWeatherInfo = forecastCardView.findViewById(R.id.text_view_forecast_weather_info);
                mImageViewIcon = forecastCardView.findViewById(R.id.image_view_forecast_weather_icon);
            }
        }

        RecyclerViewAdapter(Weather[] weathers) {
            mDataSet = new ArrayList<ArrayList<Weather>>();
            ArrayList<Weather> forecastOnDay = new ArrayList<>();
            Date date;
            int hour;

            for (int i = 0; i < weathers.length; ++i) {
                date = new Date(weathers[i].getTimestamp() * 1000);
                hour = Integer.valueOf((String) DateFormat.format("H", date));

                if (hour == 0 && i != 0) {
                    mDataSet.add(new ArrayList<Weather>(forecastOnDay));
                    forecastOnDay.clear();
                }
                forecastOnDay.add(weathers[i]);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_view_weather, parent, false);
            return new ViewHolder(cardView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Resources resources = getActivity().getResources();

            ArrayList<Weather> weathersOnDay = mDataSet.get(position);
            Weather nightWeather = weathersOnDay.get(0);
            Weather dayWeather =
                    weathersOnDay.get(WeatherProvider.PARTS_OF_DAY_AS_THREE_HOURS_UNIT / 2);

            Date date = new Date(dayWeather.getTimestamp() * 1000);
            String dateString = (String) DateFormat.format("MMM d", date);
            holder.mTextViewDay.setText(dateString);

            holder.mTextViewWindSpeed.setText(String.format("%.1f", dayWeather.getWindSpeed())
                    + ' ' + resources.getString(R.string.unit_meters_per_second));

            String temperatureSymbol = (dayWeather.getTemperature() > 0.0f) ? "+" : "";
            holder.mTextViewTemperature.setText(temperatureSymbol + (int) dayWeather.getTemperature()
                    + ' ' + resources.getString(R.string.unit_celsius));

            String weatherInfo = new Util(getActivity())
                    .getResValueByKey(R.array.weather_conditions, String.valueOf(dayWeather.getWeatherId()));
            holder.mTextViewWeatherInfo.setText(weatherInfo);

            holder.mImageViewIcon.setImageResource(
                    WeatherProvider.getDrawableWeatherIcon(dayWeather.getIcon()));
        }

        @Override
        public int getItemCount() {
            return mDataSet.size();
        }
    }
}
