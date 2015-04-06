package com.napontaratan.vibernate;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextClock;
import android.widget.TextView;

/**
 * Created by Aor-Nawattranakul on 15-04-06.
 */
public class vAdapter extends RecyclerView.Adapter<vAdapter.vViewHolder> {
    private LayoutInflater inflater;


    public vAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }


    @Override

    public vViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.v_row, parent, false);
        vViewHolder holder = new vViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(vViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class vViewHolder extends RecyclerView.ViewHolder {
        TextView description;
        TextClock startTime;
        TextClock endTime;


        public vViewHolder(View itemView) {
            super(itemView);
            description = (TextView) itemView.findViewById(R.id.v_description);
            startTime = (TextClock) itemView.findViewById(R.id.v_startTime);
            endTime = (TextClock) itemView.findViewById(R.id.v_endTime);

        }
    }
}
