package com.bhuvan_kumar.Presto.fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bhuvan_kumar.Presto.activity.ConnectionManagerActivity;
import com.bhuvan_kumar.Presto.activity.ContentSharingActivity;
import com.bhuvan_kumar.Presto.activity.StatusSaverActivity;
//import com.bhuvan_kumar.Presto.activity.YoutubeVideoSaver;
import com.bhuvan_kumar.Presto.activity.WebShareActivity;
import com.bhuvan_kumar.Presto.cleaner.CleanerMainActivity;
import com.bhuvan_kumar.Presto.instagram.InstaDownloaderActivity;
import com.bhuvan_kumar.Presto.service.CommunicationService;
import com.bhuvan_kumar.Presto.ui.callback.IconSupport;
import com.bhuvan_kumar.Presto.ui.callback.TitleSupport;
import com.bhuvan_kumar.Presto.util.AppUtils;
import com.bhuvan_kumar.Presto.R;
import com.bhuvan_kumar.Presto.wordsearch.features.gameplay.GamePlayActivity;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.NativeAd;
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

import java.util.ArrayList;
import java.util.List;
import com.facebook.ads.*;

public class ShareHomeFragment extends Fragment implements IconSupport, TitleSupport {

    public ShareHomeFragment() {}
    private UnifiedNativeAd nativeAd;
    private int backToAppFlags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_share_home, container, false);

        View viewSend = view.findViewById(R.id.sendLayoutButton);
        View viewReceive = view.findViewById(R.id.receiveLayoutButton);
        View shareBrowser = view.findViewById(R.id.share_browser);
        shareBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), WebShareActivity.class));
            }
        });

        View shareScreen = view.findViewById(R.id.share_screen);
        shareScreen.setVisibility(View.GONE);
        shareScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(getActivity(), ScreenMirrorMainActivity.class));
            }
        });

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
        if(getActivity() != null){
            boolean is_werb_pro_installed, is_werb_free_installed;
            try {
                getActivity().getPackageManager().getPackageInfo("com.developer.bhuvan_kumar.werb_pro", 0);
                is_werb_pro_installed = true;
            } catch (PackageManager.NameNotFoundException e) {
                is_werb_pro_installed = false;
            }
            try {
                getActivity().getPackageManager().getPackageInfo("com.developer.bhuvan_kumar.werb_free", 0);
                is_werb_free_installed = true;
            } catch (PackageManager.NameNotFoundException e) {
                is_werb_free_installed = false;
            }
            if(is_werb_free_installed || is_werb_pro_installed){
                werbAppView.setVisibility(View.GONE);
            }else{
                werbAppView.setVisibility(View.VISIBLE);
            }
            werbAppView.setVisibility(View.GONE);
        }
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

        CardView cleanerView = view.findViewById(R.id.clean_card);
        cleanerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cleanerIntent = new Intent(getActivity(), CleanerMainActivity.class);
                startActivity(cleanerIntent);
            }
        });
        CardView WhatsAppCard = view.findViewById(R.id.status_saver_card);
        WhatsAppCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statusSaverIntent = new Intent(getActivity(), StatusSaverActivity.class);
                startActivity(statusSaverIntent);
            }
        });

        CardView instagramCard = view.findViewById(R.id.insta_card);
        instagramCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent instaIntent = new Intent(getActivity(), InstaDownloaderActivity.class);
                startActivity(instaIntent);
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
                if(isAdded()) {
                    refreshAd(view);
                    loadNativeAd(view);
                }
            } catch (Exception ex) {
                Log.e(getTag(), "initializeAds: " + ex.toString());
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
                            Log.e(getTag(), "refreshAd errorCode: " + errorCode);
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
        if(getActivity() != null) {
            NativeAd nativeAd = new NativeAd(getActivity(), getString(R.string.fb_home_ad_unit));
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
