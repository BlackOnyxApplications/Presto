package com.bhuvan_kumar.Presto.app;

import android.content.SharedPreferences;

import com.bhuvan_kumar.Presto.util.AppUtils;
import com.bhuvan_kumar.Presto.util.NotificationUtils;
import com.bhuvan_kumar.Presto.database.AccessDatabase;

/**
 * created by: Bk
 * date: 31.03.2018 15:23
 */
abstract public class Service extends android.app.Service
{
    private NotificationUtils mNotificationUtils;

    public AccessDatabase getDatabase()
    {
        return AppUtils.getDatabase(this);
    }

    public SharedPreferences getDefaultPreferences()
    {
        return AppUtils.getDefaultPreferences(getApplicationContext());
    }

    public NotificationUtils getNotificationUtils()
    {
        if (mNotificationUtils == null)
            mNotificationUtils = new NotificationUtils(getApplicationContext(), getDatabase(), getDefaultPreferences());

        return mNotificationUtils;
    }
}
