package com.black_onyx.Presto.activity;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.black_onyx.Presto.R;
import com.black_onyx.Presto.adapter.StoryAdapter;
import com.black_onyx.Presto.app.Activity;
import com.black_onyx.Presto.model.StoryModel;
import com.black_onyx.Presto.util.Constants;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_saver_main);
        initComponents();
        setUpRecyclerView();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        refreshAd(null, getString(R.string.game_head_ad_unit_id));
    }
    private void refreshAd(View view, String adUnit) {

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
                if(view == null){
                    frameLayout = findViewById(R.id.fl_adplaceholder);
                    adView = (UnifiedNativeAdView) getLayoutInflater()
                            .inflate(R.layout.game_page_ad_view, null);
                }else{
                    frameLayout = view.findViewById(R.id.fl_adplaceholder);
                    adView = (UnifiedNativeAdView) getLayoutInflater()
                            .inflate(R.layout.home_page_custom_ad, null);
                }

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

