package com.napontaratan.vibernate;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by Aor-Nawattranakul on 15-04-06.
 */
public class vAdapter extends RecyclerView.Adapter<vAdapter.vViewHolder> {
    private LayoutInflater inflater;
    List<vInfo> data = Collections.emptyList(); // this is so that we won't be getting nullpointer exception


    public vAdapter(Context context, List<vInfo> data) {
        inflater = LayoutInflater.from(context);
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
        holder.startTime.setFormat12Hour(current.startTime);
        holder.endTime.setFormat12Hour(current.endTime);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    class vViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnTouchListener {
        TextView description;
        TextClock startTime;
        TextClock endTime;
        Button remove;


        public vViewHolder(View itemView) {
            super(itemView);
            description = (TextView) itemView.findViewById(R.id.v_description);
            startTime = (TextClock) itemView.findViewById(R.id.v_startTime);
            endTime = (TextClock) itemView.findViewById(R.id.v_endTime);
            remove = (Button) itemView.findViewById(R.id.remove_b);
            remove.setOnTouchListener(this);

        }

        @Override
        public void onClick(View v) {

        }


        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_UP:
                    if ( v == remove) {
                        delete(getPosition());
                    }
                    break;
            }
            return false;
        }
    }
}
