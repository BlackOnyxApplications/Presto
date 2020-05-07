package com.bhuvan_kumar.Presto.activity;

import android.content.Intent;
import android.widget.Toast;

import com.bhuvan_kumar.Presto.app.Activity;
import com.bhuvan_kumar.Presto.util.FileUtils;
import com.bhuvan_kumar.Presto.R;
import com.genonbeta.android.framework.io.DocumentFile;

/**
 * Created by: Bk
 * Date: 5/30/17 6:57 PM
 */

public class ChangeStoragePathActivity extends Activity
{
    public final static int REQUEST_CHOOSE_FOLDER = 1;

    @Override
    protected void onStart()
    {
        super.onStart();

        DocumentFile currentSavePath = FileUtils.getApplicationDirectory(getApplicationContext());

        startActivityForResult(new Intent(this, FilePickerActivity.class)
                .setAction(FilePickerActivity.ACTION_CHOOSE_DIRECTORY)
                .putExtra(FilePickerActivity.EXTRA_START_PATH, currentSavePath.getUri().toString())
                .putExtra(FilePickerActivity.EXTRA_ACTIVITY_TITLE, getString(R.string.text_storagePath)), REQUEST_CHOOSE_FOLDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (resultCode == Activity.RESULT_OK) {
                switch (requestCode) {
                    case REQUEST_CHOOSE_FOLDER:
                        if (data.hasExtra(FilePickerActivity.EXTRA_CHOSEN_PATH)) {
                            getDefaultPreferences()
                                    .edit()
                                    .putString("storage_path", data.getParcelableExtra(FilePickerActivity.EXTRA_CHOSEN_PATH).toString())
                                    .apply();

                            Toast.makeText(this, "\uD83D\uDC4D", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        }

        finish();
    }
}
