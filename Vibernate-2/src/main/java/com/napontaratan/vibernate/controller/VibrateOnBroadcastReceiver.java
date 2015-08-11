package com.napontaratan.vibernate.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import com.napontaratan.vibernate.model.VibernateLogger;
import com.napontaratan.vibernate.model.VibernateSettings;

/**
 * Put the phone to vibrate mode
 * @author Napon
 */
public class VibrateOnBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		VibernateLogger.log("BroadcastReceiver", "Vibrate - " + ", intent: " + intent);
		AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
		VibernateSettings.setActiveTimerSession(context, intent);
	}
}
