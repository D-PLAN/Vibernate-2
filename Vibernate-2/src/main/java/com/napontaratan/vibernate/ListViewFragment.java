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
        v_Adapter = new vAdapter(getActivity(), getData());
        vRecyclerView.setAdapter(v_Adapter);
        vRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return rootView;
    }



    // Obtain data:
    public static List<vInfo> getData() {
        List<vInfo> data = new ArrayList<vInfo>();
        String descritpions[] = {"d1", "d2", "d3", "d4", "d5"};

        for (int i = 0; i < descritpions.length; i++) {
            vInfo current = new vInfo();
            current.descrition = descritpions[i];
            data.add(current);
        }
        System.out.println("size of data is: " + data.size());
        return data;
    }
}
