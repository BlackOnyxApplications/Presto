package com.bhuvan_kumar.Presto.activity;

import android.os.Bundle;
import android.view.MenuItem;

 import androidx.annotation.Nullable;

import com.bhuvan_kumar.Presto.app.Activity;
import com.bhuvan_kumar.Presto.R;

/**
 * created by: Bk
 * date: 7/20/18 10:19 PM
 */
public class ThirdPartyLibrariesActivity extends Activity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_party_libraries);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
}
