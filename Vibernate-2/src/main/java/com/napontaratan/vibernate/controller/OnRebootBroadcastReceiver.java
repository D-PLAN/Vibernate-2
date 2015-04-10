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
		TimerController controller = new TimerController(context);
		List<TimerSession> timers = datastore.getAllTimers();
		for(TimerSession vt : timers) {
			controller.setAlarm(vt);
		}
	}
}