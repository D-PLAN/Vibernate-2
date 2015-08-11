package com.napontaratan.vibernate.dispatcher;

import com.napontaratan.vibernate.action.TimerSessionAction;
import com.napontaratan.vibernate.model.TimerSession;
import com.napontaratan.vibernate.store.TimerSessionStore;

/**
 * Created by daniel on 2015-07-12.
 *
 * Dispatcher sends Actions to registered Stores.
 */
public class Dispatcher {

    private static Dispatcher instance = new Dispatcher();

    private TimerSessionStore store;

    private Dispatcher() {
    }

    public static Dispatcher getInstance() {
        return instance;
    }

    public void registerStore(TimerSessionStore store) {
        this.store = store;
    }

    public void dispatchAction(TimerSessionAction action, TimerSession timerSession) throws Exception{
        store.onAction(action, timerSession);
    }

    public void dispatchAction(TimerSessionAction action, TimerSession oldTimerSession, TimerSession newTimerSession) throws Exception{
        store.onAction(action, oldTimerSession, newTimerSession);
    }
}
