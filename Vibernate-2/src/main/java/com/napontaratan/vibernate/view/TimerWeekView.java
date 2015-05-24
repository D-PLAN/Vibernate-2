package com.napontaratan.vibernate.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.daimajia.swipe.SwipeLayout;
import com.napontaratan.vibernate.CreateTimerActivity;
import com.napontaratan.vibernate.R;
import com.napontaratan.vibernate.model.TimerSession;
import com.napontaratan.vibernate.model.TimerSessionHolder;
import com.napontaratan.vibernate.model.TimerUtils;

import java.util.*;

/**
 * Custom Timer week view displaying timers for 7-day week that scales each timer's view
 * by the earliest and latest timers for the week
 * 3 main components in drawing are the timer rectangles, dividers ,and columns for each day
 */
public class TimerWeekView extends View {
    // View
    private View root;
    private View timerPlaceholder;
    private View timerInfoView;
    private TextView timerName;
    private ImageView timerTypeIcon;
    private ImageView timerDeleteIcon;
    private ImageView timerEditIcon;
    private Switch timerOnOffSwitch;
    private TextView timerStartTimeView;
    private TextView timerEndTimeView;
    private TextView timerDaysView;
    private SwipeLayout swipeLayout;
    private RelativeLayout swipeBottomWrapperLayout;

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

    // percentages
    private final double PERCENTAGE_COLUMN = 0.95;
    private final double PERCENTAGE_DIVIDER = 0.05;
    private final double PERCENTAGE_TIMER_PADDING_TOP = 0.025;
    private final double PERCENTAGE_TIMER_WIDTH = 0.5;
    private final double PERCENTAGE_TIMER_PADDING_LEFT = 0.25;


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
    private int BORDER_RADIUS = 15;

    // Bitmaps
    private Bitmap vibrateBitmap;
    private Bitmap silentBitmap;
    private Bitmap vibrateBitmapWhite;
    private Bitmap silentBitmapWhite;
    private Drawable vibrateDrawable;
    private Drawable silentDrawable;

    //timer info
    private int earliestTime;
    private final String TIME_STRING_FORMAT = "HH:mm";

    private HashMap<Integer, List<RectF>> timerRectsMaps; // List of timer rectangles
    private TimerSessionHolder timerSessionHolder;
    private int prevTimer = -1;

    public TimerWeekView(Context context) {
        // Simple constructor to use when creating a view from code.
        super(context);
        init(null, 0);
    }

    public TimerWeekView(Context context, AttributeSet attrs) {
        // Constructor that is called when inflating a view from XML.
        // This is called when a view is being constructed from an XML file, supplying attributes that were specified in the XML file.
        // This version uses a default style of 0, so the only attribute values applied are those in the Context's Theme and the given AttributeSet.
        // The method onFinishInflate() will be called after all children have been added.
        super(context, attrs);
        init(attrs, 0);
        timerSessionHolder = TimerSessionHolder.getInstance();
        timerSessionHolder.setView(this);
    }

