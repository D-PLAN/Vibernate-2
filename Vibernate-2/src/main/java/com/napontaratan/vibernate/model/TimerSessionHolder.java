package com.napontaratan.vibernate.model;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
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
    private RecyclerView.Adapter recyclerViewAdapter;
    private TimerWeekView timerWeekView;
    // SparseArray are more memory efficient
    // but unsuitable for large dataset > 100 items
    private SparseArray<TimerSession> timers;

    private TimerSessionHolder() {
        timers = new SparseArray<TimerSession>();
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
                timers.put(timerSession.getId(), timerSession);
                timerSession.addObserver(this);
            }
        }
    }

    // ======   Common data structure operations =============
    public boolean isEmpty() {
        return !(timers.size() > 0);
    }

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
                add(timerSession);
                timerController.updateDeviceState(timerSession);
            }
        }
    }

    private void add(TimerSession timerSession) {
        timerSession.setId(timerController.generateNextId());
        timerSession.addObserver(this);
        if(timerSession.getActive()) {
            timerController.setAlarm(timerSession);
        }
        timers.put(timerSession.getId(), timerSession);
        notifyViewChanged(timerSession);
    }

    /**
     * Returns timersession base on position in the data structure
     * @param pos
     * @return timersession at pos in the data structure
     */
    public TimerSession get(int pos) {
        return timers.valueAt(pos);
    }

    /**
     * Returns timersession base on it's id
     * @param id
     * @return timersession with id
     */
    public TimerSession getTimerById(int id) {
        return timers.get(id);
    }

    public List<TimerSession> getTimerOnThisDay(int day) {
        List<TimerSession> timersOnThisDay = new ArrayList<TimerSession>();
        for(int i = 0; i < timers.size(); i++) {
            TimerSession session = timers.get(timers.keyAt(i));
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
        timerSession.setActive(false);
        timerController.updateDeviceState(timerSession);
        return remove(timerSession);
    }

    /**
     * Removes an existing timer from collection and db by position in data structure
     * @param pos
     * @return true if successfully removed, false otherwise
     */
    public boolean removeTimer(int pos) {
        return removeTimer(timers.valueAt(pos));
    }

    private boolean remove(TimerSession timerSession) {
        if(timerSession != null) {
            timerController.removeAlarm(timerSession);
            timers.remove(timerSession.getId());
            notifyViewChanged(null);
            return true;
        }
        return false;
    }

    public int getSize() {
        return timers.size();
    }

    /**
     * Snooze an existing timer
     */
    public void setTimerInactive(TimerSession timerSession) {
        timerController.cancelAlarm(timerSession);
        timerController.updateDeviceState(timerSession);
    }

    /**
     * Wake an existing snoozed timer
     */
    public void setTimerActive(TimerSession timerSession) {
        timerController.setAlarm(timerSession);
        timerController.updateDeviceState(timerSession);
    }

    // major updates only
    public void updateTimer(TimerSession newTimer, TimerSession oldTimer) throws TimerConflictException{
        remove(oldTimer);
        if(isTimerConflict(newTimer)) {
            throw new TimerConflictException("Timer is in conflict with existing timers");
        } else {
            timerController.updateDeviceState(newTimer, oldTimer);
            add(newTimer);
        }
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
            timerWeekView.invalidateTimerWeekView(timerSession);
        }
    }

    @Override
    public Iterator<TimerSession> iterator() {
        return new SparseIterator<TimerSession>(timers);
    }

    private static final class SparseIterator<TimerSession> implements Iterator<TimerSession> {
        private int pos = 0;
        private SparseArray<TimerSession> array;

        private SparseIterator(SparseArray<TimerSession> array) {
            this.array = array;
        }

        @Override
        public boolean hasNext() {
            return pos < array.size();
        }

        @Override
        public TimerSession next() {
            return array.valueAt(pos++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
