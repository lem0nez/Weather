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
import android.support.annotation.ArrayRes;
import android.support.annotation.Nullable;

class Util {
    private final Context mContext;

    public Util(Context context) {
        this.mContext = context;
    }

    /*
     * Sumulate the map structure using a resource arrays.
     */
    @Nullable
    public String getResValueByKey(@ArrayRes int resId, String key) {
        String[] array = mContext.getResources().getStringArray(resId);
        String value = null;

        for (String pairStr : array) {
            String[] pair = pairStr.split("[|]", 2);
            if (pair[0].equals(key)) {
                value = pair[1];
                break;
            }
        }
        return value;
    }
}