    public TimerWeekView(Context context, AttributeSet attrs, int defStyle) {
        // Perform inflation from XML and apply a class-specific base style.
        // This constructor of View allows subclasses to use their own base style when they are inflating.
        // For example, a Button class's constructor would call this version of the super class constructor and supply R.attr.buttonStyle for defStyle;
        // this allows the theme's button style to modify all of the base view attributes (in particular its background) as well as the Button class's attributes.
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

        vibrateBitmapWhite = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_action_vibrate_white);
        silentBitmapWhite = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_action_silent_white );

        vibrateBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_action_vibrate);
        silentBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_action_silent);

        vibrateDrawable = new BitmapDrawable(getResources(), vibrateBitmapWhite);
        silentDrawable = new BitmapDrawable(getResources(), silentBitmapWhite);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(!isDimensionsSet()) {
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

            totalColumnWidth = (int) (contentWidth * PERCENTAGE_COLUMN);
            totalDividerWidth = (int) (contentWidth * PERCENTAGE_DIVIDER);

            columnWidth = totalColumnWidth / numColumns;
            dividerWidth = totalDividerWidth / numDividers;

            // timer containers
            containerXLeft = 0;
            containerXRight = 0;

            timerYPadding = (int) (contentHeight * PERCENTAGE_TIMER_PADDING_TOP);

            timerWidth = (int) (columnWidth * PERCENTAGE_TIMER_WIDTH);
            timerPaddingLeft = (int) (columnWidth * PERCENTAGE_TIMER_PADDING_LEFT);

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
        timerRectsMaps =  new HashMap<Integer, List<RectF>>();

        for(int i = 0; i < numColumns; i++) {
            containerXLeft = (i * columnWidth) + (i * dividerWidth);
            containerXRight = containerXLeft + columnWidth;
            //(int left, int top, int right, int bottom)
            containerRect.set(containerXLeft, 0, containerXRight, contentHeight);
            canvas.drawRoundRect(containerRect, 0, 0, containerPaint);

            //for each timer in this day
            timerXLeft = containerXLeft + timerPaddingLeft;
            timerXRight = timerXLeft + timerWidth; // - timer padding right?
            List<TimerSession> timersForTheDay = timerSessionHolder.getTimerOnThisDay(i);
            float timerYStart = 0;
            float timerYEnd = 0;
            int iconDimension = timerXRight - timerXLeft;
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
                // if timer id is already in map, add current timer to it's value
                List<RectF> currTimers = (timerRectsMaps.get(timer.getId()) == null)? new ArrayList<RectF>() :
                        timerRectsMaps.get(timer.getId());
                currTimers.add(timerRect);
                timerRectsMaps.put(timer.getId(), currTimers);
                timerPaint.setColor(timer.getColor());
                canvas.drawRoundRect(timerRect, BORDER_RADIUS, BORDER_RADIUS, timerPaint);
                if(timer.getType() == TimerSession.TimerSessionType.VIBRATE) {
                    vibrateDrawable.setBounds(timerXLeft, (int) timerYStart, timerXRight, (int)(timerYStart + iconDimension));
                    vibrateDrawable.draw(canvas);
                } else if (timer.getType() == TimerSession.TimerSessionType.SILENT) {
                    silentDrawable.setBounds(timerXLeft, (int) timerYStart, timerXRight, (int)(timerYStart + iconDimension));
                    silentDrawable.draw(canvas);
                }

            }
        }

        for(int i = 0; i < numDividers; i++) {
            // i+1  to account for every timer block before the divider
            divXLeft = (((i+1) % (numDividers+1)) * columnWidth) + (i * dividerWidth);
            divXRight = divXLeft + dividerWidth;
            dividerRect.set(divXLeft, 0, divXRight, contentHeight);
            canvas.drawRoundRect(dividerRect, 0, 0, dividerPaint);
        }

    }

    private int getTimerDuration() {
        // Drawing height is determine by get the total duration of the day for the week
        // and dividing it with the total content height scale to minutes
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

    private int scaled(Calendar realTime) {
        //we scale the time so that the earliest time would be the starting point instead of 0
        // scaling by HOUR
        int time = realTime.get(Calendar.MINUTE) + (60 * realTime.get(Calendar.HOUR_OF_DAY));
        return (time - earliestTime);
    }


    private boolean isDimensionsSet() {
        // Determines if the dimension has been set, all preset to -1
        // since our dimensions do not change, we only want to set it once in onDraw
        return this.paddingLeft > -1 &&
                this.paddingRight > -1 &&
                this.paddingTop > -1 &&
                this.paddingBottom > -1 &&
                this.contentHeight > -1 &&
                this.contentWidth > -1;
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
            displayTimerInfo(selectedTimer);
            prevTimer = selectedTimer.getId();
        }
        return true;
    }

    private void setupCardView() {
        if(root == null) {
            root = getRootView();

            swipeLayout = (SwipeLayout) root.findViewById(R.id.timer_swipe_layout);
            swipeBottomWrapperLayout = (RelativeLayout) root.findViewById(R.id.bottom_wrapper);
            timerPlaceholder = root.findViewById(R.id.timer_placeholder);
            timerInfoView = root.findViewById(R.id.timer_info_layout);
            timerName = (TextView) root.findViewById(R.id.timer_name);
            timerTypeIcon = (ImageView) root.findViewById(R.id.timer_type_icon);
            timerDeleteIcon = (ImageView) root.findViewById(R.id.timer_delete_icon);
            timerEditIcon = (ImageView) root.findViewById(R.id.timer_edit_icon);
            timerOnOffSwitch = (Switch) root.findViewById(R.id.timer_switch);
            timerStartTimeView = (TextView) root.findViewById(R.id.timer_start_time);
            timerEndTimeView = (TextView) root.findViewById(R.id.timer_end_time);
            timerDaysView = (TextView) root.findViewById(R.id.timer_days);
        }
    }

    private void displayTimerInfo(final TimerSession selectedTimer) {
        // Display this timer's specific information on the bottom cardview
        if(selectedTimer != null) {
            setupCardView();
            timerPlaceholder.setVisibility(View.GONE);
            timerInfoView.setVisibility(View.VISIBLE);

            swipeBottomWrapperLayout.setBackgroundColor(selectedTimer.getColor());

            swipeLayout.setClickable(true);
            swipeLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("onClick", "clicked swipeLayout");
                    if(swipeLayout.getOpenStatus() == SwipeLayout.Status.Close){
                        swipeLayout.open(SwipeLayout.DragEdge.Bottom);
                    } else {
                        swipeLayout.close(true);
                    }
                }
            });

            timerEditIcon.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent mIntent = new Intent(view.getContext(), CreateTimerActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable("Timer", selectedTimer);
                    mIntent.putExtras(mBundle);
                    view.getContext().startActivity(mIntent);
                }
            });

            timerDeleteIcon.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Delete timer")
                            .setMessage("Are you sure you want to delete this timer?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    timerSessionHolder.removeTimer(selectedTimer);
                                    timerRectsMaps.remove(selectedTimer.getId());
                                    invalidateDisplayTimerInfo();
                                    Toast.makeText(getContext(), "Timer " + selectedTimer.getName() + " deleted", Toast.LENGTH_SHORT).show();
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

            timerName.setText(selectedTimer.getName());
            timerName.setTextColor(selectedTimer.getColor());

            if(selectedTimer.getType() == TimerSession.TimerSessionType.VIBRATE) {
                timerTypeIcon.setImageBitmap(vibrateBitmap);
            } else if(selectedTimer.getType() == TimerSession.TimerSessionType.SILENT) {
                timerTypeIcon.setImageBitmap(silentBitmap);
            }

            timerOnOffSwitch.setOnCheckedChangeListener(null);
            timerOnOffSwitch.setChecked(!selectedTimer.getTimerSnooze());
            drawSwitch(timerOnOffSwitch, selectedTimer.getColor(), getResources().getColor(android.R.color.darker_gray));
            timerOnOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        selectedTimer.setTimerSnooze(false);
                        timerSessionHolder.wakeTimer(selectedTimer);
                        timerOnOffSwitch.getTrackDrawable().setColorFilter(selectedTimer.getColor(), PorterDuff.Mode.MULTIPLY);
                        timerOnOffSwitch.getThumbDrawable().setColorFilter(selectedTimer.getColor(), PorterDuff.Mode.MULTIPLY);
                    } else {
                        selectedTimer.setTimerSnooze(true);
                        timerSessionHolder.snoozeTimer(selectedTimer);
                        timerOnOffSwitch.getTrackDrawable().setColorFilter(getResources().getColor(android.R.color.darker_gray), PorterDuff.Mode.MULTIPLY);
                        timerOnOffSwitch.getThumbDrawable().setColorFilter(getResources().getColor(android.R.color.darker_gray), PorterDuff.Mode.MULTIPLY);
                    }
                    Toast.makeText(getContext(), (b == true) ? "Switching timer on" : "Switching timer off", Toast.LENGTH_SHORT).show();
                }
            });

            String startTimeText = TimerUtils.getStartTimeInFormat(selectedTimer, TIME_STRING_FORMAT);
            timerStartTimeView.setText(startTimeText);

            String endTimeText = TimerUtils.getEndTimeInFormat(selectedTimer, TIME_STRING_FORMAT);
            timerEndTimeView.setText(endTimeText);

            String dayText = TimerUtils.getDaysInFormat(selectedTimer);
            timerDaysView.setText(dayText);
        }

    }

    private void drawSwitch(Switch timerOnOffSwitch, int onColor, int offColor) {
        if(timerOnOffSwitch.isChecked()) {
            timerOnOffSwitch.getTrackDrawable().setColorFilter(onColor, PorterDuff.Mode.MULTIPLY);
            timerOnOffSwitch.getThumbDrawable().setColorFilter(onColor, PorterDuff.Mode.MULTIPLY);
        } else {
            timerOnOffSwitch.getTrackDrawable().setColorFilter(offColor, PorterDuff.Mode.MULTIPLY);
            timerOnOffSwitch.getThumbDrawable().setColorFilter(offColor, PorterDuff.Mode.MULTIPLY);
        }
    }

    /**
     * Clears the display of timer's information. Called after timer has been edited or deleted
     */
    public void invalidateDisplayTimerInfo() {
        setupCardView();
        timerPlaceholder.setVisibility(View.VISIBLE);
        timerInfoView.setVisibility(View.GONE);
        if(swipeLayout != null) swipeLayout.setLeftSwipeEnabled(false);
        swipeLayout.setOnClickListener(null);
        prevTimer = -1;
        this.invalidate();
    }

    private TimerSession getSelectedTimer(float x, float y) {
        // Find the rectangle that contains this point
        // get the timer using id correspoding to this timer
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
}