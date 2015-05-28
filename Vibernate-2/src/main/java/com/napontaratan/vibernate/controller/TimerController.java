package com.napontaratan.vibernate.controller;

import java.util.Calendar;
import java.util.List;

import android.media.AudioManager;
import android.util.Log;
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
     * If current time is within the range of the timer and
     * the timer is active then adjust phone's state to reflect
     * the timer state
     * @param timer
     * @param oldTimer - if timer is a modification of existing timer
     */
    public void updateDeviceState(TimerSession timer, TimerSession oldTimer) {
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        // if timer is in range and on same day then update state and we are done
        if(isInTimeRange(timer) && isOnTimerDay(timer)) {
            if(!timer.getActive()) {
                audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            } else if(timer.getType() == TimerSession.TimerSessionType.VIBRATE) {
                audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            } else {
                audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }

            return;
        }

        // if timer isn't in range or not on same day AND oldTimer isn't null
        // then we have to check if time now was in range of old timer
        // if it is and the new timer is not in range that means we have to set
        // the device state back to normal
        // ie. modifying an in range timer so its no longer in range
        if(oldTimer != null && oldTimer.getActive() && isInTimeRange(oldTimer) && isOnTimerDay(oldTimer)) {
            audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
    }

    // No existing timer (only applicable for new creates)
    public void updateDeviceState(TimerSession newTimer) {
        updateDeviceState(newTimer, null);
    }

    // compare if currentTime is within range of timer
    // regardless of date/day and status(active or not)
    private boolean isInTimeRange(TimerSession timer) {
        Calendar now = Calendar.getInstance();
        Calendar start = Calendar.getInstance();
        Calendar end   = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, timer.getStartTime().get(Calendar.HOUR_OF_DAY));
        start.set(Calendar.MINUTE, timer.getStartTime().get(Calendar.MINUTE));
        start.set(Calendar.SECOND, timer.getStartTime().get(Calendar.SECOND));
        end.set(Calendar.HOUR_OF_DAY, timer.getEndTime().get(Calendar.HOUR_OF_DAY));
        end.set(Calendar.MINUTE, timer.getEndTime().get(Calendar.MINUTE));
        boolean isInTimeRange = now.after(start) && now.before(end);
        return isInTimeRange;
    }

    // compare if current time is on the same day as
    // one of the days specified in timer
    private boolean isOnTimerDay(TimerSession timer) {
        int today = getIntFromDayOfWeek(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
        boolean[] days = timer.getDays();
        for(int i = 0; i < 7; i++) {
            if (days[i] && i == today) {
                return true;
            }
        }
        return false;
    }

    // FOR DEBUGGING ONLY
    private String printCal(Calendar c) {
        return c.get(Calendar.YEAR) + " " + c.get(Calendar.MONTH) + " " + c.get(Calendar.DAY_OF_WEEK) + " " + c.get(Calendar.HOUR_OF_DAY) + " " + c.get(Calendar.MINUTE);
    }

    private int getIntFromDayOfWeek(int DAY_OF_WEEK) {
        switch(DAY_OF_WEEK) {
            case Calendar.SUNDAY:
                return 0;
            case Calendar.MONDAY:
                return 1;
            case Calendar.TUESDAY:
                return 2;
            case Calendar.WEDNESDAY:
                return 3;
            case Calendar.THURSDAY:
                return 4;
            case Calendar.FRIDAY:
                return 5;
            case Calendar.SATURDAY:
                return 6;
        }
        return -1;
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