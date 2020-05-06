/*
 *  Copyright 2019 TheRedSpy15
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.black_onyx.Presto.cleaner;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.black_onyx.Presto.R;
import com.black_onyx.Presto.app.Activity;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdBase;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.fxn.stash.Stash;
import com.google.android.gms.ads.formats.UnifiedNativeAd;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;

public class CleanerMainActivity extends Activity {

    ConstraintSet constraintSet = new ConstraintSet();
    static boolean running = false;
    SharedPreferences prefs;
    static boolean cleanMode;

    LinearLayout fileListView;
    ScrollView fileScrollView;
    ProgressBar scanPBar;
    TextView progressText;
    TextView statusText;
    ConstraintLayout layout;
    FancyButton fancyButton;
    private UnifiedNativeAd nativeAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cleaner_main);
        Stash.init(getApplicationContext());
        fancyButton = findViewById(R.id.cleanButton);
        cleanMode = false;
        fancyButton.setText("Scan");

        fileListView = findViewById(R.id.fileListView);
        fileScrollView = findViewById(R.id.fileScrollView);
        scanPBar = findViewById(R.id.scanProgress);
        progressText = findViewById(R.id.ScanTextView);
        statusText = findViewById(R.id.statusTextView);
        layout = findViewById(R.id.main_layout);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        constraintSet.clone(layout);

        requestWriteExternalPermission();
        loadNativeAd();
    }

    /**
     * Starts the settings activity
     * @param view the view that is clickedprefs = getSharedPreferences("Settings",0);
     */
    public final void settings(View view) {
        Intent intent = new Intent(this, CleanerSettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Runs search and delete on background thread
     */
    public final void clean(View view) {
        if (!running) {
            if (!cleanMode){
                cleanMode = true;
                new Thread(()-> scan(false)).start();
                fancyButton.setText("Clean");
            }else{
                cleanMode = false;
                new Thread(()-> scan(true)).start();
                fancyButton.setText("Scan");
            }
        }
    }

    public void animateBtn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition(layout);
        }
        constraintSet.clear(R.id.cleanButton, ConstraintSet.TOP);
        constraintSet.clear(R.id.statusTextView, ConstraintSet.BOTTOM);
        constraintSet.setMargin(R.id.statusTextView, ConstraintSet.TOP,50);
        constraintSet.applyTo(layout);
    }

    /**
     * Searches entire device, adds all files to a list, then a for each loop filters
     * out files for deletion. Repeats the process as long as it keeps finding files to clean,
     * unless nothing is found to begin with
     */
    @SuppressLint("SetTextI18n")
    private void scan(boolean delete) {
        Looper.prepare();
        running = true;
        reset();

        File path = Environment.getExternalStorageDirectory();

        // scanner setup
        CleanerFileScanner fs = new CleanerFileScanner(path);
        fs.setEmptyDir(prefs.getBoolean("empty", false));
        fs.setAutoWhite(prefs.getBoolean("auto_white", true));
        fs.setDelete(delete);
        fs.setGUI(this);

        // filters
        fs.setUpFilters(prefs.getBoolean("generic", true),
                prefs.getBoolean("aggressive", false),
                prefs.getBoolean("apk", false));

        // failed scan
        if (path.listFiles() == null) { // is this needed? yes.
            TextView textView = printTextView("Scan failed.", Color.RED);
            runOnUiThread(() -> fileListView.addView(textView));
        }

        runOnUiThread(() -> {
            animateBtn();
            statusText.setText(getString(R.string.status_running));
        });

        // start scanning
        long kilobytesTotal = fs.startScan();

        // crappy but working fix for percentage never reaching 100
        runOnUiThread(() -> {
            scanPBar.setProgress(scanPBar.getMax());
            progressText.setText("100%");
        });

        // kilobytes found/freed text
        runOnUiThread(() -> {
            if (delete) {
                statusText.setText(getString(R.string.freed) + " " + convertSize(kilobytesTotal));
            } else {
                statusText.setText(getString(R.string.found) + " " + convertSize(kilobytesTotal));
            }
        });
        fileScrollView.post(() -> fileScrollView.fullScroll(ScrollView.FOCUS_DOWN));

        running = false;
        Looper.loop();
    }


    /**
     * Convenience method to quickly create a textview
     * @param text - text of textview
     * @return - created textview
     */
    private synchronized TextView printTextView(String text, int color) {
        TextView textView = new TextView(CleanerMainActivity.this);
        textView.setTextColor(color);
        textView.setText(text);
        textView.setPadding(3,3,3,3);
        return textView;
    }

    private String convertSize(long length) {
        final DecimalFormat format = new DecimalFormat("#.##");
        final long MiB = 1024 * 1024;
        final long KiB = 1024;

        if (length > MiB) {
            return format.format(length / MiB) + " MB";
        }
        if (length > KiB) {
            return format.format(length / KiB) + " KB";
        }
        return format.format(length) + " B";
    }

    /**
     * Increments amount removed, then creates a text view to add to the scroll view.
     * If there is any error while deleting, turns text view of path red
     * @param file file to delete
     */
    synchronized TextView displayPath(File file) {
        // creating and adding a text view to the scroll view with path to file
        TextView textView = printTextView(file.getAbsolutePath(), getResources().getColor(R.color.colorSecondary));

        // adding to scroll view
        runOnUiThread(() -> fileListView.addView(textView));

        // scroll to bottom
        fileScrollView.post(() -> fileScrollView.fullScroll(ScrollView.FOCUS_DOWN));

        return textView;
    }


    /**
     * Removes all views present in fileListView (linear view), and sets found and removed
     * files to 0
     */
    private synchronized void reset() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        runOnUiThread(() -> {
            fileListView.removeAllViews();
            scanPBar.setProgress(0);
            scanPBar.setMax(1);
        });
    }

    /**
     * Request write permission
     */
    public synchronized void requestWriteExternalPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                1);
    }

    /**
     * Handles the whether the user grants permission. Launches new fragment asking the user to give file permission.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 &&
                grantResults.length > 0 &&
                grantResults[0] != PackageManager.PERMISSION_GRANTED)
            prompt();
    }

    /**
     * Launches the prompt activity
     */
    public final void prompt() {
        Intent intent = new Intent(this, CleanerPromptActivity.class);
        startActivity(intent);
    }

    private void loadNativeAd() {
            NativeAd nativeAd = new NativeAd(this, getString(R.string.fb_clean_ad_unit));
            nativeAd.setAdListener(new NativeAdListener() {
                @Override
                public void onMediaDownloaded(Ad ad) {
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    Log.e(getLocalClassName(), "FB Ad error code: "+ adError.getErrorCode() + ", FB Ad error message: " + adError.getErrorMessage());
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    if (nativeAd == null || nativeAd != ad) {
                        return;
                    }
                    inflateAd(nativeAd);
                }

                @Override
                public void onAdClicked(Ad ad) {
                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            });

            nativeAd.loadAd(NativeAdBase.MediaCacheFlag.ALL);
    }

    private void inflateAd(NativeAd nativeAd) {
        Context context = this;
        nativeAd.unregisterView();
        NativeAdLayout nativeAdLayout = findViewById(R.id.fb_native_ad_container);
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.facebook_native_ad_view, nativeAdLayout, false);
        AdIconView nativeAdIcon = adView.findViewById(R.id.fb_native_ad_icon);
        TextView nativeAdTitle = adView.findViewById(R.id.fb_ad_headline);

        nativeAdTitle.setText(nativeAd.getAdvertiserName());

        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdLayout);
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdIcon);

        nativeAd.registerViewForInteraction(
                adView,
                nativeAdIcon,
                clickableViews);

        nativeAdLayout.addView(adView);
    }

}
