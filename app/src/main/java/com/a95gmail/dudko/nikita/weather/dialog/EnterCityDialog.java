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
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.a95gmail.dudko.nikita.weather.MainActivity;
import com.a95gmail.dudko.nikita.weather.R;
import com.a95gmail.dudko.nikita.weather.WeatherProvider;

import java.net.HttpURLConnection;

public class EnterCityDialog extends DialogFragment {
    private final OnResponseCityIdListener responseListener;
    private EditText editText;
    private TextView summary;
    private ProgressBar progressBar;

    public EnterCityDialog(OnResponseCityIdListener responseListener) {
        this.responseListener = responseListener;
    }

    public interface OnResponseCityIdListener {
        void onResponse(int cityId);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.fragment_enter_city, null);

        editText = layout.findViewById(R.id.edit_text_city);
        summary = layout.findViewById(R.id.text_view_enter_city_summary);
        progressBar = layout.findViewById(R.id.progress_bar_enter_city);

        builder.setTitle(R.string.location);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            // Do nothing, because we override this button in the onResume() callback
            // (for prevent closing the dialog on click).
        });

        builder.setView(layout);
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog dialog = (AlertDialog) getDialog();

        if (dialog != null) {
            Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            editText.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
                // Fast clicking on the done button using the keyboard "Enter" button.
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    positiveButton.performClick();
                }
                return false;
            });

            positiveButton.setOnClickListener((view) -> {
                String city = editText.getText().toString();

                editText.requestFocus();
                summary.setVisibility(View.VISIBLE);

                ConnectivityManager connectivityManager = (ConnectivityManager)
                        getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = null;

                netInfo = connectivityManager != null
                        ? connectivityManager.getActiveNetworkInfo() : null;
                boolean isConnected = netInfo != null && netInfo.isConnectedOrConnecting();

                if (!isConnected) {
                    summary.setTextColor(getActivity().getColor(R.color.color_red));
                    summary.setText(R.string.need_network_access);
                    return;
                } else if (city.isEmpty()) {
                    dismiss();
                    return;
                }

                summary.setTextColor(getActivity().getColor(R.color.color_text));
                summary.setText(R.string.checking);
                progressBar.setVisibility(View.VISIBLE);

                new WeatherProvider(getActivity()).requestCityIdByName(city,
                        (cityId, responseStatusCode) -> {
                    progressBar.setVisibility(View.GONE);

                    if (responseStatusCode == HttpURLConnection.HTTP_NOT_FOUND) {
                        summary.setTextColor(getActivity().getColor(R.color.color_red));
                        summary.setText(R.string.city_not_found);
                    } else if (cityId == WeatherProvider.INVALID_CITY_ID) {
                        summary.setTextColor(getActivity().getColor(R.color.color_red));
                        summary.setText(R.string.error);
                    } else {
                        dismiss();
                        responseListener.onResponse(cityId);
                    }
                });
            });
        }
    }
}
