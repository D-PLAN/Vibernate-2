package com.napontaratan.vibernate.controller;

import java.util.List;

import com.napontaratan.vibernate.database.VibernateDB;
import com.napontaratan.vibernate.model.TimerSession;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnRebootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		VibernateDB datastore = VibernateDB.getInstance(context);
		VibrateTimerController controller = new VibrateTimerController(context);
		List<TimerSession> timers = datastore.getAllVibrateTimers();
		for(TimerSession vt : timers) {
			controller.setAlarm(vt,context);
		}
	}
}