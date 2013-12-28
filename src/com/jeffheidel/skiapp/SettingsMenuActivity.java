package com.jeffheidel.skiapp;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsMenuActivity extends PreferenceActivity {

	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        addPreferencesFromResource(R.layout.activity_settings_menu);
	    }
}