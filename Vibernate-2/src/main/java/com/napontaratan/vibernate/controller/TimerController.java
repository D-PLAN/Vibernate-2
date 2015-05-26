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
		if(!datastore.contains(timerSession.getId()))
			datastore.addToDB(timerSession);
        setupTimers(timerSession);

	}

    private void setupTimers(TimerSession timerSession) {
        int timerId = timerSession.getId();
        List<Calendar> startTimes = timerSession.getStartAlarmCalendars();
        List<Calendar> endTimes = timerSession.getEndAlarmCalendars();
        Intent activateVibration = null;
        if(timerSession.getType() == TimerSession.TimerSessionType.VIBRATE) {
            activateVibration = new Intent(context, VibrateOnBroadcastReceiver.class);
        } else if(timerSession.getType() == TimerSession.TimerSessionType.SILENT) {
            activateVibration = new Intent(context, SilentOnBroadcastReceiver.class);
        }
        Intent disableVibration = new Intent(context, OffBroadcastReceiver.class);
        // Timer on
        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.DAY_OF_WEEK);
        Calendar start = null;
        for (Calendar startTime : startTimes) {
            int day = startTime.get(Calendar.DAY_OF_WEEK);
            int id = timerId + day;
            long time = startTime.getTimeInMillis();
            if(day == today){
               start = startTime;
            }
            createSystemTimer(time, id, activateVibration, true);
        }
        // Timer off
        Calendar end = null;
        for(Calendar endTime : endTimes){
            int day = endTime.get(Calendar.DAY_OF_WEEK);
            int id = timerId + day + 10;
            if(day == today) {
                end = endTime;
            }
            createSystemTimer(endTime.getTimeInMillis(), id, disableVibration, true);
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
	 * Cancel the services corresponding to the VibrateTimer object
	 */
	public void cancelAlarm(TimerSession timerSession) {
		tearDownTimers(timerSession);
	}

    private void tearDownTimers(TimerSession timerSession) {
        List<Calendar> startTimes = timerSession.getStartAlarmCalendars();
        List<Calendar> endTimes = timerSession.getEndAlarmCalendars();
        Intent activateVibration = null;
        if(timerSession.getType() == TimerSession.TimerSessionType.VIBRATE) {
            activateVibration = new Intent(context, VibrateOnBroadcastReceiver.class);
        } else if (timerSession.getType() == TimerSession.TimerSessionType.SILENT) {
            activateVibration = new Intent(context, SilentOnBroadcastReceiver.class);
        }
        Intent disableVibration = new Intent(context, OffBroadcastReceiver.class);
        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.DAY_OF_WEEK);
        Calendar start = null;
        for(Calendar startTime: startTimes) {
            int day = startTime.get(Calendar.DAY_OF_WEEK);
            if(day == today) {
                start = startTime;
            }
        }
        Calendar end = null;
        for(Calendar endTime : endTimes){
            int day = endTime.get(Calendar.DAY_OF_WEEK);
            int id = timerSession.getId() + day;
            if(day == today) {
                end = endTime;
            }
            cancelSystemTimer(id, activateVibration);
            cancelSystemTimer(id + 10, disableVibration);
        }
    }


    /**
	 * Create a PendingIntent that will activate at the specified time
	 * @param time - time in milliseconds
	 * @param id - unique id from generateNextId(context)
	 * @param intent - either VibrateOn or VibrateOff
	 * @author Napon, Sunny
	 */
	private void createSystemTimer(long time, int id, Intent intent, boolean repeat){
		PendingIntent startVibrating = PendingIntent.getBroadcast(context,
                id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		if(repeat) {
			am.setRepeating(AlarmManager.RTC, time, WEEK_MILLISECONDS, startVibrating);
        } else {
			am.set(AlarmManager.RTC, time, startVibrating);
		}

	}

	private void cancelSystemTimer(int id, Intent intent) {
		PendingIntent stopVibrating = PendingIntent.getBroadcast(context,
				id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		stopVibrating.cancel();
		am.cancel(stopVibrating);
	}

    public List<TimerSession> getAllTimers() {
        return datastore.getAllTimers();
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