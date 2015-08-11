package com.napontaratan.vibernate;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ListViewFragment extends Fragment {
    private RecyclerView vRecyclerView;
    private V_Adapter v_Adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.list_fragment, container, false);
        vRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycle_view);
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setChangeDuration(1000);
        vRecyclerView.setItemAnimator(animator);
        
        v_Adapter = new V_Adapter(getActivity());
        vRecyclerView.setAdapter(v_Adapter);
        vRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return rootView;
    }

}
