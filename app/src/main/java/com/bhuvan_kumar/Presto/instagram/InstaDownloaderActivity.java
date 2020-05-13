package com.bhuvan_kumar.Presto.instagram;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bhuvan_kumar.Presto.R;
import com.bhuvan_kumar.Presto.app.Activity;
import com.bhuvan_kumar.Presto.model.StoryModel;
import com.bhuvan_kumar.Presto.util.Constants;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import static android.view.View.GONE;

public class InstaDownloaderActivity extends Activity {
    public static final String TAG = "InstaDownloaderActivity";
    private InstaStoryAdapter recyclerViewAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout recyclerLayout;
    ArrayList<Object> filesList;
    private String link;
    private SpinKitView spinKitView;
    private Button download_button;
    private UnifiedNativeAd nativeAd;
    private final String FOLDER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.SAVE_FOLDER_NAME + "Instagram/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insta_downloader);
        initComponents();
        setUpRecyclerView();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        refreshAd(getString(R.string.game_hint_ad_unit_id));

        EditText editText = findViewById(R.id.insta_link);
        download_button = findViewById(R.id.insta_download);
        spinKitView = findViewById(R.id.spin_kit);

        if(getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey("LINK")) {
            String instaLinkDirect = getIntent().getStringExtra("LINK");
            editText.setText(instaLinkDirect);
            download_button.setAlpha(1f);
            download_button.setClickable(true);
        }else{
            download_button.setText("Invalid URL");
            download_button.setAlpha(0.5f);
            download_button.setClickable(false);
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(Helpers.isInstagramUrlValid(s.toString().trim())){
                    download_button.setText(R.string.butn_download);
                    download_button.setAlpha(1f);
                    download_button.setClickable(true);
                }else{
                    download_button.setText("Invalid URL");
                    download_button.setAlpha(0.5f);
                    download_button.setClickable(false);
                }
            }
        });

        download_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                link = editText.getText().toString().trim();
                if (Helpers.isInstagramUrlValid(link)) {
                    download_button.setVisibility(View.GONE);
                    spinKitView.setVisibility(View.VISIBLE);
                    new DownloadTask().execute();
                }
            }
        });
    }

    private void refreshAd(String adUnit) {

        AdLoader.Builder builder = new AdLoader.Builder(Objects.requireNonNull(this), adUnit);

        builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                if (nativeAd != null) {
                    nativeAd.destroy();
                }
                nativeAd = unifiedNativeAd;
                FrameLayout frameLayout;
                UnifiedNativeAdView adView;
                frameLayout = findViewById(R.id.fl_adplaceholder);
                adView = (UnifiedNativeAdView) getLayoutInflater()
                        .inflate(R.layout.home_page_custom_ad, null);

                populateUnifiedNativeAdView(unifiedNativeAd, adView);
                frameLayout.removeAllViews();
                frameLayout.addView(adView);
            }
        });

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.e(getLocalClassName(), "errorCode :"+ errorCode);
            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {

        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);

    }

    private void initComponents() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRecyclerView);
        recyclerLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerLayout.setRefreshing(true);
                setUpRecyclerView();
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recyclerLayout.setRefreshing(false);
                    }
                }, 2000);

            }
        });
    }

    private void setUpRecyclerView() {
        ArrayList<Object> files = getData();
        View emptyLayout = findViewById(R.id.empty_layout);
        if(files.isEmpty()){
            emptyLayout.setVisibility(View.VISIBLE);
            ImageView emptyIcon = findViewById(R.id.empty_icon);
            emptyIcon.setImageResource(R.drawable.instagram);
            TextView emptytext = findViewById(R.id.empty_text);
            emptytext.setText("No Media Downloaded");
        }
        else
            emptyLayout.setVisibility(View.GONE);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdapter = new InstaStoryAdapter(InstaDownloaderActivity.this, files);

        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.getItemCount();
        recyclerViewAdapter.notifyDataSetChanged();

    }

    private ArrayList<Object> getData() {
        filesList = new ArrayList<>();
        StoryModel f;
        File targetDirector = new File(FOLDER_PATH);
        File[] files = targetDirector.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }
        try {
            Arrays.sort(files, new Comparator() {
                public int compare(Object o1, Object o2) {

                    if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                        return -1;
                    } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                        return +1;
                    } else {
                        return 0;
                    }
                }
            });

            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                f = new StoryModel();
                f.setName("Insta: "+(i));
                f.setUri(Uri.fromFile(file));
                f.setPath(file.getAbsolutePath());
                f.setFilename(file.getName());
                f.setFileSize(file.length());
                filesList.add(f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filesList;
    }

    private class DownloadTask extends AsyncTask<String, Boolean, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            InstagramDownloader downloader = new InstagramDownloader(InstaDownloaderActivity.this);
            return downloader.downloadMedia(link, FOLDER_PATH);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    download_button.setVisibility(View.VISIBLE);
                    spinKitView.setVisibility(View.GONE);
                    if(result){
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), Html.fromHtml("<font color=\"#000000\">File is downloading!</font>"), Snackbar.LENGTH_LONG);
                        snackbar.getView().setBackgroundColor(ContextCompat.getColor(InstaDownloaderActivity.this, R.color.save_green));
                        snackbar.show();
                        Log.e(TAG, "Download PostExecution: " + result);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setUpRecyclerView();
                                recyclerViewAdapter.notifyItemInserted(0);
                            }
                        }, 1000);
                    }else{
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), Html.fromHtml("<font color=\"#000000\">Cannot be saved!</font>"), Snackbar.LENGTH_LONG);
                        snackbar.getView().setBackgroundColor(ContextCompat.getColor(InstaDownloaderActivity.this, R.color.colorSecondary));
                        snackbar.show();
                        Log.e(TAG, result + "");
                    }
                }
            });
        }
    }
}
