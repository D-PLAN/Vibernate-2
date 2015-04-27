package com.napontaratan.vibernate.model;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by daniel on 2015-04-26.
 */
public class TimerUtils {

    /**
     *
     * @param selectedTimer
     * @return the string representation of the days this timer is on
     */
    public static String getDaysInFormat(TimerSession selectedTimer) {
        String dayString = "";
        String[] daysStrings = new String[]{"SUN", "MON", "TUES", "WED", "THU", "FRI", "SAT"};
        boolean [] days = selectedTimer.getDays();
        // determines which days the timer are active and log it as String
        for(int day = 0; day < days.length; day++){
            if(days[day]) {
                if (!dayString.equals("")) {
                    dayString += ", ";
                }
                dayString += daysStrings[day];
            }
        }
        return dayString;
    }

    /**
     * Convert startTime as Calendar into String with proper dateFormat
     * @param sDateFormat
     * @return String - startTime after applying sDateFormat
     * @author Paul, Amelia
     */
    public static String getStartTimeInFormat (TimerSession vt , String sDateFormat) {
        String startTest = null;
        SimpleDateFormat sDateTest = new SimpleDateFormat(sDateFormat, Locale.getDefault());
        if (vt.getStartTime() != null) {
            startTest = sDateTest.format(vt.getStartTime().getTime());
        }
        return startTest;

    }

    /**
     * Convert endTime as Calendar into String with proper dateFormat
     * @param eDateFormat
     * @return String
     * @author Paul, Amelia
     */
    public static String getEndTimeInFormat (TimerSession vt , String eDateFormat) {
        String endTest = null;
        //SimpleDateFormat sDateTest = new SimpleDateFormat(eDateFormat, Locale.getDefault());
        SimpleDateFormat sDateTest = new SimpleDateFormat(eDateFormat, Locale.getDefault());
        if (vt.getEndTime() != null) {
            endTest = sDateTest.format(vt.getEndTime().getTime());
        }
        return endTest;
    }
}
