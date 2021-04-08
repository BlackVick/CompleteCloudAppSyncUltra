package com.cloudappsync.ultra.Basic;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.cloudappsync.ultra.Models.Domains;
import com.cloudappsync.ultra.R;
import com.cloudappsync.ultra.Ultra.Setting;
import com.cloudappsync.ultra.Utilities.Common;
import com.cloudappsync.ultra.Utilities.Database;
import com.cloudappsync.ultra.Utilities.FTPClientFunctions;
import com.cloudappsync.ultra.Utilities.UserDatabase;
import com.squareup.picasso.Picasso;
import io.paperdb.Paper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BasicSetting extends AppCompatActivity {

    //widget
    private ImageView backButton;
    private ImageView backgroundImage;

    //appearance
    private SwitchCompat darkModeSwitch;

    //refresh
    private TextView timePickerEdt, refreshTimeSelector;
    private Spinner refreshSpinner;

    //sync
    private TextView appTimePickerEdt, appTimeSelector;
    private Spinner syncSpinner;
    private TextView syncIndicator;
    private SwitchCompat syncSwitch;

    //custom redirect
    private TextView changeCustomUrl, changeCustomImage;

    //security
    private SwitchCompat singleAccessSwitch;
    private TextView changePassword, passwordIndicator;

    //splash
    private TextView splashIndicator;
    private SwitchCompat splashSwitch;

    //online indicator
    private TextView onlineIndicator;
    private SwitchCompat onlineSwitch;

    //schedule
    private TextView scheduleIndicator;
    private SwitchCompat scheduleSwitch;

    //launch from
    private TextView launchIndicator;
    private SwitchCompat launchSwitch;

    //test mode toggle
    private TextView testIndicator;
    private SwitchCompat testSwitch;

    //start mode
    private TextView startIndicator;
    private SwitchCompat startSwitch;

    //refresh toggle
    private TextView refreshIndicator;
    private SwitchCompat refreshSwitch;

    //user agent
    private TextView agentIndicator;
    private SwitchCompat agentSwitch;

    //sync mode
    private RadioGroup syncModeRadio;
    private RadioButton ftpZipRadio, urlZipRadio, parseRadio;

    //master domains
    private Spinner domainSpinner;

    //custom background
    private TextView changeAppBackground, restoreDefaultBackground;

    //restore settings to default
    private TextView restoreBtn;

    //values
    private String passwordType;
    private String savedPassword = "";
    private String savedMasterUrl = "";
    private String savedUsername = "";
    private String path;

    //image
    private static final int BRANDING_IMAGE_REQUEST_CODE = 665;
    private static final int BACKGROUND_IMAGE_REQUEST_CODE = 765;
    private Uri imageUri;

    //time picker
    int hour = 0;
    int min = 0;

    //licence
    private String companyId;
    private String licenceKey;

    //static values
    public static final int PERMISSION_REQUEST_CODE = 234;

    //ftp
    private FTPClientFunctions ftpclient = null;

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

        setContentView(R.layout.activity_basic_setting);

        //widgets
        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        singleAccessSwitch = findViewById(R.id.singleAccessSwitch);
        changePassword = findViewById(R.id.changePassword);
        refreshSpinner = findViewById(R.id.refreshSpinner);
        syncSpinner = findViewById(R.id.syncSpinner);
        syncIndicator = findViewById(R.id.syncIndicator);
        syncSwitch = findViewById(R.id.syncSwitch);
        splashIndicator = findViewById(R.id.splashIndicator);
        splashSwitch = findViewById(R.id.splashSwitch);
        launchIndicator = findViewById(R.id.launchIndicator);
        launchSwitch = findViewById(R.id.launchSwitch);
        timePickerEdt = findViewById(R.id.timePickerEdt);
        appTimePickerEdt = findViewById(R.id.appTimePickerEdt);
        refreshTimeSelector = findViewById(R.id.refreshTimeSelector);
        appTimeSelector = findViewById(R.id.appTimeSelector);
        backButton = findViewById(R.id.backButton);
        changeCustomUrl = findViewById(R.id.changeCustomUrl);
        changeCustomImage = findViewById(R.id.changeCustomImage);
        onlineIndicator = findViewById(R.id.onlineIndicator);
        onlineSwitch = findViewById(R.id.onlineSwitch);
        scheduleIndicator = findViewById(R.id.scheduleIndicator);
        scheduleSwitch = findViewById(R.id.scheduleSwitch);
        passwordIndicator = findViewById(R.id.passwordIndicator);
        domainSpinner = findViewById(R.id.domainSpinner);
        syncModeRadio = findViewById(R.id.syncModeRadio);
        ftpZipRadio = findViewById(R.id.ftpZipRadio);
        urlZipRadio = findViewById(R.id.urlZipRadio);
        parseRadio = findViewById(R.id.parseRadio);
        testIndicator = findViewById(R.id.testIndicator);
        testSwitch = findViewById(R.id.testSwitch);
        startIndicator = findViewById(R.id.startIndicator);
        startSwitch = findViewById(R.id.startSwitch);
        changeAppBackground = findViewById(R.id.changeAppBackground);
        backgroundImage = findViewById(R.id.backgroundImage);
        restoreBtn = findViewById(R.id.restoreBtn);
        restoreDefaultBackground = findViewById(R.id.restoreDefaultBackground);
        agentIndicator = findViewById(R.id.agentIndicator);
        agentSwitch = findViewById(R.id.agentSwitch);
        refreshIndicator = findViewById(R.id.refreshIndicator);
        refreshSwitch = findViewById(R.id.refreshSwitch);

        //ftp
        ftpclient = new FTPClientFunctions();

        //set values
        setDefaultValues();

        //init
        initialize();
    }

    private void setDefaultValues() {

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

        //get licence
        companyId = Paper.book().read(Common.COMPANY_ID);
        licenceKey = Paper.book().read(Common.LICENCE_ID);

        //launch from
        if (Paper.book().read(Common.LOAD_STYLE) != null && Paper.book().read(Common.LOAD_STYLE).equals(Common.LOAD_FROM_ONLINE)){
            launchIndicator.setText("Online");
            launchSwitch.setChecked(true);
        } else

        if (Paper.book().read(Common.LOAD_STYLE) != null && Paper.book().read(Common.LOAD_STYLE).equals(Common.LOAD_FROM_LOCAL)){
            launchIndicator.setText("Local");
            launchSwitch.setChecked(false);
        } else

        if (Paper.book().read(Common.LOAD_STYLE) != null && Paper.book().read(Common.LOAD_STYLE).equals(Common.LOAD_FROM_SERVER)){
            launchIndicator.setText("Server");
            launchSwitch.setChecked(true);
        } else {
            Paper.book().write(Common.LOAD_STYLE, Common.LOAD_FROM_LOCAL);
            launchIndicator.setText("Local");
            launchSwitch.setChecked(false);
        }

        //visual
        if(Paper.book().read(Common.VISUAL_STYLE) != null){

            if (Paper.book().read(Common.VISUAL_STYLE).equals(Common.NIGHT_MODE)){

                darkModeSwitch.setChecked(true);

                //set dark mode
                setDarkMode(true);

            } else {

                darkModeSwitch.setChecked(false);

                //set dark mode
                setDarkMode(false);

            }

        } else {

            Paper.book().write(Common.VISUAL_STYLE, Common.DAY_MODE);
            darkModeSwitch.setChecked(false);

            //set dark mode
            setDarkMode(false);

        }

        //access password
        try {
            File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
            BufferedReader reader = new BufferedReader(new FileReader(dir.getAbsoluteFile() + "/" + Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey + "/" + Common.USER_CONFIG_FOLDER + "/configFile.txt"));

            String line = "";
            if ((line = reader.readLine()) != null)
                savedMasterUrl = line;
            if ((line = reader.readLine()) != null)
                savedUsername = line;
            if ((line = reader.readLine()) != null)
                savedPassword = line;
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //password type
        passwordType = Paper.book().read(Common.PASSWORD_REQUEST_TYPE);

        //indicator
        if (Paper.book().read(Common.ONLINE_INDICATOR_STATE).equals(Common.ONLINE_INDICATOR_SHOW)){

            onlineIndicator.setText("Shown");
            onlineSwitch.setChecked(true);

        } else

        if (Paper.book().read(Common.ONLINE_INDICATOR_STATE).equals(Common.ONLINE_INDICATOR_HIDE)){

            onlineIndicator.setText("Hidden");
            onlineSwitch.setChecked(false);

        } else {

            Paper.book().write(Common.ONLINE_INDICATOR_STATE, Common.ONLINE_INDICATOR_SHOW);
            onlineIndicator.setText("Shown");
            onlineSwitch.setChecked(true);

        }

        //schedule
        if (Paper.book().read(Common.SCHEDULE_LOCATION_CHOICE).equals(Common.SCHEDULE_LOCATION_LOCAL)){

            scheduleIndicator.setText("Local");
            scheduleSwitch.setChecked(false);

        } else

        if (Paper.book().read(Common.SCHEDULE_LOCATION_CHOICE).equals(Common.SCHEDULE_LOCATION_ONLINE)){

            scheduleIndicator.setText("Online");
            scheduleSwitch.setChecked(true);

        } else {

            Paper.book().write(Common.SCHEDULE_LOCATION_CHOICE, Common.SCHEDULE_LOCATION_LOCAL);
            scheduleIndicator.setText("Local");
            scheduleSwitch.setChecked(false);

        }

        //test mode toggle
        if (Paper.book().read(Common.CURRENT_TEST_MODE).equals(Common.TEST_ONE_OFF)){

            testIndicator.setText(Common.TEST_ONE_OFF);
            testSwitch.setChecked(false);

        } else

        if (Paper.book().read(Common.CURRENT_TEST_MODE).equals(Common.TEST_PERSISTENT)){

            testIndicator.setText(Common.TEST_PERSISTENT);
            testSwitch.setChecked(true);

        } else {

            //set default
            Paper.book().write(Common.CURRENT_TEST_MODE, Common.TEST_ONE_OFF);
            testIndicator.setText(Common.TEST_ONE_OFF);
            testSwitch.setChecked(false);

        }

        //user agent toggle
        if (Paper.book().read(Common.CURRENT_USER_AGENT, Common.USER_AGENT_DESKTOP).equals(Common.USER_AGENT_MOBILE)){

            agentIndicator.setText(Common.USER_AGENT_MOBILE);
            agentSwitch.setChecked(false);

        } else

        if (Paper.book().read(Common.CURRENT_USER_AGENT, Common.USER_AGENT_DESKTOP).equals(Common.USER_AGENT_DESKTOP)){

            agentIndicator.setText(Common.USER_AGENT_DESKTOP);
            agentSwitch.setChecked(true);

        } else {

            //set default
            Paper.book().write(Common.CURRENT_USER_AGENT, Common.USER_AGENT_DESKTOP);
            agentIndicator.setText(Common.USER_AGENT_DESKTOP);
            agentSwitch.setChecked(true);

        }

        //start mode toggle
        if (Paper.book().read(Common.CURRENT_START_MODE).equals(Common.START_MODE_BOOT)){

            startIndicator.setText(Common.START_MODE_BOOT);
            startSwitch.setChecked(true);

        } else

        if (Paper.book().read(Common.CURRENT_START_MODE).equals(Common.START_MODE_NORMAL)){

            startIndicator.setText(Common.START_MODE_NORMAL);
            startSwitch.setChecked(false);

        } else {

            //set default
            Paper.book().write(Common.CURRENT_START_MODE, Common.START_MODE_BOOT);
            startIndicator.setText(Common.START_MODE_BOOT);
            startSwitch.setChecked(true);

        }

        //refresh toggle
        if (Paper.book().read(Common.PAGE_REFRESH_STATUS).equals(Common.PAGE_REFRESH_ACTIVE)){

            refreshIndicator.setText(Common.PAGE_REFRESH_ACTIVE);
            refreshSwitch.setChecked(true);

        } else

        if (Paper.book().read(Common.PAGE_REFRESH_STATUS).equals(Common.PAGE_REFRESH_INACTIVE)){

            refreshIndicator.setText(Common.PAGE_REFRESH_INACTIVE);
            refreshSwitch.setChecked(false);

        } else {

            //set default
            Paper.book().write(Common.PAGE_REFRESH_STATUS, Common.PAGE_REFRESH_ACTIVE);
            refreshIndicator.setText(Common.PAGE_REFRESH_ACTIVE);
            refreshSwitch.setChecked(true);

        }



        //init splash spinner
        initializeSplashSpinner();

        //set interval update
        setIntervalDefault();

        //set app interval update
        setAppIntervalDefault();

        //set domains spinner
        initDomainSpinner();

        //init sync mode
        initSyncModeDefaults();

    }

    private void initSyncModeDefaults() {

        //get string
        String syncType = Paper.book().read(Common.CURRENT_SYNC_TYPE);

        //get current
        switch (syncType){
            case Common.SYNC_TYPE_FTP_ZIP:
                ftpZipRadio.setChecked(true);
                break;

            case Common.SYNC_TYPE_URL_ZIP:
                urlZipRadio.setChecked(true);
                break;

            case Common.SYNC_TYPE_PARSE:
                parseRadio.setChecked(true);
                break;
        }

        //set new
        syncModeRadio.setOnCheckedChangeListener((group, checkedId) -> {

            switch (checkedId){

                case R.id.ftpZipRadio:
                    //create table
                    String licenceFTPZIP = Paper.book().read(Common.COMPANY_ID) + "_" + Paper.book().read(Common.LICENCE_ID) + "_ZIP";
                    new Database(getBaseContext()).createTable(licenceFTPZIP);

                    Paper.book().write(Common.CURRENT_SYNC_TYPE, Common.SYNC_TYPE_FTP_ZIP);
                    Paper.book().write(Common.CURRENT_DB_LICENCE, licenceFTPZIP);
                    break;

                case R.id.urlZipRadio:
                    //create table
                    String licenceURLZIP = Paper.book().read(Common.COMPANY_ID) + "_" + Paper.book().read(Common.LICENCE_ID) + "_URL";
                    new Database(getBaseContext()).createTable(licenceURLZIP);

                    Paper.book().write(Common.CURRENT_SYNC_TYPE, Common.SYNC_TYPE_URL_ZIP);
                    Paper.book().write(Common.CURRENT_DB_LICENCE, licenceURLZIP);
                    break;

                case R.id.parseRadio:
                    //create table
                    String licenceParse = Paper.book().read(Common.COMPANY_ID) + "_" + Paper.book().read(Common.LICENCE_ID) + "_PARSING";
                    new Database(getBaseContext()).createTable(licenceParse);

                    Paper.book().write(Common.CURRENT_SYNC_TYPE, Common.SYNC_TYPE_PARSE);
                    Paper.book().write(Common.CURRENT_DB_LICENCE, licenceParse);
                    break;

            }

        });

    }

    private void initDomainSpinner() {

        //fix customized
        File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME + "/" + Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey + "/" + Common.USER_CONFIG_FOLDER);
        //list
        final List<String> domainList = new ArrayList<>();
        domainList.add(0, "Custom Domains");

        //list
        final List<Domains> domainDataList = new ArrayList<>();
        domainDataList.add(0, new Domains("", "", "", "", ""));

        //adapter
        final ArrayAdapter<String> dataAdapterDomains;
        dataAdapterDomains = new ArrayAdapter(this, R.layout.custom_spinner_item, domainList);
        dataAdapterDomains.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //get domain list
        List<Domains> savedList = Paper.book().read(Common.MASTER_DOMAINS);
        for (int i = 1; i < savedList.size(); i++) {
            Domains theDomain = savedList.get(i);
            domainList.add(theDomain.getName());
            domainDataList.add(theDomain);
        }


        //set adapter
        domainSpinner.setAdapter(dataAdapterDomains);
        dataAdapterDomains.notifyDataSetChanged();

        //selector
        domainSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).toString().equals("Custom Domains")) {

                    //create dialog
                    android.app.AlertDialog confirmDialog = new android.app.AlertDialog.Builder(BasicSetting.this).create();
                    LayoutInflater inflater = BasicSetting.this.getLayoutInflater();
                    View viewOptions = inflater.inflate(R.layout.domain_change_layout, null);

                    //widgets
                    final EditText username = viewOptions.findViewById(R.id.username);
                    final EditText password = viewOptions.findViewById(R.id.password);
                    final EditText companyIdEdt = viewOptions.findViewById(R.id.companyIdEdt);
                    final EditText companyLicenceEdt = viewOptions.findViewById(R.id.companyLicenceEdt);
                    final RelativeLayout cancelButton = viewOptions.findViewById(R.id.cancelButton);
                    final RelativeLayout connectButton = viewOptions.findViewById(R.id.connectButton);
                    final TextView connectText = viewOptions.findViewById(R.id.connectText);
                    final ProgressBar connectProgress = viewOptions.findViewById(R.id.connectProgress);

                    //dialog props
                    confirmDialog.setView(viewOptions);
                    confirmDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
                    confirmDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    //grant access
                    connectButton.setOnClickListener(v -> {

                        Paper.book().write(Common.CURRENT_MASTER_DOMAIN, domainDataList.get(position).getWeb_domain());
                        Paper.book().write(Common.FTP_HOST, domainDataList.get(position).getFtp_host());
                        Paper.book().write(Common.FTP_PORT, domainDataList.get(position).getFtp_port());

                        //get selected details
                        String extDomain = domainDataList.get(position).getWeb_domain();
                        String extHost = domainDataList.get(position).getFtp_host();
                        String extPort = domainDataList.get(position).getFtp_port();

                        //fetch deta
                        String extUser = username.getText().toString().trim();
                        String extPass = password.getText().toString().trim();
                        String extCompany = companyIdEdt.getText().toString().trim();
                        String extLicence = companyLicenceEdt.getText().toString().trim();

                        if (!TextUtils.isEmpty(extUser) && !TextUtils.isEmpty(extPass) && !TextUtils.isEmpty(extCompany) && !TextUtils.isEmpty(extLicence)){

                            //start loading
                            connectButton.setEnabled(false);
                            connectText.setVisibility(View.GONE);
                            connectProgress.setVisibility(View.VISIBLE);

                            //get port
                            int laPort = Integer.parseInt(extPort);

                            new Thread(() -> {

                                boolean isSuccess = false;
                                try {
                                    isSuccess = ftpclient.ftpConnect(extHost, extUser, extPass, laPort);

                                    //check
                                    if (isSuccess){

                                        BasicSetting.this.runOnUiThread(() -> {
                                            //check if its exist
                                            if (!isUserExist(extCompany, extLicence)){
                                                new UserDatabase(BasicSetting.this).addNewUser(extDomain, extHost, extUser, extPass, extPort, extCompany, extLicence);
                                            }

                                            //stop loading
                                            connectButton.setEnabled(true);
                                            connectProgress.setVisibility(View.GONE);
                                            connectText.setVisibility(View.VISIBLE);

                                            //set new details
                                            Paper.book().write(Common.USER_NAME, extUser);
                                            Paper.book().write(Common.COMPANY_ID, extCompany);
                                            Paper.book().write(Common.LICENCE_ID, extLicence);

                                            //ftp
                                            Paper.book().write(Common.CURRENT_MASTER_DOMAIN, extDomain);
                                            Paper.book().write(Common.FTP_HOST, extHost);
                                            Paper.book().write(Common.FTP_PORT, extPort);
                                            Paper.book().write(Common.FTP_USERNAME, extUser);
                                            Paper.book().write(Common.FTP_PASSWORD, extPass);

                                            //dismiss dialog.
                                            confirmDialog.dismiss();
                                        });

                                    } else {

                                        BasicSetting.this.runOnUiThread(() -> {
                                            //stop loading
                                            connectButton.setEnabled(true);
                                            connectProgress.setVisibility(View.GONE);
                                            connectText.setVisibility(View.VISIBLE);

                                            Toast.makeText(BasicSetting.this, "Error Logging in", Toast.LENGTH_SHORT).show();
                                        });

                                    }

                                } catch (Exception e){

                                    BasicSetting.this.runOnUiThread(() -> {
                                        //stop loading
                                        connectButton.setEnabled(true);
                                        connectProgress.setVisibility(View.GONE);
                                        connectText.setVisibility(View.VISIBLE);

                                        Toast.makeText(BasicSetting.this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });

                                }

                            }).start();

                        } else {

                            Toast.makeText(BasicSetting.this, "Details Incomplete", Toast.LENGTH_SHORT).show();

                        }

                    });

                    //cancel
                    cancelButton.setOnClickListener(v -> {

                        //dismiss
                        confirmDialog.dismiss();

                    });

                    //show dialog
                    confirmDialog.show();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    //check user
    private boolean isUserExist(String companyId, String licenceKey) {

        return new UserDatabase(this).userExists(companyId, licenceKey);

    }

    private void initialize() {

        //check permissions
        checkAppPermissions();

        //splash
        splashSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked){

                Paper.book().write(Common.SPLASH_TYPE, Common.SPLASH_TYPE_IMAGE);
                splashIndicator.setText("Image");

            } else {

                Paper.book().write(Common.SPLASH_TYPE, Common.SPLASH_TYPE_VIDEO);
                splashIndicator.setText("Video");

            }

        });

        //indicator
        onlineSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked){

                Paper.book().write(Common.ONLINE_INDICATOR_STATE, Common.ONLINE_INDICATOR_SHOW);
                onlineIndicator.setText("Shown");

            } else {

                Paper.book().write(Common.ONLINE_INDICATOR_STATE, Common.ONLINE_INDICATOR_HIDE);
                onlineIndicator.setText("Hidden");

            }

        });

        //schedule
        scheduleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked){

                Paper.book().write(Common.SCHEDULE_LOCATION_CHOICE, Common.SCHEDULE_LOCATION_ONLINE);
                scheduleIndicator.setText("Online");

            } else {

                Paper.book().write(Common.SCHEDULE_LOCATION_CHOICE, Common.SCHEDULE_LOCATION_LOCAL);
                scheduleIndicator.setText("Local");

            }

        });

        //launch
        launchSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked){

                showStreamLayout();

            } else {

                Paper.book().write(Common.LOAD_STYLE, Common.LOAD_FROM_LOCAL);
                launchIndicator.setText("Local");

            }

        });

        //sync
        syncSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked){

                Paper.book().write(Common.APP_SYNC_TYPE, Common.APP_SYNC_CHANGE);
                syncIndicator.setText("Sync on change");

            } else {

                if (!Paper.book().read(Common.CURRENT_SYNC_TYPE).equals(Common.SYNC_TYPE_INDEX)) {

                    Paper.book().write(Common.APP_SYNC_TYPE, Common.APP_SYNC_DOWNLOAD);
                    syncIndicator.setText("Sync at interval");
                } else {
                    syncSwitch.setChecked(true);
                }

            }

        });

        //test mode
        testSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked){

                Paper.book().write(Common.CURRENT_TEST_MODE, Common.TEST_PERSISTENT);
                testIndicator.setText(Common.TEST_PERSISTENT);

                //enable single request password
                Paper.book().write(Common.PASSWORD_REQUEST_TYPE, Common.PASSWORD_REQUEST_ONCE);
                passwordIndicator.setText("Request Once");
                singleAccessSwitch.setChecked(true);

            } else {

                Paper.book().write(Common.CURRENT_TEST_MODE, Common.TEST_ONE_OFF);
                testIndicator.setText(Common.TEST_ONE_OFF);

                //enable single request password
                Paper.book().write(Common.PASSWORD_REQUEST_TYPE, Common.PASSWORD_REQUEST_ALWAYS);
                passwordIndicator.setText("Request Always");
                singleAccessSwitch.setChecked(false);

            }

        });

        //user agent mode
        agentSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked){

                Paper.book().write(Common.CURRENT_USER_AGENT, Common.USER_AGENT_DESKTOP);
                agentIndicator.setText(Common.USER_AGENT_DESKTOP);

            } else {

                Paper.book().write(Common.CURRENT_USER_AGENT, Common.USER_AGENT_MOBILE);
                agentIndicator.setText(Common.USER_AGENT_MOBILE);

            }

        });

        //start mode
        startSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked){

                Paper.book().write(Common.CURRENT_START_MODE, Common.START_MODE_BOOT);
                startIndicator.setText(Common.START_MODE_BOOT);

            } else {

                Paper.book().write(Common.CURRENT_START_MODE, Common.START_MODE_NORMAL);
                startIndicator.setText(Common.START_MODE_NORMAL);

            }

        });

        //refresh mode
        refreshSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked){

                Paper.book().write(Common.PAGE_REFRESH_STATUS, Common.PAGE_REFRESH_ACTIVE);
                refreshIndicator.setText(Common.PAGE_REFRESH_ACTIVE);

            } else {

                Paper.book().write(Common.PAGE_REFRESH_STATUS, Common.PAGE_REFRESH_INACTIVE);
                refreshIndicator.setText(Common.PAGE_REFRESH_INACTIVE);

            }

        });

        //change url
        changeCustomUrl.setOnClickListener(v -> {

            //build dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Change Custom Url");
            final EditText input = new EditText(this);
            input.setInputType(1);
            input.setText(Paper.book().read(Common.CUSTOM_VISIT_URL, "https://cloudappplayer.co.uk"));
            builder.setView(input);
            builder.setPositiveButton("SAVE", (dialog, which) -> {

                if (!TextUtils.isEmpty(input.getText().toString().trim())){

                    Paper.book().write(Common.CUSTOM_VISIT_URL, input.getText().toString().trim());

                    //dismiss
                    dialog.dismiss();

                } else {

                    Toast.makeText(this, "Cant be empty", Toast.LENGTH_SHORT).show();

                }

            });
            builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.cancel());
            builder.show();

        });

        //change image
        changeCustomImage.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto , BRANDING_IMAGE_REQUEST_CODE);
        });

        //change background image
        changeAppBackground.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto , BACKGROUND_IMAGE_REQUEST_CODE);
        });

        //default background
        restoreDefaultBackground.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Restore Default Background!")
                    .setMessage("Are you sure you want to restore the custom application background to defaults?")
                    .setPositiveButton("Yels", (dialog, which) -> {
                        Paper.book().write(Common.APP_BACKGROUND, "");
                        backgroundImage.setVisibility(View.GONE);
                        dialog.dismiss();
                    })
                    .setNegativeButton("No", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        });

        //back
        backButton.setOnClickListener(v -> {
            finish();
            this.overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);
        });

        //dark mode
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked){

                AppCompatDelegate
                        .setDefaultNightMode(
                                AppCompatDelegate
                                        .MODE_NIGHT_YES);

                //write to pref
                Paper.book().write(Common.VISUAL_STYLE, Common.NIGHT_MODE);

            } else {

                AppCompatDelegate
                        .setDefaultNightMode(
                                AppCompatDelegate
                                        .MODE_NIGHT_NO);

                //write to pref
                Paper.book().write(Common.VISUAL_STYLE, Common.DAY_MODE);

            }

        });

        //check for password type
        if (passwordType != null) {

            if (passwordType.equals(Common.PASSWORD_REQUEST_ONCE)) {

                singleAccessSwitch.setChecked(true);
                passwordIndicator.setText("Request Once");

            } else if (passwordType.equals(Common.PASSWORD_REQUEST_ALWAYS)) {

                singleAccessSwitch.setChecked(false);
                passwordIndicator.setText("Request Always");

            }

        } else {

            Paper.book().write(Common.PASSWORD_REQUEST_TYPE, Common.PASSWORD_REQUEST_ALWAYS);

            singleAccessSwitch.setChecked(false);
            passwordIndicator.setText("Request Always");

        }

        //singleAccessSwitch
        singleAccessSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked){

                Paper.book().write(Common.PASSWORD_REQUEST_TYPE, Common.PASSWORD_REQUEST_ONCE);
                passwordIndicator.setText("Request Once");

            } else {

                Paper.book().write(Common.PASSWORD_REQUEST_TYPE, Common.PASSWORD_REQUEST_ALWAYS);
                passwordIndicator.setText("Request Always");

            }

        });

        //change pass
        changePassword.setOnClickListener(v -> {
            confirmCurrentPass();
        });

        //restore settings to default
        restoreBtn.setOnClickListener(v -> {

            //create dialog
            android.app.AlertDialog resetDialog = new android.app.AlertDialog.Builder(this).create();
            LayoutInflater inflater = this.getLayoutInflater();
            View viewOptions = inflater.inflate(R.layout.reset_dialog, null);

            //widgets
            final Button cancelReset = viewOptions.findViewById(R.id.cancelReset);
            final Button acceptReset = viewOptions.findViewById(R.id.acceptReset);

            //dialog props
            resetDialog.setView(viewOptions);
            resetDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
            resetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            //cancel
            cancelReset.setOnClickListener(view -> {

                //dismiss
                resetDialog.dismiss();

            });

            //accept
            acceptReset.setOnClickListener(view -> {

                restoreAllSettings();

                resetDialog.dismiss();

            });

            //show dialog
            resetDialog.show();

        });

    }

    private void showStreamLayout() {

        //create dialog
        android.app.AlertDialog streamDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.live_stream_layout, null);

        //widgets
        final EditText customLink = viewOptions.findViewById(R.id.customLink);
        final RelativeLayout cancelButton = viewOptions.findViewById(R.id.cancelButton);
        final RelativeLayout connectButton = viewOptions.findViewById(R.id.connectButton);
        final TextView connectText = viewOptions.findViewById(R.id.connectText);
        final ProgressBar connectProgress = viewOptions.findViewById(R.id.connectProgress);

        //dialog props
        streamDialog.setView(viewOptions);
        streamDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        streamDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //populate edt
        if (Paper.book().read(Common.CUSTOM_ONLINE_LINK_STATUS, Common.CUSTOM_ONLINE_LINK_INACTIVE).equals(Common.CUSTOM_ONLINE_LINK_ACTIVE) && !TextUtils.isEmpty(Paper.book().read(Common.CUSTOM_ONLINE_LINK))){

            customLink.setText(Paper.book().read(Common.CUSTOM_ONLINE_LINK));

        }

        //grant access
        connectButton.setOnClickListener(v -> {

            //fetch deta
            String theLink = customLink.getText().toString().trim();

            if (!TextUtils.isEmpty(theLink)){

                //start loading
                connectButton.setEnabled(false);
                cancelButton.setEnabled(false);
                connectText.setVisibility(View.GONE);
                connectProgress.setVisibility(View.VISIBLE);

                new Thread(() -> {

                    try {

                        URL url = new URL(theLink);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        int code = connection.getResponseCode();

                        if(code == 200) {

                            this.runOnUiThread(() -> {

                                //stop loading
                                connectButton.setEnabled(true);
                                cancelButton.setEnabled(true);
                                connectProgress.setVisibility(View.GONE);
                                connectText.setVisibility(View.VISIBLE);

                                //set link
                                Paper.book().write(Common.CUSTOM_ONLINE_LINK_STATUS, Common.CUSTOM_ONLINE_LINK_ACTIVE);
                                Paper.book().write(Common.CUSTOM_ONLINE_LINK, theLink);

                                Paper.book().write(Common.LOAD_STYLE, Common.LOAD_FROM_ONLINE);
                                launchIndicator.setText("Online");

                                //dismiss
                                streamDialog.dismiss();

                            });

                        } else {

                            this.runOnUiThread(() -> {
                                //stop loading
                                connectButton.setEnabled(true);
                                cancelButton.setEnabled(true);
                                connectProgress.setVisibility(View.GONE);
                                connectText.setVisibility(View.VISIBLE);

                                Toast.makeText(this, "Page not found", Toast.LENGTH_LONG).show();
                            });

                        }



                    } catch (Exception e){

                        this.runOnUiThread(() -> {
                            //stop loading
                            connectButton.setEnabled(true);
                            cancelButton.setEnabled(true);
                            connectProgress.setVisibility(View.GONE);
                            connectText.setVisibility(View.VISIBLE);

                            Toast.makeText(this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

                    }

                }).start();

            } else {

                Toast.makeText(this, "Please, provide link", Toast.LENGTH_SHORT).show();

            }

        });

        //cancel
        cancelButton.setOnClickListener(v -> {


            Paper.book().write(Common.LOAD_STYLE, Common.LOAD_FROM_ONLINE);
            launchIndicator.setText("Online");

            //set link
            Paper.book().write(Common.CUSTOM_ONLINE_LINK_STATUS, Common.CUSTOM_ONLINE_LINK_INACTIVE);
            Paper.book().write(Common.CUSTOM_ONLINE_LINK, "");

            //dismiss
            streamDialog.dismiss();

        });

        //show dialog
        streamDialog.show();

    }

    private void checkAppPermissions() {

        //check app mobile sdk version
        if (Build.VERSION.SDK_INT >= 23){

            //check permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


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

    private void restoreAllSettings() {

        //splash
        Paper.book().write(Common.SPLASH_TYPE, Common.SPLASH_TYPE_VIDEO);
        splashIndicator.setText("Video");
        splashSwitch.setChecked(false);

        //launch
        Paper.book().write(Common.LOAD_STYLE, Common.LOAD_FROM_LOCAL);
        launchIndicator.setText("Local");
        launchSwitch.setChecked(false);

        //page timeout
        Paper.book().write(Common.PAGE_TIMEOUT_VALUE, "15");

        //page resync
        Paper.book().write(Common.PAGE_RESYNC_VALUE, "10");
        timePickerEdt.setText(formatSetInterval("10"));

        //app resync
        Paper.book().write(Common.APP_SYNC_INTERVAL, "60");
        appTimePickerEdt.setText(formatSetInterval("60"));

        //app sync type
        Paper.book().write(Common.APP_SYNC_TYPE, Common.APP_SYNC_DOWNLOAD);
        syncIndicator.setText("Sync at interval");
        syncSwitch.setChecked(false);

        //custom visit
        Paper.book().write(Common.CUSTOM_VISIT_URL, "https://cloudappplayer.co.uk");
        Paper.book().write(Common.CUSTOM_VISIT_IMAGE, "");

        //set indicator default
        Paper.book().write(Common.ONLINE_INDICATOR_STATE, Common.ONLINE_INDICATOR_SHOW);
        onlineIndicator.setText("Shown");
        onlineSwitch.setChecked(true);

        //set schedule
        Paper.book().write(Common.SCHEDULE_LOCATION_CHOICE, Common.SCHEDULE_LOCATION_LOCAL);
        scheduleIndicator.setText("Local");
        scheduleSwitch.setChecked(false);

        //set default app background
        Paper.book().write(Common.CUSTOM_APP_BACKGROUND, "");

        //set default test mode
        Paper.book().write(Common.IS_IN_TEST_MODE, Common.NO);

        //test mode
        Paper.book().write(Common.CURRENT_TEST_MODE, Common.TEST_ONE_OFF);
        testIndicator.setText(Common.TEST_ONE_OFF);
        testSwitch.setChecked(false);

        //set default start mode
        Paper.book().write(Common.CURRENT_START_MODE, Common.START_MODE_BOOT);
        startIndicator.setText(Common.START_MODE_BOOT);
        startSwitch.setChecked(true);

        //custom default app background
        Paper.book().write(Common.APP_BACKGROUND, "");

        //enable single request password
        Paper.book().write(Common.PASSWORD_REQUEST_TYPE, Common.PASSWORD_REQUEST_ALWAYS);
        passwordIndicator.setText("Request Always");
        singleAccessSwitch.setChecked(false);


        //dark mode
        AppCompatDelegate
                .setDefaultNightMode(
                        AppCompatDelegate
                                .MODE_NIGHT_NO);

        //write to pref
        Paper.book().write(Common.VISUAL_STYLE, Common.DAY_MODE);
        darkModeSwitch.setChecked(false);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BRANDING_IMAGE_REQUEST_CODE && resultCode == RESULT_OK){

            if (data.getData() != null) {
                imageUri = data.getData();

                Paper.book().write(Common.CUSTOM_VISIT_IMAGE, imageUri.toString());
            }

        }

        if (requestCode == BACKGROUND_IMAGE_REQUEST_CODE && resultCode == RESULT_OK){

            if (data.getData() != null) {
                imageUri = data.getData();

                Paper.book().write(Common.APP_BACKGROUND, imageUri.toString());

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
            }

        }
    }

    private void setDarkMode(boolean isDark) {

        if (isDark){

            AppCompatDelegate
                    .setDefaultNightMode(
                            AppCompatDelegate
                                    .MODE_NIGHT_YES);

        } else {

            AppCompatDelegate
                    .setDefaultNightMode(
                            AppCompatDelegate
                                    .MODE_NIGHT_NO);

        }

    }

    private void setIntervalDefault() {

        //fetch default from local
        String interval = Paper.book().read(Common.PAGE_RESYNC_VALUE);

        switch (interval){

            case "5":
                timePickerEdt.setText(formatSetInterval("5"));
                break;
            case "10":
                timePickerEdt.setText(formatSetInterval("10"));
                break;
            case "15":
                timePickerEdt.setText(formatSetInterval("15"));
                break;
            case "30":
                timePickerEdt.setText(formatSetInterval("30"));
                break;
            case "60":
                timePickerEdt.setText(formatSetInterval("60"));
                break;
            case "120":
                timePickerEdt.setText(formatSetInterval("120"));
                break;
            case "180":
                timePickerEdt.setText(formatSetInterval("180"));
                break;
            case "240":
                timePickerEdt.setText(formatSetInterval("240"));
                break;
            case "360":
                timePickerEdt.setText(formatSetInterval("360"));
                break;
            case "600":
                timePickerEdt.setText(formatSetInterval("600"));
                break;
            case "1440":
                timePickerEdt.setText(formatSetInterval("1440"));
                break;
            default:
                timePickerEdt.setText(formatSetInterval(interval));
                break;

        }


        //set listener on edi
        refreshTimeSelector.setOnClickListener(v -> showRefreshDialog());



        //list
        final List<String> refreshList = new ArrayList<>();
        refreshList.add(0, "Defined Intervals");
        refreshList.add(1, "5 Minutes");
        refreshList.add(2, "10 Minutes");
        refreshList.add(3, "15 Minutes");
        refreshList.add(4, "30 Minutes");
        refreshList.add(5, "1 Hour");
        refreshList.add(6, "2 Hours");
        refreshList.add(7, "3 Hours");
        refreshList.add(8, "4 Hours");
        refreshList.add(9, "6 Hours");
        refreshList.add(10, "10 Hours");
        refreshList.add(11, "24 Hours");

        //adapter
        final ArrayAdapter<String> dataAdapterRefresh;
        dataAdapterRefresh = new ArrayAdapter(BasicSetting.this, R.layout.custom_spinner_item, refreshList);
        dataAdapterRefresh.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //set adapter
        refreshSpinner.setAdapter(dataAdapterRefresh);
        dataAdapterRefresh.notifyDataSetChanged();

        //selector
        refreshSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).toString().equals("Defined Intervals")) {

                    switch (parent.getItemAtPosition(position).toString()){

                        case "5 Minutes":

                            Paper.book().write(Common.PAGE_RESYNC_VALUE, "5");
                            timePickerEdt.setText(formatSetInterval("5"));
                            break;

                        case "10 Minutes":

                            Paper.book().write(Common.PAGE_RESYNC_VALUE, "10");
                            timePickerEdt.setText(formatSetInterval("10"));
                            break;

                        case "15 Minutes":

                            Paper.book().write(Common.PAGE_RESYNC_VALUE, "15");
                            timePickerEdt.setText(formatSetInterval("15"));
                            break;

                        case "30 Minutes":

                            Paper.book().write(Common.PAGE_RESYNC_VALUE, "30");
                            timePickerEdt.setText(formatSetInterval("30"));
                            break;

                        case "1 Hour":

                            Paper.book().write(Common.PAGE_RESYNC_VALUE, "60");
                            timePickerEdt.setText(formatSetInterval("60"));
                            break;

                        case "2 Hours":

                            Paper.book().write(Common.PAGE_RESYNC_VALUE, "120");
                            timePickerEdt.setText(formatSetInterval("120"));
                            break;

                        case "3 Hours":

                            Paper.book().write(Common.PAGE_RESYNC_VALUE, "180");
                            timePickerEdt.setText(formatSetInterval("180"));
                            break;

                        case "4 Hours":

                            Paper.book().write(Common.PAGE_RESYNC_VALUE, "240");
                            timePickerEdt.setText(formatSetInterval("240"));
                            break;

                        case "6 Hours":

                            Paper.book().write(Common.PAGE_RESYNC_VALUE, "360");
                            timePickerEdt.setText(formatSetInterval("360"));
                            break;

                        case "10 Hours":

                            Paper.book().write(Common.PAGE_RESYNC_VALUE, "600");
                            timePickerEdt.setText(formatSetInterval("600"));
                            break;

                        case "24 Hours":

                            Paper.book().write(Common.PAGE_RESYNC_VALUE, "1440");
                            timePickerEdt.setText(formatSetInterval("1440"));
                            break;

                    }

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void setAppIntervalDefault() {

        //fetch default from local
        String interval = Paper.book().read(Common.APP_SYNC_INTERVAL);

        switch (interval){

            case "5":
                appTimePickerEdt.setText(formatSetInterval("5"));
                break;
            case "10":
                appTimePickerEdt.setText(formatSetInterval("10"));
                break;
            case "15":
                appTimePickerEdt.setText(formatSetInterval("15"));
                break;
            case "30":
                appTimePickerEdt.setText(formatSetInterval("30"));
                break;
            case "60":
                appTimePickerEdt.setText(formatSetInterval("60"));
                break;
            case "120":
                appTimePickerEdt.setText(formatSetInterval("120"));
                break;
            case "180":
                appTimePickerEdt.setText(formatSetInterval("180"));
                break;
            case "240":
                appTimePickerEdt.setText(formatSetInterval("240"));
                break;
            case "360":
                appTimePickerEdt.setText(formatSetInterval("360"));
                break;
            case "600":
                appTimePickerEdt.setText(formatSetInterval("600"));
                break;
            case "1440":
                appTimePickerEdt.setText(formatSetInterval("1440"));
                break;
            default:
                appTimePickerEdt.setText(formatSetInterval(interval));
                break;

        }


        //set click
        appTimeSelector.setOnClickListener(v -> showSyncDialog());


        //list
        final List<String> syncList = new ArrayList<>();
        syncList.add(0, "Defined Intervals");
        syncList.add(1, "5 Minutes");
        syncList.add(2, "10 Minutes");
        syncList.add(3, "15 Minutes");
        syncList.add(4, "30 Minutes");
        syncList.add(5, "1 Hour");
        syncList.add(6, "2 Hours");
        syncList.add(7, "3 Hours");
        syncList.add(8, "4 Hours");
        syncList.add(9, "6 Hours");
        syncList.add(10, "10 Hours");
        syncList.add(11, "24 Hours");

        //adapter
        final ArrayAdapter<String> dataAdapterSync;
        dataAdapterSync = new ArrayAdapter(BasicSetting.this, R.layout.custom_spinner_item, syncList);
        dataAdapterSync.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //set adapter
        syncSpinner.setAdapter(dataAdapterSync);
        dataAdapterSync.notifyDataSetChanged();

        //selector
        syncSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).toString().equals("Defined Intervals")) {

                    switch (parent.getItemAtPosition(position).toString()){

                        case "5 Minutes":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "5");
                            appTimePickerEdt.setText(formatSetInterval("5"));
                            break;

                        case "10 Minutes":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "10");
                            appTimePickerEdt.setText(formatSetInterval("10"));
                            break;

                        case "15 Minutes":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "15");
                            appTimePickerEdt.setText(formatSetInterval("15"));
                            break;

                        case "30 Minutes":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "30");
                            appTimePickerEdt.setText(formatSetInterval("30"));
                            break;

                        case "1 Hour":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "60");
                            appTimePickerEdt.setText(formatSetInterval("60"));
                            break;

                        case "2 Hours":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "120");
                            appTimePickerEdt.setText(formatSetInterval("120"));
                            break;

                        case "3 Hours":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "180");
                            appTimePickerEdt.setText(formatSetInterval("180"));
                            break;

                        case "4 Hours":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "240");
                            appTimePickerEdt.setText(formatSetInterval("240"));
                            break;

                        case "6 Hours":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "360");
                            appTimePickerEdt.setText(formatSetInterval("360"));
                            break;

                        case "10 Hours":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "600");
                            appTimePickerEdt.setText(formatSetInterval("600"));
                            break;

                        case "24 Hours":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "1440");
                            appTimePickerEdt.setText(formatSetInterval("1440"));
                            break;

                    }

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //switch
        if (Paper.book().read(Common.APP_SYNC_TYPE) != null && Paper.book().read(Common.APP_SYNC_TYPE).equals(Common.APP_SYNC_DOWNLOAD)){

            syncSwitch.setChecked(false);
            syncIndicator.setText("Sync at interval");

        } else

        if (Paper.book().read(Common.APP_SYNC_TYPE) != null && Paper.book().read(Common.APP_SYNC_TYPE).equals(Common.APP_SYNC_CHANGE)){

            syncSwitch.setChecked(true);
            syncIndicator.setText("Sync on change");

        } else {

            Paper.book().write(Common.APP_SYNC_TYPE, Common.APP_SYNC_DOWNLOAD);
            syncSwitch.setChecked(false);
            syncIndicator.setText("Sync at interval");

        }

    }

    private void showRefreshDialog() {

        //create dialog
        android.app.AlertDialog refreshDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.time_picker_dialog, null);

        //widgets
        final TimePicker timePicker = viewOptions.findViewById(R.id.timePicker);
        final Button cancelBtn = viewOptions.findViewById(R.id.cancelBtn);
        final Button setBtn = viewOptions.findViewById(R.id.setBtn);

        //dialog props
        refreshDialog.setView(viewOptions);
        refreshDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        refreshDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //initialize time picker
        timePicker.setHour(0);
        timePicker.setMinute(0);
        timePicker.setIs24HourView(true);
        Calendar calendar = Calendar.getInstance();
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {

            //get time
            hour = hourOfDay;
            min = minute;

        });

        //cancel
        cancelBtn.setOnClickListener(v -> {

            //dismiss
            refreshDialog.dismiss();

        });

        //grant access
        setBtn.setOnClickListener(v -> {

            //calculate minutes
            int totalMins = (hour * 60) + min;

            //save setting
            Paper.book().write(Common.PAGE_RESYNC_VALUE, String.valueOf(totalMins));

            //set on edt
            timePickerEdt.setText(String.format("%02d:%02d", hour, min));

            //close dialog
            refreshDialog.dismiss();
        });

        //show dialog
        refreshDialog.show();

    }

    private void showSyncDialog() {

        //create dialog
        android.app.AlertDialog syncDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.time_picker_dialog, null);

        //widgets
        final TimePicker timePicker = viewOptions.findViewById(R.id.timePicker);
        final Button cancelBtn = viewOptions.findViewById(R.id.cancelBtn);
        final Button setBtn = viewOptions.findViewById(R.id.setBtn);

        //dialog props
        syncDialog.setView(viewOptions);
        syncDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        syncDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //initialize time picker
        timePicker.setHour(0);
        timePicker.setMinute(0);
        timePicker.setIs24HourView(true);
        Calendar calendar = Calendar.getInstance();
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {

            //get time
            hour = hourOfDay;
            min = minute;

        });

        //cancel
        cancelBtn.setOnClickListener(v -> {

            //dismiss
            syncDialog.dismiss();

        });

        //grant access
        setBtn.setOnClickListener(v -> {

            //calculate minutes
            int totalMins = (hour * 60) + min;

            //save setting
            Paper.book().write(Common.APP_SYNC_INTERVAL, String.valueOf(totalMins));

            //set on edt
            appTimePickerEdt.setText(String.format("%02d:%02d", hour, min));

            //close dialog
            syncDialog.dismiss();
        });

        //show dialog
        syncDialog.show();

    }

    private void initializeSplashSpinner() {

        //check saved
        if (Paper.book().read(Common.SPLASH_TYPE) != null && Paper.book().read(Common.SPLASH_TYPE).equals(Common.SPLASH_TYPE_IMAGE)){

            splashIndicator.setText("Image");
            splashSwitch.setChecked(true);

        } else

        if (Paper.book().read(Common.SPLASH_TYPE) != null && Paper.book().read(Common.SPLASH_TYPE).equals(Common.SPLASH_TYPE_VIDEO)){

            splashIndicator.setText("Video");
            splashSwitch.setChecked(false);

        } else {

            Paper.book().write(Common.SPLASH_TYPE, Common.SPLASH_TYPE_VIDEO);
            splashIndicator.setText("Video");
            splashSwitch.setChecked(false);

        }

    }

    private String formatSetInterval(String interv) {

        int theMins = Integer.parseInt(interv);
        int hour = theMins / 60;
        int min = theMins % 60;

        return String.format("%02d:%02d", hour, min);
    }

    private void confirmCurrentPass() {

        //create dialog
        android.app.AlertDialog passwordDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.password_dialog, null);

        //widgets
        final EditText appPassword = viewOptions.findViewById(R.id.appPassword);
        final Button cancelAccess = viewOptions.findViewById(R.id.cancelAccess);
        final Button grantAccess = viewOptions.findViewById(R.id.grantAccess);

        //dialog props
        passwordDialog.setView(viewOptions);
        passwordDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        passwordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //lock dialog
        passwordDialog.setCancelable(false);
        passwordDialog.setCanceledOnTouchOutside(false);

        //cancel
        cancelAccess.setOnClickListener(v -> {

            //dismiss
            passwordDialog.dismiss();

        });

        //grant access
        grantAccess.setOnClickListener(v -> {

            if (!TextUtils.isEmpty(appPassword.getText().toString().trim())){

                if (appPassword.getText().toString().trim().equals(savedPassword)){

                    //register password entry
                    Paper.book().write(Common.PASSWORD_STATUS, Common.PASSWORD_PROVIDED);

                    //grant access
                    passwordDialog.dismiss();

                    //show new password dialog
                    showNewPasswordStuff();

                } else {

                    Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show();

                }

            } else {

                Toast.makeText(this, "Provide old password", Toast.LENGTH_SHORT).show();

            }

        });

        //show dialog
        passwordDialog.show();

    }

    private void showNewPasswordStuff() {

        //create dialog
        android.app.AlertDialog passwordDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.new_password_dialog, null);

        //widgets
        final EditText appPassword = viewOptions.findViewById(R.id.appPassword);
        final Button cancelAccess = viewOptions.findViewById(R.id.cancelAccess);
        final Button grantAccess = viewOptions.findViewById(R.id.grantAccess);

        //dialog props
        passwordDialog.setView(viewOptions);
        passwordDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        passwordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //lock dialog
        passwordDialog.setCancelable(false);
        passwordDialog.setCanceledOnTouchOutside(false);

        //cancel
        cancelAccess.setOnClickListener(v -> {

            //dismiss
            passwordDialog.dismiss();

        });

        //grant access
        grantAccess.setOnClickListener(v -> {

            if (!TextUtils.isEmpty(appPassword.getText().toString().trim())){

                passwordDialog.dismiss();

                reWriteConfig(appPassword.getText().toString().trim());

            } else {

                Toast.makeText(this, "Provide password", Toast.LENGTH_SHORT).show();

            }

        });

        //show dialog
        passwordDialog.show();

    }

    private void reWriteConfig(String newPass) {

        //save new url
        savedPassword = newPass;

        new Thread(() -> {

            //check if file exist in directory and create if not
            File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
            if (!dir.exists()) {
                dir.mkdir();
            }

            //check if config file exists and create file if not
            File newFile = new File(dir.getAbsolutePath(), Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey + "/" + Common.USER_CONFIG_FOLDER + "/configFile.txt");
            if (newFile.exists()) {

                newFile.delete();

                try {
                    FileOutputStream fos = new FileOutputStream(newFile);
                    fos.write(savedMasterUrl.getBytes());
                    fos.write("\n".getBytes());
                    fos.write(savedUsername.getBytes());
                    fos.write("\n".getBytes());
                    fos.write(newPass.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }).start();

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
    }
}