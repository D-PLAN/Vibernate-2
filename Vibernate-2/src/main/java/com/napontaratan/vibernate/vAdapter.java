package com.napontaratan.vibernate;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.*;
import com.napontaratan.vibernate.model.TimerSession;
import com.napontaratan.vibernate.model.TimerSessionHolder;
import com.napontaratan.vibernate.view.TimerWeekView;

import java.util.Collections;
import java.util.List;

/**
 * Created by Aor-Nawattranakul on 15-04-06.
 */
public class vAdapter extends RecyclerView.Adapter<vAdapter.vViewHolder> {
    private LayoutInflater inflater;
    //List<TimerSession> data = Collections.emptyList(); // this is so that we won't be getting nullpointer exception
    private TimerSessionHolder timerSessionHolder;
    private Context context;

    public vAdapter(Context context, TimerSessionHolder timerSessionHolder) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.timerSessionHolder = timerSessionHolder;
    }

    @Override
    public vViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.v_row, parent, false); // inflat xml into a view
        vViewHolder holder = new vViewHolder(view); // turn a view into viewHolder storing specified values
        return holder;
    }

    @Override
    public void onBindViewHolder(vViewHolder holder, int position) {
        TimerSession current = timerSessionHolder.getTimer(position);
        holder.description.setText(current.getName());
        holder.startTime.setText(TimerWeekView.getStartTimeInFormat(current, "HH:MM"));
        holder.endTime.setText(TimerWeekView.getEndTimeInFormat(current, "HH:MM"));
        TimerSession.TimerSessionType cType = current.getType();
        if (cType == TimerSession.TimerSessionType.SILENT) {

        }

    }

    @Override
    public int getItemCount() {
        return timerSessionHolder.getSize();
    }


    class vViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnTouchListener {
        TextView description;
        TextView startTime;
        TextView endTime;
        TextView activeDays;
        ImageView typeIcon;
        View colorTab;


        public vViewHolder(View itemView) {
            super(itemView);
            description = (TextView) itemView.findViewById(R.id.v_description);
            startTime = (TextView) itemView.findViewById(R.id.v_startTime);
            endTime = (TextView) itemView.findViewById(R.id.v_endTime);
            typeIcon = (ImageView) itemView.findViewById(R.id.v_mute_icon);
            activeDays = (TextView) itemView.findViewById(R.id.v_show_activeDays);
            colorTab = itemView.findViewById(R.id.TSisActive);
            typeIcon.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (v == typeIcon) {
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
