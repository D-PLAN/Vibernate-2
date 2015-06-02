package com.napontaratan.vibernate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;

import android.graphics.drawable.*;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;

import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.napontaratan.vibernate.model.TimerConflictException;
import com.napontaratan.vibernate.model.TimerSession;
import com.napontaratan.vibernate.model.TimerSessionHolder;
import com.napontaratan.vibernate.view.ColorPickerDialog;
import com.napontaratan.vibernate.view.ColorPickerSwatch;
import com.napontaratan.vibernate.view.CreateTimerTimePicker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class CreateTimerActivity extends FragmentActivity implements TimePickerDialog.OnTimeSetListener {

    private static String COLOR_CAL_TAG  = "cal";
    private static String START_TIME_TAG = "start";
    private static String END_TIME_TAG   = "end";
    public static String KEY_HOUR        = "hour";
    public static String KEY_MINUTE      = "minute";

    // if modifying a timer, the information is passed through a bundle
    private TimerSession bundledTimer;
    private boolean[] bundledTimerDays; // used to compare if timer has changed

    // ELEMENTS
    private CreateTimerTimePicker timePicker;
    private ColorPickerDialog colorPicker;
    private Toolbar toolbar;
    private Dialog dialog;

    private EditText nameField;
    private CharSequence defaultName;

    private TextView start_time_display;
    private TextView end_time_display;

    private ToggleButton vibrate_toggle;
    private ToggleButton silent_toggle;
    private ToggleButton weekdays_toggle;
    private ToggleButton weekends_toggle;
    private List<ToggleButton> toggle_btn_days;
    private boolean[] boolean_days = new boolean[7];

    private ImageButton done;

    // VALUES
    private int colorPicked;

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    public void setupElements() {
        nameField = (EditText) findViewById(R.id.create_timer_name_field);
        nameField.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        nameField.clearFocus();

        vibrate_toggle = (ToggleButton) findViewById(R.id.create_timer_type_vibrate);
        silent_toggle  = (ToggleButton) findViewById(R.id.create_timer_type_silent);

        timePicker     = new CreateTimerTimePicker();

        start_time_display = (TextView) findViewById(R.id.create_timer_start_time_clock);
        end_time_display   = (TextView) findViewById(R.id.create_timer_end_time_clock);

        weekdays_toggle = (ToggleButton) findViewById(R.id.create_timer_weekdays_btn);
        weekends_toggle = (ToggleButton) findViewById(R.id.create_timer_weekends_btn);

        toggle_btn_days = new ArrayList<ToggleButton>();
        toggle_btn_days.add((ToggleButton) findViewById(R.id.create_timer_sun));
        toggle_btn_days.add((ToggleButton) findViewById(R.id.create_timer_mon));
        toggle_btn_days.add((ToggleButton) findViewById(R.id.create_timer_tue));
        toggle_btn_days.add((ToggleButton) findViewById(R.id.create_timer_wed));
        toggle_btn_days.add((ToggleButton) findViewById(R.id.create_timer_thu));
        toggle_btn_days.add((ToggleButton) findViewById(R.id.create_timer_fri));
        toggle_btn_days.add((ToggleButton) findViewById(R.id.create_timer_sat));

        colorPicked = getResources().getColor(R.color.colorPrimary);
        done = (ImageButton) findViewById(R.id.add_timer_button);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.create_timer);

        initializeToolbar();
        setupElements();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            bundledTimer = (TimerSession) extras.getSerializable("Timer");
            initializeView(bundledTimer);
        } else {
            initializeView();
        }

        setupFunctionality();
    }

    // called via XML
    public void onPaletteClick(MenuItem item) {
        hideSoftKeyboard();
        colorPicker.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                colorPicked = color;
                changeThemeColor(color);
                changeButtonColors(color);
            }
        });
        colorPicker.show(getFragmentManager(), COLOR_CAL_TAG);
    }

    private void initializeToolbar() {
        toolbar = (Toolbar) findViewById(R.id.create_timer_toolbar);
        toolbar.setTitle("New Timer");
        toolbar.setNavigationIcon(R.drawable.ic_action_remove);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.inflateMenu(R.menu.color_menu);

        /* ColorPicker Menu */
        String[] color_array = getBaseContext().getResources().getStringArray(R.array.default_color_choice_values);
        int[] cArray = new int[color_array.length];
        for (int k = 0; k < color_array.length; k++) {
            cArray[k] = Color.parseColor(color_array[k]);
        }
        colorPicker = ColorPickerDialog.newInstance(R.string.color_picker_title, cArray, R.color.colorPrimary, 5, ColorPickerDialog.SIZE_SMALL);
    }

    // default (called when no bundledTimer is passed in)
    public void initializeView() {
        Calendar nextHour = Calendar.getInstance();
        nextHour.add(Calendar.HOUR_OF_DAY, 1);
        boolean[] default_days = new boolean[7];
        default_days[getIntFromDayOfWeek(nextHour.get(Calendar.DAY_OF_WEEK))] = true;
        initializeView("", TimerSession.TimerSessionType.VIBRATE, Calendar.getInstance(), nextHour, default_days);
    }

    public void initializeView(TimerSession t) {
        bundledTimerDays = Arrays.copyOf(t.getDays(), 7);
        colorPicked = t.getColor();
        initializeView(t.getName(), t.getType(), t.getStartTime(), t.getEndTime(), bundledTimerDays);
    }

    // Apply values to UI elements
    public void initializeView(String name, TimerSession.TimerSessionType type, Calendar start_time, Calendar end_time, boolean[] days) {
        nameField.setText(name);
        defaultName = generateHint();
        nameField.setHint("Enter a name (eg. " + defaultName + ")");

        if(type == TimerSession.TimerSessionType.VIBRATE) {
            markVibrateType(vibrate_toggle, true);
        }
        else {
            markVibrateType(silent_toggle, true);
        }

        start_time_display.setText(generateTimeFromCalendar(start_time));
        end_time_display.setText(generateTimeFromCalendar(end_time));

        for(int i = 0; i < 7; i++){
            if(days[i]) markDay(i, true);
            else markDay(i, false);
        }

        checkDays();
        changeButtonColors(colorPicked);
        changeThemeColor(colorPicked);
    }

    private CharSequence generateHint() {
        String[] hints = getResources().getStringArray(R.array.hints);
        int index = new Random().nextInt(hints.length);
        return hints[index];
    }

    // Attach listeners to UI elements
    public void setupFunctionality() {
        // vibrate and silent toggles cannot both be checked at the same time
        vibrate_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard();
                markVibrateType(vibrate_toggle, true);
                if(silent_toggle.isChecked()) {
                    markVibrateType(silent_toggle, false);
                }
            }
        });
        silent_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard();
                markVibrateType(silent_toggle, true);
                if(vibrate_toggle.isChecked()) {
                    markVibrateType(vibrate_toggle, false);
                }
            }
        });

        // timePicker dialog should popup when tapping on start/end TextView
        start_time_display.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                String[] start = start_time_display.getText().toString().split(":");
                int h = Integer.parseInt(start[0]);
                int m = Integer.parseInt(start[1]);
                showTimePickerDialog(h, m, START_TIME_TAG);
            }
        });
        end_time_display.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                String[] end = end_time_display.getText().toString().split(":");
                int h = Integer.parseInt(end[0]);
                int m = Integer.parseInt(end[1]);
                showTimePickerDialog(h, m, END_TIME_TAG);
            }
        });

        // circle days btn
        // if sunday(0) OR saturday(6) is clicked
        for(int i = 0; i < 7; i+=6){
            final int finalI = i;
            toggle_btn_days.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(toggle_btn_days.get(finalI).isChecked()) {
                        boolean_days[finalI] = true;
                        checkDays();
                    } else {
                        weekends_toggle.setChecked(false);
                        boolean_days[finalI] = false;
                    }
                }
            });
        }
        // if one of the toggle_btn_days from mon-fri is clicked
        for(int i = 1; i < 6; i++){
            final int finalI = i;
            toggle_btn_days.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(toggle_btn_days.get(finalI).isChecked()) {
                        boolean_days[finalI] = true;
                        checkDays();
                    } else {
                        weekdays_toggle.setChecked(false);
                        boolean_days[finalI] = false;
                    }
                }
            });
        }

        // weekdays and weekends button
        weekdays_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                if(weekdays_toggle.isChecked()) {
                    for(int i = 1; i <= 5; i++)
                        markDay(i, true);
                } else {
                    for(int i = 1; i <= 5; i++)
                        markDay(i, false);
                }
            }
        });
        weekends_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                if(weekends_toggle.isChecked()) {
                    for(int i = 0; i < 7; i+=6)
                        markDay(i, true);
                } else {
                    for(int i = 0; i < 7; i+=6)
                        markDay(i, false);
                }
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimerSession newTimer = produceNewTSObject();
                // check if fields are valid
                if(!checkValidity(newTimer)) return;
                try {
                    if(bundledTimer != null) modifyTimer(newTimer);
                    else createTimerSession(newTimer);
                } catch (TimerConflictException e) {
                    createDialog("Timer Conflict", "The time specified is in conflict with another timer. Please try again.");
                }
            }
        });
    }

    // Rules:
    //    - cannot create a timer with an empty name
    //    - cannot create a timer that ends before it begins
    //    - cannot create a timer without a day
    private boolean checkValidity(TimerSession newTimer) {
        String name = newTimer.getName().trim();

        if (!newTimer.getStartTime().before(newTimer.getEndTime())) {
            createDialog("Invalid Time Range", "Please specify a valid range.");
            return false;
        }

        boolean[] days = newTimer.getDays();
        for (int i = 0; i < 7; i++) {
            if (days[i]) return true;
        }

        createDialog("Insufficient info", "Please specify a day.");
        return false;
    }

    // Sum up the values on the interface and produce a new TimerSession object
    private TimerSession produceNewTSObject() {
        String name = (nameField.getText().toString().equals(""))?
                defaultName.toString(): nameField.getText().toString();
        TimerSession.TimerSessionType type;
        if(vibrate_toggle.isChecked()) type = TimerSession.TimerSessionType.VIBRATE;
        else type = TimerSession.TimerSessionType.SILENT;

        String[] temp = start_time_display.getText().toString().split(":");
        int h = Integer.parseInt(temp[0]);
        int m = Integer.parseInt(temp[1]);
        Calendar startTime = generateCalendar(h,m);

        temp = end_time_display.getText().toString().split(":");
        h = Integer.parseInt(temp[0]);
        m = Integer.parseInt(temp[1]);
        Calendar endTime = generateCalendar(h,m);

        return new TimerSession(name, type, startTime, endTime, boolean_days, colorPicked);
    }

    // Create a new timer unless it is in conflict with another existing timer
    private void createTimerSession(TimerSession newTimer) throws TimerConflictException {
        TimerSessionHolder timerSessionHolder = TimerSessionHolder.getInstance();
        timerSessionHolder.addTimer(newTimer);
        finish();
    }

    // Doesn't create a new system timer if only the name or color has been modified
    private void modifyTimer(TimerSession newTimer) throws TimerConflictException {
        TimerSessionHolder timerSessionHolder = TimerSessionHolder.getInstance();
        int modify = isModified(newTimer);
        switch(modify) {
            case -1: // no new change has been made
                break;
            case 0: // minor change
                TimerSession existingTimer = timerSessionHolder.getTimerById(bundledTimer.getId());
                existingTimer.setName(newTimer.getName());
                existingTimer.setColor(newTimer.getColor());
                finish();
                break;
            case 1: // big change
                newTimer.setActive(bundledTimer.getActive());
                timerSessionHolder.updateTimer(newTimer, bundledTimer);
                finish();
                break;
        }
    }

    //change weekdays + weekends button colors
    private void changeButtonColors(int colorPicked) {
        for(ToggleButton day : toggle_btn_days) {
            if(day.isChecked()) {
                changeButtonColor(day, colorPicked);
            } else {
                day.setChecked(true);
                changeButtonColor(day, colorPicked);
                day.setChecked(false);
            }
        }

        if(weekdays_toggle.isChecked()) {
            changeButtonColor(weekdays_toggle, colorPicked);
        } else {
            weekdays_toggle.setChecked(true);
            changeButtonColor(weekdays_toggle, colorPicked);
            weekdays_toggle.setChecked(false);
        }
        if(weekends_toggle.isChecked()) {
            changeButtonColor(weekends_toggle, colorPicked);
        } else {
            weekends_toggle.setChecked(true);
            changeButtonColor(weekends_toggle, colorPicked);
            weekends_toggle.setChecked(false);
        }

        if(vibrate_toggle.isChecked()) vibrate_toggle.setTextColor(colorPicked);
        else silent_toggle.setTextColor(colorPicked);
    }

    // change the color of the checked state of the ToggleButton
    private void changeButtonColor(ToggleButton btn, int rgbColor) {
        try {
            StateListDrawable stateListDrawable = (StateListDrawable) btn.getBackground();
            int[] currentState = btn.getBackground().getState();
            Method getStateDrawable = StateListDrawable.class.getMethod("getStateDrawable", int.class);
            Method getStateDrawableIndex = StateListDrawable.class.getMethod("getStateDrawableIndex", int[].class);
            int index = (int) getStateDrawableIndex.invoke(stateListDrawable,currentState);
            GradientDrawable drawable = (GradientDrawable) getStateDrawable.invoke(stateListDrawable,index);
            drawable.setColor(rgbColor);
            drawable.invalidateSelf();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // change toolbar and status bar color
    private void changeThemeColor(int color) {
        //To darken the colorPicked
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        int colorPickedDarker = Color.HSVToColor(hsv);

        //Changing Status Bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(colorPickedDarker);
        }
        colorPicked = color;
        toolbar.setBackgroundColor(color);
    }

    //to get rid of keyboard
    private void hideSoftKeyboard() {
        if (getCurrentFocus() != null && getCurrentFocus() instanceof EditText) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(nameField.getWindowToken(), 0);
        }
    }

    // generate a calendar object on the current day with hour and minute
    private Calendar generateCalendar(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    // create an alert dialog
    private void createDialog(String title, String msg) {
        if(dialog != null && dialog.isShowing()) dialog.dismiss();
        dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setIcon(R.drawable.ic_launcher)
                .setMessage(msg)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    // create a string in the format HH:mm from a calendar object
    private String generateTimeFromCalendar(Calendar c) {
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        String hour_str = (hour < 10)?  "0" + hour: Integer.toString(hour);
        String min_str = (min < 10)?  "0" + min: Integer.toString(min);
        return hour_str + ":" + min_str;
    }

    // set boolean at index d to false and
    // uncheck the toggle button on screen
    private void markDay(int d, boolean b) {
        boolean_days[d] = b;
        toggle_btn_days.get(d).setChecked(b);
    }

    // set highlight to toggle btn text
    // and set toggle btn to be checked
    private void markVibrateType(ToggleButton btn, Boolean b) {
        btn.setChecked(b);
        if(b) btn.setTextColor(colorPicked); // checked
        else  btn.setTextColor(getResources().getColor(android.R.color.black)); // unchecked
    }

    // if Mon-Fri are checked, check the weekdays button
    // if Sat,Sun are checked, check the weekends button
    private void checkDays() {
        weekdays_toggle.setChecked(true);
        for(int i = 1; i <= 5; i++) {
            if(!boolean_days[i]) {
                weekdays_toggle.setChecked(false);
                break;
            }
        }
        weekends_toggle.setChecked(false);
        if(boolean_days[0] && boolean_days[6]) weekends_toggle.setChecked(true);
    }

    // return 1 if modified
    // return 0 if only color or name are modified
    // return -1 if nothing is modified
    private int isModified(TimerSession newTimer) {
        if (!newTimer.getStartTime().equals(bundledTimer.getStartTime()) || !newTimer.getEndTime().equals(bundledTimer.getEndTime()) ||
                !arrayCompare(newTimer.getDays(),bundledTimer.getDays()) || newTimer.getType() != bundledTimer.getType()){
            return 1;
        } else if (newTimer.getColor() != bundledTimer.getColor() || !newTimer.getName().equals(bundledTimer.getName())) {
            return 0;
        } else {
            return -1;
        }
    }

    private boolean arrayCompare(boolean[] arr1, boolean[] arr2) {
        for(int i = 0; i < arr1.length; i++) {
            if(arr1[i] != arr2[i]) return false;
        }
        return true;
    }

    private void showTimePickerDialog(int h, int m, String tag) {
        Bundle b = new Bundle();
        b.putInt(KEY_HOUR, h);
        b.putInt(KEY_MINUTE, m);
        timePicker.setArguments(b);
        timePicker.show(getSupportFragmentManager(), tag);
    }

    @Override
    public void onTimeSet(TimePicker t, int h, int m) {
        if(timePicker.getTag().equals(START_TIME_TAG)) {
            start_time_display.setText(fixTimeFormat(h) + ":" + fixTimeFormat(m));
        } else {
            end_time_display.setText(fixTimeFormat(h) + ":" + fixTimeFormat(m));
        }
    }

    // Given a time append 0 if needed
    // eg. 1 -> 01
    private String fixTimeFormat(int n) {
        if(n < 10) return "0" + n;
        else return Integer.toString(n);
    }

    private int getIntFromDayOfWeek(int DAY_OF_WEEK) {
        switch(DAY_OF_WEEK) {
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

    // ONLY FOR DEBUGGING
    private String printArray(boolean[] array) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < array.length; i++){
            if(array[i]) sb.append("1 ");
            else sb.append("0 ");
        }
        return sb.toString();
    }
}
