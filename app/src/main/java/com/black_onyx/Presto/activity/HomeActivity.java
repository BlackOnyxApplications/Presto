package com.black_onyx.Presto.activity;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import com.black_onyx.Presto.app.Activity;
import com.black_onyx.Presto.fragment.HomeFragment;
import com.black_onyx.Presto.util.AppUtils;
import com.black_onyx.Presto.R;
import com.black_onyx.Presto.service.CommunicationService;

public class HomeActivity
        extends Activity
{
    public static final int REQUEST_PERMISSION_ALL = 1;

//    private NavigationView mNavigationView;
//    private DrawerLayout mDrawerLayout;
//    private PowerfulActionMode mActionMode;
    private HomeFragment mHomeFragment;
//    private MenuItem mTrustZoneToggle;
    private IntentFilter mFilter = new IntentFilter();
    private BroadcastReceiver mReceiver = null;

    private long mExitPressTime;
    private int mChosenMenuItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mHomeFragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.activitiy_home_fragment);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        toggleTrustZone();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (mReceiver != null)
            unregisterReceiver(mReceiver);

        mReceiver = null;
    }

    @Override
    public void onBackPressed()
    {
        if (mHomeFragment.onBackPressed())
            return;
        if ((System.currentTimeMillis() - mExitPressTime) < 2000)
            moveTaskToBack(true);
        else {
            mExitPressTime = System.currentTimeMillis();
            Toast.makeText(this, R.string.mesg_secureExit, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUserProfileUpdated()
    {
    }

    public void requestTrustZoneStatus()
    {
        AppUtils.startForegroundService(this, new Intent(this, CommunicationService.class)
                .setAction(CommunicationService.ACTION_REQUEST_TRUSTZONE_STATUS));
    }

    public void toggleTrustZone()
    {
        AppUtils.startForegroundService(this, new Intent(this, CommunicationService.class)
                .setAction(CommunicationService.ACTION_TOGGLE_SEAMLESS_MODE));
    }
}
