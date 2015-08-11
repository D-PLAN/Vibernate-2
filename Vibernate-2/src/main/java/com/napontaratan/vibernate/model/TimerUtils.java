package com.napontaratan.vibernate.model;

import android.util.SparseArray;

import java.io.*;
import java.util.*;

/**
 * Created by daniel on 2015-04-26.
 */
public class TimerUtils {

    /**
     * Java's Calendar DAY_OF_WEEK starts with value 1 for Sunday
     * Vibernate starts with value 0 for Sunday
     * This maps Java's calendar day to our day
     * @param day
     */
    public static int getVibernateDayOfWeek(Calendar day) {
        switch(day.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                return 0;
            case Calendar.MONDAY:
                return 1;
            case Calendar.TUESDAY:
                return 2;
            case Calendar.WEDNESDAY:
                return 3;
            case Calendar.THURSDAY:
                return 4;
            case Calendar.FRIDAY:
                return 5;
            case Calendar.SATURDAY:
                return 6;
        }
        return -1;
    }

    /**
     * @param timerSessions the list of timers session to search from
     * @param day           the day which the timers are on
     * @return the list of timer sessions on day
     */
    public static List<TimerSession> getTimerOnThisDay(SparseArray<TimerSession> timerSessions, int day) {
        List<TimerSession> timersOnThisDay = new ArrayList<TimerSession>();
        for (int i = 0; i < timerSessions.size(); i++) {
            TimerSession session = timerSessions.get(timerSessions.keyAt(i));
            if (session.getDays()[day]) {
                timersOnThisDay.add(session);
            }
        }
        Collections.sort(timersOnThisDay);
        return timersOnThisDay;
    }

    /**
     * Convert a VibrateTimer object into an array of Bytes to be stored into the Database
     * @param obj - (Object) VibrateTimer object
     * @return byte[]
     * @throws IOException
     * @author Napon
     */
    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    /**
     * Convert an array of Bytes back to its object form
     * @param  -data
     * @return (Object) VibrateTimer
     * @throws IOException
     * @throws ClassNotFoundException
     * @author Napon
     */
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }
}
