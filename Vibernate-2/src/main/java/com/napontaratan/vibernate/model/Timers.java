package com.napontaratan.vibernate.model;

import java.util.*;

/**
 * Created by daniel on 2015-02-28.
 *
 * This is the collection we use to store our timers
 * encapsulates the way we add the timers and potentially decide how to resize the length of timers
 * on CalendarFragment
 */
public class Timers implements Iterable<TimerSession> {

    private List<TimerSession> timers;
    private HashMap<Integer, TimerSession> timersIdMap;
    private int index;

    public Timers() {
        // TODO grabs timers from database
        timers = new ArrayList<TimerSession>();
        timersIdMap = new HashMap<Integer, TimerSession>();
        index = 0;
    }

    @Override
    public Iterator<TimerSession> iterator() {
        return timers.iterator();
    }

    public void addTimer(TimerSession ...  timerSessions) throws TimerConflictException {
        //TODO: we can maybe try to coagulate 2 timers which are back to back of the same type
        for(TimerSession timerSession: timerSessions) {
            if(isTimerConflict(timerSession)) {
                throw new TimerConflictException();
            } else {
                timers.add(timerSession);
                timersIdMap.put(timerSession.getId(), timerSession);
            }
        }
    }

    public boolean removeTimer(TimerSession timerSession) {
        if(timerSession != null) {
            timersIdMap.remove(timerSession.getId());
        }
        return timers.remove(timerSession);
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
        int startTime = timerToAdd.getStartTimeInHours();
        int endTime = timerToAdd.getEndTimeInHours();
        for(TimerSession timer: timersOnThisDay) {
            // if you start earlier, then you have to end earlier than the next timer's start time
            if((startTime < timer.getEndTimeInHours()) && (endTime > timer.getStartTimeInHours())) {
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
