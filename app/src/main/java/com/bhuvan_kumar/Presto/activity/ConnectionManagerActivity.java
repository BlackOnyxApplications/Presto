package com.bhuvan_kumar.Presto.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentTransaction;

import com.bhuvan_kumar.Presto.app.Activity;
import com.bhuvan_kumar.Presto.database.AccessDatabase;
import com.bhuvan_kumar.Presto.fragment.BarcodeConnectFragment;
import com.bhuvan_kumar.Presto.fragment.HotspotManagerFragment;
import com.bhuvan_kumar.Presto.fragment.NetworkDeviceListFragment;
import com.bhuvan_kumar.Presto.fragment.NetworkManagerFragment;
import com.bhuvan_kumar.Presto.ui.UIConnectionUtils;
import com.bhuvan_kumar.Presto.ui.UITask;
import com.bhuvan_kumar.Presto.ui.callback.NetworkDeviceSelectedListener;
import com.bhuvan_kumar.Presto.ui.callback.TitleSupport;
import com.bhuvan_kumar.Presto.ui.help.ConnectionSetUpAssistant;
import com.bhuvan_kumar.Presto.util.AppUtils;
import com.bhuvan_kumar.Presto.util.ConnectionUtils;
import com.bhuvan_kumar.Presto.util.NetworkDeviceLoader;
import com.bhuvan_kumar.Presto.R;
import com.bhuvan_kumar.Presto.dialog.ManualIpAddressConnectionDialog;
import com.bhuvan_kumar.Presto.object.NetworkDevice;
import com.bhuvan_kumar.Presto.service.CommunicationService;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdBase;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.genonbeta.android.framework.ui.callback.SnackbarSupport;
import com.genonbeta.android.framework.util.Interrupter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class ConnectionManagerActivity
        extends Activity
        implements SnackbarSupport
{
    public static final String ACTION_CHANGE_FRAGMENT = "com.genonbeta.intent.action.CONNECTION_MANAGER_CHANGE_FRAGMENT";
    public static final String EXTRA_FRAGMENT_ENUM = "extraFragmentEnum";
    public static final String EXTRA_DEVICE_ID = "extraDeviceId";
    public static final String EXTRA_CONNECTION_ADAPTER = "extraConnectionAdapter";
    public static final String EXTRA_REQUEST_TYPE = "extraRequestType";
    public static final String EXTRA_ACTIVITY_SUBTITLE = "extraActivitySubtitle";

    private final IntentFilter mFilter = new IntentFilter();
    private HotspotManagerFragment mHotspotManagerFragment;
    private BarcodeConnectFragment mBarcodeConnectFragment;
    private NetworkManagerFragment mNetworkManagerFragment;
    private NetworkDeviceListFragment mDeviceListFragment;
    private OptionsFragment mOptionsFragment;
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mToolbarLayout;
    private ProgressBar mProgressBar;
    private String mTitleProvided;
    private static String mTitle;
    private RequestType mRequestType = RequestType.RETURN_RESULT;

    private final NetworkDeviceSelectedListener mDeviceSelectionListener = new NetworkDeviceSelectedListener()
    {
        @Override
        public boolean onNetworkDeviceSelected(NetworkDevice networkDevice, NetworkDevice.Connection connection)
        {
            if (mRequestType.equals(RequestType.RETURN_RESULT)) {
                setResult(RESULT_OK, new Intent()
                        .putExtra(EXTRA_DEVICE_ID, networkDevice.deviceId)
                        .putExtra(EXTRA_CONNECTION_ADAPTER, connection.adapterName));

                finish();
            } else {
                ConnectionUtils connectionUtils = ConnectionUtils.getInstance(ConnectionManagerActivity.this);
                UIConnectionUtils uiConnectionUtils = new UIConnectionUtils(connectionUtils, ConnectionManagerActivity.this);

                UITask uiTask = new UITask()
                {
                    @Override
                    public void updateTaskStarted(Interrupter interrupter)
                    {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void updateTaskStopped()
                    {
                        mProgressBar.setVisibility(GONE);
                    }
                };

                NetworkDeviceLoader.OnDeviceRegisteredListener registeredListener = new NetworkDeviceLoader.OnDeviceRegisteredListener()
                {
                    @Override
                    public void onDeviceRegistered(AccessDatabase database, final NetworkDevice device, final NetworkDevice.Connection connection)
                    {
                        createSnackbar(R.string.mesg_completing).show();
                    }
                };

                uiConnectionUtils.makeAcquaintance(ConnectionManagerActivity.this, uiTask,
                        connection.ipAddress, -1, registeredListener);
            }

            return true;
        }

        @Override
        public boolean isListenerEffective()
        {
            return true;
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (ACTION_CHANGE_FRAGMENT.equals(intent.getAction())
                    && intent.hasExtra(EXTRA_FRAGMENT_ENUM)) {
                String fragmentEnum = intent.getStringExtra(EXTRA_FRAGMENT_ENUM);

                try {
                    AvailableFragment value = AvailableFragment.valueOf(fragmentEnum);

                    if (AvailableFragment.EnterIpAddress.equals(value))
                        showEnterIpAddressDialog();
                    else
                        setFragment(value);
                } catch (Exception e) {
                    // do nothing
                }
            } else if (mRequestType.equals(RequestType.RETURN_RESULT)) {
                if (CommunicationService.ACTION_DEVICE_ACQUAINTANCE.equals(intent.getAction())
                        && intent.hasExtra(CommunicationService.EXTRA_DEVICE_ID)
                        && intent.hasExtra(CommunicationService.EXTRA_CONNECTION_ADAPTER_NAME)) {
                    NetworkDevice device = new NetworkDevice(intent.getStringExtra(CommunicationService.EXTRA_DEVICE_ID));
                    NetworkDevice.Connection connection = new NetworkDevice.Connection(device.deviceId, intent.getStringExtra(CommunicationService.EXTRA_CONNECTION_ADAPTER_NAME));

                    try {
                        AppUtils.getDatabase(ConnectionManagerActivity.this).reconstruct(device);
                        AppUtils.getDatabase(ConnectionManagerActivity.this).reconstruct(connection);

                        mDeviceSelectionListener.onNetworkDeviceSelected(device, connection);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (mRequestType.equals(RequestType.MAKE_ACQUAINTANCE)) {
                if (CommunicationService.ACTION_INCOMING_TRANSFER_READY.equals(intent.getAction())
                        && intent.hasExtra(CommunicationService.EXTRA_GROUP_ID)) {
                    ViewTransferActivity.startInstance(ConnectionManagerActivity.this,
                            intent.getLongExtra(CommunicationService.EXTRA_GROUP_ID, -1));
                    finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_connection_manager);

        FragmentFactory factory = getSupportFragmentManager().getFragmentFactory();
        Toolbar toolbar = findViewById(R.id.toolbar);
        mAppBarLayout = findViewById(R.id.app_bar);
        mProgressBar = findViewById(R.id.activity_connection_establishing_progress_bar);
        mToolbarLayout = findViewById(R.id.toolbar_layout);
        mOptionsFragment = (OptionsFragment) factory.instantiate(getClassLoader(), OptionsFragment.class.getName());
        mBarcodeConnectFragment = (BarcodeConnectFragment) factory.instantiate(getClassLoader(), BarcodeConnectFragment.class.getName());
        mHotspotManagerFragment = (HotspotManagerFragment) factory.instantiate(getClassLoader(), HotspotManagerFragment.class.getName());
        mNetworkManagerFragment = (NetworkManagerFragment) factory.instantiate(getClassLoader(), NetworkManagerFragment.class.getName());
        mDeviceListFragment = (NetworkDeviceListFragment) factory.instantiate(getClassLoader(), NetworkDeviceListFragment.class.getName());

        mFilter.addAction(ACTION_CHANGE_FRAGMENT);
        mFilter.addAction(CommunicationService.ACTION_DEVICE_ACQUAINTANCE);
        mFilter.addAction(CommunicationService.ACTION_INCOMING_TRANSFER_READY);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent() != null) {
            if (getIntent().hasExtra(EXTRA_REQUEST_TYPE))
                try {
                    mRequestType = RequestType.valueOf(getIntent().getStringExtra(EXTRA_REQUEST_TYPE));
                } catch (Exception e) {
                    // do nothing
                }

            if (getIntent().hasExtra(EXTRA_ACTIVITY_SUBTITLE)) {
                mTitleProvided = getIntent().getStringExtra(EXTRA_ACTIVITY_SUBTITLE);
                mTitle = getIntent().getStringExtra(EXTRA_ACTIVITY_SUBTITLE);
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        checkFragment();
        registerReceiver(mReceiver, mFilter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onBackPressed()
    {
        if (getShowingFragment() instanceof OptionsFragment)
            super.onBackPressed();
        else
            setFragment(AvailableFragment.Options);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == android.R.id.home)
            onBackPressed();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    public void applyViewChanges(Fragment fragment, String mTitleProvided)
    {
        boolean isOptions = fragment instanceof OptionsFragment;

        if (fragment instanceof DeviceSelectionSupport)
            ((DeviceSelectionSupport) fragment).setDeviceSelectedListener(mDeviceSelectionListener);

        if (getSupportActionBar() != null) {
            CharSequence titleCurrent = fragment instanceof TitleSupport
                    ? ((TitleSupport) fragment).getTitle(ConnectionManagerActivity.this)
                    : getString(R.string.text_connectDevices);

            if (isOptions)
                mToolbarLayout.setTitle(mTitleProvided != null ? mTitleProvided : titleCurrent);
            else
                mToolbarLayout.setTitle(titleCurrent);
        }

        mAppBarLayout.setExpanded(isOptions, true);
    }

    private void checkFragment()
    {
        Fragment currentFragment = getShowingFragment();

        if (currentFragment == null)
            setFragment(AvailableFragment.Options);
        else
            applyViewChanges(currentFragment, mTitleProvided);
    }

    @Override
    public Snackbar createSnackbar(int resId, Object... objects)
    {
        return Snackbar.make(findViewById(R.id.activity_connection_establishing_content_view), getString(resId, objects), Snackbar.LENGTH_LONG);
    }

//    @IdRes
//    public AvailableFragment getShowingFragmentId()
//    {
//        Fragment fragment = getShowingFragment();
//
//        if (fragment instanceof BarcodeConnectFragment)
//            return AvailableFragment.ScanQrCode;
//        else if (fragment instanceof HotspotManagerFragment)
//            return AvailableFragment.CreateHotspot;
//        else if (fragment instanceof NetworkManagerFragment)
//            return AvailableFragment.UseExistingNetwork;
//        else if (fragment instanceof NetworkDeviceListFragment)
//            return AvailableFragment.UseKnownDevice;
//
//        // Probably OptionsFragment
//        return AvailableFragment.Options;
//    }

    @Nullable
    public Fragment getShowingFragment()
    {
        return getSupportFragmentManager().findFragmentById(R.id.activity_connection_establishing_content_view);
    }

    public void setFragment(AvailableFragment fragment)
    {
        @Nullable
        Fragment activeFragment = getShowingFragment();
        Fragment fragmentCandidate = null;

        switch (fragment) {
            case ScanQrCode:
                //fragmentCandidate = mBarcodeConnectFragment;
                if (mOptionsFragment.isAdded())
                    mOptionsFragment.startCodeScanner();
                return;
            case CreateHotspot:
                fragmentCandidate = mHotspotManagerFragment;
                break;
            case UseExistingNetwork:
                fragmentCandidate = mNetworkManagerFragment;
                break;
            case UseKnownDevice:
                fragmentCandidate = mDeviceListFragment;
                break;
            default:
                fragmentCandidate = mOptionsFragment;
        }

        if (activeFragment == null || fragmentCandidate != activeFragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if (activeFragment != null)
                transaction.remove(activeFragment);

            if (activeFragment != null && fragmentCandidate instanceof OptionsFragment)
                transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
            else
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);

            transaction.add(R.id.activity_connection_establishing_content_view, fragmentCandidate);
            transaction.commit();

            applyViewChanges(fragmentCandidate, mTitleProvided);
        }
    }

    protected void showEnterIpAddressDialog()
    {
        ConnectionUtils connectionUtils = ConnectionUtils.getInstance(this);
        UIConnectionUtils uiConnectionUtils = new UIConnectionUtils(connectionUtils, this);
        new ManualIpAddressConnectionDialog(this, uiConnectionUtils, mDeviceSelectionListener).show();
    }

    public enum RequestType
    {
        RETURN_RESULT,
        MAKE_ACQUAINTANCE
    }

    public enum AvailableFragment
    {
        Options,
        UseExistingNetwork,
        UseKnownDevice,
        ScanQrCode,
        CreateHotspot,
        EnterIpAddress
    }

    public interface DeviceSelectionSupport
    {
        void setDeviceSelectedListener(NetworkDeviceSelectedListener listener);
    }

    public static class OptionsFragment
            extends com.genonbeta.android.framework.app.Fragment
            implements DeviceSelectionSupport
    {
        public static final int REQUEST_CHOOSE_DEVICE = 100;
        private UnifiedNativeAd nativeAd;
        private NetworkDeviceSelectedListener mListener;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            View view = inflater.inflate(R.layout.layout_connection_options_fragment, container, false);

            View.OnClickListener listener = new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    switch (v.getId()) {
                        case R.id.connection_option_devices:
                            updateFragment(AvailableFragment.UseKnownDevice);
                            break;
                        case R.id.connection_option_hotspot:
                            updateFragment(AvailableFragment.CreateHotspot);
                            break;
                        case R.id.connection_option_network:
                            updateFragment(AvailableFragment.UseExistingNetwork);
                            break;
                        case R.id.connection_option_manual_ip:
                            updateFragment(AvailableFragment.EnterIpAddress);
                            break;
                        case R.id.connection_option_scan:
                            startCodeScanner();
                    }
                }
            };

            view.findViewById(R.id.connection_option_devices).setOnClickListener(listener);
            view.findViewById(R.id.connection_option_hotspot).setOnClickListener(listener);
            view.findViewById(R.id.connection_option_network).setOnClickListener(listener);
            view.findViewById(R.id.connection_option_scan).setOnClickListener(listener);
            view.findViewById(R.id.connection_option_manual_ip).setOnClickListener(listener);

            view.findViewById(R.id.connection_option_guide).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    new ConnectionSetUpAssistant(getActivity())
                            .startShowing();
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
                        loadFacebookAd(view);
                    }
                } catch (Exception ex) {
                    Log.e(getTag(), "initializeAds: " + ex.toString());
                }
            }
        }

        private void loadGoogleAd(View view) {
            Context context = getActivity();
            if(context!=null) {
                try{
                    AdLoader.Builder builder = new AdLoader.Builder(context, getString(R.string.transfer_ad_unit_id));
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
                                Log.e(getTag(), e.toString());
                            }
                        }
                    });

                    AdLoader adLoader = builder.withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            Log.e(getTag(), "Google Ads errorCode: " + errorCode);
                        }
                    }).build();

                    adLoader.loadAd(new AdRequest.Builder().build());
                } catch (Exception e){
                    Log.e(getTag(), e.toString());
                }
            }
        }

        private void loadFacebookAd(View view) {
            Context context = getActivity();
            if(context != null) {
                NativeAd nativeAd = new NativeAd(context, getString(R.string.fb_transfer_history_ad_unit));
                nativeAd.setAdListener(new NativeAdListener() {
                    @Override
                    public void onMediaDownloaded(Ad ad) {
                    }

                    @Override
                    public void onError(Ad ad, AdError adError) {
                        loadGoogleAd(view);
                        Log.e(getTag(), "FB Ad error code: "+ adError.getErrorCode() + ", FB Ad error message: " + adError.getErrorMessage());
                    }

                    @Override
                    public void onAdLoaded(Ad ad) {
                        if (nativeAd == null || nativeAd != ad) {
                            return;
                        }
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

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_CHOOSE_DEVICE)
                if (resultCode == RESULT_OK && data != null) {
                    try {
                        NetworkDevice device = new NetworkDevice(data.getStringExtra(BarcodeScannerActivity.EXTRA_DEVICE_ID));
                        AppUtils.getDatabase(getContext()).reconstruct(device);
                        NetworkDevice.Connection connection = new NetworkDevice.Connection(device.deviceId, data.getStringExtra(BarcodeScannerActivity.EXTRA_CONNECTION_ADAPTER));
                        AppUtils.getDatabase(getContext()).reconstruct(connection);

                        if (mListener != null)
                            mListener.onNetworkDeviceSelected(device, connection);
                    } catch (Exception e) {
                        Log.e(getTag(), "" + e.getMessage());
                        // do nothing
                    }
                }
        }

        private void startCodeScanner()
        {
            startActivityForResult(new Intent(getActivity(), BarcodeScannerActivity.class),
                    REQUEST_CHOOSE_DEVICE);
        }

        public void updateFragment(AvailableFragment fragment)
        {
            if (getContext() != null)
                getContext().sendBroadcast(new Intent(ACTION_CHANGE_FRAGMENT)
                        .putExtra(EXTRA_FRAGMENT_ENUM, fragment.toString()));
        }

        @Override
        public void setDeviceSelectedListener(NetworkDeviceSelectedListener listener)
        {
            mListener = listener;
        }
    }
}
