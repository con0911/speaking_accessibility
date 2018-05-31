/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.accessibility.speakingassistant.activity;

import com.accessibility.speakingassistant.R;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private AlertDialog mDialogAccessibility;
    private Button settingsBtn;
    private Button contentBtn;
    private Button helpButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingsBtn = (Button) findViewById(R.id.settingsButton);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goSettings();
            }
        });

        contentBtn = (Button) findViewById(R.id.contentButton);
        contentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goContent();
            }
        });

        helpButton = (Button) findViewById(R.id.helpButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goHelp();
            }
        });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(!isServicesEnabled(getApplicationContext()) && !preferences.getBoolean("tutorial", false)){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("tutorial", true);
            editor.apply();
            goTutorial();

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mDialogAccessibility!=null && mDialogAccessibility.isShowing()){
            mDialogAccessibility.dismiss();
        }
        if(!isServicesEnabled(getApplicationContext())){
            showAccesibilityDialog();
        }
    }


    private void showAccesibilityDialog() {
        mDialogAccessibility = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_universal_warning_msg))
                .setMessage(getString(R.string.turn_on_accessibility_msg))
                .setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        goSettings();
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mDialogAccessibility.dismiss();
                        finish();
                    }
                })
                .setCancelable(false)
                .create();
                mDialogAccessibility.show();
    }
    public void goSettings() {

        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        /*intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);*/
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void goContent() {

        Intent intent = new Intent(MainActivity.this, LastContent.class);
        startActivity(intent);
    }

    public void goTutorial() {

        Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
        startActivity(intent);
    }

    public void goHelp() {

        Intent intent = new Intent(MainActivity.this, SpeakPreferencesActivity.class);
        startActivity(intent);
    }

    public static boolean isServicesEnabled(Context context) {
        String accesibilityService = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (accesibilityService != null) {
            return (accesibilityService.matches("(?i).*com.accessibility.speakingassistant.service.GlobalButtonService.*"));
        }
        return false;
    }
}
