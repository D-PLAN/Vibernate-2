package com.napontaratan.vibernate.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import com.napontaratan.vibernate.model.VibernateLogger;
import com.napontaratan.vibernate.model.VibernateSettings;

/**
 * Created by daniel on 2015-04-08.
 */
public class SilentOnBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        VibernateLogger.log("BroadcastReceiver", "Silent - " + ", intent: " + intent);
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        VibernateSettings.setActiveTimerSession(context, intent);
    }
}
