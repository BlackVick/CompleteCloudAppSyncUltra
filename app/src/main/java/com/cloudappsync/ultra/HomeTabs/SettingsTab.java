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

import com.cloudappsync.ultra.Basic.BasicSetting;
import com.cloudappsync.ultra.LicenceVerify;
import com.cloudappsync.ultra.R;
import com.cloudappsync.ultra.Ultra.Setting;
import com.cloudappsync.ultra.Ultra.SignIn;
import com.cloudappsync.ultra.Utilities.Common;
import com.cloudappsync.ultra.Utilities.Database;
import com.squareup.picasso.Picasso;

import io.paperdb.Paper;

public class SettingsTab extends Fragment {

    //widgets
    private CardView settingsCard, logoutCard;
    private CardView visitUs;
    private ImageView visitImage;

    public SettingsTab() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings_tab, container, false);

        //widgets
        settingsCard = v.findViewById(R.id.settingsCard);
        logoutCard = v.findViewById(R.id.logoutCard);
        visitUs = v.findViewById(R.id.visitUs);
        visitImage = v.findViewById(R.id.visitImage);

        //init
        init();

        return v;
    }

    private void init() {

        //custom image
        if (Paper.book().read(Common.CUSTOM_VISIT_IMAGE) != null && !TextUtils.isEmpty(Paper.book().read(Common.CUSTOM_VISIT_IMAGE))) {

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
            if (Paper.book().read(Common.CUSTOM_VISIT_URL) != null && !TextUtils.isEmpty(Paper.book().read(Common.CUSTOM_VISIT_URL))) {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Paper.book().read(Common.CUSTOM_VISIT_URL)));
                startActivity(browserIntent);

            } else {

                Toast.makeText(getContext(), "Please update custom link", Toast.LENGTH_LONG).show();

            }

        });

        //settings
        settingsCard.setOnClickListener(v -> {
            //null
            Intent settingIntent = null;

            //intent
            if (Paper.book().read(Common.CURRENT_USER_TYPE, Common.USER_TYPE_BASIC).equals(Common.USER_TYPE_ULTRA)) {
                settingIntent = new Intent(getContext(), Setting.class);
            } else {
                settingIntent = new Intent(getContext(), BasicSetting.class);
            }
            startActivity(settingIntent);
            getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_out_left);
        });

        //logout
        logoutCard.setOnClickListener(v -> {
            logoutUser();
        });

    }

    private void logoutUser() {

        //clean database
        new Database(getContext()).cleanFileHistory(Paper.book().read(Common.CURRENT_DB_LICENCE));


        //clear local database
        Paper.book().delete(Common.USER_NAME);
        Paper.book().delete(Common.COMPANY_ID);
        Paper.book().delete(Common.LICENCE_ID);
        Paper.book().delete(Common.FTP_HOST);
        Paper.book().delete(Common.FTP_USERNAME);
        Paper.book().delete(Common.FTP_PASSWORD);
        Paper.book().delete(Common.PASSWORD_STATUS);
        Paper.book().delete(Common.PASSWORD_REQUEST_TYPE);
        Paper.book().delete(Common.CUSTOM_APP_BACKGROUND);
        Paper.book().delete(Common.CURRENT_SYNC_TYPE);
        Paper.book().delete(Common.CURRENT_MASTER_DOMAIN);
        Paper.book().delete(Common.MASTER_DOMAINS);
        Paper.book().delete(Common.CURRENT_TEST_MODE);
        Paper.book().delete(Common.IS_IN_TEST_MODE);
        Paper.book().delete(Common.CUSTOM_APP_BACKGROUND);
        Paper.book().delete(Common.CURRENT_DB_LICENCE);
        Paper.book().delete(Common.PAGE_REFRESH_STATUS);
        Paper.book().delete(Common.CUSTOM_ONLINE_LINK_STATUS);
        Paper.book().delete(Common.CUSTOM_ONLINE_LINK);
        Paper.book().delete(Common.CURRENT_USER_AGENT);
        Paper.book().delete(Common.CURRENT_USER_TYPE);

        //log out
        Intent exitIntent = new Intent(getContext(), LicenceVerify.class);
        exitIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        exitIntent.putExtra(Common.EXIT_APP_INTENT, true);
        startActivity(exitIntent);
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);

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