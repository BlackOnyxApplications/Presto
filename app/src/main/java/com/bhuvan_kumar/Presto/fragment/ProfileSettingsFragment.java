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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bhuvan_kumar.Presto.GlideApp;
import com.bhuvan_kumar.Presto.activity.HomeActivity;
import com.bhuvan_kumar.Presto.activity.ManageDevicesActivity;
import com.bhuvan_kumar.Presto.activity.PreferencesActivity;
import com.bhuvan_kumar.Presto.app.Activity;
import com.bhuvan_kumar.Presto.config.AppConfig;
import com.bhuvan_kumar.Presto.dialog.ProfileEditorDialog;
import com.bhuvan_kumar.Presto.dialog.ShareAppDialog;
import com.bhuvan_kumar.Presto.object.NetworkDevice;
import com.bhuvan_kumar.Presto.service.CommunicationService;
import com.bhuvan_kumar.Presto.ui.callback.IconSupport;
import com.bhuvan_kumar.Presto.ui.callback.TitleSupport;
import com.bhuvan_kumar.Presto.util.AppUtils;
import com.bhuvan_kumar.Presto.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Objects;

public class ProfileSettingsFragment extends Fragment implements IconSupport, TitleSupport, View.OnClickListener {
    public ProfileSettingsFragment() {}

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

        ImageView imageView = view.findViewById(R.id.layout_profile_picture_image_default);
        ImageView editImageView = view.findViewById(R.id.layout_profile_picture_image_preferred);
        TextView deviceNameText = view.findViewById(R.id.header_default_device_name_text);

        deviceNameText.setText(localDevice.nickname);
        loadProfilePictureInto(activity, localDevice.nickname, imageView);

        editImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ProfileEditorDialog dialog = new ProfileEditorDialog(activity);
                dialog.show();
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

        return view;
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
