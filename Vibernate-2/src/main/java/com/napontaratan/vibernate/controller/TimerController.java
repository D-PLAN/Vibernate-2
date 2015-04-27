package com.napontaratan.vibernate.controller;

import java.util.Calendar;
import java.util.List;

import com.napontaratan.vibernate.database.VibernateDB;
import com.napontaratan.vibernate.model.TimerSession;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public final class TimerController {

	private static final int WEEK_MILLISECONDS = 604800000;
	private VibernateDB datastore;
	private AlarmManager am; 
	private Context context;

	public TimerController(Context context) {
		this.context = context;
		am = (AlarmManager) this.context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		datastore = VibernateDB.getInstance(context);
	}

	/**
	 * Start a repeating vibrate service at the start time and
	 * a repeating ringtone service at the end time 
	 * @param timerSession is the vibrateAlarm object to create a timer for
	 * @author Napon, Sunny
	 */
	public void setAlarm(TimerSession timerSession){
		int timerId = timerSession.getId();
		if(!datastore.contains(timerId))
			datastore.addToDB(timerSession);
		List<Calendar> startTimes = timerSession.getStartAlarmCalendars();
		List<Calendar> endTimes = timerSession.getEndAlarmCalendars();
		// Timer on
		Intent activateVibration = null;
		if(timerSession.getType() == TimerSession.TimerSessionType.VIBRATE) {
			activateVibration = new Intent(context, VibrateOnBroadcastReceiver.class);
		} else if(timerSession.getType() == TimerSession.TimerSessionType.SILENT) {
			activateVibration = new Intent(context, SilentOnBroadcastReceiver.class);
		}
		for (Calendar startTime : startTimes) {
			int id = timerId + startTime.get(Calendar.DAY_OF_WEEK);
			createSystemTimer(startTime.getTimeInMillis(), id, activateVibration);
		}
		// Timer off
		Intent disableVibration = new Intent(context, OffBroadcastReceiver.class);
		for(Calendar endTime : endTimes){
			int id = timerId + endTime.get(Calendar.DAY_OF_WEEK) + 10;
			createSystemTimer(endTime.getTimeInMillis(), id, disableVibration);
		}
	}

	/**
	 * Update DB with the timer's new information
	 * @param timerSession
	 */
	public void updateTimer(TimerSession timerSession) {
		datastore.updateTimerInDB(timerSession);
	}

	/**
	 * Remove the TimerSession object from db and cancel the services
	 * @param timerSession VibrateTimer object to cancel
	 * @author Napon, Sunny
	 */
	public void removeAlarm(TimerSession timerSession){
		datastore.remove(timerSession);
		cancelAlarm(timerSession);
	}

	/**
	 * Remove all TimerSession objects form the db and cancels the services
	 */
	public void removeAllAlarm(List<TimerSession> timers) {
		for(TimerSession timer: timers) {
			cancelAlarm(timer);
		}
		datastore.deleteAllFromDB();
	}

	/**
	 * Cancel the services corresponding to the VibrateTimer object
	 */
	public void cancelAlarm(TimerSession timerSession) {
		List<Calendar> times = timerSession.getStartAlarmCalendars();
		Intent intent = null;
		if(timerSession.getType() == TimerSession.TimerSessionType.VIBRATE) {
			intent = new Intent(context, VibrateOnBroadcastReceiver.class);
		} else if (timerSession.getType() == TimerSession.TimerSessionType.SILENT) {
			intent = new Intent(context, SilentOnBroadcastReceiver.class);
		}
		PendingIntent pi = null;
		for(Calendar time : times){
			int id = timerSession.getId() + time.get(Calendar.DAY_OF_WEEK);
			pi = PendingIntent.getBroadcast(context, id,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);
			pi.cancel();
			am.cancel(pi);
			pi = PendingIntent.getBroadcast(context, id+10,
					new Intent(context, OffBroadcastReceiver.class),
					PendingIntent.FLAG_UPDATE_CURRENT);
			pi.cancel();
			am.cancel(pi);
		}
	}

	public List<TimerSession> getAllTimers() {
		return datastore.getAllTimers();
	}

	/**
	 * Create a PendingIntent that will activate at the specified time
	 * @param time - time in milliseconds
	 * @param id - unique id from generateNextId(context)
	 * @param intent - either VibrateOn or VibrateOff
	 * @author Napon, Sunny
	 */
	private void createSystemTimer(long time, int id, Intent intent){
		PendingIntent startVibrating = PendingIntent.getBroadcast(context,
				id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		am.setRepeating(AlarmManager.RTC, time, WEEK_MILLISECONDS, startVibrating); 
	}

	/**
	 * Generate a unique id for each alarm.
	 * @return a unique alarm id
	 * @author Napon
	 */
	public int generateNextId() {
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