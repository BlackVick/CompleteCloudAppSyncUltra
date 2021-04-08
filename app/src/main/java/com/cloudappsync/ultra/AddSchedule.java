package com.cloudappsync.ultra;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.cloudappsync.ultra.Models.Schedule;
import com.cloudappsync.ultra.Utilities.Common;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.paperdb.Paper;

public class AddSchedule extends AppCompatActivity {

    //tab
    private ImageView backButton;
    private ImageView backgroundImage;
    private EditText redirectUrl;
    private TextView startTime, stopTime, typeIndicator, locationIndicator;
    private SwitchCompat typeSwitch, locationSwitch;
    private LinearLayout dateSelectionLayout;
    private TextView startDate, endDate;
    private TextView scopeIndicator;
    private SwitchCompat scopeSwitch;
    private RelativeLayout addScheduleBtn;
    private RelativeLayout sunButton, monButton, tueButton, wedButton, thuButton, friButton, satButton;
    private View sunIndicator, monIndicator, tueIndicator, wedIndicator, thuIndicator, friIndicator, satIndicator;

    //values
    private String selectedType = "";
    private String selectedScope = "";
    private boolean isSundayChecked = false;
    private boolean isMondayChecked = false;
    private boolean isTuesdayChecked = false;
    private boolean isWednesdayChecked = false;
    private boolean isThursdayChecked = false;
    private boolean isFridayChecked = false;
    private boolean isSaturdayChecked = false;
    private String selectedStartTime = "";
    private String selectedStopTime = "";
    private String selectedStartDate = "";
    private String selectedEndDate = "";

    //time picker dialog
    private int hour = 0;
    private int min = 0;

    //calendar
    private Calendar myCalendar = Calendar.getInstance();

    //folder
    private File newFile;

    //loading
    private TextView addText;
    private ProgressBar addProgress;

    //licence values
    private String companyId;
    private String licenceKey;



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
        setContentView(R.layout.activity_add_schedule);

        //values
        companyId = Paper.book().read(Common.COMPANY_ID);
        licenceKey = Paper.book().read(Common.LICENCE_ID);

        //widgets
        backButton = findViewById(R.id.backButton);
        redirectUrl = findViewById(R.id.redirectUrl);
        startTime = findViewById(R.id.startTime);
        stopTime = findViewById(R.id.stopTime);
        typeIndicator = findViewById(R.id.typeIndicator);
        typeSwitch = findViewById(R.id.typeSwitch);
        dateSelectionLayout = findViewById(R.id.dateSelectionLayout);
        startDate = findViewById(R.id.startDate);
        endDate = findViewById(R.id.endDate);
        scopeIndicator = findViewById(R.id.scopeIndicator);
        scopeSwitch = findViewById(R.id.scopeSwitch);
        addScheduleBtn = findViewById(R.id.addScheduleBtn);
        sunButton = findViewById(R.id.sunButton);
        monButton = findViewById(R.id.monButton);
        tueButton = findViewById(R.id.tueButton);
        wedButton = findViewById(R.id.wedButton);
        thuButton = findViewById(R.id.thuButton);
        friButton = findViewById(R.id.friButton);
        satButton = findViewById(R.id.satButton);
        sunIndicator = findViewById(R.id.sunIndicator);
        monIndicator = findViewById(R.id.monIndicator);
        tueIndicator = findViewById(R.id.tueIndicator);
        wedIndicator = findViewById(R.id.wedIndicator);
        thuIndicator = findViewById(R.id.thuIndicator);
        friIndicator = findViewById(R.id.friIndicator);
        satIndicator = findViewById(R.id.satIndicator);
        addText = findViewById(R.id.addText);
        addProgress = findViewById(R.id.addProgress);
        backgroundImage = findViewById(R.id.backgroundImage);
        locationIndicator = findViewById(R.id.locationIndicator);
        locationSwitch = findViewById(R.id.locationSwitch);

