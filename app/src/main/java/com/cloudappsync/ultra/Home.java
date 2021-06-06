package com.cloudappsync.ultra;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.cloudappsync.ultra.Adapters.TabsPager;
import com.cloudappsync.ultra.Basic.BasicWebActivity;
import com.cloudappsync.ultra.R;
import com.cloudappsync.ultra.Ultra.WebActivity;
import com.cloudappsync.ultra.Utilities.Common;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import java.io.File;

import io.paperdb.Paper;

public class Home extends AppCompatActivity {

    //tabs
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TabsPager tabsPager;
    private ImageView backgroundImage;
    private ImageView backButton;

    //values
    public static int DRAW_OVER_OTHER_APP_PERMISSION = 2011;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        doGraphicsStuff();

        //attach layout
        setContentView(R.layout.activity_home);

        //tabs
        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewPager);
        backgroundImage = findViewById(R.id.backgroundImage);
        backButton = findViewById(R.id.backButton);
        tabsPager = new TabsPager(getSupportFragmentManager());

        //init ui
        initUI();

    }

    private void initUI() {

        //back
        backButton.setOnClickListener(v -> {

            if (Paper.book().read(Common.LOAD_STYLE) != null && Paper.book().read(Common.LOAD_STYLE).equals(Common.LOAD_FROM_ONLINE)){

                //build string
                String companyId = Paper.book().read(Common.COMPANY_ID);
                String licenceKey = Paper.book().read(Common.LICENCE_ID);

                //go to licence page
                String theUrl = null;

                if (Paper.book().read(Common.CUSTOM_ONLINE_LINK_STATUS, Common.CUSTOM_ONLINE_LINK_INACTIVE).equals(Common.CUSTOM_ONLINE_LINK_ACTIVE)){
                    theUrl = Paper.book().read(Common.CUSTOM_ONLINE_LINK);
                } else {
                    theUrl = Paper.book().read(Common.CURRENT_MASTER_DOMAIN) + "/" + companyId + "/" + licenceKey + "/App/Application/index.html";
                }

                //null
                Intent webIntent = null;

                //intent
                if (Paper.book().read(Common.CURRENT_USER_TYPE, Common.USER_TYPE_BASIC).equals(Common.USER_TYPE_ULTRA)) {
                    webIntent = new Intent(this, WebActivity.class);
                } else {
                    webIntent = new Intent(this, BasicWebActivity.class);
                }
                webIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_NO_SYNC_MODE);
                webIntent.putExtra(Common.WEB_PAGE_TYPE, Common.LOAD_FROM_ONLINE);
                webIntent.putExtra(Common.WEB_PAGE_INTENT, theUrl);
                startActivity(webIntent);
                this.overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);

            } else {

                //build string
                String companyId = Paper.book().read(Common.COMPANY_ID);
                String licenceKey = Paper.book().read(Common.LICENCE_ID);

                //go to licence page
                File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
                File liDir = new File(dir.getAbsolutePath(), Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey + "/" + Common.USER_WEBPAGE_FOLDER);

                //null
                Intent webIntent = null;

                //intent
                if (Paper.book().read(Common.CURRENT_USER_TYPE, Common.USER_TYPE_BASIC).equals(Common.USER_TYPE_ULTRA)) {
                    webIntent = new Intent(this, WebActivity.class);
                } else {
                    webIntent = new Intent(this, BasicWebActivity.class);
                }
                webIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_NO_SYNC_MODE);
                webIntent.putExtra(Common.WEB_PAGE_TYPE, Common.LOAD_FROM_LOCAL);
                webIntent.putExtra(Common.WEB_PAGE_INTENT, "file:///" + liDir.getAbsolutePath() + "/index.html");
                startActivity(webIntent);
                this.overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);

            }

        });

        //init for tabs
        initTabs();

    }

    private void initTabs() {

        //check overlay background
        checkOverlayBackground();

        //background check
        if (!TextUtils.isEmpty(Paper.book().read(Common.APP_BACKGROUND))){

            //get uri
            String uri = Paper.book().read(Common.APP_BACKGROUND);

            //display image
            Picasso.get()
                    .load(uri)
                    .config(Bitmap.Config.RGB_565)
                    .fit().centerCrop()
                    .into(backgroundImage);

        }

        //set adapter
        viewPager.setAdapter(tabsPager);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void checkOverlayBackground() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DRAW_OVER_OTHER_APP_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "On Screen Shortcut back to app wont work", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (Paper.book().read(Common.LOAD_STYLE) != null && Paper.book().read(Common.LOAD_STYLE).equals(Common.LOAD_FROM_ONLINE)){

            //build string
            String companyId = Paper.book().read(Common.COMPANY_ID);
            String licenceKey = Paper.book().read(Common.LICENCE_ID);

            //go to licence page
            String theUrl = null;

            if (Paper.book().read(Common.CUSTOM_ONLINE_LINK_STATUS, Common.CUSTOM_ONLINE_LINK_INACTIVE).equals(Common.CUSTOM_ONLINE_LINK_ACTIVE)){
                theUrl = Paper.book().read(Common.CUSTOM_ONLINE_LINK);
            } else {
                theUrl = Paper.book().read(Common.CURRENT_MASTER_DOMAIN) + "/" + companyId + "/" + licenceKey + "/App/Application/index.html";
            }

            //null
            Intent webIntent = null;

            //intent
            if (Paper.book().read(Common.CURRENT_USER_TYPE, Common.USER_TYPE_BASIC).equals(Common.USER_TYPE_ULTRA)) {
                webIntent = new Intent(this, WebActivity.class);
            } else {
                webIntent = new Intent(this, BasicWebActivity.class);
            }
            webIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_NO_SYNC_MODE);
            webIntent.putExtra(Common.WEB_PAGE_TYPE, Common.LOAD_FROM_ONLINE);
            webIntent.putExtra(Common.WEB_PAGE_INTENT, theUrl);
            startActivity(webIntent);
            this.overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);

        } else {

            //build string
            String companyId = Paper.book().read(Common.COMPANY_ID);
            String licenceKey = Paper.book().read(Common.LICENCE_ID);

            //go to licence page
            File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
            File liDir = new File(dir.getAbsolutePath(), Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey + "/" + Common.USER_WEBPAGE_FOLDER);

            //null
            Intent webIntent = null;

            //intent
            if (Paper.book().read(Common.CURRENT_USER_TYPE, Common.USER_TYPE_BASIC).equals(Common.USER_TYPE_ULTRA)) {
                webIntent = new Intent(this, WebActivity.class);
            } else {
                webIntent = new Intent(this, BasicWebActivity.class);
            }
            webIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_NO_SYNC_MODE);
            webIntent.putExtra(Common.WEB_PAGE_TYPE, Common.LOAD_FROM_LOCAL);
            webIntent.putExtra(Common.WEB_PAGE_INTENT, "file:///" + liDir.getAbsolutePath() + "/index.html");
            startActivity(webIntent);
            this.overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);

        }

    }

    private void doGraphicsStuff(){

        //check mode
        if (Paper.book().read(Common.CURRENT_USER_TYPE, Common.USER_TYPE_BASIC).equals(Common.USER_TYPE_ULTRA)) {
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES || Paper.book().read(Common.VISUAL_STYLE, Common.DAY_MODE).equals(Common.NIGHT_MODE)) {
                setTheme(R.style.UltraDarkTheme);
            } else {
                setTheme(R.style.UltraLightTheme);
            }
        } else {
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES || Paper.book().read(Common.VISUAL_STYLE, Common.DAY_MODE).equals(Common.NIGHT_MODE)) {
                setTheme(R.style.BasicDarkTheme);
            } else {
                setTheme(R.style.BasicLightTheme);
            }
        }

        if (!TextUtils.isEmpty(Paper.book().read(Common.APP_BACKGROUND))){

            //get uri
            String uri = Paper.book().read(Common.APP_BACKGROUND);

            //display image
            Picasso.get()
                    .load(uri)
                    .config(Bitmap.Config.RGB_565)
                    .fit().centerCrop()
                    .into(backgroundImage);

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        doGraphicsStuff();
    }

    @Override
    protected void onResume() {
        super.onResume();
        doGraphicsStuff();
    }
}