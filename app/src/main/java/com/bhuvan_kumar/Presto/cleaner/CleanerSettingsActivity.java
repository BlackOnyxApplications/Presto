/*
 *  Copyright 2019 TheRedSpy15
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.bhuvan_kumar.Presto.cleaner;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.bhuvan_kumar.Presto.R;
import com.bhuvan_kumar.Presto.app.Activity;

public class CleanerSettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cleaner_settings);

        getSupportFragmentManager().beginTransaction().replace(R.id.layout, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setHasOptionsMenu(true);
        }

        /**
         * Inflate Preferences
         */
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.cleaner_settings_preferences);
        }

        /**
         * ClickEvent Listener for Preferences
         */
        @Override
        public boolean onPreferenceTreeClick(androidx.preference.Preference preference) {
            String key = preference.getKey();
            if ("suggestion".equals(key)) {
//                reportIssue(getContext());
                return true;
            }
            return super.onPreferenceTreeClick(preference);
        }

        /**
         * Creates a menu that allows the user to create an issue on github
         */
//        final void reportIssue(Context context) {
//
//            IssueReporterLauncher.forTarget("TheRedSpy15", "LTECleanerFOSS")
//                    .theme(R.style.CustomIssueReportTheme)
//                    .guestEmailRequired(false)
//                    .guestToken("194835cbf18259752d316f680ef4842aa7ca9dc5")
//                    .minDescriptionLength(20)
//                    .homeAsUpEnabled(true)
//                    .launch(context);
//        }
    }
}
