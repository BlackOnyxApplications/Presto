package com.bhuvan_kumar.Presto.activity;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bhuvan_kumar.Presto.R;
import com.bhuvan_kumar.Presto.adapter.StoryAdapter;
import com.bhuvan_kumar.Presto.app.Activity;
import com.bhuvan_kumar.Presto.model.StoryModel;
import com.bhuvan_kumar.Presto.util.Constants;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdBase;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static android.view.View.GONE;

public class StatusSaverActivity extends Activity {
    public static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 123;
    private StoryAdapter recyclerViewAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout recyclerLayout;
    public static final int ITEMS_PER_AD = 6;
    ArrayList<Object> filesList;
    private UnifiedNativeAd nativeAd;
    private final String TAG = "StatusSaverActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_saver_main);
        initComponents();
        setUpRecyclerView();

       initializeAds();
    }
    private void initializeAds(){
        AudienceNetworkAds.initialize(this);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        try {
            loadGoogleAd();
        } catch (Exception ex) {
            Log.e(TAG, "initializeAds: " + ex.toString());
        }
    }

    private void loadGoogleAd() {
        try{
            AdLoader.Builder builder = new AdLoader.Builder(this, getString(R.string.receiving_page_ad_unit_id));
            builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                @Override
                public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                    if (nativeAd != null) {
                        nativeAd.destroy();
                    }
                    nativeAd = unifiedNativeAd;
                    FrameLayout frameLayout = findViewById(R.id.fl_adplaceholder);
                    try {
                        UnifiedNativeAdView adView = (UnifiedNativeAdView) getLayoutInflater()
                                .inflate(R.layout.home_page_custom_ad, null);

                        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
                        adView.setIconView(adView.findViewById(R.id.ad_app_icon));

                        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

                        if (nativeAd.getIcon() == null) {
                            adView.getIconView().setVisibility(View.GONE);
                        } else {
                            ((ImageView) adView.getIconView()).setImageDrawable(
                                    nativeAd.getIcon().getDrawable());
                            adView.getIconView().setVisibility(View.VISIBLE);
                        }

                        adView.setNativeAd(nativeAd);

                        frameLayout.removeAllViews();
                        frameLayout.addView(adView);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
            });

            AdLoader adLoader = builder.withAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int errorCode) {
                    loadFacebookAd();
                    Log.e(TAG, "Google Ads errorCode: " + errorCode);
                }
            }).build();

            adLoader.loadAd(new AdRequest.Builder().build());
        } catch (Exception e){
            Log.e(TAG, e.toString());
        }
    }

    private void loadFacebookAd() {
        NativeAd nativeAd = new NativeAd(this, getString(R.string.fb_whatsapp_ad_unit));
        nativeAd.setAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                Log.e(TAG, "FB Ad error code: "+ adError.getErrorCode() + ", FB Ad error message: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (nativeAd == null || nativeAd != ad) {
                    return;
                }
                nativeAd.unregisterView();
                NativeAdLayout nativeAdLayout = findViewById(R.id.fb_native_ad_container);
                LayoutInflater inflater = LayoutInflater.from(StatusSaverActivity.this);
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

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        });

        nativeAd.loadAd(NativeAdBase.MediaCacheFlag.ALL);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpRecyclerView();
                }
                break;
        }
    }

    private void setUpRecyclerView() {
        ArrayList<Object> files = getData();
        LinearLayout empty_layout = findViewById(R.id.empty_layout);
        if(files.isEmpty())empty_layout.setVisibility(View.VISIBLE);
        else empty_layout.setVisibility(View.GONE);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdapter = new StoryAdapter(StatusSaverActivity.this, files);

        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.getItemCount();
        recyclerViewAdapter.notifyDataSetChanged();
    }

    private ArrayList<Object> getData() {
        filesList = new ArrayList<>();
        StoryModel f;
        String targetPath = Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.FOLDER_NAME + "Media/.Statuses";
        File targetDirector = new File(targetPath);
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File f, String name)
            {
                return !name.endsWith(".nomedia");
            }
        };
        File[] files = targetDirector.listFiles(filter);
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
                f.setName("Status: "+(i));
                f.setUri(Uri.fromFile(file));
                f.setPath(files[i].getAbsolutePath());
                f.setFilename(file.getName());
                filesList.add(f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filesList;
    }

}

