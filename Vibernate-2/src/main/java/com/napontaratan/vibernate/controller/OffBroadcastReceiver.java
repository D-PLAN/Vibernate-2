package com.napontaratan.vibernate.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import com.napontaratan.vibernate.MainActivity;
import com.napontaratan.vibernate.action.TimerSessionAction;
import com.napontaratan.vibernate.dispatcher.Dispatcher;
import com.napontaratan.vibernate.model.VibernateLogger;

/**
 * Put the phone to ringtone mode
 * @author Napon
 */
public class OffBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		VibernateLogger.log("BroadcastReceiver", "Off - " + ", intent: " + intent);
		AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		removeOneTimeTimer(context);
	}

	private void removeOneTimeTimer(Context context) {
		MainActivity.setupDispatcherAndStore(context);
		try {
			Dispatcher.getInstance().dispatchAction(TimerSessionAction.REMOVE_ONE_TIME_TIMER, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
