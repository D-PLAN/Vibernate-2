package com.napontaratan.vibernate.view;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import com.napontaratan.vibernate.CreateTimerActivity;

import java.util.Calendar;

/**
 * Created by napontaratan on 15-04-06.
 */
public class CreateTimerTimePicker extends DialogFragment {

    private Activity mActivity;
    private TimePickerDialog.OnTimeSetListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = this.getArguments();
        int hour = args.getInt(CreateTimerActivity.KEY_HOUR);
        int min = args.getInt(CreateTimerActivity.KEY_MINUTE);
        return new TimePickerDialog(mActivity, mListener, hour, min,
                DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        mListener = (TimePickerDialog.OnTimeSetListener) activity;
    }
}
