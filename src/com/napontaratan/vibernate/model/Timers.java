package com.napontaratan.vibernate.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by daniel on 2015-02-28.
 *
 * This is the collection we use to store our timers
 * encapsulates the way we add the timers and potentially decide how to resize the length of timers
 * on CalendarFragment
 */
public class Timers implements Iterable<TimerSession> {

    private List<TimerSession> timers;
    private int index;

    public Timers() {
        timers = new ArrayList<TimerSession>();
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

    public void addTimer(TimerSession timerSession) {
        //TODO: check for overlapping times, and reject the conflict times
        //          get time start and compare to other time ends
        //          throw add timer exception to tell user the timer has conflict and not allow them to add
        timers.add(timerSession);
    }


}
