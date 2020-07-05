package com.bhuvan_kumar.Presto.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.bhuvan_kumar.Presto.app.Activity;
import com.bhuvan_kumar.Presto.app.EditableListFragment;
import com.bhuvan_kumar.Presto.app.EditableListFragmentImpl;
import com.bhuvan_kumar.Presto.fragment.ApplicationListFragment;
import com.bhuvan_kumar.Presto.fragment.FileExplorerFragment;
import com.bhuvan_kumar.Presto.fragment.ImageListFragment;
import com.bhuvan_kumar.Presto.fragment.MusicListFragment;
import com.bhuvan_kumar.Presto.fragment.VideoListFragment;
import com.bhuvan_kumar.Presto.ui.callback.SharingActionModeCallback;
import com.bhuvan_kumar.Presto.R;
import com.bhuvan_kumar.Presto.adapter.SmartFragmentPagerAdapter;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdBase;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.genonbeta.android.framework.widget.PowerfulActionMode;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class ContentSharingActivity extends Activity
{
    public static final String TAG = ContentSharingActivity.class.getSimpleName();

    private PowerfulActionMode mMode;
    private SharingActionModeCallback mSelectionCallback;
    private Activity.OnBackPressedListener mBackPressedListener;
    private UnifiedNativeAd nativeAd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_sharing);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        mMode = findViewById(R.id.activity_content_sharing_action_mode);
        final TabLayout tabLayout = findViewById(R.id.activity_content_sharing_tab_layout);
        final ViewPager viewPager = findViewById(R.id.activity_content_sharing_view_pager);

        mSelectionCallback = new SharingActionModeCallback(null);
        final PowerfulActionMode.SelectorConnection selectorConnection = new PowerfulActionMode.SelectorConnection(mMode, mSelectionCallback);

        final SmartFragmentPagerAdapter pagerAdapter = new SmartFragmentPagerAdapter(this, getSupportFragmentManager())
        {
            @Override
            public void onItemInstantiated(StableItem item)
            {
                EditableListFragmentImpl fragmentImpl = (EditableListFragmentImpl) item.getInitiatedItem();
                //EditableListFragmentModelImpl fragmentModelImpl = (EditableListFragmentModelImpl) item.getInitiatedItem();

                fragmentImpl.setSelectionCallback(mSelectionCallback);
                fragmentImpl.setSelectorConnection(selectorConnection);
                //fragmentModelImpl.setLayoutClickListener(groupLayoutClickListener);

                if (viewPager.getCurrentItem() == item.getCurrentPosition())
                    attachListeners(fragmentImpl);
            }
        };

        //mMode.setContainerLayout(findViewById(R.id.activity_content_sharing_action_mode_layout));
        Bundle fileExplorerArgs = new Bundle();
        fileExplorerArgs.putBoolean(FileExplorerFragment.ARG_SELECT_BY_CLICK, true);

        pagerAdapter.add(new SmartFragmentPagerAdapter.StableItem(0, ApplicationListFragment.class, null));
        pagerAdapter.add(new SmartFragmentPagerAdapter.StableItem(1, FileExplorerFragment.class, fileExplorerArgs)
                .setTitle(getString(R.string.text_files)));
        pagerAdapter.add(new SmartFragmentPagerAdapter.StableItem(2, MusicListFragment.class, null));
        pagerAdapter.add(new SmartFragmentPagerAdapter.StableItem(3, ImageListFragment.class, null));
        pagerAdapter.add(new SmartFragmentPagerAdapter.StableItem(4, VideoListFragment.class, null));

        pagerAdapter.createTabs(tabLayout, false, true);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                viewPager.setCurrentItem(tab.getPosition());

                final EditableListFragment fragment = (EditableListFragment) pagerAdapter.getItem(tab.getPosition());

                attachListeners(fragment);

                if (fragment.getAdapterImpl() != null)
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            fragment.getAdapterImpl().notifyAllSelectionChanges();
                        }
                    }, 200);
            }

            @Override
            public void onTabUnselected(final TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {

            }
        });

//        initializeAds();
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
                AdLoader.Builder builder = new AdLoader.Builder(this, getString(R.string.transfer_ad_unit_id));
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
            NativeAd nativeAd = new NativeAd(this, getString(R.string.fb_transfer_history_ad_unit));
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
                    LayoutInflater inflater = LayoutInflater.from(ContentSharingActivity.this);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == android.R.id.home)
            finish();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    @Override
    public void onBackPressed()
    {
        if (mBackPressedListener == null || !mBackPressedListener.onBackPressed()) {
            if (mMode.hasActive(mSelectionCallback))
                mMode.finish(mSelectionCallback);
            else
                super.onBackPressed();
        }
    }

    public void attachListeners(EditableListFragmentImpl fragment)
    {
        mSelectionCallback.updateProvider(fragment);
        mBackPressedListener = fragment instanceof Activity.OnBackPressedListener
                ? (OnBackPressedListener) fragment
                : null;
    }
}
