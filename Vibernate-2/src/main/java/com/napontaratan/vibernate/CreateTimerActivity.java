package com.napontaratan.vibernate;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.napontaratan.vibernate.model.TimerSession;
import com.napontaratan.vibernate.view.CreateTimerTimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateTimerActivity extends FragmentActivity {

    static final int TIME_DIALOG = 0;
    DialogFragment timePicker;
    List<ToggleButton> days;
    Calendar startTime;
    Calendar endTime;

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.create_timer);
        timePicker = new CreateTimerTimePicker();

        /* toolbar */
        Toolbar toolbar = (Toolbar) findViewById(R.id.create_timer_toolbar);
        toolbar.setTitle("New Timer");
        toolbar.setNavigationIcon(R.drawable.abc_ic_clear_mtrl_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Menu
        toolbar.inflateMenu(R.menu.color_menu);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                /* switch (menuItem.getItemId()){
                    case R.id.action_share:
                        Toast.makeText(ToolbarActivity.this,"Share",Toast.LENGTH_SHORT).show();
                        return true;
                } */

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

        startTime.setText("15:00"); // TODO
        endTime.setText("21:00");   // TODO

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePicker.show(getSupportFragmentManager(), "startTimePicker");
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        days.get(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (days.get(0).isChecked() && days.get(6).isChecked()) weekends_btn.setChecked(true);
                else weekends_btn.setChecked(false);
            }
        });
        days.get(6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (days.get(0).isChecked() && days.get(6).isChecked()) weekends_btn.setChecked(true);
                else weekends_btn.setChecked(false);
            }
        });

        for(int i = 1; i < 6; i++){
            final int finalI = i;
            days.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(days.get(finalI).isChecked()) {
                        for(int j = 1; j < 6; j++){
                            if(!days.get(j).isChecked()) return;
                        }
                        weekdays_btn.setChecked(true);
                    } else {
                        weekdays_btn.setChecked(false);
                    }
                }
            });
        }

        weekdays_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(weekdays_btn.isChecked()) {
                    for(int i = 0; i < 7; i++){
                        if(i != 0 && i != 6) days.get(i).setChecked(true);
                    }
                } else {
                    for(int i = 0; i < 7; i++){
                        if(i != 0 && i != 6) days.get(i).setChecked(false);
                    }
                }
            }
        });

        weekends_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(weekends_btn.isChecked()) {
                    for(int i = 0; i < 7; i++){
                        if(i == 0 || i == 6) days.get(i).setChecked(true);
                    }
                } else {
                    for(int i = 0; i < 7; i++){
                        if(i == 0 || i == 6) days.get(i).setChecked(false);
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
                createTimerSession( nameField.getText().toString(),
                                    type,
                                    Integer.parseInt(start.substring(0,2)),
                                    Integer.parseInt(start.substring((2))),
                                    Integer.parseInt(end.substring(0,2)),
                                    Integer.parseInt(end.substring((2))),
                                    days,
                                    0 //TODO: color
                        );
            }
        });
    }

    private void createTimerSession (String name, TimerSession.TimerSessionType type, int startHour, int startMin, int endHour, int endMin, List<ToggleButton> days, int color) {
        Calendar start = generateCalendar(startHour, startMin);
        Calendar end   = generateCalendar(endHour, endMin);
//        int id = VibrateTimerController.generateNextId(CreateTimerActivity.this);
        Log.d("CreateTimer", "creating timer with the following information: \n" +
                                "Name: " + name + "\n" +
                                "Type: " + type + "\n" +
                                "StartTime" + start.get(Calendar.HOUR_OF_DAY) + ":" + start.get(Calendar.MINUTE) + "\n" +
                                "EndTime" + end.get(Calendar.HOUR_OF_DAY) + ":" + end.get(Calendar.MINUTE) + "\n");
    }


    private Calendar generateCalendar(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

}
