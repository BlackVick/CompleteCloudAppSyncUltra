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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudappsync.ultra.Adapters.ScheduleAdapter;
import com.cloudappsync.ultra.Models.Schedule;
import com.cloudappsync.ultra.R;
import com.cloudappsync.ultra.Utilities.Common;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class AllSchedules extends AppCompatActivity {

    //widget
    private RecyclerView scheduleRecycler;
    private ImageView backButton;
    private ImageView backgroundImage;
    private TextView clearSchedules;

    //data
    public static final int PERMISSION_REQUEST_CODE = 234;
    private List<Schedule> tempList = new ArrayList<>();
    private List<Schedule> scheduleList = new ArrayList<>();
    private ScheduleAdapter adapter;

    //data
    private File newFile;

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
        setContentView(R.layout.activity_all_schedules);

        //widgets
        scheduleRecycler = findViewById(R.id.scheduleRecycler);
        backButton = findViewById(R.id.backButton);
        clearSchedules = findViewById(R.id.clearSchedules);
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
            finish();
            this.overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);
        });

        //check permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            //fetch files
            fetchSchedules();

        } else {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

        }

        //clear schedules
        clearSchedules.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Clear Schedules")
                    .setMessage("Please note that this will only clear the schedules locally set on your device. Are you sure you want to proceed?")
                    .setPositiveButton("YES", (dialog, which) -> {

                        dialog.dismiss();

                        clearSchedules(newFile);

                    })
                    .setNegativeButton("NO", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();



        });

    }

    private void clearSchedules(File newFile) {

        //clear schedule
        newFile.delete();

        //get licence
        String companyId = Paper.book().read(Common.COMPANY_ID);
        String licenceKey = Paper.book().read(Common.LICENCE_ID);

        //check if file exist in directory and create if not
        File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME + "/" + Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey + "/" + Common.USER_SCHEDULE_FOLDER);
        if (dir.exists()) {

            //check if config file exists and create file if not
            newFile = new File(dir, Common.LOCAL_SCHEDULE_FILE);
            if (!newFile.exists()) {

                try (FileWriter writer = new FileWriter(newFile, true)) {

                    StringBuilder sb = new StringBuilder();
                    sb.append("id");
                    sb.append(',');
                    sb.append("redirect_url");
                    sb.append(',');
                    sb.append("isDaily");
                    sb.append(',');
                    sb.append("isWeekly");
                    sb.append(',');
                    sb.append("isOneTime");
                    sb.append(',');
                    sb.append("day");
                    sb.append(',');
                    sb.append("startTime");
                    sb.append(',');
                    sb.append("stopTime");
                    sb.append(',');
                    sb.append("duration");
                    sb.append(',');
                    sb.append("date");
                    sb.append(',');
                    sb.append("priority");
                    sb.append('\n');

                    writer.write(sb.toString());

                } catch (FileNotFoundException e) {
                    Toast.makeText(this, "File Not Available", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            //fetch again
            finish();
            overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);

        } else {

            Toast.makeText(this, "Schedule Folder Missing, Please Contact Support", Toast.LENGTH_LONG).show();

        }

    }

    public void deleteSchedule(int position) {

        //get correct id to delet
        int positionToRemove = position + 1;

        //remove schedule
        try {
            CSVReader reader2 = new CSVReader(new FileReader(newFile));
            List<String[]> allElements = reader2.readAll();
            allElements.remove(positionToRemove);
            FileWriter sw = new FileWriter(newFile);
            CSVWriter writer = new CSVWriter(sw);
            writer.writeAll(allElements);
            writer.close();

            //remove from file
            scheduleList.remove(position);
            adapter.notifyDataSetChanged();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void fetchSchedules() {

        //get licence
        String companyId = Paper.book().read(Common.COMPANY_ID);
        String licenceKey = Paper.book().read(Common.LICENCE_ID);

        //check if file exist in directory and create if not
        File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME + "/" + Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey + "/" + Common.USER_SCHEDULE_FOLDER);
        if (dir.exists()) {

            //get schedule location
            String scheduleLocation = Paper.book().read(Common.SCHEDULE_LOCATION_CHOICE);

            //switch location based on user pref
            if (scheduleLocation.equals(Common.SCHEDULE_LOCATION_ONLINE)){

                //set file to use
                newFile = new File(dir, Common.ONLINE_SCHEDULE_FILE);

            } else {

                //set file to use
                newFile = new File(dir, Common.LOCAL_SCHEDULE_FILE);

            }



            //read csv
            try {
                CSVReader reader = new CSVReader(new FileReader(newFile));
                String[] nextLine;
                int count = 0;

                while ((nextLine = reader.readNext()) != null) {
                    // nextLine[] is an array of values from the line
                    count++;

                    tempList.add(new Schedule(nextLine[0], nextLine[1], Boolean.parseBoolean(nextLine[2].toLowerCase()), Boolean.parseBoolean(nextLine[3].toLowerCase()), Boolean.parseBoolean(nextLine[4].toLowerCase()), nextLine[5], nextLine[6], nextLine[7], nextLine[8], nextLine[9], nextLine[10]));

                }

            } catch (IOException e) {
            }

            //populate actual schedules list
            for (int i = 1; i < tempList.size(); i++){

                scheduleList.add(tempList.get(i));

            }

            loadList(scheduleList);
            
        } else {

            Toast.makeText(this, "Schedule Folder Missing Contact Support", Toast.LENGTH_SHORT).show();
            
        }

    }

    private void loadList(List<Schedule> scheduleList) {

        scheduleRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        scheduleRecycler.setLayoutManager(layoutManager);

        adapter = new ScheduleAdapter(this, this, scheduleList);
        scheduleRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                fetchSchedules();

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
        super.onBackPressed();
        finish();
        this.overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);
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