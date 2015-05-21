package com.napontaratan.vibernate;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.daimajia.swipe.SwipeLayout;

public class CalendarFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.calendar_fragment, container, false);
        SwipeLayout swipeLayout =  (SwipeLayout) rootView.findViewById(R.id.timer_swipe_layout);
        // Swipe from left, since swiping right is for list view
        swipeLayout.setRightSwipeEnabled(false);
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, rootView.findViewById(R.id.bottom_wrapper));
        return rootView;
    }
}
