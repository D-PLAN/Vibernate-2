package com.napontaratan.vibernate.model;

import android.content.Context;
import com.napontaratan.vibernate.controller.TimerController;

import java.util.*;

/**
 * Created by daniel on 2015-02-28.
 *
 * Singleton data holder for our timers, connects the UI to our data
 * by taking care of timer actions done with UI and DB
 */
public class TimerSessionHolder implements Iterable<TimerSession> {

    private TimerController timerController;
    private List<TimerSession> timers;
    private HashMap<Integer, TimerSession> timersIdMap;

    private TimerSessionHolder() {
        timers = new ArrayList<TimerSession>();
        timersIdMap = new HashMap<Integer, TimerSession>();
    }

    private static final TimerSessionHolder instance = new TimerSessionHolder();

    public static TimerSessionHolder getInstance() {
        return instance;
    }

    /**
     * Sets the activity context and retrieve timers from db to populate data holder
     * @param ctx
     * @return this data holder
     */
    public TimerSessionHolder setContext(Context ctx) {
        timerController = new TimerController(ctx);
        populateHolder(timerController.getAllTimers());
        return instance;
    }

    private void populateHolder(List<TimerSession> allTimers) {
        for(TimerSession timerSession: allTimers) {
            timers.add(timerSession);
            timersIdMap.put(timerSession.getId(), timerSession);
        }
    }

    @Override
    public Iterator<TimerSession> iterator() {
        return timers.iterator();
    }

    /**
     * Adds a new timer if it passes conflict checks to collection and db
     * @param timerSessions
     * @throws TimerConflictException if this timer to be added conflicts with existing timers
     */
    public void addTimer(TimerSession ...  timerSessions) throws TimerConflictException {
        //TODO we can maybe try to coagulate 2 timers which are back to back of the same type
        for(TimerSession timerSession: timerSessions) {
            if(isTimerConflict(timerSession)) {
                throw new TimerConflictException("Timer id " + timerSession.getId() + " 's time conflicts with existing timers");
            } else {
                timerController.setAlarm(timerSession);
                timers.add(timerSession);
                timersIdMap.put(timerSession.getId(), timerSession);
            }
        }
    }

    /**
     * Removes an existing timer from collection and db
     * @param timerSession
     * @return true if successfully removed, false otherwise
     */
    public boolean removeTimer(TimerSession timerSession) {
        if(timerSession != null) {
            timerController.cancelAlarm(timerSession);
            timersIdMap.remove(timerSession.getId());
        }
        return timers.remove(timerSession);
    }


    public void removeAll() {
        for(TimerSession timerSession: timers){
            timerController.cancelAlarm(timerSession);
        }
        timers = new ArrayList<TimerSession>();
        timersIdMap = new HashMap<Integer, TimerSession>();
    }

    private boolean isTimerConflict(TimerSession timer) {
        boolean[] days =  timer.getDays();
        for(int i = 0; i < days.length; i++) {
            if(days[i]) {
                if(hasConflict(timer, i)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasConflict(TimerSession timerToAdd, int day) {
        // get all the days for which this timer is on
        // check this timer's start time for conflict with the timers on these days
        List<TimerSession> timersOnThisDay = timerOnThisDay(day);
        Calendar startTime = timerToAdd.getStartTime();
        Calendar endTime = timerToAdd.getEndTime();
        for(TimerSession timer: timersOnThisDay) {
            // if you start earlier, then you have to end earlier than the next timer's start time
            if(startTime.compareTo(timer.getEndTime()) < 0 && endTime.compareTo(timer.getStartTime()) > 0) {
                return true;
            }

        }
        return false;
    }

    public List<TimerSession> timerOnThisDay(int day) {
        List<TimerSession> timersOnThisDay = new ArrayList<TimerSession>();
        for(TimerSession session: timers) {
            if(session.getDays()[day]) {
                timersOnThisDay.add(session);
            }
        }
        Collections.sort(timersOnThisDay);
        return timersOnThisDay;
    }

    public TimerSession getTimerById(int id) {
        return timersIdMap.get(id);
    }

}
