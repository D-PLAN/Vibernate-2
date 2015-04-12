package com.napontaratan.vibernate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import com.napontaratan.vibernate.model.TimerConflictException;
import com.napontaratan.vibernate.model.TimerSession;
import com.napontaratan.vibernate.model.TimerSessionHolder;
import com.napontaratan.vibernate.view.ColorPickerDialog;
import com.napontaratan.vibernate.view.ColorPickerSwatch;
import com.napontaratan.vibernate.view.CreateTimerTimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateTimerActivity extends FragmentActivity {

    CreateTimerTimePicker timePicker;
    List<ToggleButton> days;
    int colorPicked;
    int colorPickedDarker;
    boolean[] bDays = new boolean[7];

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.create_timer);
        timePicker = new CreateTimerTimePicker();

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

                        //To darken the colorPicked
                        float[] hsv = new float[3];
                        int colorPickedDarker = colorPicked;
                        Color.colorToHSV(color, hsv);
                        hsv[2] *= 0.8f; // value component
                        colorPickedDarker = Color.HSVToColor(hsv);
                        System.out.println("colorPickedDarker is " + colorPickedDarker);
                        toolbar.setBackgroundColor(color);


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
        final EditText nameField = (EditText) findViewById(R.id.create_timer_name_field);
        nameField.clearFocus();

        /* vibrate or silent mode */
        final ToggleButton typeVibrate = (ToggleButton) findViewById(R.id.create_timer_type_vibrate);
        final ToggleButton typeSilent  = (ToggleButton) findViewById(R.id.create_timer_type_silent);
        typeVibrate.setChecked(true);
        typeVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(typeSilent.isChecked()) {
                    typeSilent.setChecked(false);
                }
            }
        });

        typeSilent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(typeVibrate.isChecked()) {
                    typeVibrate.setChecked(false);
                }
            }
        });


        /* start time & end time */
        final TextView startTime = (TextView) findViewById(R.id.create_timer_start_time_clock);
        final TextView endTime = (TextView) findViewById(R.id.create_timer_end_time_clock);

        Calendar c = Calendar.getInstance();
        int currentHour = c.get(Calendar.HOUR_OF_DAY);
        int currentMin = c.get(Calendar.MINUTE);
        int nextHour = currentHour + 1;

        String minString = (currentMin < 10)?  "0" + currentMin: Integer.toString(currentMin);
        String currentString = currentHour + ":" + minString;
        String nextString = nextHour + ":" + minString;

        startTime.setText(currentString);
        endTime.setText(nextString);

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] start = startTime.getText().toString().split(":");
                timePicker.setTime(Integer.parseInt(start[0]), Integer.parseInt(start[1]));
                timePicker.show(getSupportFragmentManager(), "startTimePicker");
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        // if sunday is clicked
        days.get(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(days.get(0).isChecked()) {
                    bDays[0] = true;
                    if (days.get(6).isChecked()) weekends_btn.setChecked(true);
                    else weekends_btn.setChecked(false);
                } else {
                    bDays[0] = false;
                }
            }
        });

        // if saturday is clicked
        days.get(6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(days.get(6).isChecked()) {
                    bDays[6] = true;
                    if (days.get(0).isChecked()) weekends_btn.setChecked(true);
                    else weekends_btn.setChecked(false);
                } else {
                    bDays[6] = false;
                }
            }
        });

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
                if(weekdays_btn.isChecked()) {
                    for(int i = 0; i < 7; i++){
                        if(i != 0 && i != 6) {
                            days.get(i).setChecked(true);
                            bDays[i] = true;
                        }
                    }
                } else {
                    for(int i = 0; i < 7; i++){
                        if(i != 0 && i != 6) days.get(i).setChecked(false);
                        bDays[i] = false;
                    }
                }
            }
        });

        weekends_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        ImageButton done = (ImageButton) findViewById(R.id.add_timer_button);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimerSession.TimerSessionType type;

                if(typeVibrate.isChecked()) type = TimerSession.TimerSessionType.VIBRATE;
                else type = TimerSession.TimerSessionType.SILENT;

                String start = startTime.getText().toString().replace(":","");
                String end   = endTime.getText().toString().replace(":","");

                createTimerSession(nameField.getText().toString(),
                        type,
                        Integer.parseInt(start.substring(0, 2)),
                        Integer.parseInt(start.substring((2))),
                        Integer.parseInt(end.substring(0, 2)),
                        Integer.parseInt(end.substring((2))),
                        days,

                       R.color.colorAccent); //TODO: color
                //mToolbarView.setBackgroundColor(colorPicked);








            }
        });
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
        TimerSession newTimer = new TimerSession(name, type, start, end, bDays,color);

        try {
            TimerSessionHolder.getInstance().addTimer(newTimer);
        } catch (TimerConflictException e) {
            createDialog("Timer Conflict", "The time specified is in conflict with another timer. Please try again.");
            return;
        }

        Log.d("CreateTimer", "creating timer with the following information: \n" +
                                "Name: " + name + "\n" +
                                "Type: " + type + "\n" +
                                "StartTime" + start.get(Calendar.HOUR_OF_DAY) + ":" + start.get(Calendar.MINUTE) + "\n" +
                                "EndTime" + end.get(Calendar.HOUR_OF_DAY) + ":" + end.get(Calendar.MINUTE) + "\n");

        finish();
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
}
