package com.napontaratan.vibernate.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.*;
import com.daimajia.swipe.SwipeLayout;
import com.napontaratan.vibernate.R;
import com.napontaratan.vibernate.action.TimerSessionAction;
import com.napontaratan.vibernate.dispatcher.Dispatcher;
import com.napontaratan.vibernate.model.TimerSession;
import com.napontaratan.vibernate.model.TimerSessionCommonView;
import com.napontaratan.vibernate.store.TimerSessionStore;

/**
 * Created by daniel on 2015-07-12.
 * Bottom card view swipe layout to show timer info for selected timer
 * in the timer block view above
 */
public final class SwipeLayoutInfoView implements VibernateView{

    private View rootView;
    private View timerPlaceholderView;
    private TextView timerPlaceholderTopText;
    private TextView timerPlaceholderBottomText;
    private View timerInfoView;
    private TextView timerName;
    private ImageView timerSessionTypeIcon;
    private ImageView timerAddTypeIcon;
    private ImageView timerDeleteIcon;
    private ImageView timerEditIcon;
    private Switch timerOnOffSwitch;
    private TextView timerStartTimeView;
    private TextView timerEndTimeView;
    private TextView timerDaysView;
    private SwipeLayout swipeLayout;
    private RelativeLayout swipeBottomWrapperLayout;

    private final String LIST_VIEW_TIMER_TEXT = "You can find timer %s in list view";

    private int prevSelectedTimerSessionHash = -1;
    private Dispatcher dispatcher;

    public final static int SWIPE_VIEW = 1;

    public SwipeLayoutInfoView(View view) {
        rootView = view;
        dispatcher = Dispatcher.getInstance();
        setup();
        TimerSessionStore.getInstance().registerView(SWIPE_VIEW, this);
    }

    private void setup() {
        swipeLayout = (SwipeLayout) rootView.findViewById(R.id.timer_swipe_layout);
        swipeBottomWrapperLayout = (RelativeLayout) rootView.findViewById(R.id.bottom_wrapper);
        timerPlaceholderView = rootView.findViewById(R.id.timer_placeholder);
        timerPlaceholderTopText = (TextView) rootView.findViewById(R.id.timer_placeholder_top);
        timerPlaceholderBottomText = (TextView) rootView.findViewById(R.id.timer_placeholder_bottom);
        timerInfoView = rootView.findViewById(R.id.timer_info_layout);
        timerName = (TextView) rootView.findViewById(R.id.timer_name);
        timerSessionTypeIcon = (ImageView) rootView.findViewById(R.id.timer_session_type_icon);
        timerAddTypeIcon = (ImageView) rootView.findViewById(R.id.timer_add_type_icon);
        timerDeleteIcon = (ImageView) rootView.findViewById(R.id.timer_delete_icon);
        timerEditIcon = (ImageView) rootView.findViewById(R.id.timer_edit_icon);
        timerOnOffSwitch = (Switch) rootView.findViewById(R.id.timer_switch);
        timerStartTimeView = (TextView) rootView.findViewById(R.id.timer_start_time);
        timerEndTimeView = (TextView) rootView.findViewById(R.id.timer_end_time);
        timerDaysView = (TextView) rootView.findViewById(R.id.timer_days);
    }

