package com.napontaratan.vibernate;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.napontaratan.vibernate.view.SwipeLayoutInfoView;

public class WeekViewFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.week_fragment, container, false);
        new SwipeLayoutInfoView(rootView);
        return rootView;
    }
}
