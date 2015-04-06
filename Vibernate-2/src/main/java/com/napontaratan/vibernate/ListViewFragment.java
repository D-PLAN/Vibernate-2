package com.napontaratan.vibernate;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ListViewFragment extends Fragment {
    private RecyclerView vRecyclerView;
    private RecyclerView.Adapter vAdapter;
    private RecyclerView.LayoutManager vLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment:
        View rootView = inflater.inflate(R.layout.list_fragment, container, false);
        vRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);





        return rootView;
    }
}
