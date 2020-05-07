package com.bhuvan_kumar.Presto.fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.bhuvan_kumar.Presto.R;
import com.bhuvan_kumar.Presto.util.AppUtils;
import com.bhuvan_kumar.Presto.util.PreferenceUtils;

public class PreferencesFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener{
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        addPreferencesFromResource(R.xml.preferences_main_app);

        if (Build.VERSION.SDK_INT < 26)
            addPreferencesFromResource(R.xml.preferences_main_notification);
        else
            addPreferencesFromResource(R.xml.preferences_main_notification_oreo);

        addPreferencesFromResource(R.xml.preferences_main_advanced);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        getPreferenceManager()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        getPreferenceManager()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (("custom_fonts".equals(key) || "dark_theme".equals(key) || "amoled_theme".equals(key))
                && getActivity() != null) {
            PreferenceUtils.syncPreferences(AppUtils.getDefaultLocalPreferences(getContext()),
                    AppUtils.getDefaultPreferences(getContext()).getWeakManager());

            getActivity().recreate();
        }
    }
}