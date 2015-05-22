package com.napontaratan.vibernate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.napontaratan.vibernate.model.TimerConflictException;
import com.napontaratan.vibernate.model.TimerSession;
import com.napontaratan.vibernate.model.TimerSessionHolder;
import com.napontaratan.vibernate.view.ColorPickerDialog;
import com.napontaratan.vibernate.view.ColorPickerSwatch;
import com.napontaratan.vibernate.view.CreateTimerTimePicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class CreateTimerActivity extends FragmentActivity {

    private CreateTimerTimePicker timePicker;
    private List<ToggleButton> days;
    private int colorPicked = -13388315;
    private boolean[] bDays = new boolean[7];
    private TimerSession ts;
    private boolean[] bundleDays;
    EditText nameField;
    Button colorButtonMon;
    Button colorButtonTue;
    Button colorButtonWed;
    Button colorButtonThu;
    Button colorButtonFri;
    Button colorButtonSat;
    Button colorButtonSun;
    Button colorButtonWeekday;
    Button colorButtonWeekend;

//    @Override
//    protected void onStart() {
//        super.onStart();
//        GoogleAnalytics.getInstance(this).reportActivityStart(this);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        GoogleAnalytics.getInstance(this).reportActivityStop(this);
//    }

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.create_timer);
        timePicker = new CreateTimerTimePicker();

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            ts = (TimerSession)extras.getSerializable("Timer");
            bundleDays = Arrays.copyOf(ts.getDays(), 7);
        } else {
            ts = null;
        }

        //CheckMark Button made up here so that we can dynamically change color
        final ImageButton done = (ImageButton) findViewById(R.id.add_timer_button);

        /* toolbar */
        final Toolbar toolbar = (Toolbar) findViewById(R.id.create_timer_toolbar);
        toolbar.setTitle("New Timer");
        toolbar.setNavigationIcon(R.drawable.ic_action_remove);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /* ColorPicker Menu */
        toolbar.inflateMenu(R.menu.color_menu);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                hideSoftKeyboard();
                String[] color_array = getBaseContext().getResources().getStringArray(R.array.default_color_choice_values);
                int[] cArray = new int[color_array.length];
                for (int k = 0; k < color_array.length; k++) {
                    ;
                    cArray[k] = Color.parseColor(color_array[k]);
                }

                ColorPickerDialog colorCalendar = ColorPickerDialog.newInstance(
                        R.string.color_picker_default_title,
                        cArray,
                        R.color.blue,
                        5,
                        ColorPickerDialog.SIZE_SMALL);

                //Implement listener to get selected color value
                colorCalendar.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        Toast.makeText(getBaseContext(), "Color is " + color, Toast.LENGTH_SHORT).show();
                        colorPicked = color;
                        System.out.println("colorPicked is " + colorPicked);

                        //change button colors
                        changeButtonColors(colorPicked);

                        //To darken the colorPicked
                        float[] hsv = new float[3];
                        int colorPickedDarker = colorPicked;
                        Color.colorToHSV(color, hsv);
                        hsv[2] *= 0.8f; // value component
                        colorPickedDarker = Color.HSVToColor(hsv);
                        System.out.println("colorPickedDarker is " + colorPickedDarker);

                        //Changing toolbar color
                        toolbar.setBackgroundColor(color);

   /*                     //Changing Checkbox color
                        Drawable button = (Drawable) done.getBackground();
                        button.setColorFilter(colorPicked, PorterDuff.Mode.SRC_ATOP);
*/
                        //Changing Status Bar color
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                           Window window = getWindow();
                            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                            window.setStatusBarColor(colorPickedDarker);

                        }
                    }
                });

                colorCalendar.show(getFragmentManager(), "cal");
                return false;
            }
        });

        /* name field */
        nameField = (EditText) findViewById(R.id.create_timer_name_field);
        nameField.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        nameField.clearFocus();

        /* vibrate or silent mode */
        final ToggleButton typeVibrate = (ToggleButton) findViewById(R.id.create_timer_type_vibrate);
        final ToggleButton typeSilent  = (ToggleButton) findViewById(R.id.create_timer_type_silent);
        typeVibrate.setChecked(true);
        typeVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                if(typeSilent.isChecked()) {
                    typeSilent.setChecked(false);
                }

                if(!typeVibrate.isChecked())
                    typeVibrate.setChecked(true);
            }
        });

        typeSilent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                if(typeVibrate.isChecked()) {
                    typeVibrate.setChecked(false);
                }

                if(!typeSilent.isChecked())
                    typeSilent.setChecked(true);
            }
        });


        /* start time & end time */
        final TextView startTime = (TextView) findViewById(R.id.create_timer_start_time_clock);
        final TextView endTime = (TextView) findViewById(R.id.create_timer_end_time_clock);

        Calendar c = Calendar.getInstance();
        int currentHour = c.get(Calendar.HOUR_OF_DAY);
        int currentMin = c.get(Calendar.MINUTE);
        int nextHour = (currentHour+1 < 24)? currentHour + 1: currentHour;
        int nextMin = currentMin;
        int currentDay = c.get(Calendar.DAY_OF_WEEK) - 1;

        String minString = (currentMin < 10)?  "0" + currentMin: Integer.toString(currentMin);
        String currentString = currentHour + ":" + minString;
        String nextString = nextHour + ":" + minString;

        startTime.setText(currentString);
        endTime.setText(nextString);

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                String[] start = startTime.getText().toString().split(":");
                timePicker.setTime(Integer.parseInt(start[0]), Integer.parseInt(start[1]));
                timePicker.show(getSupportFragmentManager(), "startTimePicker");
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                String[] end = endTime.getText().toString().split(":");
                timePicker.setTime(Integer.parseInt(end[0]), Integer.parseInt(end[1]));
                timePicker.show(getSupportFragmentManager(), "endTimePicker");
            }
        });

        /* choosing days */
        final ToggleButton weekdays_btn = (ToggleButton) findViewById(R.id.create_timer_weekdays_btn);
        final ToggleButton weekends_btn = (ToggleButton) findViewById(R.id.create_timer_weekends_btn);

        days = new ArrayList<ToggleButton>();
        days.add((ToggleButton) findViewById(R.id.create_timer_sun));
        days.add((ToggleButton) findViewById(R.id.create_timer_mon));
        days.add((ToggleButton) findViewById(R.id.create_timer_tue));
        days.add((ToggleButton) findViewById(R.id.create_timer_wed));
        days.add((ToggleButton) findViewById(R.id.create_timer_thu));
        days.add((ToggleButton) findViewById(R.id.create_timer_fri));
        days.add((ToggleButton) findViewById(R.id.create_timer_sat));

        //setting current day to be highlighted
        days.get(currentDay).setChecked(true);
        bDays[currentDay] = true;

        // if sunday(0) OR saturday(6)   is clicked
        for(int i = 0; i < 7; i+=6){
            final int finalI = i;
            days.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(days.get(finalI).isChecked()) {
                        bDays[finalI] = true;
                        for(int j = 0; j < 7; j+=6){
                            if(!days.get(j).isChecked()) return;
                        }
                        weekends_btn.setChecked(true);
                    } else {
                        weekends_btn.setChecked(false);
                        bDays[finalI] = false;
                    }
                }
            });
        }

        // if one of the days from mon-fri is clicked
        for(int i = 1; i < 6; i++){
            final int finalI = i;
            days.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(days.get(finalI).isChecked()) {
                        bDays[finalI] = true;
                        for(int j = 1; j < 6; j++){
                            if(!days.get(j).isChecked()) return;
                        }
                        weekdays_btn.setChecked(true);
                    } else {
                        weekdays_btn.setChecked(false);
                        bDays[finalI] = false;
                    }
                }
            });
        }

        weekdays_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                if(weekdays_btn.isChecked()) {
                    for(int i = 0; i < 7; i++){
                        if(i != 0 && i != 6) {
                            days.get(i).setChecked(true);
                            bDays[i] = true;
                        }
                    }
                } else {
                    for(int i = 0; i < 7; i++){
                        if(i != 0 && i != 6) {
                            days.get(i).setChecked(false);
                            bDays[i] = false;
                        }
                    }
                }
            }
        });

        weekends_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                if(weekends_btn.isChecked()) {
                    for(int i = 0; i < 7; i++){
                        if(i == 0 || i == 6) {
                            days.get(i).setChecked(true);
                            bDays[i] = true;
                        }
                    }
                } else {
                    for(int i = 0; i < 7; i++){
                        if(i == 0 || i == 6) {
                            days.get(i).setChecked(false);
                            bDays[i] = false;
                        }
                    }
                }
            }
        });

        /* Click on check mark */
        //ImageButton done = (ImageButton) findViewById(R.id.add_timer_button);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameField.getText().toString();
                TimerSession.TimerSessionType type;
                if (typeVibrate.isChecked()) type = TimerSession.TimerSessionType.VIBRATE;
                else type = TimerSession.TimerSessionType.SILENT;
                String start = startTime.getText().toString().replace(":", "");
                String end = endTime.getText().toString().replace(":", "");
                int start_hour = Integer.parseInt(start.substring(0, 2));
                int start_min  = Integer.parseInt(start.substring(2));
                int end_hour   = Integer.parseInt(end.substring(0,2));
                int end_min    = Integer.parseInt(end.substring(2));

                // if this is a timer modify, check if anything is changed
                createTimerSession(name, type, start_hour, start_min, end_hour, end_min, days, colorPicked);
            }
        });

        //grabbing information from bundle
        if (ts != null) {

            //Change color
                    //To darken the colorPicked
                    float[] hsv = new float[3];
                    int colorPickedDarker = ts.getColor();
                    colorPicked = colorPickedDarker;
                    Color.colorToHSV(ts.getColor(), hsv);
                    hsv[2] *= 0.8f; // value component
                    colorPickedDarker = Color.HSVToColor(hsv);
                    System.out.println("colorPickedDarker is " + colorPickedDarker);
            
                    changeButtonColors(colorPicked);

                    //Changing toolbar color
                    toolbar.setBackgroundColor(ts.getColor());

                    /*//Changing Checkbox color
                    Drawable button = (Drawable) done.getBackground();
                    button.setColorFilter(ts.getColor(), PorterDuff.Mode.SRC_ATOP);*/

                    //Changing Status Bar color
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(colorPickedDarker);
                    }
            //Change name
            nameField.setText(ts.getName(), TextView.BufferType.EDITABLE);
            nameField.clearFocus();

            //Vibrate/Silent
            if (ts.getType() == TimerSession.TimerSessionType.VIBRATE) {
                typeVibrate.setChecked(true);
                typeSilent.setChecked(false);
            } else {
                typeSilent.setChecked(true);
                typeVibrate.setChecked(false);
            }

            //Saved StartTime and EndTime
            Calendar editedCalendar = ts.getStartTime();
            int savedStartHour = editedCalendar.get(Calendar.HOUR_OF_DAY);
            int savedStartMin = editedCalendar.get(Calendar.MINUTE);
            editedCalendar = ts.getEndTime();
            int savedEndHour = editedCalendar.get(Calendar.HOUR_OF_DAY);
            int savedEndMin = editedCalendar.get(Calendar.MINUTE);

            String savedStartHourString = (savedStartHour < 10)?  "0" + savedStartHour: Integer.toString(savedStartHour);
            String savedStartMinString = (savedStartMin < 10)?  "0" + savedStartMin: Integer.toString(savedStartMin);
            String savedEndHourString = (savedEndHour < 10)?  "0" + savedEndHour: Integer.toString(savedEndHour);
            String savedEndMinString = (savedEndMin < 10)?  "0" + savedEndMin: Integer.toString(savedEndMin);

            String start = savedStartHourString + ":" + savedStartMinString;
            String end = savedEndHourString + ":" + savedEndMinString;

            startTime.setText(start);
            endTime.setText(end);

            //Days
