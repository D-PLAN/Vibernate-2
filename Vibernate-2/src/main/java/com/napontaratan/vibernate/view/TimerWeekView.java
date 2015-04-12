package com.napontaratan.vibernate.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.napontaratan.vibernate.R;
import com.napontaratan.vibernate.model.TimerSession;
import com.napontaratan.vibernate.model.TimerSessionHolder;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Custom Timer week view displaying timers for 7-day week that scales each timer's view
 * by the earliest and latest timers for the week
 * // TODO remove all debugging statements
 */
public class TimerWeekView extends View {
    private Context ctx = this.getContext();
    // View
    private View root;
    private View timerPlaceholder;
    private View timerInfoView;
    private TextView timerName;
    private ImageView timerTypeIcon;
    private ImageView timerDeleteIcon;
    private Switch timerOnOffSwitch;
    private TextView timerStartTimeView;
    private TextView timerEndTimeView;
    private TextView timerDaysView;

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

    // Rects
    private RectF containerRect;
    private RectF dividerRect;

    // Bitmaps
    private Bitmap vibrateBitmap;
    private Bitmap silentBitmap;
    private Drawable vibrateDrawable;
    private Drawable silentDrawable;
    private final static int TIMER_ICON_HEIGHT = 50;

    //timer info
    private int earliestTime;
    private final String TIME_STRING_FORMAT = "HH:mm";

    private HashMap<Integer, List<RectF>> timerRectsMaps =  new HashMap<Integer, List<RectF>>(); // List of timer rectangles
    private TimerSessionHolder timerSessionHolder;
    private int prevTimer = -1;

    public TimerWeekView(Context context) {
        super(context);
        init(null, 0);
    }

