package com.cloudappsync.ultra.HomeTabs;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.cloudappsync.ultra.AllSchedules;
import com.cloudappsync.ultra.R;
import com.cloudappsync.ultra.AddSchedule;
import com.cloudappsync.ultra.Utilities.Common;
import com.squareup.picasso.Picasso;

import io.paperdb.Paper;

public class ScheduleTab extends Fragment {

    //widgets
    private CardView addScheduleCard, allSchedulesCard;
    private CardView visitUs;
    private ImageView visitImage;

    public ScheduleTab() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_schedule_tab, container, false);

        //widgets
        addScheduleCard = v.findViewById(R.id.addScheduleCard);
        allSchedulesCard = v.findViewById(R.id.allSchedulesCard);
        visitUs = v.findViewById(R.id.visitUs);
        visitImage = v.findViewById(R.id.visitImage);

        //init
        init();

        return v;
    }

    private void init() {

        //custom image
        if (Paper.book().read(Common.CUSTOM_VISIT_IMAGE) != null && !TextUtils.isEmpty(Paper.book().read(Common.CUSTOM_VISIT_IMAGE))){

            String uri = Paper.book().read(Common.CUSTOM_VISIT_IMAGE);

            //display image
            Picasso.get()
                    .load(uri)
                    .config(Bitmap.Config.RGB_565)
                    .fit().centerCrop()
                    .into(visitImage);

        }

        //visit us
        visitUs.setOnClickListener(v -> {

            //build link
            if (Paper.book().read(Common.CUSTOM_VISIT_URL) != null && !TextUtils.isEmpty(Paper.book().read(Common.CUSTOM_VISIT_URL)))
            {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Paper.book().read(Common.CUSTOM_VISIT_URL)));
                startActivity(browserIntent);

            } else {

                Toast.makeText(getContext(), "Please update custom link", Toast.LENGTH_LONG).show();

            }

        });

        //add schedule
        addScheduleCard.setOnClickListener(v -> {
            Intent addScheduleIntent = new Intent(getContext(), AddSchedule.class);
            startActivity(addScheduleIntent);
            getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_out_left);
        });

        //show all schedules
        allSchedulesCard.setOnClickListener(v -> {
            Intent allScheduleIntent = new Intent(getContext(), AllSchedules.class);
            startActivity(allScheduleIntent);
            getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_out_left);
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        //custom image
        if (Paper.book().read(Common.CUSTOM_VISIT_IMAGE) != null && !TextUtils.isEmpty(Paper.book().read(Common.CUSTOM_VISIT_IMAGE))){

            String uri = Paper.book().read(Common.CUSTOM_VISIT_IMAGE);

            //display image
            Picasso.get()
                    .load(uri)
                    .config(Bitmap.Config.RGB_565)
                    .fit().centerCrop()
                    .into(visitImage);

        }
    }
}