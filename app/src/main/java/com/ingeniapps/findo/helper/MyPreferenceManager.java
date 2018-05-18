package com.ingeniapps.findo.helper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by FABiO on 23/02/2016.
 * This class stores data in SharedPreferences. Here we temporarily stores
 * the unread push notifications in order to append them to new messages.
 */
public class MyPreferenceManager
{

    private String TAG = MyPreferenceManager.class.getSimpleName();

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";


    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "androidhive_gcm";

    // All Shared Preferences Keys
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_NOTIFICATIONS = "notifications";

    // Constructor
    public MyPreferenceManager(Context context)
    {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void addNotification(String notification)
    {
        // get old notifications
        String oldNotifications = getNotifications();

        if (oldNotifications != null)
        {
            oldNotifications += "|" + notification;
        }
        else
        {
            oldNotifications = notification;
        }

        editor.putString(KEY_NOTIFICATIONS, oldNotifications);
        editor.commit();
    }

    public String getNotifications()
    {
        return pref.getString(KEY_NOTIFICATIONS, null);
    }

    public void clear()
    {
        editor.clear();
        editor.commit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }
}