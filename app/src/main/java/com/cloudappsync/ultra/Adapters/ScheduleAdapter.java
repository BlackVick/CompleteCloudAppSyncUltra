package com.cloudappsync.ultra.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudappsync.ultra.AllSchedules;
import com.cloudappsync.ultra.Models.Schedule;
import com.cloudappsync.ultra.R;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    //data
    List<Schedule> scheduleList;
    Context context;
    Activity activity;

    //constructor
    public ScheduleAdapter(Context context, Activity activity, List<Schedule> theList) {
        this.scheduleList = theList;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.schedule_item, parent, false);

        return new ScheduleViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ScheduleViewHolder holder, final int position) {

        Schedule currentSchedule = scheduleList.get(position);

        //bind view
        holder.scheduleUrl.setText(currentSchedule.getRedirect_url());
        holder.startTime.setText("Start Time: " +currentSchedule.getStartTime() + "hrs");
        holder.stopTime.setText("Stop Time: " +currentSchedule.getStopTime() + "hrs");


        //check
        if (currentSchedule.isOneTime()){
            holder.spec.setText("One-Time on " + currentSchedule.getDate());
        } else

        if (currentSchedule.isDaily()){
            holder.spec.setText("Daily");
        } else

        if (currentSchedule.isWeekly()){
            holder.spec.setText(currentSchedule.getDay() + "s Weekly");
        }


        //remove schedule
        holder.deleteSchedule.setOnClickListener(v -> {

            new AlertDialog.Builder(context)
                    .setTitle("Delete Schedule")
                    .setMessage("Are you sure you want to delete this schedule?")
                    .setPositiveButton("YES", (dialog, which) -> {

                        if (context instanceof AllSchedules){
                            ((AllSchedules) context).deleteSchedule(position);
                        }

                    })
                    .setNegativeButton("NO", ((dialog, which) -> {
                        dialog.dismiss();
                    }))
                    .show();

        });

    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public class ScheduleViewHolder extends RecyclerView.ViewHolder {

        //widgets
        TextView scheduleUrl, startTime, stopTime, spec;
        ImageView deleteSchedule;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);

            scheduleUrl = itemView.findViewById(R.id.scheduleUrl);
            startTime = itemView.findViewById(R.id.startTime);
            stopTime = itemView.findViewById(R.id.stopTime);
            spec = itemView.findViewById(R.id.spec);
            deleteSchedule = itemView.findViewById(R.id.deleteSchedule);

        }
        
    }

}
