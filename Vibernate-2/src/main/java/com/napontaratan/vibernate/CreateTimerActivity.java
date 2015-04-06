package com.napontaratan.vibernate;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.napontaratan.vibernate.view.CreateTimerTimePicker;

public class CreateTimerActivity extends FragmentActivity {

    static final int TIME_DIALOG = 0;
    DialogFragment timePicker;

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.create_timer);

        timePicker = new CreateTimerTimePicker();

        Toolbar toolbar = (Toolbar) findViewById(R.id.create_timer_toolbar);
        toolbar.setTitle("New Timer");
        toolbar.setNavigationIcon(R.drawable.abc_ic_clear_mtrl_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        EditText nameField = (EditText) findViewById(R.id.create_timer_name_field);
        nameField.clearFocus();

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

    }
}
