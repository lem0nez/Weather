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

package com.a95gmail.dudko.nikita.weather.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.a95gmail.dudko.nikita.weather.db.entitie.Weather;

import java.util.List;

@Dao
public interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Weather weather);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<Weather> weathers);

    @Update
    int update(Weather weather);

    @Update
    int updateAll(List<Weather> weathers);

    @Delete
    int delete(Weather weather);

    @Delete
    int deleteAll(List<Weather> weathers);

    @Query("DELETE FROM weather")
    void clear();

    @Query("SELECT * FROM weather")
    List<Weather> getAll();

    @Query("SELECT * FROM weather WHERE timestamp = :timestamp LIMIT 1")
    Weather getWeatherByTimestamp(long timestamp);

    @Query("SELECT * FROM weather WHERE timestamp BETWEEN :lowValue AND :highValue")
    List<Weather> getWeathersBetweenTimeValues(long lowValue, long highValue);
}
