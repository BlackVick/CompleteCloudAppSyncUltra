package com.cloudappsync.ultra.Ultra;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudappsync.ultra.SyncActivity;
import com.cloudappsync.ultra.R;
import com.cloudappsync.ultra.Splash;
import com.cloudappsync.ultra.Utilities.Database;
import com.cloudappsync.ultra.Utilities.UserDatabase;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.cloudappsync.ultra.Models.Domains;
import com.cloudappsync.ultra.Models.User;
import com.cloudappsync.ultra.Utilities.Common;
import com.cloudappsync.ultra.Utilities.FTPClientFunctions;
import com.cloudappsync.ultra.Utilities.Methods;
import com.opencsv.CSVReader;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SignIn extends AppCompatActivity {

    //widgets
    private EditText username, password, port, companyId, companyLicence;
    private RelativeLayout userLayout;
    private Spinner userSpinner;
    private RelativeLayout connectButton, testButton, exitButton;
    private TextView connectText, testText;
    private ProgressBar connectProgress, testProgress;
    private TextView syncModeIndicator;
    private SwitchCompat syncSwitch;
    private Spinner syncIntervalSpinner;

    //sync type widgets
    private Spinner syncTypeSpinner;

    //indicator
    private ImageView indicatorIcon;
    private TextView indicatorText;
    private SwitchCompat indicatorSwitch;

    //static values
    public static final int PERMISSION_REQUEST_CODE = 234;

    //dynamic values
    private boolean isPermitted = false;
    private int fl = 0;
    private int cl = 0;
    private boolean companyExist = false;
    private boolean licenceExist = false;
    private String selectedSyncType = "";
    private String selectedMasterDomain = "";
    private String selectedFtpHost = "";
    private int fileCountNum = 0;
    private boolean isCancel = false;

    //ftp
    private FTPClientFunctions ftpclient = null;

    //multiuser algo
    private List<User> userList = new ArrayList<>();
    private List<User> tempList = new ArrayList<>();

    //domain
    private RelativeLayout loadingLayout, domainLayout;
    private Spinner domainSpinner;

    //data
    private File newFile;

    //cancel dialog
    private android.app.AlertDialog alertDialog;
    private boolean isLoading = false;
    private TextView fileCount;
    private Button cancelBtn;

    //threads and parseing
    private Thread parseThread, secondaryParseThread;
    private List<String> theList = new ArrayList<>();

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
        setContentView(R.layout.activity_sign_in);

        //ftp
        ftpclient = new FTPClientFunctions();
        
        //widgets
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        port = findViewById(R.id.port);
        companyId = findViewById(R.id.companyId);
        companyLicence = findViewById(R.id.companyLicence);
        connectButton = findViewById(R.id.connectButton);
        indicatorIcon = findViewById(R.id.indicatorIcon);
        indicatorText = findViewById(R.id.indicatorText);
        indicatorSwitch = findViewById(R.id.indicatorSwitch);
        userLayout = findViewById(R.id.userLayout);
        userSpinner = findViewById(R.id.userSpinner);
        connectText = findViewById(R.id.connectText);
        connectProgress = findViewById(R.id.connectProgress);
        testButton = findViewById(R.id.testButton);
        testText = findViewById(R.id.testText);
        testProgress = findViewById(R.id.testProgress);
        syncTypeSpinner = findViewById(R.id.syncTypeSpinner);
        domainLayout = findViewById(R.id.domainLayout);
        domainSpinner = findViewById(R.id.domainSpinner);
        loadingLayout = findViewById(R.id.loadingLayout);
        syncIntervalSpinner = findViewById(R.id.syncIntervalSpinner);
        syncSwitch = findViewById(R.id.syncSwitch);
        syncModeIndicator = findViewById(R.id.syncModeIndicator);
        exitButton = findViewById(R.id.exitButton);

        //initialize
        initialize();
    }

    private void initialize() {
        
        //request permissions
        requestPermissions();

        //set default for switch
        sedSwitchDefault();

        //fetch users
        fetchUsers();

        //initialize sync type spinner
        initSyncTypeSpinner();

        //switch
        indicatorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked){

                //indicator
                indicatorIcon.setImageResource(R.drawable.online);

                //text
                indicatorText.setText("Online Server");

                //show dialog
                showStreamLayout();

            } else {

                //indicator
                indicatorIcon.setImageResource(R.drawable.offline);

                //text
                indicatorText.setText("Offline Server");

            }

        });

        //test connection
        testButton.setOnClickListener(v -> {
            if (isPermitted){

                checkTestFileAndStrings();

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
        });

        //connct
        connectButton.setOnClickListener(v -> {

            if (isPermitted){

                checkFileAndStrings();

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

        });

        //page timeout
        Paper.book().write(Common.PAGE_TIMEOUT_VALUE, "15");

        //page resync
        Paper.book().write(Common.PAGE_RESYNC_VALUE, "10");

        //app resync
        Paper.book().write(Common.APP_SYNC_INTERVAL, "60");

        //user agent
        Paper.book().write(Common.CURRENT_USER_AGENT, Common.USER_AGENT_DESKTOP);

        //app sync type
        Paper.book().write(Common.APP_SYNC_TYPE, Common.APP_SYNC_DOWNLOAD);

        //custom visit
        Paper.book().write(Common.CUSTOM_VISIT_URL, "https://cloudappsync.com");
        Paper.book().write(Common.CUSTOM_VISIT_IMAGE, "");

        //set indicator default
        Paper.book().write(Common.ONLINE_INDICATOR_STATE, Common.ONLINE_INDICATOR_SHOW);

        //set indicator default
        Paper.book().write(Common.SCHEDULE_LOCATION_CHOICE, Common.SCHEDULE_LOCATION_LOCAL);

        //set default app background
        Paper.book().write(Common.CUSTOM_APP_BACKGROUND, "");

        //set default test mode
        Paper.book().write(Common.IS_IN_TEST_MODE, Common.NO);

        //set default test mode
        Paper.book().write(Common.CURRENT_START_MODE, Common.START_MODE_BOOT);

        //set default page refresh status
        Paper.book().write(Common.PAGE_REFRESH_STATUS, Common.PAGE_REFRESH_INACTIVE);

        //custom default app background
        Paper.book().write(Common.APP_BACKGROUND, "");

        //init sync switch
        syncModeIndicator.setText("Sync at set intervals");
        syncSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked){

                Paper.book().write(Common.APP_SYNC_TYPE, Common.APP_SYNC_CHANGE);
                syncModeIndicator.setText("Sync on file change");

            } else {

                Paper.book().write(Common.APP_SYNC_TYPE, Common.APP_SYNC_INTERVAL);
                syncModeIndicator.setText("Sync at set intervals");

            }

        });

        //init interval
        initIntervals();

        //download domains txt
        downloadDefaultDomainFile();

        //exit
        exitButton.setOnClickListener(v -> {
            Intent exitIntent = new Intent(this, Splash.class);
            exitIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            exitIntent.putExtra(Common.EXIT_APP_INTENT, true);
            startActivity(exitIntent);
            finish();
            this.overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);
        });

    }

    private void fetchUsers() {

        //fetch
        userList = new UserDatabase(this).getUsers();

        //check users
        if (userList == null || userList.size() == 0){

            userLayout.setVisibility(View.GONE);

        } else {

            userLayout.setVisibility(View.VISIBLE);
            populateUserSpinner();

        }
    }

    private void initIntervals() {

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
        dataAdapterSync = new ArrayAdapter(SignIn.this, R.layout.custom_spinner_item, syncList);
        dataAdapterSync.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //set adapter
        syncIntervalSpinner.setAdapter(dataAdapterSync);
        dataAdapterSync.notifyDataSetChanged();

        //selector
        syncIntervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).toString().equals("Defined Intervals")) {

                    switch (parent.getItemAtPosition(position).toString()){

                        case "5 Minutes":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "5");
                            break;

                        case "10 Minutes":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "10");
                            break;

                        case "15 Minutes":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "15");
                            break;

                        case "30 Minutes":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "30");
                            break;

                        case "1 Hour":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "60");
                            break;

                        case "2 Hours":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "120");
                            break;

                        case "3 Hours":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "180");
                            break;

                        case "4 Hours":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "240");
                            break;

                        case "6 Hours":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "360");
                            break;

                        case "10 Hours":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "600");
                            break;

                        case "24 Hours":

                            Paper.book().write(Common.APP_SYNC_INTERVAL, "1440");
                            break;

                    }

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void initSyncTypeSpinner() {

        //list
        final List<String> theSyncTypeList = new ArrayList<>();
        theSyncTypeList.add(0, "Sync Mode");
        theSyncTypeList.add(1, Common.SYNC_TYPE_FTP_FOLDER);
        theSyncTypeList.add(2, Common.SYNC_TYPE_FTP_ZIP);
        theSyncTypeList.add(3, Common.SYNC_TYPE_URL_ZIP);
        theSyncTypeList.add(4, Common.SYNC_TYPE_INDEX);
        theSyncTypeList.add(5, Common.SYNC_TYPE_PARSE);
        theSyncTypeList.add(6, Common.SYNC_TYPE_API);

        //adapter
        final ArrayAdapter<String> dataAdapterSyncType;
        dataAdapterSyncType = new ArrayAdapter(this, R.layout.custom_spinner_item, theSyncTypeList);
        dataAdapterSyncType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //set adapter
        syncTypeSpinner.setAdapter(dataAdapterSyncType);
        dataAdapterSyncType.notifyDataSetChanged();

        //selector
        syncTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).toString().equals("Sync Mode")) {

                    selectedSyncType = parent.getItemAtPosition(position).toString();

                    if (selectedSyncType.equals(Common.SYNC_TYPE_URL_ZIP) || selectedSyncType.equals(Common.SYNC_TYPE_PARSE)){

                        username.setVisibility(View.GONE);
                        password.setVisibility(View.GONE);
                        port.setVisibility(View.GONE);

                    } else {

                        username.setVisibility(View.VISIBLE);
                        password.setVisibility(View.VISIBLE);
                        port.setVisibility(View.VISIBLE);

                    }

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
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

            //set link
            Paper.book().write(Common.CUSTOM_ONLINE_LINK_STATUS, Common.CUSTOM_ONLINE_LINK_INACTIVE);
            Paper.book().write(Common.CUSTOM_ONLINE_LINK, "");

            //dismiss
            streamDialog.dismiss();

        });

        //show dialog
        streamDialog.show();

    }

    private void populateUserSpinner() {

        //list
        final List<String> theUserList = new ArrayList<>();
        theUserList.add(0, "Saved Users");

        //data list
        tempList.add(0, new User("", "", "", "", "", "", ""));

        //adapter
        final ArrayAdapter<String> dataAdapterUsers;
        dataAdapterUsers = new ArrayAdapter(this, R.layout.custom_spinner_item, theUserList);
        dataAdapterUsers.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //populate
        for (int i = 0; i < userList.size(); i++){

            theUserList.add(userList.get(i).getCompany_id());

            //populate temp
            tempList.add(userList.get(i));

        }

        //set adapter
        userSpinner.setAdapter(dataAdapterUsers);
        dataAdapterUsers.notifyDataSetChanged();

        //selector
        userSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).toString().equals("Saved Users")) {

                    User selectedUser = tempList.get(position);

                    //set fields
                    selectedMasterDomain = selectedUser.getMaster_url();
                    selectedFtpHost = selectedUser.getFtp_host();
                    username.setText(selectedUser.getFtp_user());
                    companyId.setText(selectedUser.getCompany_id());
                    companyLicence.setText(selectedUser.getLicence_key());
                    port.setText(selectedUser.getFtp_port());

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void sedSwitchDefault() {

        //indicator
        indicatorIcon.setImageResource(R.drawable.offline);

        //text
        indicatorText.setText("Offline Server");

        //switch
        indicatorSwitch.setChecked(false);

    }





    //permissions
    private void requestPermissions() {

        //check app mobile sdk version
        if (Build.VERSION.SDK_INT >= 23){

            //check permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                //approve permission
                isPermitted = true;

            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

            }

        } else {

            //approve permission
            isPermitted = true;

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //switch value
                isPermitted = true;

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






    //life cycle
    @Override
    protected void onStart() {
        super.onStart();
        //check mode
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES || Paper.book().read(Common.VISUAL_STYLE, Common.DAY_MODE).equals(Common.NIGHT_MODE)) {
            setTheme(R.style.UltraDarkTheme);
        } else {
            setTheme(R.style.UltraLightTheme);
        }
    }








    //validate fields
    private void checkFileAndStrings() {

        //check strings
        String theUsername = username.getText().toString().trim();
        String thePassword = password.getText().toString().trim();
        String thePort = port.getText().toString().trim();
        String theCompany = companyId.getText().toString().trim();
        String theLicence = companyLicence.getText().toString().trim();

        if (selectedSyncType.equals(Common.SYNC_TYPE_URL_ZIP)){

            if (TextUtils.isEmpty(theCompany)){

                companyId.requestFocus();
                companyId.setError("Required");

            } else

            if (TextUtils.isEmpty(theLicence)){

                companyLicence.requestFocus();
                companyLicence.setError("Required");

            } else

            if (TextUtils.isEmpty(selectedFtpHost)){

                Toast.makeText(this, "Selected Domain Doesnt Have FTP Access", Toast.LENGTH_LONG).show();

            } else

            if (TextUtils.isEmpty(selectedMasterDomain)){

                Toast.makeText(this, "Master Domain Not Selected", Toast.LENGTH_LONG).show();

            } else{

                loginUrlZipUser(theCompany, theLicence);

            }

        } else if (selectedSyncType.equals(Common.SYNC_TYPE_PARSE)){

            if (TextUtils.isEmpty(theCompany)){

                companyId.requestFocus();
                companyId.setError("Required");

            } else

            if (TextUtils.isEmpty(theLicence)){

                companyLicence.requestFocus();
                companyLicence.setError("Required");

            } else

            if (TextUtils.isEmpty(selectedMasterDomain)){

                Toast.makeText(this, "Master Domain Not Selected", Toast.LENGTH_LONG).show();

            } else{

                loginParseUser(theCompany, theLicence);

            }

        } else{

            //port
            if (TextUtils.isEmpty(thePort)) {

                port.setText("21");
                thePort = "21";

            }

            //check
            if (TextUtils.isEmpty(theUsername)) {

                username.requestFocus();
                username.setError("Required");

            } else if (TextUtils.isEmpty(thePassword)) {

                password.requestFocus();
                password.setError("Required");

            } else if (TextUtils.isEmpty(theCompany)) {

                companyId.requestFocus();
                companyId.setError("Required");

            } else if (TextUtils.isEmpty(theLicence)) {

                companyLicence.requestFocus();
                companyLicence.setError("Required");

            } else if (TextUtils.isEmpty(selectedMasterDomain)) {

                Toast.makeText(this, "Master Domain Not Selected", Toast.LENGTH_LONG).show();

            } else if (TextUtils.isEmpty(selectedFtpHost)) {

                Toast.makeText(this, "Selected Domain Doesnt Have FTP Access", Toast.LENGTH_LONG).show();

            } else if (TextUtils.isEmpty(selectedSyncType)) {

                Toast.makeText(this, "Select A Sync Type", Toast.LENGTH_SHORT).show();

            } else {

                loginUser(theUsername, thePassword, thePort, theCompany, theLicence);

            }

        }

    }

    private void checkTestFileAndStrings() {

        //check strings
        String theUsername = username.getText().toString().trim();
        String thePassword = password.getText().toString().trim();
        String thePort = port.getText().toString().trim();
        String theCompany = companyId.getText().toString().trim();
        String theLicence = companyLicence.getText().toString().trim();

        if (selectedSyncType.equals(Common.SYNC_TYPE_URL_ZIP)){

            if (TextUtils.isEmpty(theCompany)){

                companyId.requestFocus();
                companyId.setError("Required");

            } else

            if (TextUtils.isEmpty(theLicence)){

                companyLicence.requestFocus();
                companyLicence.setError("Required");

            } else

            if (TextUtils.isEmpty(selectedFtpHost)){

                Toast.makeText(this, "Selected Domain Doesnt Have FTP Access", Toast.LENGTH_LONG).show();

            } else

            if (TextUtils.isEmpty(selectedMasterDomain)){

                Toast.makeText(this, "Master Domain Not Selected", Toast.LENGTH_LONG).show();

            } else{

                testZipUrl(theCompany, theLicence);

            }

        } else {

            //port
            if (TextUtils.isEmpty(thePort)){

                port.setText("21");
                thePort = "21";

            }

            //check
            if (TextUtils.isEmpty(theUsername)){

                username.requestFocus();
                username.setError("Required");

            } else

            if (TextUtils.isEmpty(thePassword)){

                password.requestFocus();
                password.setError("Required");

            } else

            if (TextUtils.isEmpty(theCompany)){

                companyId.requestFocus();
                companyId.setError("Required");

            } else

            if (TextUtils.isEmpty(theLicence)){

                companyLicence.requestFocus();
                companyLicence.setError("Required");

            } else

            if (TextUtils.isEmpty(selectedFtpHost)){

                Toast.makeText(this, "Selected Domain Doesnt Have FTP Access", Toast.LENGTH_LONG).show();

            } else

            if (TextUtils.isEmpty(selectedMasterDomain)){

                Toast.makeText(this, "Master Domain Not Selected", Toast.LENGTH_LONG).show();

            } else{

                testUser(theUsername, thePassword, thePort, theCompany, theLicence);

            }

        }

    }








    //login and testing
    private void testZipUrl(String theCompany, String theLicence) {

        //start loading
        testButton.setEnabled(false);
        connectButton.setEnabled(false);
        testText.setVisibility(View.GONE);
        testProgress.setVisibility(View.VISIBLE);

        new Thread(() -> {

            try {

                //build the url
                String file_url = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/Zip/App.zip";

                URL url = new URL(file_url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int code = connection.getResponseCode();

                if(code == 200) {

                    this.runOnUiThread(() -> {

                        //stop loading
                        connectButton.setEnabled(true);
                        testButton.setEnabled(true);
                        testProgress.setVisibility(View.GONE);
                        testText.setVisibility(View.VISIBLE);

                        Toast.makeText(this, "Test completed successfully", Toast.LENGTH_LONG).show();

                    });

                } else {

                    this.runOnUiThread(() -> {
                        //stop loading
                        connectButton.setEnabled(true);
                        testButton.setEnabled(true);
                        testProgress.setVisibility(View.GONE);
                        testText.setVisibility(View.VISIBLE);

                        Toast.makeText(this, "URL Path test failed", Toast.LENGTH_LONG).show();
                    });

                }



            } catch (Exception e){

                this.runOnUiThread(() -> {
                    //stop loading
                    connectButton.setEnabled(true);
                    testButton.setEnabled(true);
                    testProgress.setVisibility(View.GONE);
                    testText.setVisibility(View.VISIBLE);

                    Toast.makeText(this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

            }

        }).start();

    }

    private void testUser(String theUsername, String thePassword, String thePort, String theCompany, String theLicence) {

        //start loading
        testButton.setEnabled(false);
        connectButton.setEnabled(false);
        testText.setVisibility(View.GONE);
        testProgress.setVisibility(View.VISIBLE);

        //get port
        int laPort = Integer.parseInt(thePort);

        new Thread(() -> {

            boolean isSuccess = false;
            try {
                isSuccess = ftpclient.ftpConnect(selectedFtpHost, theUsername, thePassword, laPort);

                //check
                if (isSuccess){

                    //initialize ftp client
                    final FTPClient ftp = new FTPClient();

                    //init ftp file list
                    FTPFile[] filesList = null;
                    FTPFile[] testList = null;


                    //FTP Server connection
                    ftp.connect(InetAddress.getByName(selectedFtpHost), laPort);
                    ftp.login(theUsername, thePassword);
                    ftp.enterLocalPassiveMode();

                    //set buffer size and ftp directory
                    ftp.setBufferSize(1024);
                    filesList = ftp.listFiles("/");
                    ftp.setFileType(FTP.BINARY_FILE_TYPE);

                    for (int fl = 0; fl < filesList.length; fl++) {

                        if (!filesList[fl].getName().equals(".") && !filesList[fl].getName().equals("..") && !filesList[fl].getName().endsWith(".zip")) {

                            Log.d("FilesIn", filesList[fl].getName());
                            Log.d("FilesIn", Methods.getTimeFromCalender(this, filesList[fl].getTimestamp()));

                            if (filesList[fl].getName().equals(theCompany)){

                                //change value
                                companyExist = true;


                                testList = ftp.listFiles("/" + theCompany + "/");
                                ftp.setFileType(FTP.BINARY_FILE_TYPE);
                                for (int tl = 0; tl < testList.length; tl++){

                                    if (!testList[tl].getName().equals(".") && !testList[tl].getName().equals("..") && !testList[tl].getName().endsWith(".zip")){

                                        if (testList[tl].getName().equals(theLicence)){

                                            //change value
                                            licenceExist = true;

                                        }

                                    }

                                }

                            }

                        }
                    }

                    if (!companyExist || !licenceExist){

                        this.runOnUiThread(() -> {
                            //stop loading
                            connectButton.setEnabled(true);
                            testButton.setEnabled(true);
                            testProgress.setVisibility(View.GONE);
                            testText.setVisibility(View.VISIBLE);

                            Toast.makeText(this, "FTP Path test failed", Toast.LENGTH_LONG).show();
                        });

                    } else {

                        this.runOnUiThread(() -> {
                            //stop loading
                            connectButton.setEnabled(true);
                            testButton.setEnabled(true);
                            testProgress.setVisibility(View.GONE);
                            testText.setVisibility(View.VISIBLE);

                            Toast.makeText(this, "All test completed successfully", Toast.LENGTH_LONG).show();
                        });

                    }

                    ftp.logout();
                    ftp.disconnect();

                } else {

                    this.runOnUiThread(() -> {
                        //stop loading
                        connectButton.setEnabled(true);
                        testButton.setEnabled(true);
                        testProgress.setVisibility(View.GONE);
                        testText.setVisibility(View.VISIBLE);

                        Toast.makeText(this, "Wrong Credentials", Toast.LENGTH_SHORT).show();
                    });

                }

            } catch (Exception e){

                this.runOnUiThread(() -> {
                    //stop loading
                    connectButton.setEnabled(true);
                    testButton.setEnabled(true);
                    testProgress.setVisibility(View.GONE);
                    testText.setVisibility(View.VISIBLE);

                    Toast.makeText(this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

            }

        }).start();

    }

    private void loginUrlZipUser(String theCompany, String theLicence) {

        //start loading
        testButton.setEnabled(false);
        connectButton.setEnabled(false);
        connectText.setVisibility(View.GONE);
        connectProgress.setVisibility(View.VISIBLE);

        new Thread(() -> {

            try {

                //build the url
                String file_url = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/Zip/App.zip";

                URL url = new URL(file_url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int code = connection.getResponseCode();

                if(code == 200) {

                    this.runOnUiThread(() -> {

                        //stop loading
                        testButton.setEnabled(true);
                        connectButton.setEnabled(true);
                        connectProgress.setVisibility(View.GONE);
                        connectText.setVisibility(View.VISIBLE);

                        //check user
                        checkZipUrlUserData(theCompany, theLicence);

                    });

                } else {

                    this.runOnUiThread(() -> {
                        //stop loading
                        testButton.setEnabled(true);
                        connectButton.setEnabled(true);
                        connectProgress.setVisibility(View.GONE);
                        connectText.setVisibility(View.VISIBLE);

                        Toast.makeText(this, "URL Path test failed", Toast.LENGTH_LONG).show();
                    });

                }



            } catch (Exception e){

                this.runOnUiThread(() -> {
                    //stop loading
                    testButton.setEnabled(true);
                    connectButton.setEnabled(true);
                    connectProgress.setVisibility(View.GONE);
                    connectText.setVisibility(View.VISIBLE);

                    Toast.makeText(this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

            }

        }).start();

    }

    private void loginParseUser(String theCompany, String theLicence) {

        //start loading
        testButton.setEnabled(false);
        connectButton.setEnabled(false);
        connectText.setVisibility(View.GONE);
        connectProgress.setVisibility(View.VISIBLE);

        new Thread(() -> {

            try {

                //build the url
                String file_url = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/index.html";

                URL url = new URL(file_url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int code = connection.getResponseCode();

                if(code == 200) {

                    this.runOnUiThread(() -> {

                        //stop loading
                        testButton.setEnabled(true);
                        connectButton.setEnabled(true);
                        connectProgress.setVisibility(View.GONE);
                        connectText.setVisibility(View.VISIBLE);

                        //check user
                        checkParseUserData(theCompany, theLicence);

                    });

                } else {

                    this.runOnUiThread(() -> {
                        //stop loading
                        testButton.setEnabled(true);
                        connectButton.setEnabled(true);
                        connectProgress.setVisibility(View.GONE);
                        connectText.setVisibility(View.VISIBLE);

                        Toast.makeText(this, "URL Path test failed", Toast.LENGTH_LONG).show();
                    });

                }



            } catch (Exception e){

                this.runOnUiThread(() -> {
                    //stop loading
                    testButton.setEnabled(true);
                    connectButton.setEnabled(true);
                    connectProgress.setVisibility(View.GONE);
                    connectText.setVisibility(View.VISIBLE);

                    Toast.makeText(this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

            }

        }).start();

    }

    private void checkParseUserData(String theCompany, String theLicence) {

        SignIn.this.runOnUiThread(() -> {
            if (!isUserExist(theCompany, theLicence)) {

                new UserDatabase(this).addNewUser(selectedMasterDomain, selectedFtpHost, "Parsing", "null", "21", theCompany, theLicence);

            }

            //write file
            writeParsingFile("Parsing", "null", "21", theCompany, theLicence);
        });

    }

    private void checkZipUrlUserData(String theCompany, String theLicence) {

        SignIn.this.runOnUiThread(() -> {
            if (!isUserExist(theCompany, theLicence)) {

                new UserDatabase(this).addNewUser(selectedMasterDomain, selectedFtpHost, "URLZip", "null", "21", theCompany, theLicence);

            }

            //write file
            writeUrlZipFile("URLZip", "null", "21", theCompany, theLicence);
        });

    }

    private void loginUser(String theUsername, String thePassword, String thePort, String theCompany, String theLicence) {

        //start loading
        connectButton.setEnabled(false);
        connectText.setVisibility(View.GONE);
        connectProgress.setVisibility(View.VISIBLE);

        //get port
        int laPort = Integer.parseInt(thePort);

        new Thread(() -> {

            boolean isSuccess = false;
            try {
                isSuccess = ftpclient.ftpConnect(selectedFtpHost, theUsername, thePassword, laPort);

                //check
                if (isSuccess){

                    this.runOnUiThread(() -> {
                        //stop loading
                        connectButton.setEnabled(true);
                        connectProgress.setVisibility(View.GONE);
                        connectText.setVisibility(View.VISIBLE);

                        Toast.makeText(this, "Logged In", Toast.LENGTH_SHORT).show();
                    });

                    //check user
                    checkUserData(theUsername, thePassword, thePort, theCompany, theLicence);

                } else {

                    this.runOnUiThread(() -> {
                        //stop loading
                        connectButton.setEnabled(true);
                        connectProgress.setVisibility(View.GONE);
                        connectText.setVisibility(View.VISIBLE);

                        Toast.makeText(this, "Error Logging in", Toast.LENGTH_SHORT).show();
                    });



                }

            } catch (Exception e){

                this.runOnUiThread(() -> {
                    //stop loading
                    connectButton.setEnabled(true);
                    connectProgress.setVisibility(View.GONE);
                    connectText.setVisibility(View.VISIBLE);

                    Toast.makeText(this, "Error SN: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

            }

        }).start();

    }

    private void checkUserData(String theUsername, String thePassword, String thePort, String theCompany, String theLicence) {

        SignIn.this.runOnUiThread(() -> {
            if (!isUserExist(theCompany, theLicence)) {

                new UserDatabase(this).addNewUser(selectedMasterDomain, selectedFtpHost, theUsername, thePassword, thePort, theCompany, theLicence);

            }

            //write file
            writeFile(theUsername, thePassword, thePort, theCompany, theLicence);
        });

    }

    public void writeUrlZipFile(String theUsername, String thePassword, String thePort, String theCompany, String theLicence) {

        //write to local
        Paper.book().write(Common.USER_NAME, theUsername);
        Paper.book().write(Common.COMPANY_ID, theCompany);
        Paper.book().write(Common.LICENCE_ID, theLicence);
        Paper.book().write(Common.CURRENT_TEST_MODE, Common.TEST_ONE_OFF);

        //ftp
        Paper.book().write(Common.CURRENT_MASTER_DOMAIN, selectedMasterDomain);
        Paper.book().write(Common.FTP_HOST, selectedFtpHost);
        Paper.book().write(Common.FTP_PORT, thePort);
        Paper.book().write(Common.FTP_USERNAME, theUsername);
        Paper.book().write(Common.FTP_PASSWORD, thePassword);

        //launch type
        if (indicatorSwitch.isChecked()){

            //register selection
            Paper.book().write(Common.LOAD_STYLE, Common.LOAD_FROM_ONLINE);

        } else {

            //register selection
            Paper.book().write(Common.LOAD_STYLE, Common.LOAD_FROM_LOCAL);

        }

        //check if file exist in directory and create if not
        File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
        if (!dir.exists()) {
            dir.mkdir();
        }

        //check sync type
        checkSyncType(theUsername, thePassword, thePort, theCompany, theLicence);
    }

    private void writeParsingFile(String theUsername, String thePassword, String thePort, String theCompany, String theLicence) {

        //write to local
        Paper.book().write(Common.USER_NAME, theUsername);
        Paper.book().write(Common.COMPANY_ID, theCompany);
        Paper.book().write(Common.LICENCE_ID, theLicence);
        Paper.book().write(Common.CURRENT_TEST_MODE, Common.TEST_ONE_OFF);

        //ftp
        Paper.book().write(Common.CURRENT_MASTER_DOMAIN, selectedMasterDomain);
        Paper.book().write(Common.FTP_HOST, selectedFtpHost);
        Paper.book().write(Common.FTP_PORT, thePort);
        Paper.book().write(Common.FTP_USERNAME, theUsername);
        Paper.book().write(Common.FTP_PASSWORD, thePassword);

        //launch type
        if (indicatorSwitch.isChecked()){

            //register selection
            Paper.book().write(Common.LOAD_STYLE, Common.LOAD_FROM_ONLINE);

        } else {

            //register selection
            Paper.book().write(Common.LOAD_STYLE, Common.LOAD_FROM_LOCAL);

        }

        //check if file exist in directory and create if not
        File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
        if (!dir.exists()) {
            dir.mkdir();
        }

        //check sync type
        checkSyncType(theUsername, thePassword, thePort, theCompany, theLicence);

    }

    public void writeFile(String theUsername, String thePassword, String thePort, String theCompany, String theLicence) {

        //write to local
        Paper.book().write(Common.USER_NAME, theUsername);
        Paper.book().write(Common.COMPANY_ID, theCompany);
        Paper.book().write(Common.LICENCE_ID, theLicence);
        Paper.book().write(Common.CURRENT_TEST_MODE, Common.TEST_ONE_OFF);

        //ftp
        Paper.book().write(Common.CURRENT_MASTER_DOMAIN, selectedMasterDomain);
        Paper.book().write(Common.FTP_HOST, selectedFtpHost);
        Paper.book().write(Common.FTP_PORT, thePort);
        Paper.book().write(Common.FTP_USERNAME, theUsername);
        Paper.book().write(Common.FTP_PASSWORD, thePassword);

        //launch type
        if (indicatorSwitch.isChecked()){

            //register selection
            Paper.book().write(Common.LOAD_STYLE, Common.LOAD_FROM_ONLINE);

        } else {

            //register selection
            Paper.book().write(Common.LOAD_STYLE, Common.LOAD_FROM_LOCAL);

        }

        //check if file exist in directory and create if not
        File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
        if (!dir.exists()) {
            dir.mkdir();
        }

        //check sync type
        checkSyncType(theUsername, thePassword, thePort, theCompany, theLicence);
    }

    public void checkSyncType(String theUsername, String thePassword, String thePort, String theCompany, String theLicence){

        switch (selectedSyncType){

            case Common.SYNC_TYPE_FTP_FOLDER:

                //create table
                String licenceStuff = theCompany + "_" + theLicence + "_FOLDER";
                new Database(getBaseContext()).createTable(licenceStuff);

                countFiles(theUsername, thePassword, thePort, theCompany, theLicence);
                break;

            case Common.SYNC_TYPE_FTP_ZIP:

                //create table
                String licence2Stuff = theCompany + "_" + theLicence + "_ZIP";
                new Database(getBaseContext()).createTable(licence2Stuff);

                getZipFiles(theUsername, thePassword, thePort, theCompany, theLicence);
                break;

            case Common.SYNC_TYPE_URL_ZIP:

                //create table
                String licence3Stuff = theCompany + "_" + theLicence + "_URL";
                new Database(getBaseContext()).createTable(licence3Stuff);

                getUrlZipFiles(theUsername, thePassword, thePort, theCompany, theLicence);
                break;

            case Common.SYNC_TYPE_API:

                Toast.makeText(this, "Please Contact Support", Toast.LENGTH_SHORT).show();
                break;

            case Common.SYNC_TYPE_INDEX:

                //create table
                String licenceIndexStuff = theCompany + "_" + theLicence + "_INDEX";
                new Database(getBaseContext()).createTable(licenceIndexStuff);

                countFiles(theUsername, thePassword, thePort, theCompany, theLicence);
                break;

            case Common.SYNC_TYPE_PARSE:

                //create table
                String licenceParseStuff = theCompany + "_" + theLicence + "_PARSING";
                new Database(getBaseContext()).createTable(licenceParseStuff);

                try {
                    //page url
                    String pageUrl = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/index.html";
                    getParsedItems(theCompany, theLicence, pageUrl);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;

        }

    }









    //parsing
    private void getParsedItems(String theCompany, String theLicence, String pageUrl) throws InterruptedException {

        //show loading
        isCancel = false;
        fileCountNum = 0;
        showLoadingDialog();

        //licence
        String licenceParseStuff = theCompany + "_" + theLicence + "_PARSING";

        //define thread
        parseThread = new Thread(() -> {
            try {
                //get file size
                URL mainUrl = new URL(pageUrl);
                HttpURLConnection mainConnection = (HttpURLConnection) mainUrl.openConnection();
                long theMainFileSize = mainConnection.getContentLength();
                String mainFileSize = String.valueOf(theMainFileSize);

                String theIndexFileName = pageUrl.substring( pageUrl.lastIndexOf('/') + 1);
                String indexFileDirectory = FilenameUtils.getPath(pageUrl);
                fillInParseList(licenceParseStuff, theIndexFileName, theIndexFileName, pageUrl, mainFileSize, Common.FILE_STATUS_PENDING, "/Application/");

                Document doc = Jsoup.connect(pageUrl).get();

                //loop through all links
                Elements links = doc.select("link[href]");
                for (Element link: links){

                    if (!link.attr("href").startsWith("#") && !link.attr("href").startsWith("http")) {

                        if (link.attr("href").startsWith("/")){
                            String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application" + link.attr("href");
                            String theFileName = theFullLink.substring( theFullLink.lastIndexOf('/') + 1);
                            String theFilePath = link.attr("href");
                            String fileDirectory = FilenameUtils.getPath(link.attr("href"));

                            //get file size
                            URL url = new URL(theFullLink);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            long theFileSize = connection.getContentLength();
                            String fileSize = String.valueOf(theFileSize);


                            fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, "/Application/" + fileDirectory);
                        } else

                        if (link.attr("href").startsWith("./")){
                            if (link.attr("href").endsWith(".html")) {
                                String theFilePath = link.attr("href");
                                String theFileName = theFilePath.substring(theFilePath.lastIndexOf('/') + 1);
                                String fileDirectory = FilenameUtils.getPath(link.attr("href"));
                                String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/" + removeBaseDir(fileDirectory) + theFileName;


                                //get secondary
                                getSecondaryParsedItems(theCompany, theLicence, theFullLink);

                            } else {
                                String theFilePath = link.attr("href");
                                String theFileName = theFilePath.substring(theFilePath.lastIndexOf('/') + 1);
                                String fileDirectory = FilenameUtils.getPath(link.attr("href"));
                                String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/" + removeBaseDir(fileDirectory) + theFileName;

                                //get file size
                                URL url = new URL(theFullLink);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                long theFileSize = connection.getContentLength();
                                String fileSize = String.valueOf(theFileSize);

                                fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, removeBaseDir(fileDirectory));
                            }
                        } else

                        if (link.attr("href").startsWith("../")){
                            String theFilePath = link.attr("href");
                            String theFileName = theFilePath.substring( theFilePath.lastIndexOf('/') + 1);
                            String fileDirectory = FilenameUtils.getPath(link.attr("href"));
                            String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/" + removeParentDir(fileDirectory) + theFileName;

                            //get file size
                            URL url = new URL(theFullLink);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            long theFileSize = connection.getContentLength();
                            String fileSize = String.valueOf(theFileSize);

                            fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, removeParentDir(fileDirectory));
                        } else {
                            String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/" + link.attr("href");
                            String theFileName = theFullLink.substring( theFullLink.lastIndexOf('/') + 1);
                            String theFilePath = link.attr("href");
                            String fileDirectory = FilenameUtils.getPath(link.attr("href"));

                            //get file size
                            URL url = new URL(theFullLink);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            long theFileSize = connection.getContentLength();
                            String fileSize = String.valueOf(theFileSize);

                            fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, "/Application/" + fileDirectory);
                        }

                    }

                }

                //loop through all srcs
                Elements srcs = doc.select("[src]");
                for (Element src: srcs){

                    if (!src.attr("src").startsWith("#") && !src.attr("src").startsWith("http")) {

                        if (src.attr("src").startsWith("/")){
                            String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application" + src.attr("src");
                            String theFileName = theFullLink.substring( theFullLink.lastIndexOf('/') + 1);
                            String theFilePath = src.attr("src");
                            String fileDirectory = FilenameUtils.getPath(src.attr("src"));

                            //get file size
                            URL url = new URL(theFullLink);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            long theFileSize = connection.getContentLength();
                            String fileSize = String.valueOf(theFileSize);

                            fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, "/Application/" + fileDirectory);
                        } else

                        if (src.attr("src").startsWith("./")){
                            String theFilePath = src.attr("src");
                            String theFileName = theFilePath.substring( theFilePath.lastIndexOf('/') + 1);
                            String fileDirectory = FilenameUtils.getPath(src.attr("src"));
                            String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/" + removeBaseDir(fileDirectory) + theFileName;

                            //get file size
                            URL url = new URL(theFullLink);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            long theFileSize = connection.getContentLength();
                            String fileSize = String.valueOf(theFileSize);

                            fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, removeBaseDir(fileDirectory));
                        } else

                        if (src.attr("src").startsWith("../")){
                            String theFilePath = src.attr("src");
                            String theFileName = theFilePath.substring( theFilePath.lastIndexOf('/') + 1);
                            String fileDirectory = FilenameUtils.getPath(src.attr("src"));
                            String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/" + removeParentDir(fileDirectory) + theFileName;

                            //get file size
                            URL url = new URL(theFullLink);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            long theFileSize = connection.getContentLength();
                            String fileSize = String.valueOf(theFileSize);

                            fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink,  fileSize, Common.FILE_STATUS_PENDING, removeParentDir(fileDirectory));
                        } else {
                            String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/" + src.attr("src");
                            String theFileName = theFullLink.substring( theFullLink.lastIndexOf('/') + 1);
                            String theFilePath = src.attr("src");
                            String fileDirectory = FilenameUtils.getPath(src.attr("src"));

                            //get file size
                            URL url = new URL(theFullLink);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            long theFileSize = connection.getContentLength();
                            String fileSize = String.valueOf(theFileSize);

                            fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, "/Application/" + fileDirectory);
                        }

                    }

                }

                //loop through all backgrounds
                Elements backgrounds = doc.select("[background]");
                for (Element background: backgrounds){

                    if (!background.attr("src").startsWith("#") && !background.attr("src").startsWith("http") && !background.attr("abs:src").contains(":")) {
                        if (background.attr("src").startsWith("/")){
                            String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application" + background.attr("src");
                            String theFileName = theFullLink.substring( theFullLink.lastIndexOf('/') + 1);
                            String theFilePath = background.attr("src");
                            String fileDirectory = FilenameUtils.getPath(background.attr("src"));

                            //get file size
                            URL url = new URL(theFullLink);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            long theFileSize = connection.getContentLength();
                            String fileSize = String.valueOf(theFileSize);

                            fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, "/Application/" + fileDirectory);
                        } else

                        if (background.attr("src").startsWith("./")){
                            String theFilePath = background.attr("src");
                            String theFileName = theFilePath.substring( theFilePath.lastIndexOf('/') + 1);
                            String fileDirectory = FilenameUtils.getPath(background.attr("src"));
                            String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/" + removeBaseDir(fileDirectory) + theFileName;

                            //get file size
                            URL url = new URL(theFullLink);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            long theFileSize = connection.getContentLength();
                            String fileSize = String.valueOf(theFileSize);

                            fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, removeBaseDir(fileDirectory));
                        } else

                        if (background.attr("src").startsWith("../")){
                            String theFilePath = background.attr("src");
                            String theFileName = theFilePath.substring( theFilePath.lastIndexOf('/') + 1);
                            String fileDirectory = FilenameUtils.getPath(background.attr("src"));
                            String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/" + removeParentDir(fileDirectory) + theFileName;

                            //get file size
                            URL url = new URL(theFullLink);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            long theFileSize = connection.getContentLength();
                            String fileSize = String.valueOf(theFileSize);

                            fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, removeParentDir(fileDirectory));
                        } else {
                            String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/" + background.attr("src");
                            String theFileName = theFullLink.substring( theFullLink.lastIndexOf('/') + 1);
                            String theFilePath = background.attr("src");
                            String fileDirectory = FilenameUtils.getPath(background.attr("src"));

                            //get file size
                            URL url = new URL(theFullLink);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            long theFileSize = connection.getContentLength();
                            String fileSize = String.valueOf(theFileSize);

                            fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, "/Application/" + fileDirectory);
                        }
                    }

                }

                //loop through all videos
                Elements as = doc.select("a[href]");
                for (Element a: as){

                    if (!a.attr("href").startsWith("#") && !a.attr("href").startsWith("http") && !a.attr("href").contains(":")) {
                        if (a.attr("href").startsWith("/")){
                            String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application" + a.attr("href");
                            String theFileName = theFullLink.substring( theFullLink.lastIndexOf('/') + 1);
                            String theFilePath = a.attr("href");
                            String fileDirectory = FilenameUtils.getPath(a.attr("href"));

                            //get file size
                            URL url = new URL(theFullLink);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            long theFileSize = connection.getContentLength();
                            String fileSize = String.valueOf(theFileSize);

                            fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, "/Application/" + fileDirectory);
                        } else

                        if (a.attr("href").startsWith("./")){
                            if (a.attr("href").endsWith(".html")) {
                                String theFilePath = a.attr("href");
                                String theFileName = theFilePath.substring(theFilePath.lastIndexOf('/') + 1);
                                String fileDirectory = FilenameUtils.getPath(a.attr("href"));
                                String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/" + removeBaseDir(fileDirectory) + theFileName;


                                //get secondary
                                getSecondaryParsedItems(theCompany, theLicence, theFullLink);


                            } else {
                                String theFilePath = a.attr("href");
                                String theFileName = theFilePath.substring(theFilePath.lastIndexOf('/') + 1);
                                String fileDirectory = FilenameUtils.getPath(a.attr("href"));
                                String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/" + removeBaseDir(fileDirectory) + theFileName;

                                //get file size
                                URL url = new URL(theFullLink);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                long theFileSize = connection.getContentLength();
                                String fileSize = String.valueOf(theFileSize);

                                fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, removeBaseDir(fileDirectory));
                            }
                        } else

                        if (a.attr("href").startsWith("../")){
                            String theFilePath = a.attr("href");
                            String theFileName = theFilePath.substring( theFilePath.lastIndexOf('/') + 1);
                            String fileDirectory = FilenameUtils.getPath(a.attr("href"));
                            String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/" + removeParentDir(fileDirectory) + theFileName;

                            //get file size
                            URL url = new URL(theFullLink);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            long theFileSize = connection.getContentLength();
                            String fileSize = String.valueOf(theFileSize);

                            fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, removeParentDir(fileDirectory));
                        } else {
                            String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/" + a.attr("href");
                            String theFileName = theFullLink.substring( theFullLink.lastIndexOf('/') + 1);
                            String theFilePath = a.attr("href");
                            String fileDirectory = FilenameUtils.getPath(a.attr("href"));

                            //get file size
                            URL url = new URL(theFullLink);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            long theFileSize = connection.getContentLength();
                            String fileSize = String.valueOf(theFileSize);

                            fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, "/Application/" + fileDirectory);
                        }
                    }

                }

                //hide loading
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }

                if (!isCancel) {

                    //go to sync manager
                    Intent syncIntent = new Intent(SignIn.this, SyncActivity.class);
                    syncIntent.putExtra(Common.LICENCED_DB_INTENT, licenceParseStuff);
                    syncIntent.putExtra(Common.SYNC_TYPE_INTENT, selectedSyncType);
                    startActivity(syncIntent);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        //start thread
        parseThread.start();

    }

    private void getSecondaryParsedItems(String theCompany, String theLicence, String pageUrl) throws InterruptedException {

        //licence
        String licenceParseStuff = theCompany + "_" + theLicence + "_PARSING";

        try {

            //get file size
            URL mainUrl = new URL(pageUrl);
            HttpURLConnection mainConnection = (HttpURLConnection) mainUrl.openConnection();
            long theMainFileSize = mainConnection.getContentLength();
            String mainFileSize = String.valueOf(theMainFileSize);

            String theIndexFileName = pageUrl.substring( pageUrl.lastIndexOf('/') + 1);
            String indexFileDirectory = FilenameUtils.getPath(pageUrl);
            fillInParseList(licenceParseStuff, theIndexFileName, theIndexFileName, pageUrl, mainFileSize, Common.FILE_STATUS_PENDING, "/Application/");

            Document doc = Jsoup.connect(pageUrl).get();

            //loop through all links
            Elements links = doc.select("link[href]");
            for (Element link: links){

                if (!link.attr("href").startsWith("#") && !link.attr("href").startsWith("http")) {

                    if (link.attr("href").startsWith("/")){
                        String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application" + link.attr("href");
                        String theFileName = theFullLink.substring( theFullLink.lastIndexOf('/') + 1);
                        String theFilePath = link.attr("href");
                        String fileDirectory = FilenameUtils.getPath(link.attr("href"));

                        //get file size
                        URL url = new URL(theFullLink);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        long theFileSize = connection.getContentLength();
                        String fileSize = String.valueOf(theFileSize);

                        fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, "/Application/" + fileDirectory);
                    } else

                    if (link.attr("href").startsWith("./")){
                        String theFilePath = link.attr("href");
                        String theFileName = theFilePath.substring( theFilePath.lastIndexOf('/') + 1);
                        String fileDirectory = FilenameUtils.getPath(link.attr("href"));
                        String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/" + removeBaseDir(fileDirectory) + theFileName;

                        //get file size
                        URL url = new URL(theFullLink);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        long theFileSize = connection.getContentLength();
                        String fileSize = String.valueOf(theFileSize);

                        fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, removeBaseDir(fileDirectory));
                    } else

                    if (link.attr("href").startsWith("../")){
                        String theFilePath = link.attr("href");
                        String theFileName = theFilePath.substring( theFilePath.lastIndexOf('/') + 1);
                        String fileDirectory = FilenameUtils.getPath(link.attr("href"));
                        String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/" + removeParentDir(fileDirectory) + theFileName;

                        //get file size
                        URL url = new URL(theFullLink);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        long theFileSize = connection.getContentLength();
                        String fileSize = String.valueOf(theFileSize);

                        fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, removeParentDir(fileDirectory));
                    } else {
                        String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/" + link.attr("href");
                        String theFileName = theFullLink.substring( theFullLink.lastIndexOf('/') + 1);
                        String theFilePath = link.attr("href");
                        String fileDirectory = FilenameUtils.getPath(link.attr("href"));

                        //get file size
                        URL url = new URL(theFullLink);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        long theFileSize = connection.getContentLength();
                        String fileSize = String.valueOf(theFileSize);

                        fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, "/Application/" + fileDirectory);
                    }

                }

            }

            //loop through all srcs
            Elements srcs = doc.select("[src]");
            for (Element src: srcs){

                if (!src.attr("src").startsWith("#") && !src.attr("src").startsWith("http")) {

                    if (src.attr("src").startsWith("/")){
                        String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application" + src.attr("src");
                        String theFileName = theFullLink.substring( theFullLink.lastIndexOf('/') + 1);
                        String theFilePath = src.attr("src");
                        String fileDirectory = FilenameUtils.getPath(src.attr("src"));

                        //get file size
                        URL url = new URL(theFullLink);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        long theFileSize = connection.getContentLength();
                        String fileSize = String.valueOf(theFileSize);

                        fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, "/Application/" + fileDirectory);
                    } else

                    if (src.attr("src").startsWith("./")){
                        String theFilePath = src.attr("src");
                        String theFileName = theFilePath.substring( theFilePath.lastIndexOf('/') + 1);
                        String fileDirectory = FilenameUtils.getPath(src.attr("src"));
                        String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/" + removeBaseDir(fileDirectory) + theFileName;

                        //get file size
                        URL url = new URL(theFullLink);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        long theFileSize = connection.getContentLength();
                        String fileSize = String.valueOf(theFileSize);

                        fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, removeBaseDir(fileDirectory));
                    } else

                    if (src.attr("src").startsWith("../")){
                        String theFilePath = src.attr("src");
                        String theFileName = theFilePath.substring( theFilePath.lastIndexOf('/') + 1);
                        String fileDirectory = FilenameUtils.getPath(src.attr("src"));
                        String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/" + removeParentDir(fileDirectory) + theFileName;

                        //get file size
                        URL url = new URL(theFullLink);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        long theFileSize = connection.getContentLength();
                        String fileSize = String.valueOf(theFileSize);

                        fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, removeParentDir(fileDirectory));
                    } else {
                        String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/" + src.attr("src");
                        String theFileName = theFullLink.substring( theFullLink.lastIndexOf('/') + 1);
                        String theFilePath = src.attr("src");
                        String fileDirectory = FilenameUtils.getPath(src.attr("src"));

                        //get file size
                        URL url = new URL(theFullLink);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        long theFileSize = connection.getContentLength();
                        String fileSize = String.valueOf(theFileSize);

                        fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, "/Application/" + fileDirectory);
                    }

                }

            }

            //loop through all backgrounds
            Elements backgrounds = doc.select("[background]");
            for (Element background: backgrounds){

                if (!background.attr("src").startsWith("#") && !background.attr("src").startsWith("http") && !background.attr("abs:src").contains(":")) {
                    if (background.attr("src").startsWith("/")){
                        String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application" + background.attr("src");
                        String theFileName = theFullLink.substring( theFullLink.lastIndexOf('/') + 1);
                        String theFilePath = background.attr("src");
                        String fileDirectory = FilenameUtils.getPath(background.attr("src"));

                        //get file size
                        URL url = new URL(theFullLink);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        long theFileSize = connection.getContentLength();
                        String fileSize = String.valueOf(theFileSize);

                        fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, "/Application/" + fileDirectory);
                    } else

                    if (background.attr("src").startsWith("./")){
                        String theFilePath = background.attr("src");
                        String theFileName = theFilePath.substring( theFilePath.lastIndexOf('/') + 1);
                        String fileDirectory = FilenameUtils.getPath(background.attr("src"));
                        String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/" + removeBaseDir(fileDirectory) + theFileName;

                        //get file size
                        URL url = new URL(theFullLink);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        long theFileSize = connection.getContentLength();
                        String fileSize = String.valueOf(theFileSize);

                        fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, removeBaseDir(fileDirectory));
                    } else

                    if (background.attr("src").startsWith("../")){
                        String theFilePath = background.attr("src");
                        String theFileName = theFilePath.substring( theFilePath.lastIndexOf('/') + 1);
                        String fileDirectory = FilenameUtils.getPath(background.attr("src"));
                        String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/" + removeParentDir(fileDirectory) + theFileName;

                        //get file size
                        URL url = new URL(theFullLink);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        long theFileSize = connection.getContentLength();
                        String fileSize = String.valueOf(theFileSize);

                        fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, removeParentDir(fileDirectory));
                    } else {
                        String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/" + background.attr("src");
                        String theFileName = theFullLink.substring( theFullLink.lastIndexOf('/') + 1);
                        String theFilePath = background.attr("src");
                        String fileDirectory = FilenameUtils.getPath(background.attr("src"));

                        //get file size
                        URL url = new URL(theFullLink);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        long theFileSize = connection.getContentLength();
                        String fileSize = String.valueOf(theFileSize);

                        fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, "/Application/" + fileDirectory);
                    }
                }

            }

            //loop through all videos
            Elements as = doc.select("a[href]");
            for (Element a: as){

                if (!a.attr("href").startsWith("#") && !a.attr("href").startsWith("http") && !a.attr("href").contains(":")) {
                    if (a.attr("href").startsWith("/")){
                        String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application" + a.attr("href");
                        String theFileName = theFullLink.substring( theFullLink.lastIndexOf('/') + 1);
                        String theFilePath = a.attr("href");
                        String fileDirectory = FilenameUtils.getPath(a.attr("href"));

                        //get file size
                        URL url = new URL(theFullLink);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        long theFileSize = connection.getContentLength();
                        String fileSize = String.valueOf(theFileSize);

                        fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, "/Application/" + fileDirectory);
                    } else

                    if (a.attr("href").startsWith("./")){
                        String theFilePath = a.attr("href");
                        String theFileName = theFilePath.substring(theFilePath.lastIndexOf('/') + 1);
                        String fileDirectory = FilenameUtils.getPath(a.attr("href"));
                        String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/" + removeBaseDir(fileDirectory) + theFileName;

                        //get file size
                        URL url = new URL(theFullLink);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        long theFileSize = connection.getContentLength();
                        String fileSize = String.valueOf(theFileSize);

                        fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, removeBaseDir(fileDirectory));
                    } else

                    if (a.attr("href").startsWith("../")){
                        String theFilePath = a.attr("href");
                        String theFileName = theFilePath.substring( theFilePath.lastIndexOf('/') + 1);
                        String fileDirectory = FilenameUtils.getPath(a.attr("href"));
                        String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/" + removeParentDir(fileDirectory) + theFileName;

                        //get file size
                        URL url = new URL(theFullLink);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        long theFileSize = connection.getContentLength();
                        String fileSize = String.valueOf(theFileSize);

                        fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, removeParentDir(fileDirectory));
                    } else {
                        String theFullLink = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/" + a.attr("href");
                        String theFileName = theFullLink.substring( theFullLink.lastIndexOf('/') + 1);
                        String theFilePath = a.attr("href");
                        String fileDirectory = FilenameUtils.getPath(a.attr("href"));

                        //get file size
                        URL url = new URL(theFullLink);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        long theFileSize = connection.getContentLength();
                        String fileSize = String.valueOf(theFileSize);

                        fillInParseList(licenceParseStuff, theFilePath, theFileName, theFullLink, fileSize, Common.FILE_STATUS_PENDING, "/Application/" + fileDirectory);
                    }
                }

            }

            //Log.d("Parsed", "Parse Links" + theList.toString() + "  The length: " + theList.size());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String removeBaseDir(String theString){

       return theString.substring(2);

    }

    private String removeParentDir(String theString){

        return theString.substring(3);

    }

    private void fillInParseList(String licenceStuff, String filePath, String fileName, String downloadUrl, String timeFromCalender, String fileStatusPending, String localDir) {

        //check file
        boolean isExist = new Database(getBaseContext()).fileExists(licenceStuff, filePath, fileName);

        if (isExist){
            //if file exists in database, update it
            new Database(getBaseContext()).updateFileToHistory(licenceStuff, filePath, fileName, timeFromCalender, fileStatusPending);



        } else {
            //if file doesnt exist, add file to database
            new Database(getBaseContext()).addFileToHistory(licenceStuff, filePath, localDir, fileName, downloadUrl, timeFromCalender, fileStatusPending);

            this.runOnUiThread(() -> {

                fileCountNum++;
                fileCount.setText("File count: " + fileCountNum);

            });

        }

    }








    //count all files for folder and index
    private void countFiles(String theUsername, String thePassword, String thePort, String theCompany, String theLicence) {

        //show loading
        isCancel = false;
        fileCountNum = 0;
        showLoadingDialog();

        //get port
        int laPort = Integer.parseInt(thePort);


        new Thread(() -> {

            try {
                final FTPClient ftp = new FTPClient();

                //init ftp file list
                FTPFile[] filesList = null;

                //FTP Server connection
                ftp.connect(InetAddress.getByName(selectedFtpHost), laPort);
                ftp.login(theUsername, thePassword);
                ftp.enterLocalPassiveMode();

                //set buffer size and ftp directory
                ftp.setBufferSize(1024);
                filesList = ftp.listFiles("/" + theCompany + "/" + theLicence + "/App/");
                ftp.setFileType(FTP.BINARY_FILE_TYPE);

                while (cl < filesList.length) {

                    if (!filesList[cl].getName().equals(".") && !filesList[cl].getName().equals("..")) {

                        Log.d("FilesIn", filesList[cl].getName());
                        Log.d("FilesIn", Methods.getTimeFromCalender(this, filesList[cl].getTimestamp()));

                        if (filesList[cl].isDirectory()){

                            String filePath = theCompany + "/" + theLicence + "/App/" + filesList[cl].getName();
                            String saveDirToPath = "/" + filesList[cl].getName();

                            //sort folder
                            countFilesInDirectory(ftp, filePath, "", saveDirToPath, theUsername, thePassword, theCompany, theLicence);

                        } else {

                            //create download link
                            String filePath = theCompany + "/" + theLicence + "/App/" + filesList[cl].getName();
                            String fileName = filesList[cl].getName();

                            //licence
                            String licenceStuff = null;
                            if (selectedSyncType.equals(Common.SYNC_TYPE_FTP_FOLDER)) {
                                licenceStuff = theCompany + "_" + theLicence + "_FOLDER";
                            } else

                            if (selectedSyncType.equals(Common.SYNC_TYPE_INDEX)) {
                                licenceStuff = theCompany + "_" + theLicence + "_INDEX";
                            }

                            //generate url
                            String downloadUrl = selectedMasterDomain + "/" + filePath;

                            //update file list
                            fillInFileList(licenceStuff, filePath, fileName, downloadUrl, Methods.getTimeFromCalender(this, filesList[cl].getTimestamp()), Common.FILE_STATUS_PENDING, "");


                        }

                    }
                    cl++;
                }

                SignIn.this.runOnUiThread(() -> {

                    //hide loading
                    if (alertDialog != null && alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }

                    if (!isCancel) {

                        //licence
                        String licenceStuff = null;
                        if (selectedSyncType.equals(Common.SYNC_TYPE_FTP_FOLDER)) {
                            licenceStuff = theCompany + "_" + theLicence + "_FOLDER";
                        } else

                        if (selectedSyncType.equals(Common.SYNC_TYPE_INDEX)) {
                            licenceStuff = theCompany + "_" + theLicence + "_INDEX";
                        }

                        //go to sync manager
                        Intent syncIntent = new Intent(SignIn.this, SyncActivity.class);
                        syncIntent.putExtra(Common.LICENCED_DB_INTENT, licenceStuff);
                        syncIntent.putExtra(Common.SYNC_TYPE_INTENT, selectedSyncType);
                        startActivity(syncIntent);
                    }

                });


                ftp.logout();
                ftp.disconnect();

            } catch (Exception e) {

                SignIn.this.runOnUiThread(() -> {

                    //error
                    Toast.makeText(this, "Error " + e.getMessage(), Toast.LENGTH_LONG).show();

                });

                e.printStackTrace();
            }

        }).start();

    }

    private void countFilesInDirectory(FTPClient ftpClient, String parentDir, String currentDir, String saveDir, String theUsername, String thePassword, String theCompany, String theLicence) {

        try {
            String dirToList = parentDir;
            if (!currentDir.equals("")) {
                dirToList += "/" + currentDir;
            }

            FTPFile[] subFiles = ftpClient.listFiles(dirToList);

            if (subFiles != null && subFiles.length > 0) {
                for (FTPFile aFile : subFiles) {
                    String currentFileName = aFile.getName();
                    if (currentFileName.equals(".") || currentFileName.equals("..")) {
                        // skip parent directory and the directory itself
                        continue;
                    }
                    String filePath = parentDir + "/" + currentDir + "/"
                            + currentFileName;
                    if (currentDir.equals("")) {
                        filePath = parentDir + "/" + currentFileName;
                    }

                    String newDirPath = saveDir + parentDir + File.separator
                            + currentDir + File.separator + currentFileName;
                    if (currentDir.equals("")) {
                        newDirPath = saveDir + parentDir + File.separator
                                + currentFileName;
                    }

                    if (aFile.isDirectory()) {
                        // create the directory in saveDir
                        File baseDir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
                        File baseLicenceDir = new File(baseDir.getAbsolutePath(), Common.LICENCED_FOLDER_NAME);
                        File licenceDir = new File(baseLicenceDir.getAbsolutePath(), theCompany + "-" + theLicence);
                        File theDir = new File(licenceDir.getAbsolutePath(), saveDir);
                        boolean created = theDir.mkdirs();
                        if (created) {
                            System.out.println("CREATED the directory: " + theDir);
                        } else {
                            System.out.println("COULD NOT create the directory: " + theDir);
                        }

                        //new directory
                        String newDirOnLocal = saveDir + "/" + aFile.getName();
                        String newDirToList = aFile.getName();

                        // count files in sub directory
                        countFilesInDirectory(ftpClient, dirToList, newDirToList,
                                newDirOnLocal, theUsername, thePassword, theCompany, theLicence);
                    } else {

                        //create ftp download
                        String fileName = aFile.getName();

                        String licenceStuff = null;
                        if (selectedSyncType.equals(Common.SYNC_TYPE_FTP_FOLDER)) {
                            licenceStuff = theCompany + "_" + theLicence + "_FOLDER";
                        } else

                        if (selectedSyncType.equals(Common.SYNC_TYPE_INDEX)) {
                            licenceStuff = theCompany + "_" + theLicence + "_INDEX";
                        }

                        //generate url
                        String downloadUrl = selectedMasterDomain + "/" + filePath;

                        //update file list
                        fillInFileList(licenceStuff, filePath, fileName, downloadUrl, Methods.getTimeFromCalender(this, aFile.getTimestamp()), Common.FILE_STATUS_PENDING, saveDir);

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

            SignIn.this.runOnUiThread(() -> {
                Toast.makeText(this, "Folder error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }

    }

    private void fillInFileList(String licenceStuff, String filePath, String fileName, String downloadUrl, String timeFromCalender, String fileStatusPending, String localDir) {

        //check file
        boolean isExist = new Database(getBaseContext()).fileExists(licenceStuff, filePath, fileName);

        if (isExist){
            //if file exists in database, update it
            new Database(getBaseContext()).updateFileToHistory(licenceStuff, filePath, fileName, timeFromCalender, fileStatusPending);

            this.runOnUiThread(() -> {
                if (!isCancel){
                    fileCountNum++;
                    fileCount.setText("File count: " + fileCountNum);
                }

            });

        } else {
            //if file doesnt exist, add file to database
            Log.d("TheDir", localDir);
            new Database(getBaseContext()).addFileToHistory(licenceStuff, filePath, localDir, fileName, downloadUrl, timeFromCalender, fileStatusPending);

            if (!isCancel){
                fileCountNum++;
                fileCount.setText("File count: " + fileCountNum);
            }
        }

    }









    //get zip file
    private void getZipFiles(String theUsername, String thePassword, String thePort, String theCompany, String theLicence) {

        //show loading
        isCancel = false;
        fileCountNum = 0;
        showLoadingDialog();

        //get port
        int laPort = Integer.parseInt(thePort);

        new Thread(() -> {

            try {
                final FTPClient ftp = new FTPClient();

                //init directory for saving zip
                File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME + "/" + Common.LICENCE_FOLDER_DOWNLOAD_NAME);

                //check if directory exist
                if (!dir.exists()) {
                    dir.mkdir();
                }

                //init ftp file list
                FTPFile[] filesList = null;

                //FTP Server connection
                ftp.connect(InetAddress.getByName(selectedFtpHost), laPort);
                ftp.login(theUsername, thePassword);
                ftp.enterLocalPassiveMode();

                //set buffer size and ftp directory
                ftp.setBufferSize(1024);
                filesList = ftp.listFiles( "/" + theCompany + "/" + theLicence + "/Zip/");
                ftp.setFileType(FTP.BINARY_FILE_TYPE);

                while (fl < filesList.length) {

                    if (!filesList[fl].getName().equals(".") && !filesList[fl].getName().equals("..")) {

                        if (filesList[fl].getName().endsWith(".zip")){

                            //create ftp download
                            String ftpUrl = "ftp://%s:%s@%s/%s";
                            String host = selectedFtpHost;
                            String user = theUsername;
                            String pass = thePassword;
                            String filePath = theCompany + "/" + theLicence + "/Zip/" + filesList[fl].getName();
                            String fileName = filesList[fl].getName();
                            String theDetailPath = theCompany + "/" + theLicence + "/Zip/" + fileName;

                            ftpUrl = String.format(ftpUrl, user, pass, host, filePath);

                            //licence
                            String licenceStuff = theCompany + "_" + theLicence + "_ZIP";

                            //generate url
                            String downloadUrl = selectedMasterDomain + "/" + filePath;

                            //update file list
                            fillInZipList(licenceStuff, filePath, fileName, downloadUrl, Methods.getTimeFromCalender(this, filesList[fl].getTimestamp()), Common.FILE_STATUS_PENDING, Common.LICENCE_FOLDER_DOWNLOAD_NAME);

                        }

                    }
                    fl++;
                }

                SignIn.this.runOnUiThread(() -> {

                    //hide loading
                    if (alertDialog != null && alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }

                    if (!isCancel) {

                        //licence
                        String licenceStuff = theCompany + "_" + theLicence + "_ZIP";

                        //go to sync manager
                        Intent syncIntent = new Intent(SignIn.this, SyncActivity.class);
                        syncIntent.putExtra(Common.LICENCED_DB_INTENT, licenceStuff);
                        syncIntent.putExtra(Common.SYNC_TYPE_INTENT, selectedSyncType);
                        startActivity(syncIntent);

                    } else {

                    }

                });

                ftp.logout();
                ftp.disconnect();
            } catch (Exception e) {

                SignIn.this.runOnUiThread(() -> {

                    //error
                    Toast.makeText(this, "Error " + e.getMessage(), Toast.LENGTH_LONG).show();

                });

                e.printStackTrace();
            }

        }).start();

    }

    private void getUrlZipFiles(String theUsername, String thePassword, String thePort, String theCompany, String theLicence) {

        //show loading
        isCancel = false;
        fileCountNum = 0;
        showLoadingDialog();

        new Thread(() -> {

            try {
                //build the url
                String file_url = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/Zip/App.zip";

                URL url = new URL(file_url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int code = connection.getResponseCode();

                if(code == 200) {

                    this.runOnUiThread(() -> {

                        //stop loading
                        alertDialog.dismiss();

                        try {
                            connection.getInputStream();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        long theFileSize = connection.getContentLength();

                        //licence
                        String licenceStuff = theCompany + "_" + theLicence + "_URL";

                        String filePath = theCompany + "/" + theLicence + "/Zip/App.zip";
                        String fileName = "App.zip";

                        //update file list
                        fillInZipList(licenceStuff, filePath, fileName, file_url, String.valueOf(theFileSize), Common.FILE_STATUS_PENDING, Common.LICENCE_FOLDER_DOWNLOAD_NAME);

                        if (!isCancel) {
                            //go to sync manager
                            Intent syncIntent = new Intent(SignIn.this, SyncActivity.class);
                            syncIntent.putExtra(Common.LICENCED_DB_INTENT, licenceStuff);
                            syncIntent.putExtra(Common.SYNC_TYPE_INTENT, selectedSyncType);
                            startActivity(syncIntent);
                        }

                    });

                } else {

                    this.runOnUiThread(() -> {

                        //stop loading
                        loadingLayout.setVisibility(View.GONE);

                        //Toast
                        Toast.makeText(this, "404 File non fount", Toast.LENGTH_SHORT).show();

                    });

                }
            } catch (Exception jayWalk){

                jayWalk.printStackTrace();

            }

        }).start();

    }

    private void fillInZipList(String licenceStuff, String filePath, String fileName, String downloadUrl, String timeFromCalender, String fileStatusPending, String localDir) {

        //check file
        boolean isExist = new Database(getBaseContext()).fileExists(licenceStuff, filePath, fileName);

        if (isExist){
            //if file exists in database, update it
            new Database(getBaseContext()).updateFileToHistory(licenceStuff, filePath, fileName, timeFromCalender, fileStatusPending);

            fileCountNum++;
            fileCount.setText("File count: " + fileCountNum);

        } else {
            //if file doesnt exist, add file to database
            new Database(getBaseContext()).addFileToHistory(licenceStuff, filePath, localDir, fileName, downloadUrl, timeFromCalender, fileStatusPending);

            fileCountNum++;
            fileCount.setText("File count: " + fileCountNum);
        }

    }






    //check user
    private boolean isUserExist(String companyId, String licenceKey) {

        return new UserDatabase(this).userExists(companyId, licenceKey);

    }





    //default domain stuff
    private void downloadDefaultDomainFile() {

        //show loading
        loadingLayout.setVisibility(View.VISIBLE);

        //dir
        File baseDir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
        File theDir = new File(baseDir.getAbsolutePath(), "/" + Common.SPLASH_FOLDER_NAME);

        //build url
        String theUrl = "http://cloudappsync.com/cloudappsync/admin/" + Common.DEFAULT_DOMAIN_FILE;

        if (Paper.book().read(Common.MASTER_DOMAINS) == null) {

            //new download
            PRDownloader.download(theUrl, theDir.getAbsolutePath(), Common.DEFAULT_DOMAIN_FILE)
                    .build()
                    .setOnStartOrResumeListener(() -> {

                    })

                    .setOnPauseListener(() -> {

                    })

                    .setOnCancelListener(() -> {

                    })

                    .setOnProgressListener(progress -> {})

                    .start(new OnDownloadListener() {
                        @Override
                        public void onDownloadComplete() {

                            //hide loading
                            loadingLayout.setVisibility(View.GONE);

                            //read file
                            readFileAndPopulateDomains();

                        }

                        @Override
                        public void onError(Error error) {

                            //hide loading
                            loadingLayout.setVisibility(View.GONE);

                            //error
                            Toast.makeText(SignIn.this, "Error occurred while downloading default settings. Please try again", Toast.LENGTH_SHORT).show();

                        }
                    });

        } else {

            //show loading
            loadingLayout.setVisibility(View.GONE);

            //populate with local
            populateDomainSpinner();

        }

    }

    private void populateDomainSpinner() {

        //list
        final List<String> domainList = new ArrayList<>();
        domainList.add(0, "Master Domains");

        //list
        final List<Domains> domainDataList = new ArrayList<>();
        domainDataList.add(0, new Domains("", "", "", "", ""));

        //adapter
        final ArrayAdapter<String> dataAdapterDomains;
        dataAdapterDomains = new ArrayAdapter(this, R.layout.custom_spinner_item, domainList);
        dataAdapterDomains.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //populate
        List<Domains> savedDomains = new ArrayList<>();
        savedDomains = Paper.book().read(Common.MASTER_DOMAINS);
        for (int p = 1; p < savedDomains.size(); p++){

            domainList.add(savedDomains.get(p).getName());
            domainDataList.add(savedDomains.get(p));

        }

        //set adapter
        domainSpinner.setAdapter(dataAdapterDomains);
        dataAdapterDomains.notifyDataSetChanged();

        //update ui
        loadingLayout.setVisibility(View.GONE);
        domainLayout.setVisibility(View.VISIBLE);

        //selector
        domainSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).toString().equals("Master Domains")) {

                    if (parent.getItemAtPosition(position).toString().equals("Demo")){

                        //update texts
                        username.setText(Common.DEMO_USERNAME);
                        password.setText(Common.DEMO_PASSWORD);
                        companyId.setText(Common.DEMO_COMPANY);
                        companyLicence.setText(Common.DEMO_LICENCE);

                        //update values

                    } else {

                        //update texts
                        username.setText("");
                        password.setText("");
                        companyId.setText("");
                        companyLicence.setText("");

                        //update values

                    }
                    selectedMasterDomain = domainDataList.get(position).getWeb_domain();
                    selectedFtpHost = domainDataList.get(position).getFtp_host();
                    port.setText(domainDataList.get(position).getFtp_port());

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void readFileAndPopulateDomains() {

        //list
        final List<String> domainList = new ArrayList<>();
        domainList.add(0, "Master Domains");

        //list
        final List<Domains> domainDataList = new ArrayList<>();
        domainDataList.add(0, new Domains("", "", "", "", ""));

        //adapter
        final ArrayAdapter<String> dataAdapterDomains;
        dataAdapterDomains = new ArrayAdapter(this, R.layout.custom_spinner_item, domainList);
        dataAdapterDomains.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //check if file exist in directory and populate spinner
        File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME + "/" + Common.SPLASH_FOLDER_NAME);
        if (dir.exists()) {

            //set file to use
            newFile = new File(dir, Common.DEFAULT_DOMAIN_FILE);

            //read csv
            try {
                CSVReader reader = new CSVReader(new FileReader(newFile));
                String[] nextLine;
                int count = 0;

                //read off first
                reader.readNext();

                while ((nextLine = reader.readNext()) != null) {

                    //populate spinner and background data
                    domainList.add(nextLine[1]);
                    domainDataList.add(new Domains(nextLine[0], nextLine[1], nextLine[2], nextLine[3], nextLine[4]));

                    // nextLine[] is an array of values from the line
                    count++;
                }

            } catch (IOException e) {
                Toast.makeText(this, "CSV Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {

                //delete default domain
                newFile.delete();
            }

        } else {

            //create dir
            dir.mkdir();

            Toast.makeText(this, "Domain File Download Failed", Toast.LENGTH_SHORT).show();

        }

        //save list
        Paper.book().write(Common.MASTER_DOMAINS, domainDataList);

        //set adapter
        domainSpinner.setAdapter(dataAdapterDomains);
        dataAdapterDomains.notifyDataSetChanged();

        //update ui
        loadingLayout.setVisibility(View.GONE);
        domainLayout.setVisibility(View.VISIBLE);

        //selector
        domainSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).toString().equals("Master Domains")) {

                    if (parent.getItemAtPosition(position).toString().equals("Demo")){

                        //update texts
                        username.setText(Common.DEMO_USERNAME);
                        password.setText(Common.DEMO_PASSWORD);
                        companyId.setText(Common.DEMO_COMPANY);
                        companyLicence.setText(Common.DEMO_LICENCE);

                        //update values

                    } else {

                        //update texts
                        username.setText("");
                        password.setText("");
                        companyId.setText("");
                        companyLicence.setText("");

                        //update values

                    }
                    selectedMasterDomain = domainDataList.get(position).getWeb_domain();
                    selectedFtpHost = domainDataList.get(position).getFtp_host();
                    port.setText(domainDataList.get(position).getFtp_port());

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }




    //show loading
    public void showLoadingDialog(){

        //start syncing
        isLoading = true;

        //create dialog
        alertDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.file_count_dialog, null);

        //widgets
        fileCount = viewOptions.findViewById(R.id.fileCount);
        cancelBtn = viewOptions.findViewById(R.id.cancelBtn);

        //dialog props
        alertDialog.setView(viewOptions);
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //lock
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        //listener
        alertDialog.setOnDismissListener(dialog -> isLoading = false);
        alertDialog.setOnCancelListener(dialog -> isLoading = false);

        //cancel
        cancelBtn.setOnClickListener(v -> {

            isCancel = true;
            fileCountNum = 0;

            //alert dialog dismiss
            alertDialog.dismiss();

            //clear local
            Paper.book().delete(Common.USER_NAME);
            Paper.book().delete(Common.COMPANY_ID);
            Paper.book().delete(Common.LICENCE_ID);
            Paper.book().delete(Common.CURRENT_TEST_MODE);
            Paper.book().delete(Common.CURRENT_MASTER_DOMAIN);
            Paper.book().delete(Common.FTP_HOST);
            Paper.book().delete(Common.FTP_PORT);
            Paper.book().delete(Common.FTP_USERNAME);
            Paper.book().delete(Common.FTP_PASSWORD);

            Intent intent = getIntent();
            overridePendingTransition(0, 0);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);

        });

        //show dialog
        alertDialog.show();

    }

}