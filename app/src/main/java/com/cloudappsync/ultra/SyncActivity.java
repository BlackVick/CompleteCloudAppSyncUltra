package com.cloudappsync.ultra;

import android.os.Handler;
import android.os.Looper;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.cloudappsync.ultra.Adapters.SyncAdapter;
import com.cloudappsync.ultra.Basic.BasicWebActivity;
import com.cloudappsync.ultra.Interface.DownloadHelper;
import com.cloudappsync.ultra.Models.FileHistory;
import com.cloudappsync.ultra.Ultra.SignIn;
import com.cloudappsync.ultra.Ultra.WebActivity;
import com.cloudappsync.ultra.Utilities.Common;
import com.cloudappsync.ultra.Utilities.Database;
import com.cloudappsync.ultra.Utilities.DownloadFromUrl;
import com.cloudappsync.ultra.Utilities.ZipManager;
import com.wwdablu.soumya.wzip.WZip;
import com.wwdablu.soumya.wzip.WZipCallback;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import io.paperdb.Paper;

public class SyncActivity extends AppCompatActivity {

    //widgets
    private ImageView backButton;
    private RecyclerView fileRecycler;
    private TextView cancelBtn, retryBtn, launchBtn, fileCountProgress, fileCountProgressPercent;
    private ProgressBar fileDownloadProgress;
    private TextView fileProgress;
    private RelativeLayout loadingOverlay;
    private TextView loadingText;

    //data
    private List<FileHistory> fileList;
    private List<FileHistory> processedList;
    private List<FileHistory> processedRetryList;
    private SyncAdapter adapter;
    private LinearLayoutManager layoutManager;

    //cancel dialog
    private android.app.AlertDialog alertDialog;
    private boolean isCanceling = false;
    private boolean isRetrying = false;
    private boolean isIncomplete = false;

    //static values
    public static final int PERMISSION_REQUEST_CODE = 234;

    //values
    public String syncDbLicence = "";
    public String syncType = "";
    public int downloadedFiles = 0;
    private String companyId;
    private String licenceKey;

    //timer
    private Timer timer;

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

        setContentView(R.layout.activity_sync);

        //intent data
        syncDbLicence = getIntent().getStringExtra(Common.LICENCED_DB_INTENT);
        syncType = getIntent().getStringExtra(Common.SYNC_TYPE_INTENT);

        //values
        companyId = Paper.book().read(Common.COMPANY_ID);
        licenceKey = Paper.book().read(Common.LICENCE_ID);

        //save current licence
        Paper.book().write(Common.CURRENT_DB_LICENCE, syncDbLicence);
        Paper.book().write(Common.CURRENT_SYNC_TYPE, syncType);

        //widgets
        backButton = findViewById(R.id.backButton);
        fileRecycler = findViewById(R.id.fileRecycler);
        cancelBtn = findViewById(R.id.cancelBtn);
        retryBtn = findViewById(R.id.retryBtn);
        launchBtn = findViewById(R.id.launchBtn);
        fileDownloadProgress = findViewById(R.id.fileDownloadProgress);
        fileProgress = findViewById(R.id.fileProgress);
        fileCountProgress = findViewById(R.id.fileCountProgress);
        fileCountProgressPercent = findViewById(R.id.fileCountProgressPercent);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        loadingText = findViewById(R.id.loadingText);

        //request perm
        requestPermissions();

