package com.napontaratan.vibernate.model;

import android.util.SparseArray;

import java.util.Iterator;

/**
 * Created by daniel on 2015-07-17.
 * SparseArray are more memory efficient
 * but performance is unsuitable for large dataset > 100 items
 */
public class TimerSessions extends SparseArray<TimerSession> implements Iterable<TimerSession>{
    public TimerSessions() {
    }

    /**
     * Initializes with a deep copy of timerSessions
     * @param timerSessions
     */
    public TimerSessions(TimerSessions timerSessions) {
        for(TimerSession timerSession: timerSessions) {
            this.put(timerSession.getId(), timerSession);
        }
    }

    @Override
    public Iterator<TimerSession> iterator() {
        return new SparseIterator<TimerSession>(this);
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
