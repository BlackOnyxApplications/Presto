package com.bhuvan_kumar.Presto.fragment;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
 import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bhuvan_kumar.Presto.activity.ConnectionManagerActivity;
import com.bhuvan_kumar.Presto.activity.ContentSharingActivity;
import com.bhuvan_kumar.Presto.service.CommunicationService;
import com.bhuvan_kumar.Presto.ui.callback.IconSupport;
import com.bhuvan_kumar.Presto.ui.callback.TitleSupport;
import com.bhuvan_kumar.Presto.util.AppUtils;
import com.bhuvan_kumar.Presto.R;
import com.bhuvan_kumar.Presto.wordsearch.features.gameplay.GamePlayActivity;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import com.facebook.ads.*;

public class ShareHomeFragment extends Fragment implements IconSupport, TitleSupport {

    public ShareHomeFragment() {}
    private UnifiedNativeAd nativeAd;
    private Runnable mTicker = null;
    private int backToAppFlags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_share_home, container, false);

        View viewSend = view.findViewById(R.id.sendLayoutButton);
        View viewReceive = view.findViewById(R.id.receiveLayoutButton);

        View gameButton = view.findViewById(R.id.game);
        gameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GamePlayActivity.class);
                intent.putExtra(GamePlayActivity.EXTRA_ROW_COUNT, 5);
                intent.putExtra(GamePlayActivity.EXTRA_COL_COUNT, 5);
                startActivity(intent);
            }
        });

        viewSend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(getContext(), ContentSharingActivity.class));
            }
        });

        viewReceive.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(getContext(), ConnectionManagerActivity.class)
                        .putExtra(ConnectionManagerActivity.EXTRA_ACTIVITY_SUBTITLE, getString(R.string.text_receive))
                        .putExtra(ConnectionManagerActivity.EXTRA_REQUEST_TYPE, ConnectionManagerActivity.RequestType.MAKE_ACQUAINTANCE.toString()));
            }
        });

        CardView werbAppView = view.findViewById(R.id.werb_app_view);
        werbAppView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    final String werb_free = "com.developer.bhuvan_kumar.werb_free";
                    Uri uri = Uri.parse("market://details?id=" + werb_free);
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    goToMarket.addFlags(backToAppFlags);
                    try {
                        startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=" + werb_free)));
                    }
                }
        });
        initializeAds(view);
        return view;
    }

    private void initializeAds(View view){
        if(getActivity() != null) {
            AudienceNetworkAds.initialize(getActivity());
            MobileAds.initialize(getActivity(), new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                }
            });

            try {
                Handler mHandler = new Handler();
                mTicker = new Runnable() {
                    @Override
                    public void run() {
                        if(isAdded()) {
                            refreshAd(view);
                            loadNativeAd(view);
                        }
                        mHandler.postDelayed(mTicker, 1000 * 12);
                    }
                };
                mHandler.postDelayed(mTicker, 1000 * 12);
                if(isAdded()) {
                    refreshAd(view);
                    loadNativeAd(view);
                }
            } catch (Exception ex) {
                Log.e(getTag(), "" + ex.toString());
            }
        }
    }

    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {

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

    }

    private void refreshAd(View view) {
        Context context = getActivity();
        if(context!=null) {
            try{
                List<Fragment> fragments = getFragmentManager() != null ? getFragmentManager().getFragments() : new ArrayList<Fragment>();
                if(fragments.size() > 1) {

                    AdLoader.Builder builder = new AdLoader.Builder(context, getString(R.string.home_ad_unit_id));
                    builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                        @Override
                        public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                            if (nativeAd != null) {
                                nativeAd.destroy();
                            }
                            nativeAd = unifiedNativeAd;
                            FrameLayout frameLayout = view.findViewById(R.id.fl_adplaceholder);
                            try {
                                UnifiedNativeAdView adView = (UnifiedNativeAdView) getLayoutInflater()
                                        .inflate(R.layout.home_page_custom_ad, null);
                                populateUnifiedNativeAdView(unifiedNativeAd, adView);
                                frameLayout.removeAllViews();
                                frameLayout.addView(adView);
                            } catch (Exception e) {
                                Log.e(getTag(), e.toString());
                            }
                        }
                    });

                    AdLoader adLoader = builder.withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(int errorCode) {

                        }
                    }).build();

                    adLoader.loadAd(new AdRequest.Builder().build());
                }
        } catch (Exception e){
            Log.e(getTag(), e.toString());
        }
    }
    }


//    Facebook ads
private void loadNativeAd(View view) {
    Log.e(getTag(), "Loading FB native ads");
        if(getActivity() != null) {
            NativeAd nativeAd = new NativeAd(getActivity(), getString(R.string.fb_home_ad_unit));
            Log.e(getTag(), nativeAd.getPlacementId());
            nativeAd.setAdListener(new NativeAdListener() {
                @Override
                public void onMediaDownloaded(Ad ad) {
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    Log.e(getTag(), "FB Ad error code: "+ adError.getErrorCode() + ", FB Ad error message: " + adError.getErrorMessage());
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    if (nativeAd == null || nativeAd != ad) {
                        Log.e(getTag(), ad.getPlacementId());
                        return;
                    }
                    inflateAd(nativeAd, view);
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
}

private void inflateAd(NativeAd nativeAd, View view) {
        Context context = getActivity();
        if(context != null) {
            nativeAd.unregisterView();
            NativeAdLayout nativeAdLayout = view.findViewById(R.id.fb_native_ad_container);
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public int getIconRes() {
        return R.drawable.ic_home_white_24dp;
    }

    @Override
    public CharSequence getTitle(Context context) {
        return context.getString(R.string.text_home);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        AppUtils.startForegroundService(getActivity(), new Intent(getActivity(), CommunicationService.class)
                .setAction(CommunicationService.ACTION_REQUEST_TASK_RUNNING_LIST_CHANGE));
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }
}
