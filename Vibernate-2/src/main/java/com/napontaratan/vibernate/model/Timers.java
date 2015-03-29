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
    private HashMap<Integer, TimerSession> timersMap;
    private int index;

    public Timers() {
        timers = new ArrayList<TimerSession>();
        timersMap = new HashMap<Integer, TimerSession>();
        index = 0;
    }

    @Override
    public Iterator<TimerSession> iterator() {
        return timers.iterator();
    }

    public boolean hasNext() {
        return index < timers.size();
    }

    public TimerSession next() {
        if(this.hasNext()) {
            return timers.get(index);
        } else {
            return null;
        }
    }

    public void addTimer(TimerSession ...  timerSessions) throws TimerConflictException {
        //TODO: check for overlapping times, and reject the conflict times
        //          get time start and compare to other time ends
        //          throw add timer exception to catch in UI activity to tell user
        //          the timer has conflict and not allow them to add
        //          CURRENTLY THE CHECK IS A PLACEHOLDER, STILL NEEDS TO BE IMPLEMENTED

        //TODO: we can maybe try to coagulate 2 timers which are back to back of the same type
        for(TimerSession timerSession: timerSessions) {
            if(isTimerConflict(timerSession, getAllStartTimes())) {
                throw new TimerConflictException();
            } else {
                timers.add(timerSession);
                timersMap.put(timerSession.getId(), timerSession);
            }
        }

    }

    public void removeTimer(TimerSession timerSession) {
        timers.remove(timerSession);
    }

    private List<Calendar> getAllStartTimes() {
        List<Calendar> startTimes = new ArrayList<Calendar>();
        for(TimerSession timer: timers) {
            startTimes.add(timer.getStartTime());
        }
        return startTimes;
    }

    private boolean isTimerConflict(TimerSession timer, List<Calendar> startTimes) {
        //TODO only check timers on the same day
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
        return timersMap.get(id);
    }

}
