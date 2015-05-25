package com.napontaratan.vibernate.model;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import com.napontaratan.vibernate.controller.TimerController;
import com.napontaratan.vibernate.view.TimerWeekView;

import java.util.*;

/**
 * Created by daniel on 2015-02-28.
 *
 * Singleton data holder for our timers, timerWeekView controller connecting UI to our model
 * by handling the user interactions and updating our model accordingly
 */
public class TimerSessionHolder implements Iterable<TimerSession>, Observer {

    private TimerController timerController;
    private List<TimerSession> timers;
    private HashMap<Integer, TimerSession> timersIdMap;
    private RecyclerView.Adapter recyclerViewAdapter;
    private TimerWeekView timerWeekView;

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
        initialPopulateHolder(timerController.getAllTimers());
        return instance;
    }

    public void setAdapter(RecyclerView.Adapter recyclerViewAdapter) {
        this.recyclerViewAdapter = recyclerViewAdapter;
    }

    public void setView(TimerWeekView timerWeekView) {
        this.timerWeekView = timerWeekView;
    }

    private void initialPopulateHolder(List<TimerSession> allTimers) {
        if(isEmpty()) {
            for(TimerSession timerSession: allTimers) {
                timers.add(timerSession);
                timersIdMap.put(timerSession.getId(), timerSession);
                timerSession.addObserver(this);
            }
        }
    }

    // ======   Common data structure operations =============

    /**
     * Adds a new timer if it passes conflict checks to collection and db
     * Timer id is generated, observers added
     * @param timerSessions
     * @throws TimerConflictException if this timer to be added conflicts with existing timers
     */
    public void addTimer(TimerSession ...  timerSessions) throws TimerConflictException{
        for(TimerSession timerSession: timerSessions) {
            if(isTimerConflict(timerSession)) {
                throw new TimerConflictException("Timer " + timerSession + " conflicts with existing timers");
            } else {
                timerSession.setId(timerController.generateNextId());
                timerSession.addObserver(this);
                if(timerSession.getActive()) {
                    timerController.setAlarm(timerSession);
                }
                timers.add(timerSession);
                timersIdMap.put(timerSession.getId(), timerSession);
                notifyViewChanged(timerSession);
            }
        }
    }

    public TimerSession get(int pos) {
        return timers.get(pos);
    }

    public TimerSession getTimerById(int id) {
        return timersIdMap.get(id);
    }


    public List<TimerSession> getTimerOnThisDay(int day) {
        List<TimerSession> timersOnThisDay = new ArrayList<TimerSession>();
        for(TimerSession session: timers) {
            if(session.getDays()[day]) {
                timersOnThisDay.add(session);
            }
        }
        Collections.sort(timersOnThisDay);
        return timersOnThisDay;
    }

    /**
     * Removes an existing timer from collection and db
     * @param timerSession
     * @return true if successfully removed, false otherwise
     */
    public boolean removeTimer(TimerSession timerSession) {
        return remove(timerSession);
    }

    /**
     * Removes an existing timer from collection and db
     * @param pos
     * @return true if successfully removed, false otherwise
     */
    public boolean removeTimer(int pos) {
        TimerSession timerSession = timers.get(pos);
        return remove(timerSession);
    }

    private boolean remove(TimerSession timerSession) {
        if(timerSession != null) {
            timerController.removeAlarm(timerSession);
            timersIdMap.remove(timerSession.getId());
            timers.remove(timerSession);
            notifyViewChanged(null);
            return true;
        }
        return false;
    }


    public void removeAll() {
        timerController.removeAllAlarm(timers);
        timers = new ArrayList<TimerSession>();
        timersIdMap = new HashMap<Integer, TimerSession>();
        notifyViewChanged(null);
    }

    @Override
    public Iterator<TimerSession> iterator() {
        return timers.iterator();
    }

    public boolean isEmpty() {
        return timers.isEmpty() && timersIdMap.isEmpty();
    }

    public int getSize() {
        return timers.size();
    }

    /**
     * Snooze an existing timer
     */
    public void setTimerInactive(TimerSession timerSession) {
        timerController.cancelAlarm(timerSession);
    }

    /**
     * wake an existing snoozed timer
     */
    public void setTimerActive(TimerSession timerSession) {
        timerController.setAlarm(timerSession);
    }


    // ======   Helpers  =============
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
        List<TimerSession> timersOnThisDay = getTimerOnThisDay(day);
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



    // ======   Observer   =============
    public void update(Observable observable, Object o) {
        timerController.updateTimer((TimerSession) observable);
        notifyViewChanged((TimerSession) observable);
    }

    private void notifyViewChanged(TimerSession timerSession) {
        // notify both week and list view
        if(recyclerViewAdapter != null) {
            recyclerViewAdapter.notifyDataSetChanged();
        }
        if(timerWeekView != null) {
            timerWeekView.invalidateDisplayTimerInfo();
            if(timerSession != null)  timerWeekView.displayTimerInfo(timerSession);
        }
    }
}
