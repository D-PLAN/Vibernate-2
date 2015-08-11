package com.napontaratan.vibernate.view;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import com.napontaratan.vibernate.R;
import com.napontaratan.vibernate.action.TimerSessionAction;
import com.napontaratan.vibernate.dispatcher.Dispatcher;
import com.napontaratan.vibernate.model.TimerSession;
import com.napontaratan.vibernate.model.TimerSessions;
import com.napontaratan.vibernate.model.TimerUtils;
import com.napontaratan.vibernate.store.TimerSessionStore;

import java.util.*;

/**
 * Custom Timer week view displaying timers for 7-day week that scales each timer's view
 * by the earliest and latest timers for the week
 * 3 main components in drawing are the timer rectangles, dividers ,and columns for each day
 */
public class TimerWeekView extends View implements VibernateView {
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
    private int latestTime;

    private SparseArray<List<RectF>> timerRectsById;
    private TimerSessions displayedTimerSessions;

    private enum TimerBlockType {
        START, END
    }

    public final static int TIMER_WEEK_VIEW = 0;

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
        timerRectsById =  new SparseArray<>();

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
        if(!isDimensionsSet) {
            //These are member variables to reduce allocations per draw cycle.
            // only available in ondraw, not init
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

            // to ensure all measurements are received
            TimerSessionStore.getInstance().registerView(TIMER_WEEK_VIEW, this);
        }

        drawDividers(canvas);
        drawTimerBlocks(canvas);
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
        if(displayedTimerSessions == null || !(displayedTimerSessions.size() > 0)) return;
        timerRectsById = new SparseArray<>(); // clear out
        float timerLength = getTimerBlockLength(displayedTimerSessions);
        for(int i = 0; i < NUM_COLUMNS; i++) {
            List<TimerSession> timerOnThisDay = TimerUtils.getTimerOnThisDay(displayedTimerSessions, i);
            timerYStart = 0;
            timerYEnd = 0;
            for (TimerSession timerSession : timerOnThisDay) {
                timerYStart = scaled(timerSession.getStartTime(), timerLength);
                timerYEnd = scaled(timerSession.getEndTime(), timerLength);
                if (accountForPadding(TimerBlockType.START, timerSession.getStartTime())) {
                    timerYStart += timerPadding;
                }
                if (accountForPadding(TimerBlockType.END, timerSession.getEndTime())) {
                    timerYEnd -= timerPadding;
                }
                RectF timerRect = new RectF(timerXLefts[i], timerYStart, timerXRights[i], timerYEnd);
                drawTimerRect(canvas, timerSession, timerRect);
                RectF iconRect = new RectF(timerXLefts[i], timerYStart, timerXRights[i], timerYStart + iconDimension);
                drawTimerTypeIcon(canvas, timerSession, iconRect);
                storeTimerRects(timerSession.getId(), timerRect);
            }
        }
    }

    private void drawTimerRect(Canvas canvas, TimerSession timerSession,  RectF timerRect) {
        timerPaint.setColor(timerSession.getColor());
        canvas.drawRoundRect(timerRect, BORDER_RADIUS, BORDER_RADIUS, timerPaint);
    }

    private void drawTimerTypeIcon(Canvas canvas, TimerSession timerSession, RectF rectFBound) {
        Rect rectBound = new Rect();
        rectFBound.round(rectBound);
        if(timerSession.getSessionType() == TimerSession.TimerSessionType.VIBRATE) {
            vibrateDrawable.setBounds(rectBound);
            vibrateDrawable.draw(canvas);
        } else if (timerSession.getSessionType() == TimerSession.TimerSessionType.SILENT) {
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

    private float getTimerBlockLength(TimerSessions timerSesssions) {
        // To determine the drawing pixel per minute
        // Drawing height is determine by get the total duration of the day for the week
        // and dividing it with the total content height scale to minutes
        earliestTime = 86400;
        latestTime = 0;
        for(TimerSession timerSession: timerSesssions) {
            int startMins = getTimeInMinutes(timerSession.getStartTime());
            int endMins = getTimeInMinutes(timerSession.getEndTime());
            if(startMins < earliestTime) {
                earliestTime = startMins;
            }
            if(endMins > latestTime) {
                latestTime = endMins;
            }
        }
        float duration = (float)(latestTime - earliestTime);
        return contentHeight / duration;
    }

    private int getTimeInMinutes(Calendar time) {
        return time.get(Calendar.MINUTE) + (60* time.get(Calendar.HOUR_OF_DAY));
    }

    private float scaled(Calendar realTime, float timerLength) {
        //we scale the time so that the earliest time would be the starting point instead of 0
        int time = realTime.get(Calendar.MINUTE) + (60 * realTime.get(Calendar.HOUR_OF_DAY));
        return (float)(time - earliestTime) * timerLength;
    }

    private boolean isTimerBlockVisible(TimerSession timerSession, float timerLength) {
        float yStart = scaled(timerSession.getStartTime(), timerLength);
        float yEnd = scaled(timerSession.getEndTime(), timerLength);
        float adjustedYStart = accountForPadding(TimerBlockType.START, timerSession.getStartTime())? yStart + timerPadding: yStart;
        float adjustedYEnd = accountForPadding(TimerBlockType.END, timerSession.getEndTime())?  yEnd - timerPadding: yEnd;
        return (adjustedYEnd - adjustedYStart) > iconDimension;
    }

    private boolean accountForPadding(TimerBlockType blockType, Calendar time) {
        return getTimeInMinutes(time) == (blockType == TimerBlockType.START? earliestTime: latestTime);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        float x = e.getX();
        float y = e.getY();
        TimerSession selectedTimerSession = getSelectedTimer(x, y);
        if(selectedTimerSession != null) {
            try {
                Dispatcher.getInstance().dispatchAction(TimerSessionAction.SHOW, selectedTimerSession);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
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
                    return displayedTimerSessions.get(timerId);
                }
            }
        }
        return null;
    }

    @Override
    public void storeChanged(TimerSessionStore store) {
        //everything needs to be recalculated even if just one is added
        TimerSession timerSession = store.getCurrentTimerSession();
        displayedTimerSessions = getDrawableTimers(store.getTimerSessions());
        render();
        showCurrentTimerInSwipeView(timerSession);
    }

    public void render() {
        this.invalidate();
    }

    private TimerSessions getDrawableTimers(TimerSessions timerSessions) {
        TimerSessions drawableTimerSessions = new TimerSessions();
        float tentativeTimerLength = getTimerBlockLength(timerSessions);
        for(TimerSession timerSession: timerSessions) {
            if(isDrawable(timerSession, tentativeTimerLength)) {
                drawableTimerSessions.put(timerSession.getId(), timerSession);
            }
        }
        return drawableTimerSessions;
    }

    private boolean isDrawable(TimerSession timerSession, float timerLength) {
        return isTimerBlockVisible(timerSession, timerLength);
    }

    private void showCurrentTimerInSwipeView(TimerSession timerSession) {
        Dispatcher dispatcher = Dispatcher.getInstance();
        try {
            if(timerSession != null && displayedTimerSessions.get(timerSession.getId()) == null) {
                dispatcher.dispatchAction(TimerSessionAction.SHOW_SHORT, timerSession);
            }  else {
                dispatcher.dispatchAction(TimerSessionAction.SHOW, timerSession);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}