package com.napontaratan.vibernate;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.*;

import java.util.Collections;
import java.util.List;

/**
 * Created by Aor-Nawattranakul on 15-04-06.
 */
public class vAdapter extends RecyclerView.Adapter<vAdapter.vViewHolder> {
    private LayoutInflater inflater;
    List<vInfo> data = Collections.emptyList(); // this is so that we won't be getting nullpointer exception
    private Context context;

    public vAdapter(Context context, List<vInfo> data) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.data = data;
    }

    // remove a row at 'position'
    public void delete(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    // Add 'item' at 'position'
    public void addItem(vInfo item, int position) {
        data.add(item);
        notifyItemInserted(position);
    }

    @Override
    public vViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.v_row, parent, false); // inflat xml into a view
        vViewHolder holder = new vViewHolder(view); // turn a view into viewHolder storing specified values
        return holder;
    }

    @Override
    public void onBindViewHolder(vViewHolder holder, int position) {
        vInfo current = data.get(position);
        holder.description.setText(current.descrition);
        holder.startTime.setText(current.startTime);
        holder.endTime.setText(current.endTime);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    class vViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnTouchListener {
        TextView description;
        TextView startTime;
        TextView endTime;
        TextView activeDays;
        ImageView muteIcon;


        public vViewHolder(View itemView) {
            super(itemView);
            description = (TextView) itemView.findViewById(R.id.v_description);
            startTime = (TextView) itemView.findViewById(R.id.v_startTime);
            endTime = (TextView) itemView.findViewById(R.id.v_endTime);
            muteIcon = (ImageView) itemView.findViewById(R.id.v_mute_icon);
            activeDays = (TextView) itemView.findViewById(R.id.v_show_activeDays);
            muteIcon.setOnClickListener(this);
            
        }

        @Override
        public void onClick(View v) {
            if (v == muteIcon) {
                Toast.makeText(context, "Timer" + getPosition() + "is muted", Toast.LENGTH_SHORT).show();
            }

        }


        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return false;
        }
    }
}
