package com.napontaratan.vibernate.controller;

import java.util.Calendar;
import java.util.List;

import com.napontaratan.vibernate.database.VibernateDB;
import com.napontaratan.vibernate.model.TimerSession;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.napontaratan.vibernate.model.TimerUtils;
import com.napontaratan.vibernate.model.VibernateSettings;

public final class TimerController {

	private static final int WEEK_MILLISECONDS = 604800000;
    // techinically 7 timers in a week, rounded it up
    private static final int NUM_OF_TIMERS_IN_A_WEEK = 10;
    private VibernateDB datastore;
	private AlarmManager am; 
	private Context context;

    private enum IntentType {
        ACTIVATE, DEACTIVATE
    }

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
    public void addTimerSession(TimerSession timerSession) {
        timerSession.setId(generateNextId());
        if(!datastore.contains(timerSession.getId()))
            datastore.addToDB(timerSession);
        if(timerSession.getActive()) {
            setAlarm(timerSession);
        }
    }

    /**
     * Remove the TimerSession object from db and cancel the services
     * @param timerSession VibrateTimer object to cancel
     * @author Napon, Sunny
     */
    public void removeTimerSession(TimerSession timerSession) {
        datastore.remove(timerSession);
        cancelAlarm(timerSession);
    }

    /**
     * Set the services corresponding to the TimerSession object
     */
	public void setAlarm(TimerSession timerSession){
        setupTimers(timerSession);
    }

    /**
     * Cancel the services corresponding to the TimerSession object
     */
    public void cancelAlarm(TimerSession timerSession) {
        tearDownTimers(timerSession);
    }

    /**
     * Update DB with the timer's new information
     */
    public void updateTimer(TimerSession timerSession) {
        datastore.updateTimerInDB(timerSession);
    }

    public List<TimerSession> getAllTimers() {
        return datastore.getAllTimers();
    }

    // =========   Helpers ====================
    private void setupTimers(TimerSession timerSession) {
        List<Calendar> startTimes = timerSession.getStartAlarmCalendars();
        List<Calendar> endTimes = timerSession.getEndAlarmCalendars();
        for (Calendar startTime : startTimes) {
            createSystemTimer(IntentType.ACTIVATE, startTime, timerSession, true);
        }
        for(Calendar endTime : endTimes) {
            createSystemTimer(IntentType.DEACTIVATE, endTime, timerSession, false);
        }
    }

    private void tearDownTimers(TimerSession timerSession) {
        List<Calendar> startTimes = timerSession.getStartAlarmCalendars();
        for(Calendar startTime : startTimes){
            // no need to differentiate start & end, since we just care about the day to cancel it
            cancelSystemTimer(IntentType.ACTIVATE, startTime, timerSession, false);
            cancelSystemTimer(IntentType.DEACTIVATE, startTime, timerSession, true);
        }
    }

    // ============  Create Helpers  =====================
    private void createSystemTimer(IntentType intentType, Calendar time, TimerSession timerSession, boolean triggerUpdate) {
        PendingIntent pendingIntent = getPendingIntent(intentType, time, timerSession);
        boolean isDeviceStateUpdated = triggerUpdate && updateDeviceState(time, timerSession, pendingIntent);
        scheduleAlarm(time, pendingIntent, timerSession, isDeviceStateUpdated);
    }

    private PendingIntent getPendingIntent(IntentType intentType, Calendar time, TimerSession timerSession) {
        int intentId = getTimerIntentId(intentType, time, timerSession.getId());
        Intent intent = createTimerIntent(intentType, time, timerSession);
        return PendingIntent.getBroadcast(context, intentId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private int getTimerIntentId(IntentType intentType, Calendar time, int timerId) {
        int day = time.get(Calendar.DAY_OF_WEEK);
        return (intentType == IntentType.ACTIVATE)? timerId + day : timerId + day + NUM_OF_TIMERS_IN_A_WEEK;
    }

    private Intent createTimerIntent(IntentType activationType, Calendar time, TimerSession timerSession) {
        Intent intent = activationType == IntentType.ACTIVATE?
                timerSession.getSessionType() == TimerSession.TimerSessionType.VIBRATE?
                        new Intent(context, VibrateOnBroadcastReceiver.class) : new Intent(context, SilentOnBroadcastReceiver.class) :
                new Intent(context, OffBroadcastReceiver.class);
        intent.putExtra(VibernateSettings.TIMER_ID_KEY, timerSession.getId());
        intent.putExtra(VibernateSettings.TIMER_DAY_KEY, TimerUtils.getVibernateDayOfWeek(time));
        return intent;
    }

	private void scheduleAlarm(Calendar time, PendingIntent pendingIntent, final TimerSession timerSession, boolean isDeviceStateUpdated){
        long triggerTime = getTriggerTime(timerSession, time, isDeviceStateUpdated);
        if(timerSession.getAddType() == TimerSession.TimerAddType.RECURRING) {
			am.setRepeating(AlarmManager.RTC, triggerTime, WEEK_MILLISECONDS, pendingIntent);
        } else {
			am.set(AlarmManager.RTC, triggerTime, pendingIntent);
		}
	}

    private long getTriggerTime(TimerSession timerSession, Calendar time, boolean isDeviceStateUpdated) {
        long triggerTime = time.getTimeInMillis();
        boolean isRunning =  VibernateSettings.getActiveTimerSessionId(context) == timerSession.getId() &&
                timerSession.getDays()[VibernateSettings.getActiveTimerSessionDay(context)];
        boolean shouldUpdateTime = isDeviceStateUpdated || (triggerTime < Calendar.getInstance().getTimeInMillis());
        return !isRunning && shouldUpdateTime? triggerTime + WEEK_MILLISECONDS : triggerTime;
    }

    // ============  Cancel Helpers  =====================
	private void cancelSystemTimer(IntentType intentType, Calendar time, TimerSession timerSession,  boolean triggerUpdate) {
        PendingIntent pendingIntent = getPendingIntent(intentType, time, timerSession);
        if(triggerUpdate) updateDeviceState(time, timerSession, pendingIntent);
        pendingIntent.cancel();
		am.cancel(pendingIntent);
	}

    private boolean updateDeviceState(Calendar time, TimerSession timerSession, PendingIntent pendingIntent) {
        if(isInTimeRange(time, timerSession) && timerSession.getActive()) {
            am.set(AlarmManager.RTC, Calendar.getInstance().getTimeInMillis(), pendingIntent);
            return true;
        }
        return false;
    }

    private boolean isInTimeRange(Calendar time, TimerSession timerSession) {
        // compare if time is within currentTime
        Calendar now = Calendar.getInstance();
        int today = TimerUtils.getVibernateDayOfWeek(now);

        boolean isInTimeRange = now.after(timerSession.getStartTime()) &&
                now.before(timerSession.getEndTime());
        boolean isOnTimerDay = TimerUtils.getVibernateDayOfWeek(time) == today;

        return isInTimeRange && isOnTimerDay;
    }

	/**
	 * Generate a unique id for each alarm.
	 * @return a unique alarm id
	 * @author Napon
	 */
	private int generateNextId() {
		int counter = VibernateSettings.getIdCounter(context);
        counter = counter == -1? 0: counter + 20;
        VibernateSettings.setIdCounter(context, counter);
		return counter;
	}
}