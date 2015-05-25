package com.napontaratan.vibernate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.daimajia.swipe.SwipeLayout;
import com.napontaratan.vibernate.model.TimerConflictException;
import com.napontaratan.vibernate.model.TimerSession;
import com.napontaratan.vibernate.model.TimerSessionHolder;
import com.napontaratan.vibernate.model.TimerUtils;


/**
 * Created by Aor-Nawattranakul on 15-04-06.
 */
public class vAdapter extends RecyclerView.Adapter<vAdapter.vViewHolder> {
    private LayoutInflater inflater;
    private TimerSessionHolder timerSessionHolder;
    private Context context;
    private int lastPosition = -1;
  
    public vAdapter(Context context, TimerSessionHolder timerSessionHolder) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.timerSessionHolder = timerSessionHolder;
        this.timerSessionHolder.setAdapter(this);
    }

    @Override
    public vViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.v_row, parent, false); // inflat xml into a view
        vViewHolder holder = new vViewHolder(view); // turn a view into viewHolder storing specified values
        return holder;
    }

    @Override
    public void onBindViewHolder(final vViewHolder holder, int position) {
        final TimerSession current = timerSessionHolder.get(position);
        holder.description.setText(current.getName());
        holder.startTime.setText(TimerUtils.getStartTimeInFormat(current, "HH:mm"));
        holder.endTime.setText(TimerUtils.getEndTimeInFormat(current, "HH:mm"));
        holder.activeDays.setText(TimerUtils.getDaysInFormat(current));
        if (current.getType() == TimerSession.TimerSessionType.SILENT) {
            holder.typeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_silent));
        } else {
            holder.typeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_vibrate));
        }

        holder.wrapper.setBackgroundColor(current.getColor());


        if (current.getActive()) {
            holder.colorTab.setBackgroundColor(current.getColor());
        } else {
            holder.colorTab.setBackgroundColor(context.getResources().getColor(R.color.light_grey_text));
        }

        holder.colorTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current.getActive()) {
                    current.setActive(false);
                    timerSessionHolder.setTimerInactive(current);

                } else {
                    holder.colorTab.setBackgroundColor(context.getResources().getColor(R.color.light_grey_text));
                    current.setActive(true);
                    timerSessionHolder.setTimerActive(current);
                }
            }
        });


    }



    @Override
    public int getItemCount() {
        return timerSessionHolder.getSize();
    }


    private void removeItem(int position) {
        timerSessionHolder.removeTimer(position);
        notifyItemRemoved(position);
    }

    public void addItem(TimerSession timer) {
        try {
            timerSessionHolder.addTimer(timer);
            notifyItemInserted(timerSessionHolder.getSize());
        } catch (TimerConflictException e) {
            System.out.println("Timer Conflict please check your time again");
        }
    }

    public class vViewHolder extends RecyclerView.ViewHolder {
        TextView description;
        TextView startTime;
        TextView endTime;
        TextView activeDays;
        ImageView typeIcon;
        View colorTab;
        View box;
        SwipeLayout swipeLayout;
        RelativeLayout wrapper;
        ImageView editIcon;
        ImageView deleteIcon;

        public vViewHolder(final View itemView) {
            super(itemView);
            description = (TextView) itemView.findViewById(R.id.v_description);
            startTime = (TextView) itemView.findViewById(R.id.v_startTime);
            endTime = (TextView) itemView.findViewById(R.id.v_endTime);
            typeIcon = (ImageView) itemView.findViewById(R.id.v_mute_icon);
            activeDays = (TextView) itemView.findViewById(R.id.v_show_activeDays);
            colorTab = itemView.findViewById(R.id.TSisActive);
            box = itemView.findViewById(R.id.click_area);
            wrapper = (RelativeLayout) itemView.findViewById(R.id.row_wrapper);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.listview_swipe_layout);
            editIcon = (ImageView) itemView.findViewById(R.id.listview_edit_icon);
            deleteIcon = (ImageView) itemView.findViewById(R.id.listview_delete_icon);

            swipeLayout.setLeftSwipeEnabled(false);
            swipeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public  void onClick(View v) {
                    if(swipeLayout.getOpenStatus() == SwipeLayout.Status.Close)
                        swipeLayout.open(SwipeLayout.DragEdge.Left);
                    else swipeLayout.close();
                }
            });

            editIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mIntent = new Intent(v.getContext(), CreateTimerActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable("Timer", TimerSessionHolder.getInstance().get(getPosition()));
                    mIntent.putExtras(mBundle);
                    v.getContext().startActivity(mIntent);
                }
            });

            deleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Delete timer")
                            .setMessage("Are you sure you want to delete this timer?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    removeItem(getPosition());
                                }
                            })
                            .setIcon(null)
                            .show();












                }
            });
        }

    }
}
