package com.bhuvan_kumar.Presto.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

 import androidx.annotation.Nullable;import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bhuvan_kumar.Presto.app.Activity;
import com.bhuvan_kumar.Presto.database.AccessDatabase;
import com.bhuvan_kumar.Presto.fragment.TransferFileExplorerFragment;
import com.bhuvan_kumar.Presto.ui.callback.PowerfulActionModeSupport;
import com.bhuvan_kumar.Presto.util.AppUtils;
import com.bhuvan_kumar.Presto.util.FileUtils;
import com.bhuvan_kumar.Presto.util.TextUtils;
import com.bhuvan_kumar.Presto.util.TransferUtils;
import com.bhuvan_kumar.Presto.R;
import com.bhuvan_kumar.Presto.dialog.EstablishConnectionDialog;
import com.bhuvan_kumar.Presto.dialog.SelectAssigneeDialog;
import com.bhuvan_kumar.Presto.dialog.ToggleMultipleTransferDialog;
import com.bhuvan_kumar.Presto.dialog.TransferInfoDialog;
import com.bhuvan_kumar.Presto.object.NetworkDevice;
import com.bhuvan_kumar.Presto.object.ShowingAssignee;
import com.bhuvan_kumar.Presto.object.TransferGroup;
import com.bhuvan_kumar.Presto.object.TransferObject;
import com.bhuvan_kumar.Presto.service.CommunicationService;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdBase;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.genonbeta.android.database.CursorItem;
import com.genonbeta.android.database.SQLQuery;
import com.genonbeta.android.framework.io.StreamInfo;
import com.genonbeta.android.framework.ui.callback.SnackbarSupport;
import com.genonbeta.android.framework.widget.PowerfulActionMode;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Bk
 * Date: 5/23/17 1:43 PM
 */