    private void showTimerInfo(final TimerSession selectedTimerSession) {
        toggleTimerInfoLayout(true, false);

        swipeBottomWrapperLayout.setBackgroundColor(selectedTimerSession.getColor());

        swipeLayout.setBottomSwipeEnabled(false);
        swipeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimerSessionCommonView.toggleSwipeLayout(swipeLayout, SwipeLayout.DragEdge.Bottom);
            }
        });

        timerEditIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimerSessionCommonView.editTimerSession(view.getContext(), selectedTimerSession, swipeLayout);
                TimerSessionCommonView.toggleSwipeLayout(swipeLayout, SwipeLayout.DragEdge.Bottom);
            }
        });

        timerDeleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTimerSession(selectedTimerSession);
            }
        });

        timerName.setText(selectedTimerSession.getName());
        timerName.setTextColor(selectedTimerSession.getColor());

        TimerSessionCommonView.setIconBitmaps(timerSessionTypeIcon,
                selectedTimerSession.getSessionType() == TimerSession.TimerSessionType.VIBRATE,
                TimerSessionCommonView.vibrateBitmap, TimerSessionCommonView.silentBitmap);
        TimerSessionCommonView.setIconBitmaps(timerAddTypeIcon,
                selectedTimerSession.getAddType() == TimerSession.TimerAddType.ONETIME,
                TimerSessionCommonView.oneTimeBitmap, TimerSessionCommonView.recurringBitmap);

        timerOnOffSwitch.setOnCheckedChangeListener(null);
        timerOnOffSwitch.setChecked(selectedTimerSession.getActive());
        drawSwitch(selectedTimerSession);
        timerOnOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                try {
                    dispatcher.dispatchAction(
                            b? TimerSessionAction.ACTIVATE_TIMER : TimerSessionAction.DEACTIVATE_TIMER,
                            selectedTimerSession
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        String startTimeText = TimerSessionCommonView.getTimeFormat(selectedTimerSession.getStartTime(), TimerSessionCommonView.TIMER_DATE_FORMAT);
        timerStartTimeView.setText(startTimeText);

        String endTimeText = TimerSessionCommonView.getTimeFormat(selectedTimerSession.getEndTime(), TimerSessionCommonView.TIMER_DATE_FORMAT);
        timerEndTimeView.setText(endTimeText);

        String dayText = TimerSessionCommonView.getDaysInFormat(selectedTimerSession);
        timerDaysView.setText(dayText);
    }

    private void drawSwitch(TimerSession selectedTimerSession) {
        Switch activeSwitch = timerOnOffSwitch;
        int switchColorInt = activeSwitch.isChecked()?
                selectedTimerSession.getColor() : rootView.getResources().getColor(android.R.color.darker_gray);
        activeSwitch.getTrackDrawable().setColorFilter(switchColorInt, PorterDuff.Mode.MULTIPLY);
        activeSwitch.getThumbDrawable().setColorFilter(switchColorInt, PorterDuff.Mode.MULTIPLY);
    }

    private  void deleteTimerSession(final TimerSession selectedTimerSession) {
        new AlertDialog.Builder(rootView.getContext())
                .setTitle("Delete timer")
                .setMessage("Are you sure you want to delete this timer?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            dispatcher.dispatchAction(TimerSessionAction.REMOVE, selectedTimerSession);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setIcon(R.drawable.ic_launcher)
                .show();
    }

    private void toggleTimerInfoLayout(boolean info, boolean placeholder) {
        timerPlaceholderView.setVisibility(placeholder? View.VISIBLE: View.GONE);
        timerInfoView.setVisibility(info ? View.VISIBLE : View.GONE);
    }

    private void showPlaceholderText() {
        String hint1 = rootView.getResources().getString(R.string.timer_hint_1);
        String hint2 = rootView.getResources().getString(R.string.timer_hint_2);
        setPlaceholderText(hint1, hint2);
    }

    private void showTimerTooShortText(TimerSession selectedTimerSession) {
        String shortTimerText = rootView.getResources().getString(R.string.short_timer_no_display_weekview);
        String listViewTimerText = String.format(LIST_VIEW_TIMER_TEXT, selectedTimerSession.getName());
        setPlaceholderText(shortTimerText, listViewTimerText);
    }

    private void setPlaceholderText(String topText, String bottomText) {
        if(!(timerPlaceholderView.getVisibility() == View.VISIBLE)){
            prevSelectedTimerSessionHash = -1;
            toggleTimerInfoLayout(false, true);
            if(swipeLayout != null) {
                swipeLayout.setLeftSwipeEnabled(false);
                swipeLayout.setOnClickListener(null);
            }
        }
        timerPlaceholderTopText.setText(topText);
        timerPlaceholderBottomText.setText(bottomText);
    }

    @Override
    public void storeChanged(TimerSessionStore store) {
        render(store.getCurrentAction(), store.getCurrentTimerSession());
    }

    public void render(TimerSessionAction action,TimerSession selectedTimerSession) {
        if(selectedTimerSession == null) {
            // start of app, timer deleted
            showPlaceholderText();
        } else {
            // Don't need to re draw info if it's the same as last selected
            if(prevSelectedTimerSessionHash == selectedTimerSession.hashCode()) return;
            prevSelectedTimerSessionHash = selectedTimerSession.hashCode();

            if (action == TimerSessionAction.SHOW_SHORT) {
                showTimerTooShortText(selectedTimerSession);
            } else {

                showTimerInfo(selectedTimerSession);
            }
        }
    }

}
