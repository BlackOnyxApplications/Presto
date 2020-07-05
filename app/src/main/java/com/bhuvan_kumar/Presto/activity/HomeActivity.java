package com.bhuvan_kumar.Presto.activity;

import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Html;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.bhuvan_kumar.Presto.app.Activity;
import com.bhuvan_kumar.Presto.fragment.HomeFragment;
import com.bhuvan_kumar.Presto.instagram.InstaDownloaderActivity;
import com.bhuvan_kumar.Presto.util.AppUtils;
import com.bhuvan_kumar.Presto.R;
import com.bhuvan_kumar.Presto.service.CommunicationService;
import com.bhuvan_kumar.Presto.util.PreferenceUtils;
import com.google.android.material.snackbar.Snackbar;

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
        if ((System.currentTimeMillis() - mExitPressTime) < 2500)
            moveTaskToBack(true);
        else {
            mExitPressTime = System.currentTimeMillis();
            showAlertDialog();
        }
    }

    private void showAlertDialog(){
        if(PreferenceUtils.shouldShowShareDialog(this)){
            new AlertDialog.Builder(this)
                    .setTitle("Share it with your friends")
                    .setMessage("You are important to us. If you have any issues, please do contact us at Settings -> Contact us. Share with your friends as well!")
                    .setNegativeButton("WhatsApp", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            shareViaWhatsapp();
                        }
                    })
//                    .setPositiveButton(R.string.butn_exit, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            moveTaskToBack(true);
//                        }
//                    })
                    .setNeutralButton("Don't show again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PreferenceUtils.SetShowShareDialog(HomeActivity.this, false);
                        }
                    })
                    .show();
        }else{
            Toast.makeText(this, R.string.mesg_secureExit, Toast.LENGTH_SHORT).show();
        }
    }

    private void shareViaWhatsapp(){
        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
        whatsappIntent.setType("text/plain");
        whatsappIntent.setPackage("com.whatsapp");
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, "The text you wanted to share");
        try {
            startActivity(whatsappIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), Html.fromHtml("<font color=\"#000000\">WhatsApp not found</font>"), Snackbar.LENGTH_LONG);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.colorSecondary));
            snackbar.show();
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
