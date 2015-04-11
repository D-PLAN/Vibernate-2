package com.napontaratan.vibernate;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
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
        v_Adapter.addItem(new vInfo());
        vRecyclerView.setAdapter(v_Adapter);
        vRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return rootView;
    }


    // Obtain data:
    public static List<vInfo> getData() {
        List<vInfo> data = new ArrayList<>();
        String descritpions[] = {"d1", "d2", "d3", "d4", "d5", "d6", "d7"};
        String startTimes[] = {"15:00","16:00","17:00","18:00","19:00", "20:00", "21:00"};
        String endTimes[] = {"15:01","15:02","15:03","15:04","15:05", "15:06", "15:07"};

        for (int i = 0; i < descritpions.length; i++) {
            vInfo current = new vInfo();
            current.descrition = descritpions[i];
            current.startTime = startTimes[i];
            current.endTime = endTimes[i];
            data.add(current);
        }
        System.out.println("size of data is: " + data.size());
        return data;
    }
}
