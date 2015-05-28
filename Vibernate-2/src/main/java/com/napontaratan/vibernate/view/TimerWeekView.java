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
import android.util.SparseArray;
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
    private boolean isDimensionsSet;

    private final static int NUM_COLUMNS = 7;
    private final static int NUM_DIVIDERS = 6;

    private int contentHeight;

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

    //divider layout
    private RectF[] dividerRects;
    private int divXLeft;
    private int divXRight;

    // timer layout
    private float timerPadding;
    private int timerWidth;
    private int timerPaddingLeft;

    private float timerYStart;
    private float timerYEnd;
    private float[] timerXLefts;
    private float[] timerXRights;

    // Paints
    private Paint dividerPaint = new Paint();
    private Paint timerPaint = new Paint();

    // Rects
    private final static int BORDER_RADIUS = 15;

    // Bitmaps
    private Bitmap vibrateBitmapWhite;
    private Bitmap silentBitmapWhite;
    private Drawable vibrateDrawable;
    private Drawable silentDrawable;
    private float iconDimension;

    //timer info
    private int earliestTime;

    private SparseArray<List<RectF>> timerRectsById;
    private TimerSessionHolder timerSessionHolder;
    private List<TimerSession> shortTimerSessions;
    private TimerSession selectedTimerSession;

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
        isDimensionsSet = false;

        dividerRects = new RectF[NUM_DIVIDERS];
        timerRectsById =  new SparseArray<List<RectF>>();
        shortTimerSessions = new ArrayList<TimerSession>();

        timerSessionHolder = TimerSessionHolder.getInstance();
        timerSessionHolder.setView(this);

        dividerPaint.setStyle(Paint.Style.FILL);
        dividerPaint.setColor(getResources().getColor(R.color.dividers));

        timerPaint.setStyle(Paint.Style.FILL);

        timerXLefts = new float[NUM_COLUMNS];
        timerXRights = new float[NUM_COLUMNS];

        vibrateBitmapWhite = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_action_vibrate_white);
        silentBitmapWhite = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_action_silent_white );

        vibrateDrawable = new BitmapDrawable(getResources(), vibrateBitmapWhite);
        silentDrawable = new BitmapDrawable(getResources(), silentBitmapWhite);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //These are member variables to reduce allocations per draw cycle.
        if(!isDimensionsSet) {
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            int paddingRight = getPaddingRight();
            int paddingBottom = getPaddingBottom();

            int contentWidth = getWidth() - paddingLeft - paddingRight;
            contentHeight = getHeight() - paddingTop - paddingBottom;

            int totalColumnWidth = (int) (contentWidth * PERCENTAGE_COLUMN);
            int totalDividerWidth = (int) (contentWidth * PERCENTAGE_DIVIDER);

            columnWidth = totalColumnWidth / NUM_COLUMNS;
            dividerWidth = totalDividerWidth / NUM_DIVIDERS;

            timerPadding = (float)(contentHeight * PERCENTAGE_TIMER_PADDING_TOP);

            timerWidth = (int) (columnWidth * PERCENTAGE_TIMER_WIDTH);
            timerPaddingLeft = (int) (columnWidth * PERCENTAGE_TIMER_PADDING_LEFT);

            calculateDividers();
            calculateTimerRectWidth();
            calculateTimerTypeIconDimension();
            isDimensionsSet = true;
        }

        drawDividers(canvas);

        if(!timerSessionHolder.isEmpty()) {
            timerRectsById =  new SparseArray<List<RectF>>(); // clear out
            drawTimerBlocks(canvas);
        }
        SwipeLayoutInfoView.displayTimerInfo(this.getRootView(),
                selectedTimerSession, timerSessionHolder, shortTimerSessions.contains(selectedTimerSession));
    }

    private void calculateDividers() {
        for(int i = 0; i < NUM_DIVIDERS; i++) {
            // i+1  to account for every timer block before the divider
            divXLeft = (((i+1) % (NUM_DIVIDERS +1)) * columnWidth) + (i * dividerWidth);
            divXRight = divXLeft + dividerWidth;
            RectF dividerRect = new RectF(divXLeft, 0, divXRight, contentHeight);
            dividerRects[i] = dividerRect;
        }
    }

    private void calculateTimerRectWidth() {
        float timerXLeft;
        float timerXRight;
        for(int i = 0; i < NUM_COLUMNS; i++) {
            containerXLeft = (i * columnWidth) + (i * dividerWidth);
            containerXRight = containerXLeft + columnWidth;
            timerXLeft = containerXLeft + timerPaddingLeft;
            timerXLefts[i] = timerXLeft;
            timerXRight = timerXLeft + timerWidth;
            timerXRights[i] = timerXRight;
        }
    }

    private void calculateTimerTypeIconDimension() {
        iconDimension = timerXRights[0] - timerXLefts[0];
    }

    private void drawDividers(Canvas canvas) {
        for(int i = 0; i < NUM_DIVIDERS; i++) {
            canvas.drawRoundRect(dividerRects[i], 0, 0, dividerPaint);
        }
    }

    private void drawTimerBlocks(Canvas canvas) {
        markShortTimers();
        float timerLength = calculateTimerBlockLength();
        for(int i = 0; i < NUM_COLUMNS; i++) {
            List<TimerSession> timerOnThisDay = getDrawableTimer(i);
            timerYStart = timerPadding;
            timerYEnd = 0;
            for(int j = 0; j < timerOnThisDay.size(); j++) {
                TimerSession timerSession = timerOnThisDay.get(j);
                timerYStart = scaled(timerSession.getStartTime(), timerLength);
                timerYEnd = scaled(timerSession.getEndTime(), timerLength);
                if(j == 0) {
                    timerYStart += timerPadding;
                }
                if(j == timerOnThisDay.size()-1) {
                    timerYEnd -= timerPadding;
                }
                RectF timerRect = new RectF(timerXLefts[i], timerYStart, timerXRights[i], timerYEnd);
                drawTimerRect(canvas, timerSession, timerRect);
                RectF iconRect = new RectF(timerXLefts[i], timerYStart, timerXRights[i], timerYStart+iconDimension);
                drawTimerTypeIcon(canvas, timerSession, iconRect);
                storeTimerRects(timerSession.getId(), timerRect);
            }
        }
    }

    private void markShortTimers() {
        // Short timers are not to be displayed
        // Calculating height should not take these timers into account
        shortTimerSessions = new ArrayList<TimerSession>();
        float currTimeLength = calculateTimerBlockLength();
        for(TimerSession timerSession: timerSessionHolder) {
            float yStart = scaled(timerSession.getStartTime(), currTimeLength);
            float yEnd = scaled(timerSession.getEndTime(), currTimeLength);
            if(!isTimerBlockVisible(yStart, yEnd)) {
                shortTimerSessions.add(timerSession);
            }
        }
    }

    private List<TimerSession>  getDrawableTimer(int day) {
        // Filter out short timers that will not be drawn
        List<TimerSession> timerOnThisDay = timerSessionHolder.getTimerOnThisDay(day);
        List<TimerSession> clone = new ArrayList<TimerSession>(timerOnThisDay);
        for(TimerSession timerSession: timerOnThisDay) {
            if(shortTimerSessions.contains(timerSession)) {
                clone.remove(timerSession);
            }
        }
        return clone;
    }

    private void drawTimerRect(Canvas canvas, TimerSession timerSession,  RectF timerRect) {
        timerPaint.setColor(timerSession.getColor());
        canvas.drawRoundRect(timerRect, BORDER_RADIUS, BORDER_RADIUS, timerPaint);
    }

    private void drawTimerTypeIcon(Canvas canvas, TimerSession timerSession, RectF rectFBound) {
        Rect rectBound = new Rect();
        rectFBound.round(rectBound);
        if(timerSession.getType() == TimerSession.TimerSessionType.VIBRATE) {
            vibrateDrawable.setBounds(rectBound);
            vibrateDrawable.draw(canvas);
        } else if (timerSession.getType() == TimerSession.TimerSessionType.SILENT) {
            silentDrawable.setBounds(rectBound);
            silentDrawable.draw(canvas);
        }
    }

    private void storeTimerRects(int timerId, RectF timerRect) {
        // This will be used to identify timers
        // in touch event later
        List<RectF> timerRects = (timerRectsById.get(timerId) == null)? new ArrayList<RectF>() :
                            timerRectsById.get(timerId);
        timerRects.add(timerRect);
        timerRectsById.put(timerId, timerRects);
    }

    private float calculateTimerBlockLength() {
        // To determine the drawing pixel per minute
        // Drawing height is determine by get the total duration of the day for the week
        // and dividing it with the total content height scale to minutes
        // Excludes short timers
        earliestTime = 86400;
        int latest = 0;
        for(TimerSession timerSession: timerSessionHolder) {
            if(!shortTimerSessions.contains(timerSession)) {
                int startHour = timerSession.getStartTime().get(Calendar.MINUTE) + (60* timerSession.getStartTime().get(Calendar.HOUR_OF_DAY));
                int endHour = timerSession.getEndTime().get(Calendar.MINUTE) + (60* timerSession.getEndTime().get(Calendar.HOUR_OF_DAY));
                if(startHour < earliestTime) {
                    earliestTime = startHour;
                }
                if(endHour > latest) {
                    latest = endHour;
                }
            }
        }
        float duration = (float)(latest - earliestTime);
        return contentHeight / duration;
    }

    private float scaled(Calendar realTime, float timerLength) {
        //we scale the time so that the earliest time would be the starting point instead of 0
        int time = realTime.get(Calendar.MINUTE) + (60 * realTime.get(Calendar.HOUR_OF_DAY));
        return (float)(time - earliestTime) * timerLength;
    }

    private boolean isTimerBlockVisible(float yStart, float yEnd) {
        return (yEnd - yStart) > iconDimension;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        float x = e.getX();
        float y = e.getY();
        //  show the corresponding timer info on the bottom cardview
        selectedTimerSession = getSelectedTimer(x, y);
        SwipeLayoutInfoView.displayTimerInfo(this.getRootView(), selectedTimerSession,
                timerSessionHolder, shortTimerSessions.contains(selectedTimerSession));
        return true;
    }

    private TimerSession getSelectedTimer(float x, float y) {
        // Find the rectangle that contains this point
        // get the timer using id correspoding to this timer
        for(int i = 0; i < timerRectsById.size(); i++) {
            List<RectF> timerRects = timerRectsById.valueAt(i);
            int timerId = timerRectsById.keyAt(i);
            for(RectF rect: timerRects) {
                if(rect.contains(x, y)) {
                    return timerSessionHolder.getTimerById(timerId);
                }
            }

        }
        return null;
    }

    /**
     * Clears the display of timer's information. Called after timer has been edited or deleted
     */
    public void invalidateTimerWeekView(TimerSession timerSession) {
        selectedTimerSession = timerSession;
        this.invalidate();
    }

    /**
     *   Swipe Layout
     */
    private static class SwipeLayoutInfoView {

        private static View root;
        private static View timerPlaceholderView;
        private static TextView timerPlaceholderTopText;
        private static TextView timerPlaceholderBottomText;
        private static View timerInfoView;
        private static TextView timerName;
        private static ImageView timerTypeIcon;
        private static ImageView timerDeleteIcon;
        private static ImageView timerEditIcon;
        private static Switch timerOnOffSwitch;
        private static TextView timerStartTimeView;
        private static TextView timerEndTimeView;
        private static TextView timerDaysView;
        private static SwipeLayout swipeLayout;
        private static RelativeLayout swipeBottomWrapperLayout;

        private static Bitmap vibrateBitmap;
        private static Bitmap silentBitmap;

        private static final String TIME_STRING_FORMAT = "HH:mm";

        private static int prevSelectedTimerSession = -1;


        // Display this timer's specific information on the bottom cardview
        // If no timer selected, display placeholder
        // If timer is too short to be display, tell user that
        public static void displayTimerInfo(final View view, final TimerSession selectedTimerSession,
                                            final TimerSessionHolder timerSessionHolder,
                                            boolean shortTimer) {
            setupView(view);

            if(selectedTimerSession != null) {
                // Don't need to re draw info if it's the same as last selected
                if(SwipeLayoutInfoView.prevSelectedTimerSession == selectedTimerSession.getId()) return;
                SwipeLayoutInfoView.prevSelectedTimerSession = selectedTimerSession.getId();

                if(shortTimer) {
                    // Time is too short to be displayed to users
                    // Address user
                    SwipeLayoutInfoView.timerPlaceholderView.setVisibility(View.VISIBLE);
                    SwipeLayoutInfoView.timerInfoView.setVisibility(View.GONE);

                    SwipeLayoutInfoView.timerPlaceholderTopText.setText(view.getResources().getText(R.string.timer_no_display_weekview));
                    SwipeLayoutInfoView.timerPlaceholderBottomText.setText("You can find timer " + selectedTimerSession.getName() + " in list view");

                } else {
                    SwipeLayoutInfoView.timerPlaceholderView.setVisibility(View.GONE);
                    SwipeLayoutInfoView.timerInfoView.setVisibility(View.VISIBLE);

                    SwipeLayoutInfoView.swipeBottomWrapperLayout.setBackgroundColor(selectedTimerSession.getColor());

                    SwipeLayoutInfoView.swipeLayout.setBottomSwipeEnabled(false);
                    SwipeLayoutInfoView.swipeLayout.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (SwipeLayoutInfoView.swipeLayout.getOpenStatus() == SwipeLayout.Status.Close) {
                                SwipeLayoutInfoView.swipeLayout.open(SwipeLayout.DragEdge.Bottom);
                            } else {
                                SwipeLayoutInfoView.swipeLayout.close(true);
                            }
                        }
                    });

                    SwipeLayoutInfoView.timerEditIcon.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent mIntent = new Intent(view.getContext(), CreateTimerActivity.class);
                            Bundle mBundle = new Bundle();
                            mBundle.putSerializable("Timer", selectedTimerSession);
                            mIntent.putExtras(mBundle);
                            view.getContext().startActivity(mIntent);
                        }
                    });

                    SwipeLayoutInfoView.timerDeleteIcon.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new AlertDialog.Builder(view.getContext())

                                    .setTitle("Delete timer")
                                    .setMessage("Are you sure you want to delete this timer?")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            timerSessionHolder.removeTimer(selectedTimerSession);
                                        }
                                    })
                                    .setIcon(null)
                                    .show();
                        }
                    });

                    SwipeLayoutInfoView.timerName.setText(selectedTimerSession.getName());
                    SwipeLayoutInfoView.timerName.setTextColor(selectedTimerSession.getColor());

                    if(selectedTimerSession.getType() == TimerSession.TimerSessionType.VIBRATE) {
                        SwipeLayoutInfoView.timerTypeIcon.setImageBitmap(vibrateBitmap);
                    } else if(selectedTimerSession.getType() == TimerSession.TimerSessionType.SILENT) {
                        SwipeLayoutInfoView.timerTypeIcon.setImageBitmap(silentBitmap);
                    }

                    SwipeLayoutInfoView.timerOnOffSwitch.setOnCheckedChangeListener(null);
                    SwipeLayoutInfoView.timerOnOffSwitch.setChecked(selectedTimerSession.getActive());
                    drawSwitch(SwipeLayoutInfoView.timerOnOffSwitch, selectedTimerSession.getColor(), view.getResources().getColor(android.R.color.darker_gray));
                    SwipeLayoutInfoView.timerOnOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            selectedTimerSession.setActive(b);
                            if (b) {
                                timerSessionHolder.setTimerActive(selectedTimerSession);
                                SwipeLayoutInfoView.timerOnOffSwitch.getTrackDrawable().setColorFilter(selectedTimerSession.getColor(), PorterDuff.Mode.MULTIPLY);
                                SwipeLayoutInfoView.timerOnOffSwitch.getThumbDrawable().setColorFilter(selectedTimerSession.getColor(), PorterDuff.Mode.MULTIPLY);
                            } else {
                                timerSessionHolder.setTimerInactive(selectedTimerSession);
                                SwipeLayoutInfoView.timerOnOffSwitch.getTrackDrawable().setColorFilter(view.getResources().getColor(android.R.color.darker_gray), PorterDuff.Mode.MULTIPLY);
                                SwipeLayoutInfoView.timerOnOffSwitch.getThumbDrawable().setColorFilter(view.getResources().getColor(android.R.color.darker_gray), PorterDuff.Mode.MULTIPLY);
                            }
                        }
                    });

                    String startTimeText = TimerUtils.getStartTimeInFormat(selectedTimerSession, TIME_STRING_FORMAT);
                    SwipeLayoutInfoView.timerStartTimeView.setText(startTimeText);

                    String endTimeText = TimerUtils.getEndTimeInFormat(selectedTimerSession, TIME_STRING_FORMAT);
                    SwipeLayoutInfoView.timerEndTimeView.setText(endTimeText);

                    String dayText = TimerUtils.getDaysInFormat(selectedTimerSession);
                    SwipeLayoutInfoView.timerDaysView.setText(dayText);
                }

            } else if(!(SwipeLayoutInfoView.timerPlaceholderView.getVisibility() == View.VISIBLE)){
                SwipeLayoutInfoView.prevSelectedTimerSession = -1;
                SwipeLayoutInfoView.timerPlaceholderView.setVisibility(View.VISIBLE);
                SwipeLayoutInfoView.timerPlaceholderTopText.setText(view.getResources().getString(R.string.timer_hint_1));
                SwipeLayoutInfoView.timerPlaceholderBottomText.setText(view.getResources().getString(R.string.timer_hint_2));
                SwipeLayoutInfoView.timerInfoView.setVisibility(View.GONE);
                if(SwipeLayoutInfoView.swipeLayout != null) swipeLayout.setLeftSwipeEnabled(false);
                SwipeLayoutInfoView.swipeLayout.setOnClickListener(null);
            }
        }

        private static void setupView(View view) {
            if(SwipeLayoutInfoView.root == null) {
                SwipeLayoutInfoView.root = view;

                SwipeLayoutInfoView.swipeLayout = (SwipeLayout) view.findViewById(R.id.timer_swipe_layout);
                SwipeLayoutInfoView.swipeBottomWrapperLayout = (RelativeLayout) view.findViewById(R.id.bottom_wrapper);
                SwipeLayoutInfoView.timerPlaceholderView = view.findViewById(R.id.timer_placeholder);
                SwipeLayoutInfoView.timerPlaceholderTopText = (TextView) view.findViewById(R.id.timer_placeholder_top);
                SwipeLayoutInfoView.timerPlaceholderBottomText = (TextView) view.findViewById(R.id.timer_placeholder_bottom);
                SwipeLayoutInfoView.timerInfoView = view.findViewById(R.id.timer_info_layout);
                SwipeLayoutInfoView.timerName = (TextView) view.findViewById(R.id.timer_name);
                SwipeLayoutInfoView.timerTypeIcon = (ImageView) view.findViewById(R.id.timer_type_icon);
                SwipeLayoutInfoView.timerDeleteIcon = (ImageView) view.findViewById(R.id.timer_delete_icon);
                SwipeLayoutInfoView.timerEditIcon = (ImageView) view.findViewById(R.id.timer_edit_icon);
                SwipeLayoutInfoView.timerOnOffSwitch = (Switch) view.findViewById(R.id.timer_switch);
                SwipeLayoutInfoView.timerStartTimeView = (TextView) view.findViewById(R.id.timer_start_time);
                SwipeLayoutInfoView.timerEndTimeView = (TextView) view.findViewById(R.id.timer_end_time);
                SwipeLayoutInfoView.timerDaysView = (TextView) view.findViewById(R.id.timer_days);

                SwipeLayoutInfoView.vibrateBitmap = BitmapFactory.decodeResource(view.getContext().getResources(), R.drawable.ic_action_vibrate);
                SwipeLayoutInfoView.silentBitmap = BitmapFactory.decodeResource(view.getContext().getResources(), R.drawable.ic_action_silent);

            }
        }

        private static void drawSwitch(Switch timerOnOffSwitch, int onColor, int offColor) {
            if(timerOnOffSwitch.isChecked()) {
                timerOnOffSwitch.getTrackDrawable().setColorFilter(onColor, PorterDuff.Mode.MULTIPLY);
                timerOnOffSwitch.getThumbDrawable().setColorFilter(onColor, PorterDuff.Mode.MULTIPLY);
            } else {
                timerOnOffSwitch.getTrackDrawable().setColorFilter(offColor, PorterDuff.Mode.MULTIPLY);
                timerOnOffSwitch.getThumbDrawable().setColorFilter(offColor, PorterDuff.Mode.MULTIPLY);
            }
        }

    }
}