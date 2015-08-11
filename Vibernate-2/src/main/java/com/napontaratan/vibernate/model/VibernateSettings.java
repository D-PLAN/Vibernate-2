package com.napontaratan.vibernate.model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by daniel on 2015-08-09.
 */
public class VibernateSettings {

    public final static String ACTIVE_TIMERSESSION_KEY = "activeTimerSession";
    public final static String ACTIVE_TIMERSESSION_ID_KEY = "activeTimerSessionId";
    public final static String ACTIVE_TIMERSESSION_DAY_KEY = "activeTimerSessionDay";
    public final static String ID_COUNTER_KEY = "idCounter";
    public static final String TIMER_ID_KEY = "timerId";
    public static final String TIMER_DAY_KEY = "timerDay";
    public static final String TIMER_ADD_KEY = "timerAdd";

    public static final int NUM_VIEWS = 3;
    public static final int NOT_FOUND = -1;

    public static void setActiveTimerSession(Context context, Intent intent) {
        SharedPreferences sharedPref = getSharedPreferences(context, ACTIVE_TIMERSESSION_KEY);
        SharedPreferences.Editor editor = sharedPref.edit();
        int timerId = intent.getIntExtra(TIMER_ID_KEY, NOT_FOUND);
        int day = intent.getIntExtra(TIMER_DAY_KEY, NOT_FOUND);
        editor.putInt(ACTIVE_TIMERSESSION_ID_KEY, timerId);
        editor.putInt(ACTIVE_TIMERSESSION_DAY_KEY, day);
        editor.commit();
    }

    public static int getActiveTimerSessionId(Context context) {
        SharedPreferences sharedPref = getSharedPreferences(context, ACTIVE_TIMERSESSION_KEY);
        return sharedPref.getInt(ACTIVE_TIMERSESSION_ID_KEY, NOT_FOUND);
    }

    public static int getActiveTimerSessionDay(Context context) {
        SharedPreferences sharedPref = getSharedPreferences(context, ACTIVE_TIMERSESSION_KEY);
        return sharedPref.getInt(ACTIVE_TIMERSESSION_DAY_KEY, NOT_FOUND);
    }

    public static void setIdCounter(Context context, int counter) {
        SharedPreferences prefs = VibernateSettings.getSharedPreferences(context, VibernateSettings.ID_COUNTER_KEY);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(VibernateSettings.ID_COUNTER_KEY, counter);
        editor.commit();
    }

    public static int getIdCounter(Context context) {
        SharedPreferences prefs = VibernateSettings.getSharedPreferences(context, VibernateSettings.ID_COUNTER_KEY);
        return prefs.getInt(VibernateSettings.ID_COUNTER_KEY, -1);
    }

    public static SharedPreferences getSharedPreferences(Context context, String key) {
        return context.getSharedPreferences(key, Context.MODE_PRIVATE);
    }
}
