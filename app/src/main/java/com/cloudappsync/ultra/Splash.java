package com.cloudappsync.ultra;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import com.cloudappsync.ultra.Basic.BasicWebActivity;
import com.cloudappsync.ultra.Ultra.SignIn;
import com.cloudappsync.ultra.Ultra.WebActivity;
import com.cloudappsync.ultra.Utilities.Common;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.paperdb.Paper;

public class Splash extends AppCompatActivity {

    //widgets
    private VideoView splashView;
    private ImageView splashImage;

    //static values
    public static final int PERMISSION_REQUEST_CODE = 234;

    //dynamic values
    private boolean isPermitted = false;
    private String splashType = "";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        //attach layout
        setContentView((int) R.layout.activity_splash);

        //safe exit on app
        if (getIntent() != null){
            if( getIntent().getBooleanExtra(Common.EXIT_APP_INTENT, false)){
                finish();
                return;
            }
        }

        //init widget
        splashView = findViewById(R.id.splashView);
        splashImage = findViewById(R.id.splashImage);

        //check permissions
        checkAppPermissions();

        //init ui
        initializeUI();

    }

    private void initializeUI() {

        //check local splash type
        if (Paper.book().read(Common.SPLASH_TYPE) != null){

            splashType = Paper.book().read(Common.SPLASH_TYPE);

        } else {

            splashType = Common.SPLASH_TYPE_IMAGE;

        }

        //click event
        splashView.setOnClickListener(view -> {

            skipToHome();

        });

    }

    private void skipToHome(){
        if (!isFinishing()) {

            //Toast.makeText(this, Paper.book().read(Common.USER_NAME), Toast.LENGTH_SHORT).show();

            if (Paper.book().read(Common.USER_NAME) != null && !TextUtils.isEmpty(Paper.book().read(Common.USER_NAME))) {

                if (Paper.book().read(Common.LOAD_STYLE).equals(Common.LOAD_FROM_ONLINE)){

                    //get saved licence
                    String companyId = Paper.book().read(Common.COMPANY_ID);
                    String licenceKey = Paper.book().read(Common.LICENCE_ID);
                    String theUrl = null;
                    Intent webIntent = null;

                    //server
                    if (Paper.book().read(Common.CUSTOM_ONLINE_LINK_STATUS, Common.CUSTOM_ONLINE_LINK_INACTIVE).equals(Common.CUSTOM_ONLINE_LINK_ACTIVE)){
                        theUrl = Paper.book().read(Common.CUSTOM_ONLINE_LINK);
                    } else {
                        theUrl = Paper.book().read(Common.CURRENT_MASTER_DOMAIN) + "/" + companyId + "/" + licenceKey + "/App/Application/index.html";
                    }

                    //intent
                    if (Paper.book().read(Common.CURRENT_USER_TYPE, Common.USER_TYPE_BASIC).equals(Common.USER_TYPE_ULTRA)) {
                        webIntent = new Intent(this, WebActivity.class);
                    } else {
                        webIntent = new Intent(this, BasicWebActivity.class);
                    }
                    webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_NORMAL_MODE);
                    webIntent.putExtra(Common.WEB_PAGE_TYPE, Common.LOAD_FROM_ONLINE);
                    webIntent.putExtra(Common.WEB_PAGE_INTENT, theUrl);
                    startActivity(webIntent);
                    finish();
                    this.overridePendingTransition(R.anim.slide_left, R.anim.slide_out_left);

                } else {

                    //get saved licence
                    String companyId = Paper.book().read(Common.COMPANY_ID);
                    String licenceKey = Paper.book().read(Common.LICENCE_ID);
                    Intent webIntent = null;

                    File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
                    File destinationFolder = new File(dir, Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey + "/" + Common.USER_WEBPAGE_FOLDER);

                    //intent
                    if (Paper.book().read(Common.CURRENT_USER_TYPE, Common.USER_TYPE_BASIC).equals(Common.USER_TYPE_ULTRA)) {
                        webIntent = new Intent(this, WebActivity.class);
                    } else {
                        webIntent = new Intent(this, BasicWebActivity.class);
                    }
                    webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_NORMAL_MODE);
                    webIntent.putExtra(Common.WEB_PAGE_TYPE, Common.LOAD_FROM_LOCAL);
                    webIntent.putExtra(Common.WEB_PAGE_INTENT, "file:///" + destinationFolder.getAbsolutePath() + "/index.html");
                    startActivity(webIntent);
                    finish();
                    this.overridePendingTransition(R.anim.slide_left, R.anim.slide_out_left);
                }

            } else {

                Intent verificationIntent = new Intent(this, LicenceVerify.class);
                startActivity(verificationIntent);
                finish();
                this.overridePendingTransition(R.anim.slide_left, R.anim.slide_out_left);

            }
        }
    }

    private void checkAppPermissions() {

        //check app mobile sdk version
        if (Build.VERSION.SDK_INT >= 23){

            //check permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                //approve permission
                isPermitted = true;

                //check file status
                checkFile();

            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

            }

        } else {

            //check file status
            checkFile();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //switch value
                isPermitted = true;

                //check file status
                checkFile();

            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Storage permissions needed for this app to function. Please go to setting to give permissions.");
                builder.setCancelable(false);

                builder.setPositiveButton(
                        "OK",
                        (dialog, id) -> {
                            dialog.cancel();

                            //open permissions setting
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                            finish();
                        });

                builder.setNegativeButton(
                        "NO",
                        (dialog, id) -> {
                            dialog.cancel();
                            finish();
                        });

                AlertDialog alert = builder.create();
                alert.show();

            }
        }

    }

    private void checkFile() {

        //build string
        String companyId = Paper.book().read(Common.COMPANY_ID);
        String licenceKey = Paper.book().read(Common.LICENCE_ID);

        File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
        if (!dir.exists()) {
            dir.mkdir();
        }

        //splash folder
        File splashFolder = new File(dir.getAbsolutePath(), Common.SPLASH_FOLDER_NAME);
        if (!splashFolder.exists()) {
            splashFolder.mkdir();
        }

        //branded
        File brandFolder = new File(dir.getAbsolutePath(), Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey + "/" + Common.USER_CONFIG_FOLDER);

        try {
            if (!new File(splashFolder.getAbsolutePath(), Common.SPLASH_VIDEO).exists()) {
                //display widgets
                splashView.setVisibility(View.GONE);
                splashImage.setVisibility(View.VISIBLE);
                Paper.book().write(Common.SPLASH_TYPE, Common.SPLASH_TYPE_IMAGE);

                copyAssetFile(Common.SPLASH_VIDEO, splashFolder.getAbsolutePath() + "/" + Common.SPLASH_VIDEO);

                copyAssetFile(Common.SPLASH_IMAGE_LANDSCAPE, splashFolder.getAbsolutePath() + "/" + Common.SPLASH_IMAGE_LANDSCAPE);

                copyAssetFile(Common.SPLASH_IMAGE_PORTRAIT, splashFolder.getAbsolutePath() + "/" + Common.SPLASH_IMAGE_PORTRAIT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Handler().postDelayed(() -> {

            switch (splashType){

                //check file exists
                case Common.SPLASH_TYPE_VIDEO:
                    //display widgets
                    splashImage.setVisibility(View.GONE);
                    splashView.setVisibility(View.VISIBLE);

                    if (new File(brandFolder.getAbsolutePath(), Common.SPLASH_VIDEO).exists()){

                        //play video
                        splashView.setVideoURI(Uri.parse(brandFolder + "/" + Common.SPLASH_VIDEO));
                        splashView.setOnCompletionListener(mp -> skipToHome());
                        splashView.start();

                    } else {

                        //check file exists
                        if (new File(splashFolder.getAbsolutePath(), Common.SPLASH_VIDEO).exists()) {
                            try {
                                //play video
                                splashView.setVideoURI(Uri.parse(splashFolder + "/" + Common.SPLASH_VIDEO));
                                splashView.setOnCompletionListener(mp -> skipToHome());
                                splashView.start();

                            } catch (Exception e2) {
                                skipToHome();
                            }
                        } else {
                            skipToHome();
                        }

                    }
                    break;

                default:
                    //display widgets
                    splashView.setVisibility(View.GONE);
                    splashImage.setVisibility(View.VISIBLE);

                    if (new File(brandFolder.getAbsolutePath(), Common.SPLASH_IMAGE_LANDSCAPE).exists()){

                        //the orientation
                        if (Splash.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){

                            Uri uri = Uri.fromFile(new File(brandFolder.getAbsolutePath(), Common.SPLASH_IMAGE_LANDSCAPE));

                            //display image
                            Picasso.get()
                                    .load(uri)
                                    .into(splashImage);

                            //timeout
                            new Handler().postDelayed(() -> {
                                skipToHome();
                            }, 2000);

                        } else {

                            Uri uri = Uri.fromFile(new File(brandFolder.getAbsolutePath(), Common.SPLASH_IMAGE_PORTRAIT));

                            //display image
                            Picasso.get()
                                    .load(uri)
                                    .into(splashImage);

                            //timeout
                            new Handler().postDelayed(() -> {
                                skipToHome();
                            }, 2000);

                        }

                    } else {

                        //check file exists
                        if (new File(splashFolder.getAbsolutePath(), Common.SPLASH_IMAGE_LANDSCAPE).exists()) {

                            //the orientation
                            if (Splash.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){

                                Uri uri = Uri.fromFile(new File(splashFolder.getAbsolutePath(), Common.SPLASH_IMAGE_LANDSCAPE));

                                //display image
                                Picasso.get()
                                        .load(uri)
                                        .into(splashImage);

                                //timeout
                                new Handler().postDelayed(() -> {
                                    skipToHome();
                                }, 2000);

                            } else {

                                Uri uri = Uri.fromFile(new File(splashFolder.getAbsolutePath(), Common.SPLASH_IMAGE_PORTRAIT));

                                //display image
                                Picasso.get()
                                        .load(uri)
                                        .into(splashImage);

                                //timeout
                                new Handler().postDelayed(() -> {
                                    skipToHome();
                                }, 2000);

                            }

                        } else {
                            skipToHome();
                        }
                    }
                    break;

            }


        }, 1000);

    }

    public void copyAssetFile(String assetFilePath, String destinationFilePath) throws IOException {

        new Thread(() -> {
            try {
                InputStream in = getApplicationContext().getAssets().open(assetFilePath);
                OutputStream out = new FileOutputStream(destinationFilePath);
                byte[] buf = new byte[1024];
                while (true) {
                    int read = in.read(buf);
                    int len = read;
                    if (read > 0) {
                        out.write(buf, 0, len);
                    } else {
                        in.close();
                        out.close();
                        return;
                    }
                }
            } catch (Exception e){

            }

        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

}