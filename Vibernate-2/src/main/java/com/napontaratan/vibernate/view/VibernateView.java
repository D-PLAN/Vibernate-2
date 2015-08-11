package com.napontaratan.vibernate.view;

import com.napontaratan.vibernate.store.TimerSessionStore;


/**
 * Created by daniel on 2015-07-12.
 */
public interface VibernateView {
    void storeChanged(TimerSessionStore store);
}
