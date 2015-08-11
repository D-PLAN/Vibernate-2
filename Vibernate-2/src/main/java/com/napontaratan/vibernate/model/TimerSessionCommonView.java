package com.napontaratan.vibernate.model;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import com.daimajia.swipe.SwipeLayout;
import com.napontaratan.vibernate.CreateTimerActivity;
import com.napontaratan.vibernate.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by daniel on 2015-07-29.
 * Code shared between week and list view
 */
public class TimerSessionCommonView {

    public static Bitmap vibrateBitmap;
    public static Bitmap silentBitmap;
    public static Bitmap oneTimeBitmap;
    public static Bitmap recurringBitmap;

    public static final String TIMER_DATE_FORMAT = "HH:mm";


    public static void setupImageResources(Resources resource) {
        vibrateBitmap = BitmapFactory.decodeResource(resource, R.drawable.ic_action_vibrate);
        silentBitmap = BitmapFactory.decodeResource(resource, R.drawable.ic_action_silent);
        oneTimeBitmap = BitmapFactory.decodeResource(resource, R.drawable.ic_onetime_timer_dark);
        recurringBitmap = BitmapFactory.decodeResource(resource, R.drawable.ic_recurring_timer_dark);
    }
    /**
     * @param selectedTimer
     * @return the string representation of the days this timer is on
     */
    public static String getDaysInFormat(TimerSession selectedTimer) {
        String dayString = "";
        String[] daysStrings = new String[]{"SUN", "MON", "TUES", "WED", "THU", "FRI", "SAT"};
        boolean[] days = selectedTimer.getDays();
        // determines which days the timer are active and log it as String
        for (int day = 0; day < days.length; day++) {
            if (days[day]) {
                if (!dayString.equals("")) {
                    dayString += ", ";
                }
                dayString += daysStrings[day];
            }
        }
        return dayString;
    }

    /**
     * Convert dateTime as Calendar into String with proper dateFormat
     *
     * @param dateFormat
     * @return String - dateTime after applying dateFormat
     * @author Paul, Amelia
     */
    public static String getTimeFormat(Calendar dateTime, String dateFormat) {
        return dateTime == null ? null : new SimpleDateFormat(dateFormat, Locale.getDefault()).format(dateTime.getTime());
    }

    /**
     *
     * @param view        the image view to set the bitmap to
     * @param condition   the condition to choose between bitmap
     * @param trueBitmap   bitmap to be used if condition is true
     * @param falseBitmap   bitmap to be used if condition is false
     */
    public static void setIconBitmaps(ImageView view, boolean condition, Bitmap trueBitmap, Bitmap falseBitmap) {
        view.setImageBitmap( condition? trueBitmap : falseBitmap);
    }

    public static void editTimerSession(Context ctx, TimerSession timerSession, SwipeLayout swipeLayout) {
        Intent mIntent = new Intent(ctx, CreateTimerActivity.class);
        mIntent.putExtra(VibernateSettings.TIMER_ID_KEY, timerSession.getId());
        ctx.startActivity(mIntent);
    }

    public static void toggleSwipeLayout(SwipeLayout swipeLayout, SwipeLayout.DragEdge dragEdge) {
        if (swipeLayout.getOpenStatus() == SwipeLayout.Status.Close) {
            swipeLayout.open(dragEdge);
        } else {
            swipeLayout.close(true);
        }
    }

}
