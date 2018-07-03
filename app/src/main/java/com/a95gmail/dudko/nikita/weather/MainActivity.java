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

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import static com.a95gmail.dudko.nikita.weather.data.Preferences.DEFAULT_PREF_CITY_ID;
import static com.a95gmail.dudko.nikita.weather.data.Preferences.PREF_CITY_ID;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "Weather";

    private Toolbar mToolbar;
    private SharedPreferences mPreferences;
    private int mCityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mCityId = mPreferences.getInt(PREF_CITY_ID, DEFAULT_PREF_CITY_ID);

        if (mCityId == DEFAULT_PREF_CITY_ID) {
            startActivity(new Intent(this, StartActivity.class));
            finish();
        }
    }
}
