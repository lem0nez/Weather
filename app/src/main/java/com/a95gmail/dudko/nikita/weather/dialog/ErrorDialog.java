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

package com.a95gmail.dudko.nikita.weather.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.a95gmail.dudko.nikita.weather.R;

public class ErrorDialog extends DialogFragment {
    private final String text;
    private final DialogInterface.OnClickListener clickListener;

    public ErrorDialog(String text, DialogInterface.OnClickListener clickListener) {
        this.text = text;
        this.clickListener = clickListener;
    }

    public ErrorDialog(String text) {
        this.text = text;
        this.clickListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.title_error);
        builder.setMessage(text);
        builder.setPositiveButton(android.R.string.ok, clickListener);

        return builder.create();
    }
}
