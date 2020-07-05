package com.bhuvan_kumar.Presto.util;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * created by: Bk
 * date: 31.03.2018 13:48
 */
public class PreferenceUtils extends com.genonbeta.android.framework.util.PreferenceUtils
{
    public static void syncDefaults(Context context)
    {
        syncDefaults(context, true, false);
    }

    public static void syncDefaults(Context context, boolean compare, boolean fromXml)
    {
        SharedPreferences preferences = AppUtils.getDefaultLocalPreferences(context);
        SharedPreferences binaryPreferences = AppUtils.getDefaultPreferences(context);

        if (compare)
            sync(preferences, binaryPreferences);
        else {
            if (fromXml)
                syncPreferences(preferences, binaryPreferences);
            else
                syncPreferences(binaryPreferences, preferences);
        }
    }


    public static void SetGameMuted(Context context, boolean isMuted){
        SharedPreferences sharedPreferences = context.getSharedPreferences("SharedPref", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putBoolean("isGameMuted", isMuted);
        myEdit.apply();
    }

    public static boolean IsGameMuted(Context context){
        SharedPreferences sh = context.getSharedPreferences("SharedPref", MODE_PRIVATE);
        return sh.getBoolean("isGameMuted", false);
    }

    public static void SetShowShareDialog(Context context, boolean share){
        SharedPreferences sharedPreferences = context.getSharedPreferences("SharedPref", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putBoolean("shouldShare", share);
        myEdit.apply();
    }

    public static boolean shouldShowShareDialog(Context context){
        SharedPreferences sh = context.getSharedPreferences("SharedPref", MODE_PRIVATE);
        return sh.getBoolean("shouldShare", true);
    }
}
