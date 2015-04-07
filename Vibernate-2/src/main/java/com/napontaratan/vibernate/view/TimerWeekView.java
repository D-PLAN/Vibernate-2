package com.napontaratan.vibernate.view;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.napontaratan.vibernate.R;
import com.napontaratan.vibernate.model.TimerConflictException;
import com.napontaratan.vibernate.model.TimerSession;
import com.napontaratan.vibernate.model.Timers;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Custom Timer week view displaying timers for 7-day week that scales each timer's view
 * by the earliest and latest timers for the week
 */
public class TimerWeekView extends View {
    // Draw variables, set to -1 as flag for variables' value not set
    private int paddingLeft = -1;
    private int paddingTop = -1;
    private int paddingRight = -1;
    private int paddingBottom = -1;

    private int contentWidth = -1;
    private int contentHeight = -1;

    private int numColumns;
    private int numDividers;

    // the base layout
    private int totalColumnWidth;
    private int totalDividerWidth;

    // width of each column
    private int columnWidth;
    private int dividerWidth;

    private int containerXLeft;
    private int containerXRight;

    // timer layout
    private int timerYPadding;
    private int timerWidth;
    private int timerPaddingLeft;

    private float timerLength;

    private int timerXLeft;
    private int timerXRight;

    //divider layout
    private int divXLeft = 0;
    private int divXRight = 0;

    // Paints
    private Paint containerPaint = new Paint();
    private Paint dividerPaint = new Paint();
    private Paint timerPaint = new Paint();

    //timer info
    private int earliestTime;
    private final String TIME_STRING_FORMAT = "HH:mm";

    private HashMap<Integer, List<RectF>> timerRects =  new HashMap<Integer, List<RectF>>(); // List of timer rectangles
    private Timers timers;
    private int prevTimer;

    public TimerWeekView(Context context) {
        super(context);
        init(null, 0);
    }

    public TimerWeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
        // TODO remove this mock data once we implemented get timers
        String MOCK_TIMER_NAME = "CPSC 101";
        Calendar start = createCalendar(8,0,0,0);
        Calendar end = createCalendar(12, 0, 0, 0);
        TimerSession one = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { true, false, false, false, false, false, false}, Color.rgb(205, 64, 109), 1);
        start = createCalendar(8,0,0,0);
        end = createCalendar(9, 0, 0, 0);
        TimerSession two = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { false, true, false, false, false, false, false}, Color.rgb(136, 67, 173), 2);
        start = createCalendar(15,0,0,0);
        end = createCalendar(17, 0, 0, 0);
        TimerSession three = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { false, false, false, false, false, true, false},Color.rgb(69, 146, 134), 3);
        start = createCalendar(12,0,0,0);
        end = createCalendar(15, 0, 0, 0);
        TimerSession four = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { false, false, true, false, false, false, false}, Color.rgb(106, 125, 137), 4);
        start = createCalendar(17,0,0,0);
        end = createCalendar(18, 0, 0, 0);
        TimerSession five = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { true, false, false, false, false, false, false},Color.rgb(136, 67, 173), 5);
        start = createCalendar(12,0,0,0);
        end = createCalendar(17, 0, 0, 0);
        TimerSession six = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { true, false, false, false, false, false, false}, Color.rgb(136, 67, 173), 6);
        start = createCalendar(8,0,0,0);
        try {
            timers = new Timers();
            timers.addTimer(one, two, three, four, five, six);
        } catch (TimerConflictException e) {
            e.printStackTrace();
        }
    }
    //TODO get rid of this, for testing purposes
    private Calendar createCalendar(int hour, int min, int second, int millis) {
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, millis);
        return cal;
    }

    public TimerWeekView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