        //initialize
        initialize();
    }

    @SuppressLint("ResourceType")
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

        //check file
        checkDirectory();

        //back
        backButton.setOnClickListener(v -> {
            finish();
            this.overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);
        });

        //set defaults
        setDefaults();

        //initialize day selects
        initializeDays();

        //type
        typeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){

                selectedType = Common.SCHEDULE_TYPE_SPECIFIC;
                typeIndicator.setText(Common.SCHEDULE_TYPE_SPECIFIC);
                dateSelectionLayout.setVisibility(View.VISIBLE);

            } else {

                selectedType = Common.SCHEDULE_TYPE_NORMAL;
                typeIndicator.setText(Common.SCHEDULE_TYPE_NORMAL);
                dateSelectionLayout.setVisibility(View.GONE);

            }
        });

        //scope
        scopeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                selectedScope = Common.SCHEDULE_SCOPE_DAILY;
                scopeIndicator.setText(Common.SCHEDULE_SCOPE_DAILY);

                //set days
                checkEveryDay(false, true);
            } else {
                selectedScope = Common.SCHEDULE_SCOPE_WEEKLY;
                scopeIndicator.setText(Common.SCHEDULE_SCOPE_WEEKLY);

                //set days
                checkEveryDay(true, false);
            }
        });

        //start time
        startTime.setOnClickListener(v -> {
            showStartTimeDialog();
        });

        //stop time
        stopTime.setOnClickListener(v -> {
            showStopTimeDialog();
        });

        //start date
        startDate.setOnClickListener(v -> {
            showStartDateDialog();
        });

        //end date
        endDate.setOnClickListener(v -> {
            showEndDateDialog();
        });

        //location
        locationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked){

                locationIndicator.setText("Offline");
                redirectUrl.setText("/");
                redirectUrl.setSelection(redirectUrl.getText().toString().length());

            } else {

                locationIndicator.setText("Online");
                redirectUrl.setText("https://");
                redirectUrl.setSelection(redirectUrl.getText().toString().length());


            }

        });

        //add
        addScheduleBtn.setOnClickListener(v -> validateParams());

    }

    private void showLocalList() {

        //create dialog
        android.app.AlertDialog localFileDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.local_file_dialog, null);

        //widgets
        final Spinner localSpinner = viewOptions.findViewById(R.id.localSpinner);

        //dialog props
        localFileDialog.setView(viewOptions);
        localFileDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        localFileDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //list
        final List<String> localFileList = new ArrayList<>();
        localFileList.add(0, "Local Folders");

        //adapter
        final ArrayAdapter<String> dataAdapterLocalFolder;
        dataAdapterLocalFolder = new ArrayAdapter(this, R.layout.custom_spinner_item, localFileList);
        dataAdapterLocalFolder.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //populate spinner
        String companyId = Paper.book().read(Common.COMPANY_ID);
        String licenceKey = Paper.book().read(Common.LICENCE_ID);

        String path = Environment.getExternalStorageDirectory().toString() + "/" + Common.BASE_FOLDER_NAME + "/" + Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey + "/Application/LocalPages";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();

        if (directory.exists()) {

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    Log.d("Files", "FileName:" + files[i].getName());

                    localFileList.add(files[i].getName());
                }

            }

        }

        //set adapter
        localSpinner.setAdapter(dataAdapterLocalFolder);
        dataAdapterLocalFolder.notifyDataSetChanged();

        //selector
        localSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).toString().equals("Local Folders")) {

                    String folder = parent.getItemAtPosition(position).toString();
                    File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME + "/" + Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey);

                    if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
                        redirectUrl.setTextColor(getResources().getColor(R.color.white));
                    } else {
                        redirectUrl.setTextColor(getResources().getColor(R.color.black));
                    }
                    redirectUrl.setText("file:///" + dir.getAbsolutePath() + "/Application/LocalPages/" + folder + "/index.html");

                    //dismiss dialog
                    localFileDialog.dismiss();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //show dialog
        localFileDialog.show();

    }

    private void checkDirectory() {

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

        } else {

            Toast.makeText(this, "Schedule Folder Missing, Please Contact Support", Toast.LENGTH_LONG).show();

        }



    }

    private void initializeDays() {

        //sunday
        sunButton.setOnClickListener(v -> {

            //ui change
            if (isSundayChecked) {
                sunIndicator.setBackgroundResource(R.drawable.unselected_day);
                isSundayChecked = false;
            } else {
                sunIndicator.setBackgroundResource(R.drawable.selected_day);
                isSundayChecked = true;
            }
        });

        //monday
        monButton.setOnClickListener(v -> {

            //ui change
            if (isMondayChecked) {
                monIndicator.setBackgroundResource(R.drawable.unselected_day);
                isMondayChecked = false;
            } else {
                monIndicator.setBackgroundResource(R.drawable.selected_day);
                isMondayChecked = true;
            }
        });

        //tuesday
        tueButton.setOnClickListener(v -> {

            //ui change
            if (isTuesdayChecked) {
                tueIndicator.setBackgroundResource(R.drawable.unselected_day);
                isTuesdayChecked = false;
            } else {
                tueIndicator.setBackgroundResource(R.drawable.selected_day);
                isTuesdayChecked = true;
            }
        });

        //wednesday
        wedButton.setOnClickListener(v -> {

            //ui change
            if (isWednesdayChecked) {
                wedIndicator.setBackgroundResource(R.drawable.unselected_day);
                isWednesdayChecked = false;
            } else {
                wedIndicator.setBackgroundResource(R.drawable.selected_day);
                isWednesdayChecked = true;
            }
        });

        //thursday
        thuButton.setOnClickListener(v -> {

            //ui change
            if (isThursdayChecked) {
                thuIndicator.setBackgroundResource(R.drawable.unselected_day);
                isThursdayChecked = false;
            } else {
                thuIndicator.setBackgroundResource(R.drawable.selected_day);
                isThursdayChecked = true;
            }
        });

        //friday
        friButton.setOnClickListener(v -> {

            //ui change
            if (isFridayChecked) {
                friIndicator.setBackgroundResource(R.drawable.unselected_day);
                isFridayChecked = false;
            } else {
                friIndicator.setBackgroundResource(R.drawable.selected_day);
                isFridayChecked = true;
            }
        });

        //saturday
        satButton.setOnClickListener(v -> {

            //ui change
            if (isSaturdayChecked) {
                satIndicator.setBackgroundResource(R.drawable.unselected_day);
                isSaturdayChecked = false;
            } else {
                satIndicator.setBackgroundResource(R.drawable.selected_day);
                isSaturdayChecked = true;
            }
        });

    }

    private void setDefaults() {

        //location
        locationIndicator.setText("Online");
        locationSwitch.setChecked(false);
        redirectUrl.setText("https://");
        redirectUrl.setSelection(redirectUrl.getText().toString().length() - 1);


        //type
        selectedType = Common.SCHEDULE_TYPE_NORMAL;
        typeSwitch.setChecked(false);
        typeIndicator.setText(Common.SCHEDULE_TYPE_NORMAL);
        dateSelectionLayout.setVisibility(View.GONE);

        //scope
        selectedScope = Common.SCHEDULE_SCOPE_WEEKLY;
        scopeSwitch.setChecked(false);
        scopeIndicator.setText(Common.SCHEDULE_SCOPE_WEEKLY);

        //set days
        checkEveryDay(true, false);

    }

    private void checkEveryDay(boolean isBtnActive, boolean isSelected) {

        //disable buttons
        sunButton.setEnabled(isBtnActive);
        monButton.setEnabled(isBtnActive);
        tueButton.setEnabled(isBtnActive);
        wedButton.setEnabled(isBtnActive);
        thuButton.setEnabled(isBtnActive);
        friButton.setEnabled(isBtnActive);
        satButton.setEnabled(isBtnActive);

        //set select
        if (!isBtnActive){

            sunIndicator.setBackgroundResource(R.drawable.selected_day);
            monIndicator.setBackgroundResource(R.drawable.selected_day);
            tueIndicator.setBackgroundResource(R.drawable.selected_day);
            wedIndicator.setBackgroundResource(R.drawable.selected_day);
            thuIndicator.setBackgroundResource(R.drawable.selected_day);
            friIndicator.setBackgroundResource(R.drawable.selected_day);
            satIndicator.setBackgroundResource(R.drawable.selected_day);

        } else {

            sunIndicator.setBackgroundResource(R.drawable.unselected_day);
            monIndicator.setBackgroundResource(R.drawable.unselected_day);
            tueIndicator.setBackgroundResource(R.drawable.unselected_day);
            wedIndicator.setBackgroundResource(R.drawable.unselected_day);
            thuIndicator.setBackgroundResource(R.drawable.unselected_day);
            friIndicator.setBackgroundResource(R.drawable.unselected_day);
            satIndicator.setBackgroundResource(R.drawable.unselected_day);

        }

        //register check
        isSundayChecked = isSelected;
        isMondayChecked = isSelected;
        isTuesdayChecked = isSelected;
        isWednesdayChecked = isSelected;
        isThursdayChecked = isSelected;
        isFridayChecked = isSelected;
        isSaturdayChecked = isSelected;

    }

    private void showStartTimeDialog() {

        //create dialog
        android.app.AlertDialog startDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.time_picker_dialog, null);

        //widgets
        final TimePicker timePicker = viewOptions.findViewById(R.id.timePicker);
        final Button cancelBtn = viewOptions.findViewById(R.id.cancelBtn);
        final Button setBtn = viewOptions.findViewById(R.id.setBtn);

        //dialog props
        startDialog.setView(viewOptions);
        startDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        startDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //initialize time picker
        timePicker.setIs24HourView(false);
        Calendar calendar = Calendar.getInstance();
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {

            //get time
            hour = hourOfDay;
            min = minute;

        });

        //cancel
        cancelBtn.setOnClickListener(v -> {

            //reset
            hour = 0;
            min = 0;

            //dismiss
            startDialog.dismiss();

        });

        //grant access
        setBtn.setOnClickListener(v -> {

            //set time
            selectedStartTime = String.format("%02d:%02d", hour, min);
            startTime.setText(String.format("%02d:%02d", hour, min));

            //reset
            hour = 0;
            min = 0;

            //close dialog
            startDialog.dismiss();
        });

        //show dialog
        startDialog.show();

    }

    private void showStopTimeDialog() {

        //create dialog
        android.app.AlertDialog stopDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.time_picker_dialog, null);

        //widgets
        final TimePicker timePicker = viewOptions.findViewById(R.id.timePicker);
        final Button cancelBtn = viewOptions.findViewById(R.id.cancelBtn);
        final Button setBtn = viewOptions.findViewById(R.id.setBtn);

        //dialog props
        stopDialog.setView(viewOptions);
        stopDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        stopDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //initialize time picker
        timePicker.setIs24HourView(false);
        Calendar calendar = Calendar.getInstance();
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {

            //get time
            hour = hourOfDay;
            min = minute;

        });

        //cancel
        cancelBtn.setOnClickListener(v -> {

            //reset
            hour = 0;
            min = 0;

            //dismiss
            stopDialog.dismiss();

        });

        //grant access
        setBtn.setOnClickListener(v -> {

            //set time
            selectedStopTime = String.format("%02d:%02d", hour, min);
            stopTime.setText(String.format("%02d:%02d", hour, min));

            //reset
            hour = 0;
            min = 0;

            //close dialog
            stopDialog.dismiss();
        });

        //show dialog
        stopDialog.show();

    }

    private void showStartDateDialog() {

        //start date
        final DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {

            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            //update birthday
            String myFormat = "dd/MM/yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            startDate.setText(sdf.format(myCalendar.getTime()));
            selectedStartDate = sdf.format(myCalendar.getTime());

        };
        new DatePickerDialog(this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void showEndDateDialog() {

        //stop date
        final DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {

            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            //update birthday
            String myFormat = "dd/MM/yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            endDate.setText(sdf.format(myCalendar.getTime()));
            selectedEndDate = sdf.format(myCalendar.getTime());

        };
        new DatePickerDialog(this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void validateParams() {

        //fetch strings
        String theUrl;
        if (locationSwitch.isChecked()){

            //create path
            String path = Environment.getExternalStorageDirectory().toString() + "/" + Common.BASE_FOLDER_NAME + "/" + Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey + "/Application";

            //create url
            theUrl = "file:///" + path + redirectUrl.getText().toString().trim();

        } else {

            //create url
            theUrl = redirectUrl.getText().toString().trim();

        }

        //validate
        if (TextUtils.isEmpty(theUrl)){

            redirectUrl.requestFocus();
            redirectUrl.setError("Required");

        } else

        if (TextUtils.isEmpty(selectedStartTime)){

            Toast.makeText(this, "Pick a time to start", Toast.LENGTH_SHORT).show();

        } else

        if (TextUtils.isEmpty(selectedStopTime)){

            Toast.makeText(this, "Pick a time to stop", Toast.LENGTH_SHORT).show();

        } else

        if (selectedType.equals(Common.SCHEDULE_TYPE_SPECIFIC) && TextUtils.isEmpty(selectedStartDate)){

            Toast.makeText(this, "Pick a date to start", Toast.LENGTH_SHORT).show();

        } else

        if (selectedType.equals(Common.SCHEDULE_TYPE_SPECIFIC) && TextUtils.isEmpty(selectedEndDate)){

            Toast.makeText(this, "Pick a date to end", Toast.LENGTH_SHORT).show();

        } else {

            if (isSundayChecked || isMondayChecked || isTuesdayChecked || isWednesdayChecked || isThursdayChecked || isFridayChecked || isSaturdayChecked){

                setupScheduleList(theUrl);

            } else {

                Toast.makeText(this, "A day should at least be selected", Toast.LENGTH_LONG).show();

            }

        }

    }

    private void setupScheduleList(String theUrl) {

        //disable widgets
        redirectUrl.setEnabled(false);
        addScheduleBtn.setEnabled(false);
        startTime.setEnabled(false);
        stopTime.setEnabled(false);
        startDate.setEnabled(false);
        endDate.setEnabled(false);
        typeSwitch.setEnabled(false);
        scopeSwitch.setEnabled(false);
        switchDays(false);
        addText.setVisibility(View.GONE);
        addProgress.setVisibility(View.VISIBLE);

        //create list
        new Thread(() -> {

            if (selectedType.equals(Common.SCHEDULE_TYPE_NORMAL)) {

                if (selectedScope.equals(Common.SCHEDULE_SCOPE_WEEKLY)) {

                    if (isSundayChecked){
                        setNormalWeeklySchedule(theUrl, Common.DAY_SUNDAY);
                    }

                    if (isMondayChecked){
                        setNormalWeeklySchedule(theUrl, Common.DAY_MONDAY);
                    }

                    if (isTuesdayChecked){
                        setNormalWeeklySchedule(theUrl, Common.DAY_TUESDAY);
                    }

                    if (isWednesdayChecked){
                        setNormalWeeklySchedule(theUrl, Common.DAY_WEDNESDAY);
                    }

                    if (isThursdayChecked){
                        setNormalWeeklySchedule(theUrl, Common.DAY_THURSDAY);
                    }

                    if (isFridayChecked){
                        setNormalWeeklySchedule(theUrl, Common.DAY_FRIDAY);
                    }

                    if (isSaturdayChecked){
                        setNormalWeeklySchedule(theUrl, Common.DAY_SATURDAY);
                    }

                    //cleanup after
                    cleanup();

                } else {

                    //write to csv
                    try (FileWriter writer = new FileWriter(newFile, true)) {

                        //create
                        Schedule newRedirect = new Schedule(getRandomId(), theUrl, true, false, false, "", selectedStartTime, selectedStopTime, getTimeDifference(selectedStartTime, selectedStopTime), "", Common.PRIORITY_NORMAL);

                        StringBuilder sb = new StringBuilder();
                        sb.append(newRedirect.getId());
                        sb.append(',');
                        sb.append(newRedirect.getRedirect_url());
                        sb.append(',');
                        sb.append(newRedirect.isDaily());
                        sb.append(',');
                        sb.append(newRedirect.isWeekly());
                        sb.append(',');
                        sb.append(newRedirect.isOneTime());
                        sb.append(',');
                        sb.append(newRedirect.getDay());
                        sb.append(',');
                        sb.append(newRedirect.getStartTime());
                        sb.append(',');
                        sb.append(newRedirect.getStopTime());
                        sb.append(',');
                        sb.append(newRedirect.getDuration());
                        sb.append(',');
                        sb.append(newRedirect.getDate());
                        sb.append(',');
                        sb.append(newRedirect.getPriority());
                        sb.append('\n');

                        writer.write(sb.toString());

                    } catch (FileNotFoundException e) {
                        AddSchedule.this.runOnUiThread(() -> {
                            //enable widgets
                            redirectUrl.setEnabled(true);
                            addScheduleBtn.setEnabled(true);
                            startTime.setEnabled(true);
                            stopTime.setEnabled(true);
                            startDate.setEnabled(true);
                            endDate.setEnabled(true);
                            typeSwitch.setEnabled(true);
                            scopeSwitch.setEnabled(true);
                            switchDays(true);
                            addProgress.setVisibility(View.GONE);
                            addText.setVisibility(View.VISIBLE);

                            //error
                            Toast.makeText(this, "File Not Available", Toast.LENGTH_SHORT).show();
                        });

                    } catch (IOException e) {
                        e.printStackTrace();

                        AddSchedule.this.runOnUiThread(() -> {
                            //enable widgets
                            redirectUrl.setEnabled(true);
                            addScheduleBtn.setEnabled(true);
                            startTime.setEnabled(true);
                            stopTime.setEnabled(true);
                            startDate.setEnabled(true);
                            endDate.setEnabled(true);
                            typeSwitch.setEnabled(true);
                            scopeSwitch.setEnabled(true);
                            switchDays(true);
                            addProgress.setVisibility(View.GONE);
                            addText.setVisibility(View.VISIBLE);

                            //error
                            Toast.makeText(this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }  catch (ParseException e) {
                        e.printStackTrace();
                    } finally {

                        cleanup();

                    }

                }

            } else {

                if (selectedScope.equals(Common.SCHEDULE_SCOPE_WEEKLY)) {

                    try {
                        //init date list
                        List<Date> dates = new ArrayList<Date>();

                        //init formatter
                        DateFormat formatter;

                        formatter = new SimpleDateFormat("dd/MM/yyyy");
                        Date  startDateCalc = (Date)formatter.parse(selectedStartDate);
                        Date  endDateCalc = (Date)formatter.parse(selectedEndDate);
                        long interval = 24 * 1000 * 60 * 60; // 1 day in millis
                        long endTime = endDateCalc.getTime() ; // create your endtime here, possibly using Calendar or Date
                        long curTime = startDateCalc.getTime();
                        while (curTime <= endTime) {

                            if (isSundayChecked && today(new Date(curTime)).equals(Common.DAY_SUNDAY)){

                                dates.add(new Date(curTime));

                            } else

                            if (isMondayChecked && today(new Date(curTime)).equals(Common.DAY_MONDAY)){

                                dates.add(new Date(curTime));

                            } else

                            if (isTuesdayChecked && today(new Date(curTime)).equals(Common.DAY_TUESDAY)){

                                dates.add(new Date(curTime));

                            } else

                            if (isWednesdayChecked && today(new Date(curTime)).equals(Common.DAY_WEDNESDAY)){

                                dates.add(new Date(curTime));

                            } else

                            if (isThursdayChecked && today(new Date(curTime)).equals(Common.DAY_THURSDAY)){

                                dates.add(new Date(curTime));

                            } else

                            if (isFridayChecked && today(new Date(curTime)).equals(Common.DAY_FRIDAY)){

                                dates.add(new Date(curTime));

                            } else

                            if (isSaturdayChecked && today(new Date(curTime)).equals(Common.DAY_SATURDAY)){

                                dates.add(new Date(curTime));

                            }

                            curTime += interval;
                        }

                        //loop between days to get
                        for (int i = 0; i < dates.size(); i++){

                            Date lDate =(Date) dates.get(i);
                            String ds = formatter.format(lDate);

                            //write to csv
                            try (FileWriter writer = new FileWriter(newFile, true)) {

                                //create
                                Schedule newRedirect = new Schedule(getRandomId(), theUrl, false, false, true, "", selectedStartTime, selectedStopTime, getTimeDifference(selectedStartTime, selectedStopTime), ds, Common.PRIORITY_NORMAL);

                                StringBuilder sb = new StringBuilder();
                                sb.append(newRedirect.getId());
                                sb.append(',');
                                sb.append(newRedirect.getRedirect_url());
                                sb.append(',');
                                sb.append(newRedirect.isDaily());
                                sb.append(',');
                                sb.append(newRedirect.isWeekly());
                                sb.append(',');
                                sb.append(newRedirect.isOneTime());
                                sb.append(',');
                                sb.append(newRedirect.getDay());
                                sb.append(',');
                                sb.append(newRedirect.getStartTime());
                                sb.append(',');
                                sb.append(newRedirect.getStopTime());
                                sb.append(',');
                                sb.append(newRedirect.getDuration());
                                sb.append(',');
                                sb.append(newRedirect.getDate());
                                sb.append(',');
                                sb.append(newRedirect.getPriority());
                                sb.append('\n');

                                writer.write(sb.toString());

                            } catch (FileNotFoundException e) {
                                AddSchedule.this.runOnUiThread(() -> {
                                    //enable widgets
                                    redirectUrl.setEnabled(true);
                                    addScheduleBtn.setEnabled(true);
                                    startTime.setEnabled(true);
                                    stopTime.setEnabled(true);
                                    startDate.setEnabled(true);
                                    endDate.setEnabled(true);
                                    typeSwitch.setEnabled(true);
                                    scopeSwitch.setEnabled(true);
                                    switchDays(true);
                                    addProgress.setVisibility(View.GONE);
                                    addText.setVisibility(View.VISIBLE);

                                    //error
                                    Toast.makeText(this, "File Not Available", Toast.LENGTH_SHORT).show();
                                });

                            } catch (IOException e) {
                                e.printStackTrace();

                                AddSchedule.this.runOnUiThread(() -> {
                                    //enable widgets
                                    redirectUrl.setEnabled(true);
                                    addScheduleBtn.setEnabled(true);
                                    startTime.setEnabled(true);
                                    stopTime.setEnabled(true);
                                    startDate.setEnabled(true);
                                    endDate.setEnabled(true);
                                    typeSwitch.setEnabled(true);
                                    scopeSwitch.setEnabled(true);
                                    switchDays(true);
                                    addProgress.setVisibility(View.GONE);
                                    addText.setVisibility(View.VISIBLE);

                                    //error
                                    Toast.makeText(this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }  catch (ParseException e) {
                                e.printStackTrace();
                            } finally {

                                cleanup();

                            }


                        }

                    } catch (Exception e){
                        e.printStackTrace();
                        Log.d("DailySpecificSchedule", e.getMessage());
                    }

                } else {

                    try {
                        //init date list
                        List<Date> dates = new ArrayList<Date>();

                        //init formatter
                        DateFormat formatter;

                        formatter = new SimpleDateFormat("dd/MM/yyyy");
                        Date  startDateCalc = (Date)formatter.parse(selectedStartDate);
                        Date  endDateCalc = (Date)formatter.parse(selectedEndDate);
                        long interval = 24 * 1000 * 60 * 60; // 1 day in millis
                        long endTime = endDateCalc.getTime() ; // create your endtime here, possibly using Calendar or Date
                        long curTime = startDateCalc.getTime();
                        while (curTime <= endTime) {
                            dates.add(new Date(curTime));
                            curTime += interval;
                        }

                        //loop between days to get
                        for (int i = 0; i < dates.size(); i++){

                            Date lDate =(Date)dates.get(i);
                            String ds = formatter.format(lDate);

                            //write to csv
                            try (FileWriter writer = new FileWriter(newFile, true)) {

                                //create
                                Schedule newRedirect = new Schedule(getRandomId(), theUrl, false, false, true, "", selectedStartTime, selectedStopTime, getTimeDifference(selectedStartTime, selectedStopTime), ds, Common.PRIORITY_NORMAL);

                                StringBuilder sb = new StringBuilder();
                                sb.append(newRedirect.getId());
                                sb.append(',');
                                sb.append(newRedirect.getRedirect_url());
                                sb.append(',');
                                sb.append(newRedirect.isDaily());
                                sb.append(',');
                                sb.append(newRedirect.isWeekly());
                                sb.append(',');
                                sb.append(newRedirect.isOneTime());
                                sb.append(',');
                                sb.append(newRedirect.getDay());
                                sb.append(',');
                                sb.append(newRedirect.getStartTime());
                                sb.append(',');
                                sb.append(newRedirect.getStopTime());
                                sb.append(',');
                                sb.append(newRedirect.getDuration());
                                sb.append(',');
                                sb.append(newRedirect.getDate());
                                sb.append(',');
                                sb.append(newRedirect.getPriority());
                                sb.append('\n');

                                writer.write(sb.toString());

                            } catch (FileNotFoundException e) {
                                AddSchedule.this.runOnUiThread(() -> {
                                    //enable widgets
                                    redirectUrl.setEnabled(true);
                                    addScheduleBtn.setEnabled(true);
                                    startTime.setEnabled(true);
                                    stopTime.setEnabled(true);
                                    startDate.setEnabled(true);
                                    endDate.setEnabled(true);
                                    typeSwitch.setEnabled(true);
                                    scopeSwitch.setEnabled(true);
                                    switchDays(true);
                                    addProgress.setVisibility(View.GONE);
                                    addText.setVisibility(View.VISIBLE);

                                    //error
                                    Toast.makeText(this, "File Not Available", Toast.LENGTH_SHORT).show();
                                });

                            } catch (IOException e) {
                                e.printStackTrace();

                                AddSchedule.this.runOnUiThread(() -> {
                                    //enable widgets
                                    redirectUrl.setEnabled(true);
                                    addScheduleBtn.setEnabled(true);
                                    startTime.setEnabled(true);
                                    stopTime.setEnabled(true);
                                    startDate.setEnabled(true);
                                    endDate.setEnabled(true);
                                    typeSwitch.setEnabled(true);
                                    scopeSwitch.setEnabled(true);
                                    switchDays(true);
                                    addProgress.setVisibility(View.GONE);
                                    addText.setVisibility(View.VISIBLE);

                                    //error
                                    Toast.makeText(this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }  catch (ParseException e) {
                                e.printStackTrace();
                            } finally {

                                cleanup();

                            }


                        }


                    } catch (Exception e){
                        e.printStackTrace();
                        Log.d("DailySpecificSchedule", e.getMessage());
                    }


                }

            }

        }).start();

        //wait before create
        new Handler(Looper.getMainLooper()).postDelayed(this::cleanup, 1500);

    }

    private void setNormalWeeklySchedule(String theUrl, String theDay) {

        //write to csv
        try (FileWriter writer = new FileWriter(newFile, true)) {

            //create
            Schedule newRedirect = new Schedule(getRandomId(), theUrl, false, true, false, theDay, selectedStartTime, selectedStopTime, getTimeDifference(selectedStartTime, selectedStopTime), "", Common.PRIORITY_NORMAL);

            StringBuilder sb = new StringBuilder();
            sb.append(newRedirect.getId());
            sb.append(',');
            sb.append(newRedirect.getRedirect_url());
            sb.append(',');
            sb.append(newRedirect.isDaily());
            sb.append(',');
            sb.append(newRedirect.isWeekly());
            sb.append(',');
            sb.append(newRedirect.isOneTime());
            sb.append(',');
            sb.append(newRedirect.getDay());
            sb.append(',');
            sb.append(newRedirect.getStartTime());
            sb.append(',');
            sb.append(newRedirect.getStopTime());
            sb.append(',');
            sb.append(newRedirect.getDuration());
            sb.append(',');
            sb.append(newRedirect.getDate());
            sb.append(',');
            sb.append(newRedirect.getPriority());
            sb.append('\n');

            writer.write(sb.toString());

        } catch (FileNotFoundException e) {
            AddSchedule.this.runOnUiThread(() -> {
                //enable widgets
                redirectUrl.setEnabled(true);
                addScheduleBtn.setEnabled(true);
                startTime.setEnabled(true);
                stopTime.setEnabled(true);
                startDate.setEnabled(true);
                endDate.setEnabled(true);
                typeSwitch.setEnabled(true);
                scopeSwitch.setEnabled(true);
                switchDays(true);
                addProgress.setVisibility(View.GONE);
                addText.setVisibility(View.VISIBLE);

                //error
                Toast.makeText(this, "File Not Available", Toast.LENGTH_SHORT).show();
            });

        } catch (IOException e) {
            e.printStackTrace();

            AddSchedule.this.runOnUiThread(() -> {
                //enable widgets
                redirectUrl.setEnabled(true);
                addScheduleBtn.setEnabled(true);
                startTime.setEnabled(true);
                stopTime.setEnabled(true);
                startDate.setEnabled(true);
                endDate.setEnabled(true);
                typeSwitch.setEnabled(true);
                scopeSwitch.setEnabled(true);
                switchDays(true);
                addProgress.setVisibility(View.GONE);
                addText.setVisibility(View.VISIBLE);

                //error
                Toast.makeText(this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }  catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private String today(Date date){

        //get today in millis
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());

    }

    private String getTimeDifference(String startTime, String stopTime) throws ParseException {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        Date startDate = simpleDateFormat.parse(startTime);
        Date endDate = simpleDateFormat.parse(stopTime);

        long difference = endDate.getTime() - startDate.getTime();
        if(difference<0)
        {
            Date dateMax = simpleDateFormat.parse("24:00");
            Date dateMin = simpleDateFormat.parse("00:00");
            difference=(dateMax.getTime() -startDate.getTime() )+(endDate.getTime()-dateMin.getTime());
        }
        int days = (int) (difference / (1000*60*60*24));
        int hours = (int) ((difference - (1000*60*60*24*days)) / (1000*60*60));
        int min = (int) (difference - (1000*60*60*24*days) - (1000*60*60*hours)) / (1000*60);
        int sec = (int) (difference - (1000*60*60*24*days) - (1000*60*60*hours) - (1000*60*min)) / (1000);
        //Log.i("log_tag","Hours: "+hours+", Mins: "+min+", Secs: "+sec);

        return String.valueOf(difference);
    }

    private void switchDays(boolean b) {

        monButton.setEnabled(b);
        tueButton.setEnabled(b);
        wedButton.setEnabled(b);
        thuButton.setEnabled(b);
        friButton.setEnabled(b);
        satButton.setEnabled(b);
        sunButton.setEnabled(b);

    }

    public static String getRandomId() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 5;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString();
        return generatedString;
    }

    private void cleanup() {

        //close activity
        finish();
        overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);

    }

    @Override
    public void onBackPressed() {
        finish();
        this.overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //check mode
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.UltraDarkTheme);
        } else {
            setTheme(R.style.UltraLightTheme);
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
}