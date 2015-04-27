package com.napontaratan.vibernate;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.napontaratan.vibernate.model.TimerSessionHolder;


public class ListViewFragment extends Fragment {
    private RecyclerView vRecyclerView;
    private vAdapter v_Adapter;
    private TimerSessionHolder timerSessionHolder = TimerSessionHolder.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.list_fragment, container, false);
        vRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycle_view);
        v_Adapter = new vAdapter(getActivity(), timerSessionHolder);
        vRecyclerView.setAdapter(v_Adapter);
        vRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return rootView;
    }



}
