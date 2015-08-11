package com.napontaratan.vibernate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.daimajia.swipe.SwipeLayout;
import com.napontaratan.vibernate.action.TimerSessionAction;
import com.napontaratan.vibernate.dispatcher.Dispatcher;
import com.napontaratan.vibernate.model.TimerSession;
import com.napontaratan.vibernate.model.TimerSessionCommonView;
import com.napontaratan.vibernate.model.TimerSessions;
import com.napontaratan.vibernate.store.TimerSessionStore;
import com.napontaratan.vibernate.view.VibernateView;

import static com.napontaratan.vibernate.R.*;

/**
 * Created by Aor-Nawattranakul on 15-04-06.
 */
public class V_Adapter extends RecyclerView.Adapter<V_Adapter.vViewHolder> implements VibernateView {
    private LayoutInflater inflater;
    private Context context;
    private TimerSessions timerSessions;
    private Dispatcher dispatcher;

    public final static int LIST_VIEW = 2;

    public V_Adapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        TimerSessionStore.getInstance().registerView(LIST_VIEW, this);
        dispatcher = Dispatcher.getInstance();
        timerSessions = TimerSessionStore.getInstance().getTimerSessions();
    }

    @Override
    public vViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(layout.v_row, parent, false); // inflat xml into a view
        vViewHolder holder = new vViewHolder(view); // turn a view into viewHolder storing specified values
        return holder;
    }

    @Override
    public void onBindViewHolder(final vViewHolder holder, int position) {
        final TimerSession current = timerSessions.valueAt(position);
        holder.description.setText(current.getName());
        holder.startTime.setText(TimerSessionCommonView.getTimeFormat(current.getStartTime(), TimerSessionCommonView.TIMER_DATE_FORMAT));
        holder.endTime.setText(TimerSessionCommonView.getTimeFormat(current.getEndTime(), TimerSessionCommonView.TIMER_DATE_FORMAT));
        holder.activeDays.setText(TimerSessionCommonView.getDaysInFormat(current));
        TimerSessionCommonView.setIconBitmaps(holder.sessionTypeIcon, current.getSessionType() == TimerSession.TimerSessionType.SILENT,
                TimerSessionCommonView.silentBitmap, TimerSessionCommonView.vibrateBitmap);
        TimerSessionCommonView.setIconBitmaps(holder.addTypeIcon, current.getAddType() == TimerSession.TimerAddType.ONETIME,
                TimerSessionCommonView.oneTimeBitmap, TimerSessionCommonView.recurringBitmap);
        holder.wrapper.setBackgroundColor(current.getColor());


        if (current.getActive()) {
            holder.colorTab.setBackgroundColor(current.getColor());
        } else {
            holder.colorTab.setBackgroundColor(context.getResources().getColor(color.light_grey_text));
        }

        holder.colorTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(current.getActive()) {
                        dispatcher.dispatchAction(TimerSessionAction.DEACTIVATE_TIMER, current);
                    } else {
                        dispatcher.dispatchAction(TimerSessionAction.ACTIVATE_TIMER, current);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return timerSessions.size();
    }

    protected class vViewHolder extends RecyclerView.ViewHolder {
        TextView description;
        TextView startTime;
        TextView endTime;
        TextView activeDays;
        ImageView sessionTypeIcon;
        ImageView addTypeIcon;
        View colorTab;
        View box;
        SwipeLayout swipeLayout;
        RelativeLayout wrapper;
        ImageView editIcon;
        ImageView deleteIcon;

        public vViewHolder(final View itemView) {
            super(itemView);
            description = (TextView) itemView.findViewById(id.v_description);
            startTime = (TextView) itemView.findViewById(id.v_startTime);
            endTime = (TextView) itemView.findViewById(id.v_endTime);
            sessionTypeIcon = (ImageView) itemView.findViewById(id.v_session_type_icon);
            addTypeIcon = (ImageView) itemView.findViewById(id.v_add_type_icon);
            activeDays = (TextView) itemView.findViewById(id.v_show_activeDays);
            colorTab = itemView.findViewById(id.TSisActive);
            box = itemView.findViewById(id.click_area);
            wrapper = (RelativeLayout) itemView.findViewById(id.row_wrapper);
            swipeLayout = (SwipeLayout) itemView.findViewById(id.listview_swipe_layout);
            editIcon = (ImageView) itemView.findViewById(id.timer_edit_icon);
            deleteIcon = (ImageView) itemView.findViewById(id.timer_delete_icon);

            swipeLayout.setLeftSwipeEnabled(false);
            swipeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public  void onClick(View v) {
                    TimerSessionCommonView.toggleSwipeLayout(swipeLayout, SwipeLayout.DragEdge.Left);
                }
            });

            editIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TimerSessionCommonView.editTimerSession(v.getContext(), timerSessions.valueAt(getPosition()), swipeLayout);
                    TimerSessionCommonView.toggleSwipeLayout(swipeLayout, SwipeLayout.DragEdge.Left);
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
                                    try {
                                        dispatcher.dispatchAction(TimerSessionAction.REMOVE, timerSessions.valueAt(getPosition()));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setIcon(drawable.ic_launcher)
                            .show();
                }
            });
        }
    }

    @Override
    public void storeChanged(TimerSessionStore store) {
        timerSessions = store.getTimerSessions();
        render();
    }

    public void render() {
        this.notifyDataSetChanged();
    }
}