//        final TypedArray a = getContext().obtainStyledAttributes(
//                attrs, R.styleable.TimerWeekView, defStyle, 0);
        containerPaint.setStyle(Paint.Style.FILL);
        containerPaint.setColor(getResources().getColor(R.color.background));

        dividerPaint.setStyle(Paint.Style.FILL);
        dividerPaint.setColor(getResources().getColor(R.color.dividers));

        timerPaint.setStyle(Paint.Style.FILL);
        timerPaint.setColor(Color.BLUE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(!isDimensionsSet()) {
            System.out.println("DIMENSIONS BEING SET   =========== ");
            //These are member variables to reduce allocations per draw cycle.
            paddingLeft = getPaddingLeft();
            paddingTop = getPaddingTop();
            paddingRight = getPaddingRight();
            paddingBottom = getPaddingBottom();

            contentWidth = getWidth() - paddingLeft - paddingRight;
            contentHeight = getHeight() - paddingTop - paddingBottom;

            // 7 columns , 6 dividers
            numColumns = 7;
            numDividers = 6;

            totalColumnWidth = (int) (contentWidth * 0.95);
            totalDividerWidth = (int) (contentWidth * 0.05);

            columnWidth = totalColumnWidth / numColumns;
            dividerWidth = totalDividerWidth / numDividers;

            // timer containers
            containerXLeft = 0;
            containerXRight = 0;

            timerYPadding = (int) (contentHeight * 0.025);

            // timers, account for padding bottom, will account for padding top when drawing later
            timerLength = (float) (contentHeight - timerYPadding) / (float) getTimerDuration();

            System.out.println("timer total length   "  + (contentHeight- timerYPadding) + ", timer each length  " + timerLength);

            timerWidth = (int) (columnWidth * 0.5);
            timerPaddingLeft = (int) (columnWidth * 0.25);

            timerXLeft = 0;
            timerXRight = 0;

            //divider block
            divXLeft = 0;
            divXRight = 0;
        }


        for(int i = 0; i < numColumns; i++) {
            containerXLeft = (i * columnWidth) + (i * dividerWidth);
            containerXRight = containerXLeft + columnWidth;
            //(int left, int top, int right, int bottom)
            RectF containerRect = new RectF(containerXLeft, 0, containerXRight, contentHeight);
            canvas.drawRoundRect(containerRect, 0, 0, containerPaint);

            //for each timer in this day
            timerXLeft = containerXLeft + timerPaddingLeft;
            timerXRight = timerXLeft + timerWidth; // - timer padding right?
            List<TimerSession> timersForTheDay = timers.timerOnThisDay(i);
            float timerYStart = 0;
            float timerYEnd = 0;
            for(int j = 0; j < timersForTheDay.size(); j++) {
                TimerSession timer = timersForTheDay.get(j);
                if(j == 0) {
                    // to start the padding at top
                    timerYStart = (scaled(timer.getStartTimeInHours()) * timerLength) + timerYPadding;
                } else {
                    timerYStart = scaled(timer.getStartTimeInHours()) * timerLength;
                }
                timerYEnd = scaled(timer.getEndTimeInHours()) * timerLength;
                RectF timerRect = new RectF(timerXLeft, timerYStart, timerXRight, timerYEnd);
                System.out.println("ADDING THIS TIMER    =====   "   + timer.getId());
                System.out.println("drawing starts at   " + timerYStart + ", ends at      " + timerYEnd);
                // if timer id is already in map, add current timer to it's value
                List<RectF> currTimers = (timerRects.get(timer.getId()) == null)? new ArrayList<RectF>() :
                        timerRects.get(timer.getId());
                currTimers.add(timerRect);
                timerRects.put(timer.getId(), currTimers);
                canvas.drawRoundRect(timerRect, 15, 15, timerPaint);
            }
        }

        for(int i = 0; i < numDividers; i++) {
            // i+1  to account for every timer block before the divider
            divXLeft = (((i+1) % (numDividers+1)) * columnWidth) + (i * dividerWidth);
            divXRight = divXLeft + dividerWidth;
            //(int left, int top, int right, int bottom)
            RectF dividerRect = new RectF(divXLeft, 0, divXRight, contentHeight);
            canvas.drawRoundRect(dividerRect, 0, 0, dividerPaint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        float x = e.getX();
        float y = e.getY();
        //  show the corresponding timer info on the bottom cardview
        TimerSession selectedTimer = getSelectedTimer(x, y);
        if(selectedTimer != null && (prevTimer != selectedTimer.getId())) {
            // if selectedtimer is the same as last, do not need to update
            System.out.println("update timer info");
            displayTimerInfo(selectedTimer);
            prevTimer = selectedTimer.getId();
        }
        return true;
    }

    /**
     * Display this timer's name, time, days
     * @param selectedTimer
     */
    private void displayTimerInfo(TimerSession selectedTimer) {
        View root = getRootView();

        View timerPlaceholder = root.findViewById(R.id.timer_placeholder);
        timerPlaceholder.setVisibility(View.GONE);

        View timerInfoView = root.findViewById(R.id.timer_info_layout);
        timerInfoView.setVisibility(View.VISIBLE);

        TextView timerStartTimeView = (TextView) root.findViewById(R.id.timer_start_time);
        String startTimeText = getStartTimeInFormat(selectedTimer, TIME_STRING_FORMAT);
        timerStartTimeView.setText(startTimeText);

        TextView timerEndTimeView = (TextView) root.findViewById(R.id.timer_end_time);
        String endTimeText = getEndTimeInFormat(selectedTimer, TIME_STRING_FORMAT);
        timerEndTimeView.setText(endTimeText);

        TextView timerDaysView = (TextView) root.findViewById(R.id.timer_days);
        String dayText = getDaysInFormat(selectedTimer);
        timerDaysView.setText(dayText);

    }

    /**
     *
     * @param selectedTimer
     * @return the string representation of the days this timer is on
     */
    private String getDaysInFormat(TimerSession selectedTimer) {
        String dayString = "";
        String[] daysStrings = new String[]{"Sun", "Mon", "Tues", "Wed", "Thurs", "Fri", "Sat"};
        boolean [] days = selectedTimer.getDays();
        // determines which days the timer are active and log it as String
        for(int day = 0; day < days.length; day++){
            if(days[day])
                dayString += daysStrings[day];
        }
        return dayString;
    }

    /**
     * Drawing height is determine by get the total duration of the day for the week
     * and dividing it with the total content height scale to minutes
     * @return drawing height for each second(s) for a timer
     */
    private int getTimerDuration() {
        earliestTime = 23;
        int latest = 0;
        for(TimerSession timerSession: timers) {
            int start = timerSession.getStartTimeInHours();
            int end = timerSession.getEndTimeInHours();
            if(start < earliestTime) {
                earliestTime = start;
            }
            if(end > latest) {
                latest = end;
            }
        }
        return (latest - earliestTime);
    }

    /**
     * we scale the time so that the earliest time would be the starting point instead of 0
     * @param realTime time before it got scaled
     * @return the scaled time for onDraw
     */
    private int scaled(int realTime) {
        return (realTime - earliestTime);
    }


    /**
     * Determines if the dimension has been set, all preset to -1
     * since our dimensions do not change, we only want to set it once in onDraw
     * @return true if dimensions of this view has been set, false otherwise
     */
    private boolean isDimensionsSet() {
        return this.paddingLeft > -1 &&
                this.paddingRight > -1 &&
                this.paddingTop > -1 &&
                this.paddingBottom > -1 &&
                this.contentHeight > -1 &&
                this.contentWidth > -1;
    }

    /**
     *  Find the rectangle that contains this point
     * get the timer using id correspoding to this timer
     * @param x x-coordinate of the touch
     * @param y y-coordinate of the touch
     * @return the timersession object corresponding to the selected timer on the screen
     */
    private TimerSession getSelectedTimer(float x, float y) {
        Iterator it = timerRects.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            List<RectF> listOfTimerRects = (List<RectF>) pair.getValue();
            for(RectF rect: listOfTimerRects)
            if(rect.contains(x, y)) {
                int timerId = (int) pair.getKey();
                return timers.getTimerById(timerId);
            }
        }
        return null;
    }


    /**
     * Convert startTime as Calendar into String with proper dateFormat
     * @param sDateFormat
     * @return String - startTime after applying sDateFormat
     * @author Paul, Amelia
     */
    public String getStartTimeInFormat (TimerSession vt , String sDateFormat) {
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
    public String getEndTimeInFormat (TimerSession vt , String eDateFormat) {
        String endTest = null;
        //SimpleDateFormat sDateTest = new SimpleDateFormat(eDateFormat, Locale.getDefault());
        SimpleDateFormat sDateTest = new SimpleDateFormat(eDateFormat, Locale.getDefault());
        if (vt.getEndTime() != null) {
            endTest = sDateTest.format(vt.getEndTime().getTime());
        }
        return endTest;
    }


}