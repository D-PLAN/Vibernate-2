package com.napontaratan.vibernate.model;

import android.util.Log;

import java.io.IOException;

/**
 * Created by daniel on 2015-07-30.
 */
public class VibernateLogger {
    private static boolean DEBUG_MODE = true;

    public static void init() {
        try {
            String[] cmd = new String[] { "logcat", "-v", "time", "-f", "/sdcard/vibernateLog"};
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void log(String tag, String msg) {
        if(DEBUG_MODE) {
            Log.d("TAG[ " + tag + " ]   ",  msg);
        }
    }

}
