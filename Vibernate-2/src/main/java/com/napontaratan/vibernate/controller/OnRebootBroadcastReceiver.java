package com.napontaratan.vibernate.controller;

import com.napontaratan.vibernate.MainActivity;
import com.napontaratan.vibernate.action.TimerSessionAction;
import com.napontaratan.vibernate.dispatcher.Dispatcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.napontaratan.vibernate.model.VibernateLogger;
import com.napontaratan.vibernate.store.TimerSessionStore;

public class OnRebootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		VibernateLogger.init();
		VibernateLogger.log("BroadcastReceiver", "Reboot");
		restoreTimerSession(context);
	}

	private void restoreTimerSession(Context context) {
		MainActivity.setupDispatcherAndStore(context);
		try {
			Dispatcher.getInstance().dispatchAction(TimerSessionAction.RESTORE_TIMERS, null);
			VibernateLogger.log("OnRebootBroadcast", "Retrieved " + TimerSessionStore.getInstance().getTimerSessions().size() + " timers from db");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}