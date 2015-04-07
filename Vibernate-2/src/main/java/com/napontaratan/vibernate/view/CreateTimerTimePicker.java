package com.napontaratan.vibernate.view;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TextView;
import android.widget.TimePicker;
import com.napontaratan.vibernate.R;

import java.util.Calendar;

/**
 * Created by napontaratan on 15-04-06.
 */
public class CreateTimerTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String tag = this.getTag();
        TextView textToUpdate;
        if(tag.equals("startTimePicker")) textToUpdate = (TextView) this.getActivity().findViewById(R.id.create_timer_start_time_clock);
        else textToUpdate = (TextView) this.getActivity().findViewById(R.id.create_timer_end_time_clock);
        StringBuffer time = new StringBuffer();
        if(hourOfDay < 10) time.append("0");
        time.append(hourOfDay);
        time.append(":");
        if(minute < 10) time.append("0");
        time.append(minute);
        textToUpdate.setText(time.toString());
    }
}
