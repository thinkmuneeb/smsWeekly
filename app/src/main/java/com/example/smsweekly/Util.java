package com.example.smsweekly;

import android.content.SharedPreferences;
import android.util.Log;

class Util {
    public static final String SMS_PROFILE = "smsSendingProfileValue";
    public static final String IN_TESTING = "inTesting";

    public static final String SENT_SUMMARY = "sent";
    public static final String NOT_SENT_SUMMARY = "notSent";
    public static final String SMS_TOTAL_IN_A_DAY = "perDaySms";

    public static final String COUNTER_FOR_SUMMARY = "counterForSummary";
    public static final String COUNTER_FOR_NOT_SENT_SUMMARY = "counterForNotSentSummary";

    public static final int PRIVATE_MODE = 0;
    public static final String MUNEEB_FILE = "muneebFile";
    public static final long TEN_DAYS = 10*86400*1000;
    public static final String APP_REGISTERED = "appRegistered";
    public static final String LOG = "Log";

    public static final String LOG_COUNTER = "LogC";
    public static final int LOG_COUNTER_DEFAULT = 0;

    public static final String TAG = "Muneeb";

    public static String FlagActivatedOnce = "activatedOnce";
    public static int FlagActivatedOnceDefault = 0;


    public static final int PROFILE_ALL_CONTACTS = 0;
    public static final int PROFILE_START_WITH_X = 1;
    public static final int PROFILE_NOT_START_WITH_X = 2;
    public static final int PROFILE_SELECT_FROM_FILE = 3;

    public static void log(SharedPreferences preference, SharedPreferences.Editor editor, String message)
    {
        Log.i(TAG ,  message);

        /*int counter = preference.getInt(Util.LOG_COUNTER, 0);
        counter++;

        editor.putString(Util.LOG + counter, message);
        editor.putInt(Util.LOG_COUNTER,counter);
        editor.apply();*/
    }

    public static void logSave(SharedPreferences preference, SharedPreferences.Editor editor, String message)
    {
        Log.i(TAG ,  message);

        int counter = preference.getInt(Util.LOG_COUNTER, 0);
        counter++;

        editor.putString(Util.LOG + counter, message);
        editor.putInt(Util.LOG_COUNTER,counter);
        editor.apply();
    }

    public static void show(String message)
    {
        Log.i(TAG ,  message);
    }
}