public class ViewTransferActivity
        extends Activity
        implements PowerfulActionModeSupport, SnackbarSupport
{
    public static final String TAG = ViewTransferActivity.class.getSimpleName();

    public static final String ACTION_LIST_TRANSFERS = "com.genonbeta.TrebleShot.action.LIST_TRANSFERS";
    public static final String EXTRA_GROUP_ID = "extraGroupId";
    public static final String EXTRA_REQUEST_ID = "extraRequestId";
    public static final String EXTRA_DEVICE_ID = "extraDeviceId";
    public static final String EXTRA_REQUEST_TYPE = "extraRequestType";

    public static final int REQUEST_ADD_DEVICES = 5045;
    final private List<String> mActiveProcesses = new ArrayList<>();
    final private TransferGroup.Index mTransactionIndex = new TransferGroup.Index();
    private OnBackPressedListener mBackPressedListener;
    private TransferGroup mGroup;
    private TransferObject mTransferObject;
    private PowerfulActionMode mMode;
    private String mDeviceId;
    private UnifiedNativeAd nativeAd;
    private MenuItem mCnTestMenu;
    private MenuItem mToggleMenu;
    private MenuItem mRetryMenu;
    private MenuItem mShowFilesMenu;
    private MenuItem mAddDeviceMenu;
    private MenuItem mSettingsMenu;
    private MenuItem mWebShareShortcut;
    private MenuItem mToggleBrowserShare;
    private CrunchLatestDataTask mDataCruncher;
    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (AccessDatabase.ACTION_DATABASE_CHANGE.equals(intent.getAction()) && intent.hasExtra(AccessDatabase.EXTRA_TABLE_NAME)) {
                if (AccessDatabase.TABLE_TRANSFERGROUP.equals(intent.getStringExtra(AccessDatabase.EXTRA_TABLE_NAME)))
                    reconstructGroup();
                else if (intent.hasExtra(AccessDatabase.EXTRA_CHANGE_TYPE)
                        && AccessDatabase.TABLE_TRANSFER.equals(intent.getStringExtra(AccessDatabase.EXTRA_TABLE_NAME))
                        && (AccessDatabase.TYPE_INSERT.equals(intent.getStringExtra(AccessDatabase.EXTRA_CHANGE_TYPE)) || AccessDatabase.TYPE_REMOVE.equals(intent.getStringExtra(AccessDatabase.EXTRA_CHANGE_TYPE)))) {
                    updateCalculations();
                }
            } else if (CommunicationService.ACTION_TASK_STATUS_CHANGE.equals(intent.getAction())
                    && intent.hasExtra(CommunicationService.EXTRA_GROUP_ID)
                    && intent.hasExtra(CommunicationService.EXTRA_DEVICE_ID)) {
                long groupId = intent.getLongExtra(CommunicationService.EXTRA_GROUP_ID, -1);

                if (groupId == mGroup.groupId) {
                    String deviceId = intent.getStringExtra(CommunicationService.EXTRA_DEVICE_ID);
                    int taskChange = intent.getIntExtra(CommunicationService.EXTRA_TASK_CHANGE_TYPE, -1);

                    synchronized (mActiveProcesses) {
                        if (taskChange == CommunicationService.TASK_STATUS_ONGOING)
                            mActiveProcesses.add(deviceId);
                        else
                            mActiveProcesses.remove(deviceId);
                    }

                    showMenus();
                }
            } else if (CommunicationService.ACTION_TASK_RUNNING_LIST_CHANGE.equals(intent.getAction())) {
                long[] groupIds = intent.getLongArrayExtra(CommunicationService.EXTRA_TASK_LIST_RUNNING);
                List<String> deviceIds = intent.getStringArrayListExtra(CommunicationService.EXTRA_DEVICE_LIST_RUNNING);

                if (groupIds != null && deviceIds != null
                        && groupIds.length == deviceIds.size()) {
                    int iterator = 0;

                    synchronized (mActiveProcesses) {
                        mActiveProcesses.clear();

                        for (long groupId : groupIds) {
                            String deviceId = deviceIds.get(iterator++);

                            if (groupId == mGroup.groupId)
                                mActiveProcesses.add(deviceId);
                        }

                        showMenus();
                    }
                }
            }
        }
    };

    public static void startInstance(Context context, long groupId)
    {
        context.startActivity(new Intent(context, ViewTransferActivity.class)
                .setAction(ACTION_LIST_TRANSFERS)
                .putExtra(EXTRA_GROUP_ID, groupId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_transfer);

        mMode = findViewById(R.id.activity_transaction_action_mode);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (Intent.ACTION_VIEW.equals(getIntent().getAction()) && getIntent().getData() != null) {
            try {
                StreamInfo streamInfo = StreamInfo.getStreamInfo(this, getIntent().getData());

                Log.d(TAG, "Requested file is: " + streamInfo.friendlyName);

                CursorItem fileIndex = getDatabase().getFirstFromTable(new SQLQuery.Select(AccessDatabase.TABLE_TRANSFER)
                        .setWhere(AccessDatabase.FIELD_TRANSFER_FILE + "=?", streamInfo.friendlyName));

                if (fileIndex == null)
                    throw new Exception("File is not found in the database");

                TransferObject transferObject = new TransferObject(fileIndex);
                TransferGroup transferGroup = new TransferGroup(transferObject.groupId);

                getDatabase().reconstruct(transferObject);

                mGroup = transferGroup;
                mTransferObject = transferObject;

                if (getIntent().getExtras() != null)
                    getIntent().getExtras().clear();

                getIntent()
                        .setAction(ACTION_LIST_TRANSFERS)
                        .putExtra(EXTRA_GROUP_ID, mGroup.groupId);

                new TransferInfoDialog(ViewTransferActivity.this, transferObject)
                        .show();

                Log.d(TAG, "Created instance from an file intent. Original has been cleaned " +
                        "and changed to open intent");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.mesg_notValidTransfer, Toast.LENGTH_SHORT).show();
            }
        } else if (ACTION_LIST_TRANSFERS.equals(getIntent().getAction()) && getIntent().hasExtra(EXTRA_GROUP_ID)) {
            TransferGroup group = new TransferGroup(getIntent().getLongExtra(EXTRA_GROUP_ID, -1));

            try {
                getDatabase().reconstruct(group);
                mGroup = group;

                if (getIntent().hasExtra(EXTRA_REQUEST_ID)
                        && getIntent().hasExtra(EXTRA_DEVICE_ID)
                        && getIntent().hasExtra(EXTRA_REQUEST_TYPE)) {
                    long requestId = getIntent().getLongExtra(EXTRA_REQUEST_ID, -1);
                    String deviceId = getIntent().getStringExtra(EXTRA_DEVICE_ID);

                    try {
                        TransferObject.Type type = TransferObject.Type
                                .valueOf(getIntent().getStringExtra(EXTRA_REQUEST_TYPE));
                        TransferObject transferObject = new TransferObject(requestId, deviceId, type);

                        getDatabase().reconstruct(transferObject);

                        new TransferInfoDialog(ViewTransferActivity.this, transferObject)
                                .show();
                    } catch (Exception e) {
                        // May be it
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mGroup == null)
            finish();
        else {
            Bundle transactionFragmentArgs = new Bundle();
            transactionFragmentArgs.putLong(TransferFileExplorerFragment.ARG_GROUP_ID, mGroup.groupId);
            transactionFragmentArgs.putString(TransferFileExplorerFragment.ARG_PATH,
                    mTransferObject == null || mTransferObject.directory == null
                            ? null : mTransferObject.directory);

            TransferFileExplorerFragment fragment = (TransferFileExplorerFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.activity_transaction_content_frame);

            if (fragment == null) {
                fragment = (TransferFileExplorerFragment) Fragment
                        .instantiate(ViewTransferActivity.this, TransferFileExplorerFragment.class.getName(), transactionFragmentArgs);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                transaction.add(R.id.activity_transaction_content_frame, fragment);
                transaction.commit();
            }

            attachListeners(fragment);

            mMode.setOnSelectionTaskListener(new PowerfulActionMode.OnSelectionTaskListener()
            {
                @Override
                public void onSelectionTask(boolean started, PowerfulActionMode actionMode)
                {
                    toolbar.setVisibility(!started ? View.VISIBLE : View.GONE);
                }
            });
        }
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
        NativeAd nativeAd = new NativeAd(this, getString(R.string.fb_receiving_page_ad_unit));
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
                LayoutInflater inflater = LayoutInflater.from(ViewTransferActivity.this);
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
    protected void onResume()
    {
        super.onResume();

        IntentFilter filter = new IntentFilter();

        filter.addAction(AccessDatabase.ACTION_DATABASE_CHANGE);
        filter.addAction(CommunicationService.ACTION_TASK_STATUS_CHANGE);
        filter.addAction(CommunicationService.ACTION_TASK_RUNNING_LIST_CHANGE);

        registerReceiver(mReceiver, filter);
        reconstructGroup();

        requestTaskStateUpdate();
        updateCalculations();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.actions_transfer, menu);

        mCnTestMenu = menu.findItem(R.id.actions_transfer_test_connection);
        mToggleMenu = menu.findItem(R.id.actions_transfer_toggle);
        mRetryMenu = menu.findItem(R.id.actions_transfer_receiver_retry_receiving);
        mShowFilesMenu = menu.findItem(R.id.actions_transfer_receiver_show_files);
        mAddDeviceMenu = menu.findItem(R.id.actions_transfer_sender_add_device);
        mSettingsMenu = menu.findItem(R.id.actions_transfer_settings);
        mWebShareShortcut = menu.findItem(R.id.actions_transfer_web_share_shortcut);
        mToggleBrowserShare = menu.findItem(R.id.actions_transfer_toggle_browser_share);

        showMenus();

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        {
            int devicePosition = findDevice(mDeviceId);

            Menu thisMenu = menu.findItem(R.id.actions_transfer_settings).getSubMenu();

            MenuItem checkedItem = null;

            if ((devicePosition < 0 || (checkedItem = thisMenu.getItem(devicePosition)) == null)
                    && thisMenu.size() > 0)
                checkedItem = thisMenu.getItem(thisMenu.size() - 1);

            if (checkedItem != null)
                checkedItem.setChecked(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.actions_transfer_toggle) {
            toggleTask();
        } else if (id == R.id.actions_transfer_remove) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.ques_removeAll)
                    .setMessage(R.string.text_removeCertainPendingTransfersSummary)
                    .setNegativeButton(R.string.butn_cancel, null)
                    .setPositiveButton(R.string.butn_remove, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            getDatabase().removeAsynchronous(ViewTransferActivity.this, mGroup);
                        }
                    }).show();
        } else if (id == R.id.actions_transfer_receiver_retry_receiving) {
            TransferUtils.recoverIncomingInterruptions(ViewTransferActivity.this, mGroup.groupId);

            createSnackbar(R.string.mesg_retryReceivingNotice)
                    .show();
        } else if (id == R.id.actions_transfer_receiver_show_files) {
            startActivity(new Intent(this, FileExplorerActivity.class)
                    .putExtra(FileExplorerActivity.EXTRA_FILE_PATH,
                            FileUtils.getSavePath(this, mGroup).getUri()));
        } else if (id == R.id.actions_transfer_sender_add_device) {
            startDeviceAddingActivity();
        } else if (id == R.id.actions_transfer_test_connection) {
            final List<ShowingAssignee> assignee = TransferUtils.loadAssigneeList(getDatabase(),
                    mGroup.groupId);

            if (assignee.size() == 1)
                new EstablishConnectionDialog(ViewTransferActivity.this,
                        assignee.get(0).device, null).show();
            else if (assignee.size() > 1) {
                new SelectAssigneeDialog(this, assignee, new DialogInterface
                        .OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        new EstablishConnectionDialog(ViewTransferActivity.this,
                                assignee.get(which).device, null).show();
                    }
                }).show();
            }
        } else if (item.getItemId() == R.id.actions_transfer_toggle_browser_share) {
            mGroup.isServedOnWeb = !mGroup.isServedOnWeb;
            getDatabase().update(mGroup);
            showMenus();

            if (mGroup.isServedOnWeb)
                AppUtils.startWebShareActivity(this, true);
        } else if (item.getGroupId() == R.id.actions_abs_view_transfer_activity_settings) {
            mDeviceId = item.getOrder() < mTransactionIndex.assignees.size()
                    ? mTransactionIndex.assignees.get(item.getOrder()).deviceId
                    : null;

            TransferFileExplorerFragment fragment = (TransferFileExplorerFragment)
                    getSupportFragmentManager()
                            .findFragmentById(R.id.activity_transaction_content_frame);

            if (fragment != null && fragment.setDeviceId(mDeviceId))
                fragment.refreshList();
        } else if (item.getItemId() == R.id.actions_transfer_web_share_shortcut) {
            AppUtils.startWebShareActivity(this, false);
        } else
            return super.onOptionsItemSelected(item);

        return true;
    }

    public void startDeviceAddingActivity()
    {
        startActivityForResult(new Intent(this, AddDevicesToTransferActivity.class)
                .putExtra(ShareActivity.EXTRA_GROUP_ID, mGroup.groupId), REQUEST_ADD_DEVICES);
    }

    @Override
    public void onBackPressed()
    {
        if (mBackPressedListener == null || !mBackPressedListener.onBackPressed())
            super.onBackPressed();
    }

    private void attachListeners(Fragment initiatedItem)
    {
        mBackPressedListener = initiatedItem instanceof OnBackPressedListener
                ? (OnBackPressedListener) initiatedItem
                : null;
    }

    @Override
    public Snackbar createSnackbar(int resId, Object... objects)
    {
        TransferFileExplorerFragment explorerFragment = (TransferFileExplorerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.activity_transaction_content_frame);

        if (explorerFragment != null && explorerFragment.isAdded())
            return explorerFragment.createSnackbar(resId, objects);

        return Snackbar.make(findViewById(R.id.activity_transaction_content_frame), getString(resId, objects), Snackbar.LENGTH_LONG);
    }

    public int findDevice(String device)
    {
        List<ShowingAssignee> assignees = new ArrayList<>(mTransactionIndex.assignees);

        if (device != null && assignees.size() > 0) {
            for (int i = 0; i < assignees.size(); i++) {
                ShowingAssignee assignee = assignees.get(i);

                if (device.equals(assignee.device.deviceId))
                    return i;
            }
        }

        return -1;
    }

    @Nullable
    public TransferGroup getGroup()
    {
        return mGroup;
    }

    public TransferGroup.Index getIndex()
    {
        return mTransactionIndex;
    }

    @Override
    public PowerfulActionMode getPowerfulActionMode()
    {
        return mMode;
    }

    public void reconstructGroup()
    {
        try {
            if (mGroup != null)
                getDatabase().reconstruct(mGroup);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private void requestTaskStateUpdate()
    {
        if (mGroup != null)
            AppUtils.startForegroundService(this, new Intent(this, CommunicationService.class)
                    .setAction(CommunicationService.ACTION_REQUEST_TASK_RUNNING_LIST_CHANGE));
    }

    private void showMenus()
    {
        boolean hasIncoming = getIndex().incomingCount > 0;
        boolean hasOutgoing = getIndex().outgoingCount > 0;
        boolean hasAnyFiles = hasIncoming || hasOutgoing;
        boolean hasRunning = mActiveProcesses.size() > 0;

        if (mToggleMenu == null || mRetryMenu == null || mShowFilesMenu == null)
            return;

        if (hasAnyFiles || hasRunning) {
            if (hasRunning)
                mToggleMenu.setTitle(R.string.butn_pause);
            else {
                mToggleMenu.setTitle(hasIncoming == hasOutgoing
                        ? R.string.butn_start
                        : (hasIncoming ? R.string.butn_receive : R.string.butn_send));
            }

            mToggleMenu.setVisible(true);
        } else
            mToggleMenu.setVisible(false);

        mToggleBrowserShare.setTitle(mGroup.isServedOnWeb ? R.string.butn_hideOnBrowser
                : R.string.butn_shareOnBrowser);
        mToggleBrowserShare.setVisible(hasOutgoing || mGroup.isServedOnWeb);
        mWebShareShortcut.setVisible(hasOutgoing && mGroup.isServedOnWeb);
        mCnTestMenu.setVisible(hasAnyFiles);
        mAddDeviceMenu.setVisible(hasOutgoing);
        mRetryMenu.setVisible(hasIncoming);
        mShowFilesMenu.setVisible(hasIncoming);

        if (hasOutgoing && (mTransactionIndex.assignees.size() > 0 || mDeviceId != null)) {
            Menu dynamicMenu = mSettingsMenu.setVisible(true).getSubMenu();
            dynamicMenu.clear();

            int iterator = 0;

            if (mTransactionIndex.assignees.size() > 0)
                for (; iterator < mTransactionIndex.assignees.size(); iterator++) {
                    ShowingAssignee assignee = mTransactionIndex.assignees.get(iterator);

                    dynamicMenu.add(R.id.actions_abs_view_transfer_activity_settings,
                            0, iterator, assignee.device.nickname);
                }

            dynamicMenu.add(R.id.actions_abs_view_transfer_activity_settings, 0, iterator,
                    getString(R.string.text_default));

            dynamicMenu.setGroupCheckable(R.id.actions_abs_view_transfer_activity_settings,
                    true, true);
        } else
            mSettingsMenu.setVisible(false);

        setTitle(getResources().getQuantityString(R.plurals.text_files,
                getIndex().incomingCount + getIndex().outgoingCount,
                getIndex().incomingCount + getIndex().outgoingCount));
    }

    private void toggleTask()
    {
        List<ShowingAssignee> assigneeList = TransferUtils.loadAssigneeList(getDatabase(),
                mGroup.groupId);

        if (assigneeList.size() > 0) {
            if (assigneeList.size() == 1
                    && (getIndex().outgoingCount > 0) != (getIndex().incomingCount > 0)) {
                ShowingAssignee assignee = assigneeList.get(0);

                toggleTaskForAssignee(assignee, getIndex().incomingCount > 0
                                ? TransferObject.Type.INCOMING : TransferObject.Type.OUTGOING,
                        mActiveProcesses.contains(assignee.deviceId));
            } else
                new ToggleMultipleTransferDialog(ViewTransferActivity.this,
                        mGroup, assigneeList, mActiveProcesses, getIndex()).show();
        } else if (getIndex().outgoingCount > 0)
            startDeviceAddingActivity();
    }

    public void toggleTaskForAssignee(final ShowingAssignee assignee, TransferObject.Type type,
                                      boolean ongoing)
    {
        try {
            if (ongoing)
                TransferUtils.pauseTransfer(this, assignee.groupId, assignee.deviceId);
            else {
                getDatabase().reconstruct(new NetworkDevice.Connection(assignee));
                TransferUtils.startTransferWithTest(this, mGroup, assignee, type);
            }
        } catch (Exception e) {
            e.printStackTrace();

            createSnackbar(R.string.mesg_transferConnectionNotSetUpFix)
                    .setAction(R.string.butn_setUp, new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            TransferUtils.changeConnection(ViewTransferActivity.this,
                                    getDatabase(), mGroup, assignee.device,
                                    new TransferUtils.ConnectionUpdatedListener()
                                    {
                                        @Override
                                        public void onConnectionUpdated(NetworkDevice.Connection connection, TransferGroup.Assignee assignee)
                                        {
                                            createSnackbar(R.string.mesg_connectionUpdated,
                                                    TextUtils.getAdapterName(
                                                            getApplicationContext(), connection))
                                                    .show();
                                        }
                                    });
                        }
                    }).show();
        }
    }

    public synchronized void updateCalculations()
    {
        if (mDataCruncher == null || !mDataCruncher.requestRestart()) {
            mDataCruncher = new CrunchLatestDataTask(new CrunchLatestDataTask.PostExecuteListener()
            {
                @Override
                public void onPostExecute()
                {
                    showMenus();
                    findViewById(R.id.activity_transaction_no_devices_warning)
                            .setVisibility(mTransactionIndex.assignees.size() > 0
                                    ? View.GONE : View.VISIBLE);

                    if (mTransactionIndex.assignees.size() == 0)
                        if (mTransferObject != null) {
                            new TransferInfoDialog(ViewTransferActivity.this, mTransferObject).show();
                            mTransferObject = null;
                        }
                }
            });

            mDataCruncher.execute(this);
        }
    }

    public static class CrunchLatestDataTask extends AsyncTask<ViewTransferActivity, Void, Void>
    {
        private PostExecuteListener mListener;
        private boolean mRestartRequested = false;

        public CrunchLatestDataTask(PostExecuteListener listener)
        {
            mListener = listener;
        }

        /* "possibility of having more than one ViewTransferActivity" < "sun turning into black hole" */
        @Override
        protected Void doInBackground(ViewTransferActivity... activities)
        {
            do {
                mRestartRequested = false;

                for (ViewTransferActivity activity : activities) {
                    if (activity.getGroup() != null)
                        activity.getDatabase()
                                .calculateTransactionSize(activity.getGroup().groupId, activity.getIndex());
                }
            } while (mRestartRequested && !isCancelled());

            return null;
        }

        public boolean requestRestart()
        {
            if (getStatus().equals(Status.RUNNING))
                mRestartRequested = true;

            return mRestartRequested;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            mListener.onPostExecute();
        }

        /* Should we have used a generic type class for this?
         * This interface aims to keep its parent class non-anonymous
         */
        public interface PostExecuteListener
        {
            void onPostExecute();
        }
    }
}
