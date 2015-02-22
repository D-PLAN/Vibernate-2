package com.napontaratan.vibratetimer.controller;

import java.util.Calendar;
import java.util.List;

import com.napontaratan.vibratetimer.database.VibrateTimerDB;
import com.napontaratan.vibratetimer.model.VibrateTimer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public final class VibrateTimerController {

	private static final int WEEK_MILLISECONDS = 604800000;
	private VibrateTimerDB datastore;
	private AlarmManager am; 
	private Context parent;

	public VibrateTimerController(Context context){
		am = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		datastore = VibrateTimerDB.getInstance(context);
		parent = context;
	}

	/**
	 * Start a repeating vibrate service at the start time and
	 * a repeating ringtone service at the end time 
	 * @param vt is the vibrateAlarm object to create a timer for
	 * @author Napon, Sunny
	 */
	public void setAlarm(VibrateTimer vt, Context context){
		if(!datastore.contains(vt.getId()))
			datastore.addToDB(vt);
		List<Calendar> startTimes = vt.getStartAlarmCalendars();
		List<Calendar> endTimes = vt.getEndAlarmCalendars();
		int timerId = vt.getId();
		for (Calendar startTime : startTimes) {
			int id = timerId + startTime.get(Calendar.DAY_OF_WEEK);
			Intent activateVibration = new Intent(parent, VibrateOnBroadcastReceiver.class); 
			createSystemTimer(startTime.getTimeInMillis(), id, activateVibration);
		}
		for(Calendar endTime : endTimes){
			int id = timerId + endTime.get(Calendar.DAY_OF_WEEK) + 10;
			Intent disableVibration = new Intent(parent, VibrateOffBroadcastReceiver.class);
			createSystemTimer(endTime.getTimeInMillis(), id, disableVibration);
		}
	}

	/**
	 * Cancel the services corresponding to the VibrateTimer object
	 * @param vt VibrateTimer object to cancel
	 * @author Napon, Sunny
	 */
	public void cancelAlarm(VibrateTimer vt, Context context){
		datastore.remove(vt);
		List<Calendar> times = vt.getStartAlarmCalendars();
		for(Calendar time : times){
			int id = vt.getId() + time.get(Calendar.DAY_OF_WEEK);
			PendingIntent pi = PendingIntent.getBroadcast(parent, id, 
					new Intent(parent, VibrateOnBroadcastReceiver.class), 
					PendingIntent.FLAG_UPDATE_CURRENT);
			pi.cancel();
			am.cancel(pi);
			pi = PendingIntent.getBroadcast(parent, id+10, 
					new Intent(parent, VibrateOffBroadcastReceiver.class), 
					PendingIntent.FLAG_UPDATE_CURRENT);
			pi.cancel();
			am.cancel(pi);
		}
	}

	public List<VibrateTimer> getVibrateTimers() {
		return datastore.getAllVibrateTimers();
	}
	/**
	 * Create a PendingIntent that will activate at the specified time
	 * @param time - time in milliseconds
	 * @param id - unique id from generateNextId(context)
	 * @param intent - either VibrateOn or VibrateOff
	 * @author Napon, Sunny
	 */
	private void createSystemTimer(long time, int id, Intent intent){
		PendingIntent startVibrating = PendingIntent.getBroadcast(parent,
				id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		am.setRepeating(AlarmManager.RTC, time, WEEK_MILLISECONDS, startVibrating); 
	}

	/**
	 * Generate a unique id for each alarm.
	 * @param context
	 * @return a unique alarm id
	 * @author Napon
	 */
	public static int generateNextId(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("idcounter", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		int counter = prefs.getInt("idcounter", -1);
		if(counter == -1) {
			counter = 0;
			editor.putInt("idcounter", counter);
			editor.commit();
		} else {
			counter = counter + 20;
			editor.putInt("idcounter", counter);
			editor.commit();
		}
		return counter;
	}
}