//            days.get(currentDay).setChecked(false);

            for (int i=0; i < bundleDays.length ; i++) {
                if (bundleDays[i] == true) {
                    days.get(i).setChecked(true);
                    bDays[i] = true;
                }
            }

            if(bundleDays[0] && bundleDays[6]) weekends_btn.setChecked(true);
            if(bundleDays[1] && bundleDays[2] && bundleDays[3] && bundleDays[4] && bundleDays[5]) weekdays_btn.setChecked(true);
        }
    }

    private void createTimerSession (String name, TimerSession.TimerSessionType type, int startHour, int startMin, int endHour, int endMin, List<ToggleButton> days, int color) {
        if(name == null || name.equals("")) {
            createDialog("Insufficient info", "Please specify a timer name.");
            return;
        }
        
        boolean daySelected = false;
        for(int i = 0; i < 7; i++) {
            if(bDays[i]){
                daySelected = true;
                break;
            }
        }

        if(!daySelected) {
            createDialog("Insufficient info", "Please specify a day.");
            return;
        }

        Calendar start = generateCalendar(startHour, startMin);
        Calendar end   = generateCalendar(endHour, endMin);
        if (start.after(end)) {
            createDialog("Invalid Time Range", "Please specify a valid range.");
            return;
        }

        TimerSession newTimer = new TimerSession(name, type, start, end, bDays,color);
        TimerSessionHolder timerSessionHolder = TimerSessionHolder.getInstance();

        try {
            if(ts == null || (ts!= null && isModified(newTimer) == 1)) {
                if(ts != null && isModified(newTimer) == 1) {
                    newTimer.setTimerSnooze(ts.getTimerSnooze());
                    timerSessionHolder.removeTimer(ts);
                }
                timerSessionHolder.addTimer(newTimer);
                Log.d("CreateTimer", "creating timer with the following information: \n" +
                        "Name: " + name + "\n" +
                        "Type: " + type + "\n" +
                        "StartTime" + start.get(Calendar.HOUR_OF_DAY) + ":" + start.get(Calendar.MINUTE) + "\n" +
                        "EndTime" + end.get(Calendar.HOUR_OF_DAY) + ":" + end.get(Calendar.MINUTE) + "\n" +
                        "Days" + printArray(bDays) + "\n");
            } else {
                if(ts != null && isModified(newTimer) == 0) {
                    TimerSession existingTimer = timerSessionHolder.getTimerById(ts.getId());
                    existingTimer.setName(name);
                    existingTimer.setColor(color);
                    Log.d("CreateTimer", "Bundle not null and only name or color changed");
                } else {
                    Log.d("CreateTimer", "Bundle not null and Timer not modified");
                }
            }
        } catch (TimerConflictException e) {
            createDialog("Timer Conflict", "The time specified is in conflict with another timer. Please try again.");
            return;
        }

        finish();
    }
    //change weekdays + weekends button colors
    private void changeButtonColors(int colorPicked) {
        colorButtonMon = (Button) findViewById(R.id.create_timer_mon);
        colorButtonTue = (Button) findViewById(R.id.create_timer_tue);
        colorButtonWed = (Button) findViewById(R.id.create_timer_wed);
        colorButtonThu = (Button) findViewById(R.id.create_timer_thu);
        colorButtonFri = (Button) findViewById(R.id.create_timer_fri);
        colorButtonSat = (Button) findViewById(R.id.create_timer_sat);
        colorButtonSun = (Button) findViewById(R.id.create_timer_sun);
        colorButtonWeekday = (Button) findViewById(R.id.create_timer_weekdays_btn);
        colorButtonWeekend = (Button) findViewById(R.id.create_timer_weekends_btn);

        switch (colorPicked) {
            case -14301735:
                //normal
                colorButtonMon.setBackgroundResource(R.drawable.circle_select);
                colorButtonTue.setBackgroundResource(R.drawable.circle_select);
                colorButtonWed.setBackgroundResource(R.drawable.circle_select);
                colorButtonThu.setBackgroundResource(R.drawable.circle_select);
                colorButtonFri.setBackgroundResource(R.drawable.circle_select);
                colorButtonSat.setBackgroundResource(R.drawable.circle_select);
                colorButtonSun.setBackgroundResource(R.drawable.circle_select);
                colorButtonWeekday.setBackgroundResource(R.drawable.rounded_rectangle_select);
                colorButtonWeekend.setBackgroundResource(R.drawable.rounded_rectangle_select);
                break;
            case -14309991:
                System.out.println("I AM NOW IN TURQUOISE");
                colorButtonMon.setBackgroundResource(R.drawable.circle_select_turquoise);
                colorButtonTue.setBackgroundResource(R.drawable.circle_select_turquoise);
                colorButtonWed.setBackgroundResource(R.drawable.circle_select_turquoise);
                colorButtonThu.setBackgroundResource(R.drawable.circle_select_turquoise);
                colorButtonFri.setBackgroundResource(R.drawable.circle_select_turquoise);
                colorButtonSat.setBackgroundResource(R.drawable.circle_select_turquoise);
                colorButtonSun.setBackgroundResource(R.drawable.circle_select_turquoise);
                colorButtonWeekday.setBackgroundResource(R.drawable.rounded_rectangle_select_turquoise);
                colorButtonWeekend.setBackgroundResource(R.drawable.rounded_rectangle_select_turquoise);
                break;
            case -8941669:
                colorButtonMon.setBackgroundResource(R.drawable.circle_select_grey);
                colorButtonTue.setBackgroundResource(R.drawable.circle_select_grey);
                colorButtonWed.setBackgroundResource(R.drawable.circle_select_grey);
                colorButtonThu.setBackgroundResource(R.drawable.circle_select_grey);
                colorButtonFri.setBackgroundResource(R.drawable.circle_select_grey);
                colorButtonSat.setBackgroundResource(R.drawable.circle_select_grey);
                colorButtonSun.setBackgroundResource(R.drawable.circle_select_grey);
                colorButtonWeekday.setBackgroundResource(R.drawable.rounded_rectangle_select_grey);
                colorButtonWeekend.setBackgroundResource(R.drawable.rounded_rectangle_select_grey);
                break;
            case -5617989:
                colorButtonMon.setBackgroundResource(R.drawable.circle_select_purple);
                colorButtonTue.setBackgroundResource(R.drawable.circle_select_purple);
                colorButtonWed.setBackgroundResource(R.drawable.circle_select_purple);
                colorButtonThu.setBackgroundResource(R.drawable.circle_select_purple);
                colorButtonFri.setBackgroundResource(R.drawable.circle_select_purple);
                colorButtonSat.setBackgroundResource(R.drawable.circle_select_purple);
                colorButtonSun.setBackgroundResource(R.drawable.circle_select_purple);
                colorButtonWeekday.setBackgroundResource(R.drawable.rounded_rectangle_select_purple);
                colorButtonWeekend.setBackgroundResource(R.drawable.rounded_rectangle_select_purple);
                break;
            case -1360007:
                colorButtonMon.setBackgroundResource(R.drawable.circle_select_pink);
                colorButtonTue.setBackgroundResource(R.drawable.circle_select_pink);
                colorButtonWed.setBackgroundResource(R.drawable.circle_select_pink);
                colorButtonThu.setBackgroundResource(R.drawable.circle_select_pink);
                colorButtonFri.setBackgroundResource(R.drawable.circle_select_pink);
                colorButtonSat.setBackgroundResource(R.drawable.circle_select_pink);
                colorButtonSun.setBackgroundResource(R.drawable.circle_select_pink);
                colorButtonWeekday.setBackgroundResource(R.drawable.rounded_rectangle_select_pink);
                colorButtonWeekend.setBackgroundResource(R.drawable.rounded_rectangle_select_pink);
                break;
            case -23003:
                colorButtonMon.setBackgroundResource(R.drawable.circle_select_orange);
                colorButtonTue.setBackgroundResource(R.drawable.circle_select_orange);
                colorButtonWed.setBackgroundResource(R.drawable.circle_select_orange);
                colorButtonThu.setBackgroundResource(R.drawable.circle_select_orange);
                colorButtonFri.setBackgroundResource(R.drawable.circle_select_orange);
                colorButtonSat.setBackgroundResource(R.drawable.circle_select_orange);
                colorButtonSun.setBackgroundResource(R.drawable.circle_select_orange);
                colorButtonWeekday.setBackgroundResource(R.drawable.rounded_rectangle_select_orange);
                colorButtonWeekend.setBackgroundResource(R.drawable.rounded_rectangle_select_orange);
                break;
        }
    }

    //to get rid of keyboard
    private void hideSoftKeyboard() {
        if (getCurrentFocus() != null && getCurrentFocus() instanceof EditText) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(nameField.getWindowToken(), 0);
        }
    }

    private Calendar generateCalendar(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    public void createDialog(String title, String msg) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    // return 1 if modified
    // return 0 if only color or name are modified
    // return -1 if nothing is modified
    private int isModified(TimerSession newTimer) {
        Log.d("TSDAYS", printArray(bundleDays));
        Log.d("NEWDAYS", printArray(newTimer.getDays()));
        if (!newTimer.getStartTime().equals(ts.getStartTime()) || !newTimer.getEndTime().equals(ts.getEndTime()) ||
                newTimer.getDays() != ts.getDays() || newTimer.getType() != ts.getType()){
            Log.d("isModified", "returning 1");
            return 1;
        } else if (newTimer.getColor() != ts.getColor() || !newTimer.getName().equals(ts.getName())) {
            Log.d("isModified", "returning 0");
            return 0;
        } else {
            Log.d("isModified", "returning -1");
            return -1;
        }
    }

    String printArray(boolean[] array) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < array.length; i++){
            if(array[i]) sb.append("1 ");
            else sb.append("0 ");
        }
        return sb.toString();
    }
}
