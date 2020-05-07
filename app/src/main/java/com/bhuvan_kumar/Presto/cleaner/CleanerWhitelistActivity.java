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
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bhuvan_kumar.Presto.R;
import com.bhuvan_kumar.Presto.app.Activity;
import com.fxn.stash.Stash;

import java.io.File;
import java.util.List;

public class CleanerWhitelistActivity extends Activity {

    ListView listView;
    BaseAdapter adapter;
    private static List<String> whiteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cleaner_whitelist);
        listView = findViewById(R.id.whitelistView);

        adapter = new ArrayAdapter<>(this, R.layout.custom_cleaner_textview, getWhiteList());
        listView.setAdapter(adapter);
    }

    /**
     * Clears the whitelist, then sets it up again without loading saved one from stash
     * @param view the view that is clicked
     */
    public void emptyWhitelist(View view) {

        new AlertDialog.Builder(CleanerWhitelistActivity.this)
                .setTitle(R.string.reset_whitelist)
                .setMessage(R.string.are_you_reset_whitelist)
                .setPositiveButton(R.string.butn_resetToDefault, (dialog, whichButton) -> {
                    whiteList.clear();
                    Stash.put("whiteList", whiteList);
                    refreshListView();
                })
                .setNegativeButton(R.string.butn_cancel, (dialog, whichButton) -> { }).show();
    }

    public void addRecommended(View view) {
        File externalDir = Environment.getExternalStorageDirectory();

        if (!whiteList.contains(new File(externalDir, "Music").getPath())) {
            whiteList.add(new File(externalDir, "Music").getPath());
            whiteList.add(new File(externalDir, "Podcasts").getPath());
            whiteList.add(new File(externalDir, "Ringtones").getPath());
            whiteList.add(new File(externalDir, "Alarms").getPath());
            whiteList.add(new File(externalDir, "Notifications").getPath());
            whiteList.add(new File(externalDir, "Pictures").getPath());
            whiteList.add(new File(externalDir, "Movies").getPath());
            whiteList.add(new File(externalDir, "Download").getPath());
            whiteList.add(new File(externalDir, "DCIM").getPath());
            whiteList.add(new File(externalDir, "Documents").getPath());
            Stash.put("whiteList", whiteList);
            refreshListView();

        } else
            Toast.makeText(this, "Already added",
                    Toast.LENGTH_LONG).show();
    }

    /**
     * Creates a dialog asking for a file/folder name to add to the whitelist
     * @param view the view that is clicked
     */
    public final void addToWhiteList(View view) {

        final EditText input = new EditText(CleanerWhitelistActivity.this);

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_to_whitelist)
                .setMessage(R.string.enter_file_name)
                .setView(input)
                .setPositiveButton(R.string.add, (dialog, whichButton) -> {
                    whiteList.add(String.valueOf(input.getText()));
                    Stash.put("whiteList", whiteList);
                    refreshListView();
                })
                .setNegativeButton(R.string.cancel, (dialog, whichButton) -> { }).show();
    }

    public void refreshListView() {
        runOnUiThread(() -> {
            adapter.notifyDataSetChanged();
            listView.invalidateViews();
            listView.refreshDrawableState();
        });
    }

    public static synchronized List<String> getWhiteList() {
        if (whiteList == null)
            whiteList = Stash.getArrayList("whiteList", String.class);
        return whiteList;
    }
}
