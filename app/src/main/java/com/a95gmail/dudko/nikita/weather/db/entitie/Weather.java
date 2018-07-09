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

package com.a95gmail.dudko.nikita.weather.db.entitie;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "weather")
public class Weather {
    @PrimaryKey
    private long timestamp;

    @ColumnInfo(name = "city_id")
    private int cityId;

    private String city;

    @ColumnInfo(name = "weather_id")
    private short weatherId;

    private String icon;

    private float temperature;

    @ColumnInfo(name = "temp_min")
    private float tempMin;

    @ColumnInfo(name = "temp_max")
    private float tempMax;

    private byte humidity;
    private float pressure;

    @ColumnInfo(name = "wind_speed")
    private float windSpeed;

    @ColumnInfo(name = "wind_degree")
    private short windDegree;

    private byte cloudiness;
    private long sunrise;
    private long sunset;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public short getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(short weatherId) {
        this.weatherId = weatherId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getTempMin() {
        return tempMin;
    }

    public void setTempMin(float tempMin) {
        this.tempMin = tempMin;
    }

    public float getTempMax() {
        return tempMax;
    }

    public void setTempMax(float tempMax) {
        this.tempMax = tempMax;
    }

    public byte getHumidity() {
        return humidity;
    }

    public void setHumidity(byte humidity) {
        this.humidity = humidity;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public float getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(float windSpeed) {
        this.windSpeed = windSpeed;
    }

    public short getWindDegree() {
        return windDegree;
    }

    public void setWindDegree(short windDegree) {
        this.windDegree = windDegree;
    }

    public byte getCloudiness() {
        return cloudiness;
    }

    public void setCloudiness(byte cloudiness) {
        this.cloudiness = cloudiness;
    }

    public long getSunrise() {
        return sunrise;
    }

    public void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }

    public long getSunset() {
        return sunset;
    }

    public void setSunset(long sunset) {
        this.sunset = sunset;
    }
}