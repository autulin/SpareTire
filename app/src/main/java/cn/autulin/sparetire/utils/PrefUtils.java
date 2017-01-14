package cn.autulin.sparetire.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by autulin on 2016/4/26.
 */
public class PrefUtils {
    private static final String PREFS_DEVICE_ID = "device_id";
//    private static final String PREFS_KEY = "key";
    private static final String PREFS_IDENTIFY = "identify";
    private static final String PREFS_TOPIC = "topic";
    private static final String PREFS_RAND = "rand";
    public static final int IDENTIFY_NULL = 0x00;
    public static final int IDENTIFY_HOST = 0x01;
    public static final int IDENTIFY_SLAVE = 0x02;

    public static void storeRand(final Context context, String rand) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREFS_RAND, rand).commit();
    }

    public static String getRand(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREFS_RAND, null);
    }

    public static void storeTopic(final Context context, String topic) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREFS_TOPIC, topic).commit();
    }

    public static String getTopic(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREFS_TOPIC, null);
    }

    public static String getPassword(final Context context){
        return PrefUtils.getID(context) + PrefUtils.getRand(context);
    }

    public static String getAlias(final Context context) {
        String topic = getTopic(context);
        if (getIdentify(context) == IDENTIFY_HOST) {
            return topic + "_host";
        } else if (getIdentify(context) == IDENTIFY_SLAVE) {
            return topic + "_slave";
        } else
            return null;
    }

    public static void setIdentify(final Context context, int identify) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(PREFS_IDENTIFY, identify).commit();
    }

    public static int getIdentify(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(PREFS_IDENTIFY, IDENTIFY_NULL);
    }

//    public static void setKey(final Context context, String key) {
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
//        sp.edit().putString(PREFS_KEY, key).commit();
//    }


    public static void setID(final Context context, String id){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREFS_DEVICE_ID, id).commit();
    }


    public static String getID(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String result = sp.getString(PREFS_DEVICE_ID, null);
        return result;
    }


}
