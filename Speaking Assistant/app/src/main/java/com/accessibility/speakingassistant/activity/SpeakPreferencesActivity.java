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
import com.accessibility.speakingassistant.service.GlobalButtonService;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SpeakPreferencesActivity extends Activity{

    private Switch ttsOutputSwitch;
    private Boolean ttsChecked;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speak_settings);
        ttsOutputSwitch = findViewById(R.id.switchOutput);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ttsChecked = sharedPreferences.getBoolean("tts_output_checked", true);

        ttsOutputSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("tts_output_checked", isChecked);
                editor.apply();
                if (GlobalButtonService.getService() != null) GlobalButtonService.getService().setTTSOutput(isChecked);
            }
        });

        ttsOutputSwitch.setChecked(ttsChecked);


    }
}
