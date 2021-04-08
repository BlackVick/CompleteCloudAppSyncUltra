package com.cloudappsync.ultra.HomeTabs;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudappsync.ultra.R;
import com.cloudappsync.ultra.Services.BubbleService;
import com.cloudappsync.ultra.Utilities.Common;
import com.squareup.picasso.Picasso;

import io.paperdb.Paper;

public class DeviceTab extends Fragment {

    //widgets
    private CardView deviceCard, wifiCard, volumeCard;
    private CardView visitUs;
    private ImageView visitImage;

    //dialog
    private android.app.AlertDialog volumeDialog;
    private boolean isDialogOpen = false;

    public DeviceTab() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_device, container, false);

        //widgets
        deviceCard = v.findViewById(R.id.deviceCard);
        wifiCard = v.findViewById(R.id.wifiCard);
        volumeCard = v.findViewById(R.id.volumeCard);
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

        //device
        deviceCard.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);


            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(getContext())) {
                getActivity().startService(new Intent(getContext(), BubbleService.class).putExtra("activity_background", true));

            }
        });

        //wifi
        wifiCard.setOnClickListener(v -> {
            Intent intent=new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(getContext())) {
                getActivity().startService(new Intent(getContext(), BubbleService.class).putExtra("activity_background", true));
            }
        });

        //volume
        volumeCard.setOnClickListener(v -> {

            //show volume control
            showVolumeDialog();
            /*Intent intent=new Intent(Settings.ACTION_SOUND_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);*/
        });

    }

    private void showVolumeDialog() {

        //case
        isDialogOpen = true;

        //create dialog
        volumeDialog = new android.app.AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.volume_dialog, null);

        //widgets
        final TextView decreaseVol = viewOptions.findViewById(R.id.decreaseVol);
        final TextView increaseVol = viewOptions.findViewById(R.id.increaseVol);

        //dialog props
        volumeDialog.setView(viewOptions);
        volumeDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        volumeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //lock dialog
        volumeDialog.setCancelable(true);
        volumeDialog.setCanceledOnTouchOutside(true);

        //listeners
        volumeDialog.setOnCancelListener(dialog -> isDialogOpen = false);
        volumeDialog.setOnDismissListener(dialog -> isDialogOpen = false);

        //start count down
        new Handler().postDelayed(() -> {
            if (isDialogOpen){
                volumeDialog.dismiss();
            }
        }, 10000);

        //init audio manager
        AudioManager audioManager = (AudioManager) getContext().getSystemService(getContext().AUDIO_SERVICE);

        //increase volume
        increaseVol.setOnClickListener(v -> {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
        });

        //decrease volume
        decreaseVol.setOnClickListener(v -> {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
        });

        //show dialog
        volumeDialog.show();

    }

    @Override
    public void onStop() {
        super.onStop();
        if (isDialogOpen)
            volumeDialog.dismiss();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isDialogOpen)
            volumeDialog.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().stopService(new Intent(getContext(), BubbleService.class));

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