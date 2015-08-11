package com.napontaratan.vibernate.store;

import android.content.Context;
import com.napontaratan.vibernate.action.TimerSessionAction;
import com.napontaratan.vibernate.controller.TimerController;
import com.napontaratan.vibernate.model.*;
import com.napontaratan.vibernate.V_Adapter;
import com.napontaratan.vibernate.view.SwipeLayoutInfoView;
import com.napontaratan.vibernate.view.TimerWeekView;
import com.napontaratan.vibernate.view.VibernateView;

import java.util.*;

/**
 * Created by daniel on 2015-07-12.
 */
public class TimerSessionStore {
    private TimerController timerController;
    private VibernateView[] views = new VibernateView[VibernateSettings.NUM_VIEWS];
    private TimerSessions timerSessions;
    private TimerSessionAction currentAction;
    private TimerSession currentTimerSession;
    private Context context;

    private static TimerSessionStore instance = new TimerSessionStore();;
    private TimerSessionStore() {}

    public static TimerSessionStore getInstance() {
        return instance;
    }

    public void setupStore(Context ctx) {
        this.context = ctx;
        timerController = new TimerController(this.context);
        loadTimerSessions();
    }

    public void onAction(TimerSessionAction action, TimerSession timerSession) throws Exception{
        currentAction = action;
        switch (action) {
            case ADD:
                VibernateLogger.log("TimerStore", "Adding timer session");
                addTimerSession(timerSession);
                updateCurrentTimerSession(timerSession);
                updateAllViews();
                break;
            case REMOVE:
                VibernateLogger.log("TimerStore", "Removing timer session");
                removeTimerSession(timerSession);
                updateCurrentTimerSession(null);
                updateAllViews();
                break;
            case EDIT_PROPERTY:
                VibernateLogger.log("TimerStore", "Editing timer session");
                updateTimer(timerSession);
                updateCurrentTimerSession(timerSession);
                updateAllViews();
                break;
            case SHOW:
            case SHOW_SHORT:
                VibernateLogger.log("TimerStore", "Showing timer session");
                updateCurrentTimerSession(timerSession);
                notifyChange(new int[]{
                        SwipeLayoutInfoView.SWIPE_VIEW
                });
                break;
            case ACTIVATE_TIMER:
                VibernateLogger.log("TimerStore", "Activating timer session");
                setTimerActive(timerSession);
                updateCurrentTimerSession(timerSession);
                notifyChange(new int[]{
                        SwipeLayoutInfoView.SWIPE_VIEW,
                        V_Adapter.LIST_VIEW
                });
                break;
            case DEACTIVATE_TIMER:
                VibernateLogger.log("TimerStore", "Deactivating timer session");
                setTimerInactive(timerSession);
                updateCurrentTimerSession(timerSession);
                notifyChange(new int[]{
                        SwipeLayoutInfoView.SWIPE_VIEW,
                        V_Adapter.LIST_VIEW
                });
                break;
            case REMOVE_ONE_TIME_TIMER:
                VibernateLogger.log("TimerStore", "Removing one time timer session");
                removeOneTimeTimer();
                updateCurrentTimerSession(timerSession);
                updateAllViews();
                break;
            case RESTORE_TIMERS:
                VibernateLogger.log("TimerStore", "Restoring timer session");
                restartTimerStore();
                break;
            default:
                break;
        }
    }

    public void onAction(TimerSessionAction action, TimerSession oldTimerSession, TimerSession newTimerSession) throws Exception{
        currentAction = action;
        switch (action) {
            case REPLACE_TIMER:
                replaceTimer(oldTimerSession, newTimerSession);
                updateCurrentTimerSession(newTimerSession);
                updateAllViews();
                break;
            default:
                break;
        }
    }

    public void registerView(int pos, VibernateView view) {
        views[pos] = view;
        if(hasViewsLoaded()) {
            updateAllViews();
        }
    }

    private boolean hasViewsLoaded() {
        for(VibernateView view: views) {
            if(view == null) {
                return false;
            }
        }
        return true;
    }

    private void updateAllViews() {
        notifyChange(new int[]{
                TimerWeekView.TIMER_WEEK_VIEW,
                SwipeLayoutInfoView.SWIPE_VIEW,
                V_Adapter.LIST_VIEW});
    }

    protected void notifyChange(int[] viewPositions) {
        for(int pos: viewPositions) {
            if(views[pos] != null) {
                views[pos].storeChanged(this);
            }
        }
    }

    public TimerSessions getTimerSessions() {
        return new TimerSessions(this.timerSessions);
    }

    public TimerSession getCurrentTimerSession() {
        return this.currentTimerSession;
    }

    public TimerSessionAction getCurrentAction() {
        return this.currentAction;
    }

