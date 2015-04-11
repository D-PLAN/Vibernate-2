package com.napontaratan.vibernate;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.napontaratan.vibernate.model.TimerSession;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ListViewFragment extends Fragment {
    private RecyclerView vRecyclerView;
    private vAdapter v_Adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment:
        View rootView = inflater.inflate(R.layout.list_fragment, container, false);
        vRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycle_view);
        v_Adapter = new vAdapter(getActivity(), ListViewFragment.getData());
        vRecyclerView.setAdapter(v_Adapter);
        vRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return rootView;
    }


    // Obtain data:
    public static List<TimerSession> getData() {
        List<TimerSession> data = new ArrayList<>();
        String MOCK_TIMER_NAME = "CPSC 101";
        Calendar start = createCalendar(8,0,0,0);
        Calendar end = createCalendar(12, 0, 0, 0);
        TimerSession one = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { true, true, true, true, true, true, true}, Color.rgb(205, 64, 109));
        start = createCalendar(15,0,0,0);
        end = createCalendar(17, 0, 0, 0);
        TimerSession two = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { false, false, false, false, false, true, false},Color.rgb(69, 146, 134));
        start = createCalendar(12,0,0,0);
        end = createCalendar(15, 0, 0, 0);
        TimerSession three = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { false, false, true, false, true, false, false}, Color.rgb(106, 125, 137));
        start = createCalendar(17,0,0,0);
        end = createCalendar(18, 0, 0, 0);
        TimerSession four = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { true, false, false, false, false, false, false},Color.rgb(136, 67, 173));
        start = createCalendar(12,0,0,0);
        end = createCalendar(17, 0, 0, 0);
        TimerSession five = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { true, false, false, false, false, false, false}, Color.rgb(136, 67, 173));

        data.add(one);
        data.add(two);
        data.add(three);
        data.add(four);
        data.add(five);
        data.add(one);
        data.add(two);
        data.add(three);

        System.out.println("size of data is: " + data.size());
        return data;
    }

    //TODO get rid of this, for testing purposes
    private static Calendar createCalendar(int hour, int min, int second, int millis) {
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, millis);
        return cal;
    }
}
