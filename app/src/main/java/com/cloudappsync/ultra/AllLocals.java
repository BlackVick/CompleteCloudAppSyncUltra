package com.cloudappsync.ultra;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.cloudappsync.ultra.Adapters.LocalWebpageAdapter;
import com.cloudappsync.ultra.R;
import com.cloudappsync.ultra.Utilities.Common;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class AllLocals extends AppCompatActivity {

    //widget
    private RecyclerView localPageRecycler;
    private ImageView backButton;
    private ImageView backgroundImage;

    //data
    public static final int PERMISSION_REQUEST_CODE = 234;
    private List<String> fileList = new ArrayList<>();
    private LocalWebpageAdapter adapter;

    //load check
    private boolean isHome = true;
    private String currntDir = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        setContentView(R.layout.activity_all_locals);

        //widgets
        localPageRecycler = findViewById(R.id.localPageRecycler);
        backButton = findViewById(R.id.backButton);
        backgroundImage = findViewById(R.id.backgroundImage);

        //initialize
        initialize();
    }

    private void initialize() {

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

        //back
        backButton.setOnClickListener(v -> {
            if (!isHome){
                fetchLocalDirectories();
            } else {
                finish();
                this.overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);
            }
        });

        //check permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            //fetch files
            fetchLocalDirectories();

        } else {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

        }

    }

    private void fetchLocalDirectories() {

        //tell
        isHome = true;

        //file list clear
        fileList.clear();

        //set current dir
        currntDir = "";

        //values
        String companyId = Paper.book().read(Common.COMPANY_ID);
        String licenceKey = Paper.book().read(Common.LICENCE_ID);

        String path = Environment.getExternalStorageDirectory().toString() + "/" + Common.BASE_FOLDER_NAME + "/" + Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey;
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();

        if (directory.exists()) {

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    Log.d("Files", "FileName:" + files[i].getName());

                    if (files[i].getName().equals(Common.USER_WEBPAGE_FOLDER)) {
                        fileList.add(files[i].getName());
                    }
                }

                loadList(fileList);
            }

        }

    }

    public void fetchChildDirectories(String dir) {

        //file list clear
        fileList.clear();

        //tell
        isHome = false;

        //values
        String companyId = Paper.book().read(Common.COMPANY_ID);
        String licenceKey = Paper.book().read(Common.LICENCE_ID);

        //current dir
        if (!currntDir.equals(dir) && !TextUtils.isEmpty(currntDir)){

            //set current dir
            currntDir = currntDir + "/" + dir;

            String path = Environment.getExternalStorageDirectory().toString() + "/" + Common.BASE_FOLDER_NAME + "/" + Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey + "/" + currntDir;
            Log.d("Files", "Path: " + path);
            File directory = new File(path);
            File[] files = directory.listFiles();

            if (directory.exists()) {

                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        Log.d("Files", "FileName:" + files[i].getName());
                        fileList.add(files[i].getName());
                    }

                    loadList(fileList);
                }

            }

        } else {

            //set current dir
            currntDir = dir;

            String path = Environment.getExternalStorageDirectory().toString() + "/" + Common.BASE_FOLDER_NAME + "/" + Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey + "/" + currntDir;
            Log.d("Files", "Path: " + path);
            File directory = new File(path);
            File[] files = directory.listFiles();

            if (directory.exists()) {

                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        Log.d("Files", "FileName:" + files[i].getName());
                        fileList.add(files[i].getName());
                    }

                    loadList(fileList);
                }

            }

        }


    }

    private void loadList(List<String> fileList) {

        localPageRecycler.setHasFixedSize(true);
        localPageRecycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LocalWebpageAdapter(this, this, fileList);
        localPageRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                fetchLocalDirectories();

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

    @Override
    public void onBackPressed() {

        if (!isHome){
            fetchLocalDirectories();
        } else {
            finish();
            this.overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
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
}