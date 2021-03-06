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

final class Preferences {
    public static final String PREF_CITY_ID = "city_id";
    public static final String PREF_LOCATE = "locate";
    public static final String PREF_UPDATE_INTERVAL = "update_interval";
    public static final String PREF_LAST_UPDATE = "last_update";
    public static final int DEFAULT_PREF_CITY_ID = -1;
    public static final String DEFAULT_PREF_UPDATE_INTERVAL = "10800";
}