    public TimerWeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
        timerSessionHolder = TimerSessionHolder.getInstance().setContext(getContext());
    }

    public TimerWeekView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        containerPaint.setStyle(Paint.Style.FILL);
        containerPaint.setColor(getResources().getColor(R.color.background));

        dividerPaint.setStyle(Paint.Style.FILL);
        dividerPaint.setColor(getResources().getColor(R.color.dividers));

        timerPaint.setStyle(Paint.Style.FILL);

        containerRect = new RectF();
        dividerRect = new RectF();

        vibrateBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_action_alarms);
        silentBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_action_volume_muted);

        vibrateDrawable = new BitmapDrawable(getResources(), vibrateBitmap);
        silentDrawable = new BitmapDrawable(getResources(), silentBitmap);

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

            timerWidth = (int) (columnWidth * 0.5);
            timerPaddingLeft = (int) (columnWidth * 0.25);

            timerXLeft = 0;
            timerXRight = 0;

            //divider block
            divXLeft = 0;
            divXRight = 0;
        }

        // timers, account for padding bottom, will account for padding top when drawing later
        if(timerSessionHolder.isEmpty()) {
            timerLength = 0;
        } else {
            timerLength = (float) (contentHeight - timerYPadding) / (float) getTimerDuration();
        }
        System.out.println("timer total length   " + (contentHeight - timerYPadding) + ", timer each length  " + timerLength);

        for(int i = 0; i < numColumns; i++) {
            containerXLeft = (i * columnWidth) + (i * dividerWidth);
            containerXRight = containerXLeft + columnWidth;
            //(int left, int top, int right, int bottom)
            containerRect.set(containerXLeft, 0, containerXRight, contentHeight);
            canvas.drawRoundRect(containerRect, 0, 0, containerPaint);

            //for each timer in this day
            timerXLeft = containerXLeft + timerPaddingLeft;
            timerXRight = timerXLeft + timerWidth; // - timer padding right?
            List<TimerSession> timersForTheDay = timerSessionHolder.timerOnThisDay(i);
            float timerYStart = 0;
            float timerYEnd = 0;
            for(int j = 0; j < timersForTheDay.size(); j++) {
                TimerSession timer = timersForTheDay.get(j);
                if(j == 0) {
                    // to start the padding at top
                    timerYStart = (scaled(timer.getStartTime()) * timerLength) + timerYPadding;
                } else {
                    timerYStart = scaled(timer.getStartTime()) * timerLength;
                }
                timerYEnd = scaled(timer.getEndTime()) * timerLength;
                // draw the actual timers itself as rectangle blocks
                RectF timerRect = new RectF();
                timerRect.set(timerXLeft, timerYStart, timerXRight, timerYEnd);
                System.out.println("ADDING THIS TIMER    =====   "   + timer.getId());
                System.out.println("drawing starts at   " + timerYStart + ", ends at      " + timerYEnd);
                // if timer id is already in map, add current timer to it's value
                List<RectF> currTimers = (timerRectsMaps.get(timer.getId()) == null)? new ArrayList<RectF>() :
                        timerRectsMaps.get(timer.getId());
                currTimers.add(timerRect);
                timerRectsMaps.put(timer.getId(), currTimers);
                timerPaint.setColor(timer.getColor());
                canvas.drawRoundRect(timerRect, 15, 15, timerPaint);
                if((timerYEnd - timerYStart) > TIMER_ICON_HEIGHT) {
                    // draw timer icon
                    // 50 is a good size for the icon, to prevent it being too strected in larger timer blocks
                    int iconYEnd = (int)timerYStart + TIMER_ICON_HEIGHT;
                    if(timer.getType() == TimerSession.TimerSessionType.VIBRATE) {
                        vibrateDrawable.setBounds(timerXLeft, (int) timerYStart, timerXRight, iconYEnd);
                        vibrateDrawable.draw(canvas);
                    } else if (timer.getType() == TimerSession.TimerSessionType.SILENT) {
                        silentDrawable.setBounds(timerXLeft, (int) timerYStart, timerXRight, iconYEnd);
                        silentDrawable.draw(canvas);
                    }
                }

            }
        }

        for(int i = 0; i < numDividers; i++) {
            // i+1  to account for every timer block before the divider
            divXLeft = (((i+1) % (numDividers+1)) * columnWidth) + (i * dividerWidth);
            divXRight = divXLeft + dividerWidth;
            //(int left, int top, int right, int bottom)
            dividerRect.set(divXLeft, 0, divXRight, contentHeight);
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
            System.out.println("update timer " + selectedTimer.getId()  + "'s info");
            displayTimerInfo(selectedTimer);
            prevTimer = selectedTimer.getId();
        }
        return true;
    }

    /**
     * Display this timer's name, time, days
     * @param selectedTimer
     */
    private void displayTimerInfo(final TimerSession selectedTimer) {
        if(root == null) {
            root = getRootView();

            timerPlaceholder = root.findViewById(R.id.timer_placeholder);
            timerInfoView = root.findViewById(R.id.timer_info_layout);
            timerName = (TextView) root.findViewById(R.id.timer_name);
            timerTypeIcon = (ImageView) root.findViewById(R.id.timer_type_icon);
            timerDeleteIcon = (ImageView) root.findViewById(R.id.timer_delete_icon);
            timerOnOffSwitch = (Switch) root.findViewById(R.id.timer_switch);
            timerStartTimeView = (TextView) root.findViewById(R.id.timer_start_time);
            timerEndTimeView = (TextView) root.findViewById(R.id.timer_end_time);
            timerDaysView = (TextView) root.findViewById(R.id.timer_days);

        }

        timerPlaceholder.setVisibility(View.GONE);
        timerInfoView.setVisibility(View.VISIBLE);

        timerName.setText(selectedTimer.getName());
        timerName.setTextColor(selectedTimer.getColor());

        if(selectedTimer.getType() == TimerSession.TimerSessionType.VIBRATE) {
            timerTypeIcon.setImageBitmap(vibrateBitmap);
        } else if(selectedTimer.getType() == TimerSession.TimerSessionType.SILENT) {
            timerTypeIcon.setImageBitmap(silentBitmap);
        }

        timerDeleteIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(ctx)
                        .setTitle("Delete timer")
                        .setMessage("Are you sure you want to delete this timer?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                timerSessionHolder.removeTimer(selectedTimer);
                                Toast.makeText(getContext(), "Timer " + prevTimer + " deleted", Toast.LENGTH_SHORT).show();
                                updateDeleteTimerView(selectedTimer);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getContext(), "Timer kept", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        timerOnOffSwitch.setChecked(!selectedTimer.getTimerSnooze());
        timerOnOffSwitch.getTrackDrawable().setColorFilter(selectedTimer.getColor(), PorterDuff.Mode.MULTIPLY);
        timerOnOffSwitch.getThumbDrawable().setColorFilter(selectedTimer.getColor(), PorterDuff.Mode.MULTIPLY);
        timerOnOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    selectedTimer.setTimerSnooze(false);
                    timerSessionHolder.wakeTimer(selectedTimer);
                } else {
                    selectedTimer.setTimerSnooze(true);
                    timerSessionHolder.snoozeTimer(selectedTimer);
                }
                Toast.makeText(getContext(), (b == true) ? "Switching timer on" : "Switching timer off", Toast.LENGTH_SHORT).show();

            }
        });

        String startTimeText = getStartTimeInFormat(selectedTimer, TIME_STRING_FORMAT);
        timerStartTimeView.setText(startTimeText);

        String endTimeText = getEndTimeInFormat(selectedTimer, TIME_STRING_FORMAT);
        timerEndTimeView.setText(endTimeText);

        String dayText = getDaysInFormat(selectedTimer);
        timerDaysView.setText(dayText);
    }

    /**
     * Invalidate view to show removed time and update the card view so that it doesn't show the deleted timer
     */
    private void updateDeleteTimerView(TimerSession timerSession) {
        this.invalidate();
        timerRectsMaps.remove(timerSession.getId());
        timerPlaceholder.setVisibility(View.VISIBLE);
        timerInfoView.setVisibility(View.GONE);
        prevTimer = -1;
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
     * Drawing height is determine by get the total duration of the day for the week
     * and dividing it with the total content height scale to minutes
     * @return drawing height for each second(s) for a timer
     */
    private int getTimerDuration() {
        earliestTime = 86400;
        int latest = 0;
        for(TimerSession timerSession: timerSessionHolder) {
            int startHour = timerSession.getStartTime().get(Calendar.MINUTE) + (60* timerSession.getStartTime().get(Calendar.HOUR_OF_DAY));
            int endHour = timerSession.getEndTime().get(Calendar.MINUTE) + (60* timerSession.getEndTime().get(Calendar.HOUR_OF_DAY));
            if(startHour < earliestTime) {
                earliestTime = startHour;
            }
            if(endHour > latest) {
                latest = endHour;
            }
        }
        return (latest - earliestTime);
    }

    /**
     * we scale the time so that the earliest time would be the starting point instead of 0
     * scaling by HOUR
     * @param realTime time before it got scaled
     * @return the scaled time for onDraw
     */
    private int scaled(Calendar realTime) {
        int time = realTime.get(Calendar.MINUTE) + (60 * realTime.get(Calendar.HOUR_OF_DAY));
        return (time - earliestTime);
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
        Iterator it = timerRectsMaps.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            List<RectF> listOfTimerRects = (List<RectF>) pair.getValue();
            for(RectF rect: listOfTimerRects)
            if(rect.contains(x, y)) {
                int timerId = (int) pair.getKey();
                return timerSessionHolder.getTimerById(timerId);
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