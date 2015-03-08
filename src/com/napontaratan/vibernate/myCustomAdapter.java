package com.napontaratan.vibernate;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Created by Aor-Nawattranakul on 15-03-07.
 */
public class myCustomAdapter extends ArrayAdapter{
    private Context context;
    private boolean uselist = true;


    public myCustomAdapter(Context context, int hi) {
        super(context, hi);
        this.context = context;
    }

    @Override
    public View getView(int Position, View contentView, ViewGroup parent) {

        return null;
    }


}
