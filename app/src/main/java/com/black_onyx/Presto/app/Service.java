package com.black_onyx.Presto.app;

import android.content.SharedPreferences;

import com.black_onyx.Presto.util.AppUtils;
import com.black_onyx.Presto.util.NotificationUtils;
import com.black_onyx.Presto.database.AccessDatabase;

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
