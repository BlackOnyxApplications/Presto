package com.bhuvan_kumar.Presto.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bhuvan_kumar.Presto.GlideApp;
import com.bhuvan_kumar.Presto.activity.ManageDevicesActivity;
import com.bhuvan_kumar.Presto.activity.PreferencesActivity;
import com.bhuvan_kumar.Presto.app.Activity;
import com.bhuvan_kumar.Presto.config.AppConfig;
import com.bhuvan_kumar.Presto.object.NetworkDevice;
import com.bhuvan_kumar.Presto.service.CommunicationService;
import com.bhuvan_kumar.Presto.ui.callback.IconSupport;
import com.bhuvan_kumar.Presto.ui.callback.TitleSupport;
import com.bhuvan_kumar.Presto.util.AppUtils;
import com.bhuvan_kumar.Presto.R;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.bhuvan_kumar.Presto.app.Activity.REQUEST_PICK_PROFILE_PHOTO;

public class ProfileSettingsFragment extends Fragment implements IconSupport, TitleSupport, View.OnClickListener {
    public ProfileSettingsFragment() {}
    private TextView deviceNameText;
    private ImageView imageView;
    private UnifiedNativeAd nativeAd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_settings, container, false);
        final Activity activity = (Activity) getActivity();
        assert activity != null;
        NetworkDevice localDevice = AppUtils.getLocalDevice(activity);

        imageView = view.findViewById(R.id.layout_profile_picture_image_default);
        ImageView editImageView = view.findViewById(R.id.layout_profile_picture_image_preferred);
        deviceNameText = view.findViewById(R.id.header_default_device_name_text);

        deviceNameText.setText(localDevice.nickname);
        loadProfilePictureInto(activity, localDevice.nickname, imageView);

        editImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showProfileEditorDialog();
            }
        });

        LinearLayout trused_devices = view.findViewById(R.id.trusted_devices);
        LinearLayout preferences = view.findViewById(R.id.preferences);
        LinearLayout share_app = view.findViewById(R.id.share_app);
        LinearLayout clear_preferences = view.findViewById(R.id.clear_preferences);

        trused_devices.setOnClickListener(this);
        preferences.setOnClickListener(this);
        share_app.setOnClickListener(this);
        clear_preferences.setOnClickListener(this);

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

                    AdLoader.Builder builder = new AdLoader.Builder(context, getString(R.string.settings_ad_unit_id));
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
            NativeAd nativeAd = new NativeAd(getActivity(), getString(R.string.fb_settings_ad_unit));
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


    public void showProfileEditorDialog()
    {
        Activity activity = (Activity) getActivity();
        if(activity != null) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.layout_profile_editor, null);
            final ImageView image = dialogView.findViewById(R.id.layout_profile_picture_image_default);
            final ImageView editImage = dialogView.findViewById(R.id.layout_profile_picture_image_preferred);
            final EditText editText = dialogView.findViewById(R.id.editText);
            final String[] deviceName = {AppUtils.getLocalDeviceName(getContext())};

            editText.getText().clear();
            editText.getText().append(deviceName[0]);
            loadProfilePictureInto(activity, deviceName[0], image);
            editText.requestFocus();

            editImage.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), REQUEST_PICK_PROFILE_PHOTO);
                }
            });

            alertBuilder.setNegativeButton(R.string.butn_remove, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    activity.deleteFile("profilePicture");
                    activity.notifyUserProfileChanged();
                }
            });

            alertBuilder.setPositiveButton(R.string.butn_save, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    deviceName[0] = editText.getText().toString().trim();
                    AppUtils.getDefaultPreferences(getContext()).edit()
                            .putString("device_name", deviceName[0])
                            .apply();
                    deviceNameText.setText(deviceName[0]);
                    loadProfilePictureInto(activity, deviceName[0], imageView);
                }
            });

            alertBuilder.setNeutralButton(R.string.butn_close, null);
            alertBuilder.setView(dialogView);
            alertBuilder.show();
        }
    }

    private void loadProfilePictureInto(Activity activity, String deviceName, ImageView imageView)
    {
        try {
            FileInputStream inputStream = activity.openFileInput("profilePicture");
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            GlideApp.with(this)
                    .load(bitmap)
                    .circleCrop()
                    .into(imageView);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            imageView.setImageDrawable(AppUtils.getDefaultIconBuilder(activity).buildRound(deviceName));
        }
    }

    @Override
    public int getIconRes() {
        return R.drawable.ic_settings_white_24dp;
    }

    @Override
    public CharSequence getTitle(Context context) {
        return "Settings";
    }

    private void toggleTrustZone()
    {
        AppUtils.startForegroundService(getActivity(), new Intent(getActivity(), CommunicationService.class)
                .setAction(CommunicationService.ACTION_TOGGLE_SEAMLESS_MODE));
    }

    private void constructResetDialog(){
       final Context context = getActivity();
        assert context != null;
        new AlertDialog.Builder(context)
                .setTitle(R.string.ques_resetToDefault)
                .setMessage(R.string.text_resetPreferencesToDefaultSummary)
                .setNegativeButton(R.string.butn_cancel, null)
                .setPositiveButton(R.string.butn_proceed, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    AppUtils.getDefaultPreferences(context).edit()
                            .clear()
                            .apply();

                    AppUtils.getDefaultLocalPreferences(context).edit()
                            .clear()
                            .apply();

                    Objects.requireNonNull(getActivity()).finish();
                    }
                })
                .show();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.trusted_devices:
                startActivity(new Intent(getActivity(), ManageDevicesActivity.class));
                break;

            case R.id.preferences:
                startActivity(new Intent(getActivity(), PreferencesActivity.class));
                break;

            case R.id.share_app:
                shareApp();
                break;

            case R.id.clear_preferences:
                constructResetDialog();
                break;
        }
    }

    private void shareApp(){
        final Context context = getActivity();
        assert context != null;
        new Handler(Looper.myLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    String textToShare = context.getString(R.string.text_linkTrebleshot,
                            AppConfig.URI_GOOGLE_PLAY);
                    Intent sendIntent = new Intent(Intent.ACTION_SEND)
                            .putExtra(Intent.EXTRA_TEXT, textToShare)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            .setType("text/plain");

                    context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.text_fileShareAppChoose)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