    //========  Core operations  ============
    private void loadTimerSessions() {
        VibernateLogger.log("TimerStore", "Load timer session");
        List<TimerSession> allTimers = timerController.getAllTimers();
        timerSessions = new TimerSessions();
        for (TimerSession timerSession: allTimers) {
            timerSessions.put(timerSession.getId(), timerSession);
        }
    }

    private void updateCurrentTimerSession(TimerSession timerSession) {
        currentTimerSession = timerSession;
        if(timerSession != null) {
            timerSessions.put(timerSession.getId(), timerSession);
        }
    }

    private void addTimerSession(TimerSession timerSessionToAdd) throws TimerConflictException{
        checkTimerConflict(timerSessionToAdd, timerSessions);
        add(timerSessionToAdd);
    }

    private void removeTimerSession(TimerSession timerSession) {
        remove(timerSession);
    }

    private void setTimerInactive(TimerSession timerSession) {
        timerController.cancelAlarm(timerSession);
        timerSession.setActive(false);
        updateTimer(timerSession);
    }

    private void setTimerActive(TimerSession timerSession) {
        timerSession.setActive(true);
        updateTimer(timerSession);
        timerController.setAlarm(timerSession);
    }

    private void updateTimer(TimerSession timerSession) {
        timerController.updateTimer(timerSession);
    }

    private void replaceTimer(TimerSession oldTimer, TimerSession newTimer) throws TimerConflictException {
        TimerSessions timerSessionsToCheck = getTimerSessions();
        timerSessionsToCheck.remove(oldTimer.getId());
        checkTimerConflict(newTimer, timerSessionsToCheck);
        remove(oldTimer);
        add(newTimer);
    }

    private void restartTimerStore() {
        loadTimerSessions();
        for(TimerSession timerSession : timerSessions) {
            timerController.setAlarm(timerSession);
        }
    }

    private void removeOneTimeTimer() {
        TimerSession currentActiveTimerSession = timerSessions.get(VibernateSettings.getActiveTimerSessionId(context));
        if (currentActiveTimerSession != null && currentActiveTimerSession.getAddType() == TimerSession.TimerAddType.ONETIME) {
            VibernateLogger.log("RemoveOneTimeTimer", "There is a one time timer to remove");
            updateOneTimeTimer(currentActiveTimerSession);
        }
    }

    // ======   Helpers  =============
    private void checkTimerConflict(TimerSession timerSession, TimerSessions timerSessions) throws TimerConflictException {
        boolean[] days =  timerSession.getDays();
        for(int day = 0; day < days.length; day++) {
            if(days[day]) {
                TimerSession conflictedTimerSession = getTimerInConflictWith(day, timerSession, timerSessions);
                if(conflictedTimerSession != null) {
                    throw new TimerConflictException(String.format("(%s) is in conflict with (%s)", timerSession.getName(), conflictedTimerSession.getName()));
                }
            }
        }
    }

    private TimerSession getTimerInConflictWith(int day, TimerSession timerSessionToAdd, TimerSessions timerSessions) {
        // check this timer's start time for conflict with the timers on these days
        List<TimerSession> timersOnThisDay = TimerUtils.getTimerOnThisDay(timerSessions, day);
        Calendar startTime = timerSessionToAdd.getStartTime();
        Calendar endTime = timerSessionToAdd.getEndTime();
        for(TimerSession timer: timersOnThisDay) {
            // if you start earlier, then you have to end earlier than the next timer's start time
            if(startTime.compareTo(timer.getEndTime()) < 0 && endTime.compareTo(timer.getStartTime()) > 0) {
                return timer;
            }
        }
        return null;
    }

    private void add(TimerSession timerSession) {
        timerController.addTimerSession(timerSession);
        timerSessions.put(timerSession.getId(), timerSession);
    }

    private void remove(TimerSession timerSession) {
        if(timerSession != null) {
            timerController.removeTimerSession(timerSession);
            timerSessions.remove(timerSession.getId());
        }
    }

    private void updateOneTimeTimer(TimerSession timerSession) {
        if(hasAllSessionBeenRan(timerSession)) {
            VibernateLogger.log("RemoveOneTimeTimer", "Removing one time timer");
            removeTimerSession(timerSession);
        }
    }

    private boolean hasAllSessionBeenRan(TimerSession timerSession) {
        if (Calendar.getInstance().after(timerSession.getEndTime())) {
            timerSession.setDay(VibernateSettings.getActiveTimerSessionDay(context), false);
            VibernateLogger.log("RemoveOneTimeTimer", "Updating one time timer");
            updateTimer(timerSession);
        }
        // check if all days has been ran
        for(boolean on: timerSession.getDays()) {
            if(on) return false;
        }
        return true;
    }
}