        //init ui
        initializePage();
    }

    private void initializePage() {

        //back
        backButton.setOnClickListener(v -> {

            if (isCanceling || isRetrying || isIncomplete){

                //cancel
                alertDialog.dismiss();

            } else {

                //show dialog
                showConfirmationDialog();

            }

        });

        //cancel
        cancelBtn.setOnClickListener(v -> showConfirmationDialog());

        //retry
        retryBtn.setOnClickListener(v -> showRetryConfirmationDialog());

        //load
        if (syncType.equals(Common.SYNC_TYPE_FTP_FOLDER) || syncType.equals(Common.SYNC_TYPE_INDEX)) {
            loadFiles();
        } else

        if (syncType.equals(Common.SYNC_TYPE_URL_ZIP) || syncType.equals(Common.SYNC_TYPE_FTP_ZIP)) {
            loadZipFiles();
        } else

        if (syncType.equals(Common.SYNC_TYPE_PARSE)) {
            loadParseFiles();
        }

        //launch
        launchBtn.setOnClickListener(v -> {

            if (downloadedFiles < fileList.size()){

                showIncompleteConfirmationDialog();

            } else {

                checkLaunchType();

            }

        });

    }

    private void loadParseFiles() {

        //set recycler params
        fileRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        fileRecycler.setLayoutManager(layoutManager);

        //disable default animator
        ((SimpleItemAnimator) fileRecycler.getItemAnimator()).setSupportsChangeAnimations(false);

        //data
        fileList = new Database(this).getFiles(syncDbLicence);
        processedList = new ArrayList<>();

        //adapter
        adapter = new SyncAdapter(this, this, processedList);
        fileRecycler.setAdapter(adapter);

        //download one by one
        for (FileHistory currentFile : fileList) {

            //set default
            fileDownloadProgress.setProgress(0);


            //initialize directory
            File baseDir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
            File licenceDir = new File(baseDir.getAbsolutePath(), Common.LICENCED_FOLDER_NAME);
            File theDir = new File(licenceDir.getAbsolutePath(), companyId + "-" + licenceKey);
            File cre = new File(theDir.getAbsolutePath(), currentFile.getFile_dir());
            if (!licenceDir.exists()) {
                licenceDir.mkdir();
            }
            if (!theDir.exists()) {
                theDir.mkdir();
            }
            if (!cre.exists()) {
                cre.mkdirs();
            }

            //start download
            new DownloadFromUrl(new DownloadHelper() {
                @Override
                public void afterExecutionIsComplete() {
                    //update download status
                    new Database(SyncActivity.this).updateFileForCompleteDownload(syncDbLicence, currentFile.getId(), Common.FILE_STATUS_DOWNLOADED);

                    //add to list
                    processedList.add(currentFile);
                    adapter.notifyDataSetChanged();

                    //check to see if download is complete
                    runOnUiThread(() -> {
                        calculate();
                    });

                }

                @Override
                public void whenExecutionStarts() {

                }

                @Override
                public void whileInProgress(int i) {

                    runOnUiThread(() -> {
                        //change text color by progress
                        if (i < 50) {
                            fileProgress.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                        } else {
                            fileProgress.setTextColor(getResources().getColor(R.color.white));
                        }

                        //set progress
                        fileDownloadProgress.setProgress(i);
                        fileProgress.setText(currentFile.getFile_name());
                    });

                }

            }).execute(currentFile.getFile_url(), cre.getAbsolutePath(), currentFile.getFile_name());

        }

    }

    private void loadFiles() {

        //set recycler params
        fileRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        fileRecycler.setLayoutManager(layoutManager);

        //disable default animator
        ((SimpleItemAnimator) fileRecycler.getItemAnimator()).setSupportsChangeAnimations(false);

        //data
        fileList = new Database(this).getFiles(syncDbLicence);
        processedList = new ArrayList<>();

        //adapter
        adapter = new SyncAdapter(this, this, processedList);
        fileRecycler.setAdapter(adapter);

        //download one by one
        for (FileHistory currentFile : fileList) {

            //set default
            fileDownloadProgress.setProgress(0);


            //initialize directory
            File baseDir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
            File licenceDir = new File(baseDir.getAbsolutePath(), Common.LICENCED_FOLDER_NAME);
            File theDir = new File(licenceDir.getAbsolutePath(), companyId + "-" + licenceKey);
            File cre = new File(theDir.getAbsolutePath(), currentFile.getFile_dir());
            if (!licenceDir.exists()) {
                licenceDir.mkdir();
            }
            if (!theDir.exists()) {
                theDir.mkdir();
            }
            if (!cre.exists()) {
                cre.mkdir();
            }

            //start download
            new DownloadFromUrl(new DownloadHelper() {
                @Override
                public void afterExecutionIsComplete() {
                    //update download status
                    new Database(SyncActivity.this).updateFileForCompleteDownload(syncDbLicence, currentFile.getId(), Common.FILE_STATUS_DOWNLOADED);

                    //add to list
                    processedList.add(currentFile);
                    adapter.notifyDataSetChanged();

                    //check to see if download is complete
                    runOnUiThread(() -> {
                        calculate();
                    });

                }

                @Override
                public void whenExecutionStarts() {

                }

                @Override
                public void whileInProgress(int i) {

                    runOnUiThread(() -> {
                        //change text color by progress
                        if (i < 50) {
                            fileProgress.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                        } else {
                            fileProgress.setTextColor(getResources().getColor(R.color.white));
                        }

                        //set progress
                        fileDownloadProgress.setProgress(i);
                        fileProgress.setText(currentFile.getFile_name());
                    });

                }

            }).execute(currentFile.getFile_url(), cre.getAbsolutePath(), currentFile.getFile_name());

        }

    }

    private void loadZipFiles() {

        //set recycler params
        fileRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        fileRecycler.setLayoutManager(layoutManager);

        //disable default animator
        ((SimpleItemAnimator) fileRecycler.getItemAnimator()).setSupportsChangeAnimations(false);

        //data
        fileList = new Database(this).getFiles(syncDbLicence);
        processedList = new ArrayList<>();

        //adapter
        adapter = new SyncAdapter(this, this, processedList);
        fileRecycler.setAdapter(adapter);

        //download one by one
        for (FileHistory currentFile : fileList) {

            //set default
            fileDownloadProgress.setProgress(0);

            //initialize directory
            File baseDir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
            File theDir = new File(baseDir.getAbsolutePath(), "/" + Common.LICENCE_FOLDER_DOWNLOAD_NAME);
            if (!theDir.exists()){
                theDir.mkdir();
            }

            //start download
            new DownloadFromUrl(new DownloadHelper() {
                @Override
                public void afterExecutionIsComplete() {
                    //update download status
                    new Database(SyncActivity.this).updateFileForCompleteDownload(syncDbLicence, currentFile.getId(), Common.FILE_STATUS_DOWNLOADED);

                    //add to list
                    processedList.add(currentFile);
                    adapter.notifyDataSetChanged();

                    //check to see if download is complete
                    runOnUiThread(() -> {
                        calculate();
                    });

                }

                @Override
                public void whenExecutionStarts() {

                }

                @Override
                public void whileInProgress(int i) {

                    runOnUiThread(() -> {
                        //change text color by progress
                        if (i < 50) {
                            fileProgress.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                        } else {
                            fileProgress.setTextColor(getResources().getColor(R.color.white));
                        }

                        //set progress
                        fileDownloadProgress.setProgress(i);
                        fileProgress.setText(currentFile.getFile_name());
                    });

                }

            }).execute(currentFile.getFile_url(), theDir.getAbsolutePath(), currentFile.getFile_name());

        }

    }

    private void loadForRetry() {

        //set recycler params
        fileRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        fileRecycler.setLayoutManager(layoutManager);

        //disable default animator
        ((SimpleItemAnimator) fileRecycler.getItemAnimator()).setSupportsChangeAnimations(false);

        //data
        fileList.clear();
        adapter = null;
        fileList = new Database(this).getIncompleteFiles(syncDbLicence);
        processedRetryList = new ArrayList<>();

        //adapter
        adapter = new SyncAdapter(this, this, processedRetryList);
        fileRecycler.setAdapter(adapter);

        //loop and download
        for (FileHistory theFile : fileList){

            //set default
            fileDownloadProgress.setProgress(0);

            if (syncType.equals(Common.SYNC_TYPE_INDEX) || syncType.equals(Common.SYNC_TYPE_FTP_FOLDER)) {
                //initialize directory
                File baseDir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
                File licenceDir = new File(baseDir.getAbsolutePath(), Common.LICENCED_FOLDER_NAME);
                File theDir = new File(licenceDir.getAbsolutePath(), companyId + "-" + licenceKey);
                File cre = new File(theDir.getAbsolutePath(), theFile.getFile_dir());
                if (!licenceDir.exists()) {
                    licenceDir.mkdir();
                }
                if (!theDir.exists()) {
                    theDir.mkdir();
                }
                if (!cre.exists()) {
                    cre.mkdir();
                }

                //start download
                new DownloadFromUrl(new DownloadHelper() {
                    @Override
                    public void afterExecutionIsComplete() {
                        //update download status
                        new Database(SyncActivity.this).updateFileForCompleteDownload(syncDbLicence, theFile.getId(), Common.FILE_STATUS_DOWNLOADED);

                        //add to list
                        processedRetryList.add(theFile);
                        adapter.notifyDataSetChanged();

                        //check to see if download is complete
                        runOnUiThread(() -> {
                            calculate();
                        });

                    }

                    @Override
                    public void whenExecutionStarts() {

                    }

                    @Override
                    public void whileInProgress(int i) {

                        runOnUiThread(() -> {
                            //change text color by progress
                            if (i < 50) {
                                fileProgress.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            } else {
                                fileProgress.setTextColor(getResources().getColor(R.color.white));
                            }

                            //set progress
                            fileDownloadProgress.setProgress(i);
                            fileProgress.setText(theFile.getFile_name());
                        });

                    }

                }).execute(theFile.getFile_url(), cre.getAbsolutePath(), theFile.getFile_name());

            } else

            if (syncType.equals(Common.SYNC_TYPE_FTP_ZIP) || syncType.equals(Common.SYNC_TYPE_URL_ZIP)) {
                //initialize directory
                File baseDir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
                File theDir = new File(baseDir.getAbsolutePath(), "/" + Common.LICENCE_FOLDER_DOWNLOAD_NAME);
                if (!theDir.exists()){
                    theDir.mkdir();
                }

                //start download
                new DownloadFromUrl(new DownloadHelper() {
                    @Override
                    public void afterExecutionIsComplete() {
                        //update download status
                        new Database(SyncActivity.this).updateFileForCompleteDownload(syncDbLicence, theFile.getId(), Common.FILE_STATUS_DOWNLOADED);

                        //add to list
                        processedRetryList.add(theFile);
                        adapter.notifyDataSetChanged();

                        //check to see if download is complete
                        runOnUiThread(() -> {
                            calculate();
                        });

                    }

                    @Override
                    public void whenExecutionStarts() {

                    }

                    @Override
                    public void whileInProgress(int i) {

                        runOnUiThread(() -> {
                            //change text color by progress
                            if (i < 50) {
                                fileProgress.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            } else {
                                fileProgress.setTextColor(getResources().getColor(R.color.white));
                            }

                            //set progress
                            fileDownloadProgress.setProgress(i);
                            fileProgress.setText(theFile.getFile_name());
                        });

                    }

                }).execute(theFile.getFile_url(), theDir.getAbsolutePath(), theFile.getFile_name());

            }

        }

    }




    private void requestPermissions() {

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

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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





    //do file calculation
    public void calculate(){

        if (downloadedFiles < fileList.size()) {

            //increase
            downloadedFiles++;

            //progress
            fileCountProgress.setText(downloadedFiles + " / " + fileList.size() + " Files Downloaded");
            double temp = 100.0 * downloadedFiles;
            fileCountProgressPercent.setText((int) (temp / (double) fileList.size()) + " % Complete!");

            //check if complete
            if (downloadedFiles == fileList.size()) {

                if (syncType.equals(Common.SYNC_TYPE_FTP_ZIP) || syncType.equals(Common.SYNC_TYPE_URL_ZIP)) {

                    //downloaded file
                    File downloadDir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME + "/" + Common.LICENCE_FOLDER_DOWNLOAD_NAME + "/App.zip");

                    //extract
                    extractDownload(downloadDir);


                } else {

                    checkLaunchType();

                }

            }

        }

    }

    private void extractDownload(File downloadFile) {

        //init file directories
        File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
        File liDir = new File(dir.getAbsolutePath(), Common.LICENCED_FOLDER_NAME);
        File destinationFolder = new File(liDir.getAbsolutePath(), companyId + "-" + licenceKey);

        if (downloadFile.exists()) {

            //check lir dir
            if (!liDir.exists()) {
                liDir.mkdirs();
            }

            //overwrite former
            if (!destinationFolder.exists()){
                destinationFolder.mkdirs();
            }

            //show
            showExtractionLoading(true, "Extracting. Please wait . . .");

            //unzip files
            ZipManager zipManager = new ZipManager();
            zipManager.unzip(downloadFile.getAbsolutePath(), destinationFolder.getAbsolutePath(), true);

            //set timer for extraction
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    new Handler(Looper.getMainLooper()).post(() -> {

                        //remove loading
                        showExtractionLoading(false, "Extracting. Please wait . . .");

                        //check launch type
                        checkLaunchType();

                    });
                }
            }, 30000);

        } else {

            Toast.makeText(this, "File doesn't exist", Toast.LENGTH_SHORT).show();

        }

    }

    public void showExtractionLoading (boolean showLoading, String text) {

        //overlay visibility
        if (showLoading) {
            loadingOverlay.setVisibility(View.VISIBLE);
        } else {
            loadingOverlay.setVisibility(View.GONE);
        }

        //set text
        loadingText.setText(text);

    }



    private void checkLaunchType() {

        //build string
        String companyId = Paper.book().read(Common.COMPANY_ID);
        String licenceKey = Paper.book().read(Common.LICENCE_ID);
        String launchMode = Paper.book().read(Common.LOAD_STYLE);
        String masterDomain = Paper.book().read(Common.CURRENT_MASTER_DOMAIN);

        //init file directories
        File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
        File liDir = new File(dir, Common.LICENCED_FOLDER_NAME);
        File destinationFolder = new File(liDir, companyId + "-" + licenceKey);

        //read new custom list
        if (launchMode.equals(Common.LOAD_FROM_ONLINE)){

            String theUrl = null;

            if (Paper.book().read(Common.CUSTOM_ONLINE_LINK_STATUS, Common.CUSTOM_ONLINE_LINK_INACTIVE).equals(Common.CUSTOM_ONLINE_LINK_ACTIVE)){
                theUrl = Paper.book().read(Common.CUSTOM_ONLINE_LINK);
            } else {
                theUrl = masterDomain + "/" + companyId + "/" + licenceKey + "/App/Application/index.html";
            }

            //transition to the webpage
            loadLicencedPage(true, theUrl);

        } else {

            //set location
            String theUrl = "file:///" + destinationFolder.getAbsolutePath() + "/" + Common.USER_WEBPAGE_FOLDER + "/index.html";

            //transition to the webpage
            loadLicencedPage(false, theUrl);

        }

    }

    private void loadLicencedPage(boolean isOnline, String theUrl) {

        //update UI
        Intent webIntent = null;

        //intent
        if (Paper.book().read(Common.CURRENT_USER_TYPE, Common.USER_TYPE_BASIC).equals(Common.USER_TYPE_ULTRA)) {
            webIntent = new Intent(this, WebActivity.class);
        } else {
            webIntent = new Intent(this, BasicWebActivity.class);
        }
        if (isOnline) {

            webIntent.putExtra(Common.WEB_PAGE_TYPE, Common.LOAD_FROM_ONLINE);

        } else {

            webIntent.putExtra(Common.WEB_PAGE_TYPE, Common.LOAD_FROM_LOCAL);

        }
        webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_NO_SYNC_MODE);
        webIntent.putExtra(Common.WEB_PAGE_INTENT, theUrl);
        startActivity(webIntent);
        finish();
        this.overridePendingTransition(R.anim.slide_left, R.anim.slide_out_left);

    }




    //dialogs
    public void showRetryConfirmationDialog(){

        //start syncing
        isRetrying = true;

        //create dialog
        alertDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.retry_sync_dialog, null);

        //widgets
        Button noBtn = viewOptions.findViewById(R.id.noBtn);
        Button yesBtn = viewOptions.findViewById(R.id.yesBtn);

        //dialog props
        alertDialog.setView(viewOptions);
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //listener
        alertDialog.setOnDismissListener(dialog -> isRetrying = false);
        alertDialog.setOnCancelListener(dialog -> isRetrying = false);

        //yes
        yesBtn.setOnClickListener(v -> {

            //clear file
            fileList.clear();

            //clear record
            downloadedFiles = 0;

            //dismiss
            alertDialog.dismiss();

            //kill all processes
            android.os.Process.killProcess(android.os.Process.myPid());

            //new list
            loadForRetry();

        });

        //no
        noBtn.setOnClickListener(v -> {

            //dismiss
            alertDialog.dismiss();

        });

        //show dialog
        alertDialog.show();

    }

    public void showIncompleteConfirmationDialog(){

        //start syncing
        isIncomplete = true;

        //create dialog
        alertDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.incomplete_sync_dialog, null);

        //widgets
        Button yesBtn = viewOptions.findViewById(R.id.yesBtn);
        Button forceBtn = viewOptions.findViewById(R.id.forceBtn);

        //dialog props
        alertDialog.setView(viewOptions);
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //listener
        alertDialog.setOnDismissListener(dialog -> isIncomplete = false);
        alertDialog.setOnCancelListener(dialog -> isIncomplete = false);

        //yes
        yesBtn.setOnClickListener(v -> {

            //cancel
            alertDialog.dismiss();

        });

        //force
        forceBtn.setOnClickListener(v -> {
            //close dialog
            alertDialog.dismiss();

            //force go
            checkLaunchType();
        });

        //show dialog
        alertDialog.show();

    }

    public void showConfirmationDialog(){

        //start syncing
        isCanceling = true;

        //create dialog
        alertDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.cancel_sync_dialog, null);

        //widgets
        Button noBtn = viewOptions.findViewById(R.id.noBtn);
        Button yesBtn = viewOptions.findViewById(R.id.yesBtn);

        //dialog props
        alertDialog.setView(viewOptions);
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //listener
        alertDialog.setOnDismissListener(dialog -> isCanceling = false);
        alertDialog.setOnCancelListener(dialog -> isCanceling = false);

        //yes
        yesBtn.setOnClickListener(v -> {

            //clear paper db shared pref
            Paper.book().delete(Common.USER_NAME);
            Paper.book().delete(Common.COMPANY_ID);
            Paper.book().delete(Common.LICENCE_ID);
            Paper.book().delete(Common.CURRENT_TEST_MODE);
            Paper.book().delete(Common.CURRENT_MASTER_DOMAIN);
            Paper.book().delete(Common.FTP_HOST);
            Paper.book().delete(Common.FTP_PORT);
            Paper.book().delete(Common.FTP_USERNAME);
            Paper.book().delete(Common.FTP_PASSWORD);
            Paper.book().delete(Common.LOAD_STYLE);
            Paper.book().delete(Common.CURRENT_DB_LICENCE);

            //toast
            Toast.makeText(this, "Sync Cancelled", Toast.LENGTH_SHORT).show();

            //cancel
            alertDialog.dismiss();

            //kill all processes
            android.os.Process.killProcess(android.os.Process.myPid());

            //finish
            Intent exitIntent = new Intent(SyncActivity.this, SignIn.class);
            exitIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(exitIntent);
            finish();
            overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);

        });

        //no
        noBtn.setOnClickListener(v -> {

            //dismiss
            alertDialog.dismiss();

        });

        //show dialog
        alertDialog.show();

    }

    @Override
    public void onBackPressed() {

        if (isCanceling || isRetrying || isIncomplete){

            //cancel
            alertDialog.dismiss();

        } else {

            //show dialog
            showConfirmationDialog();

        }

    }

}