package com.cloudappsync.ultra;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.cloudappsync.ultra.Basic.SignInBasic;
import com.cloudappsync.ultra.Models.Domains;
import com.cloudappsync.ultra.Models.User;
import com.cloudappsync.ultra.Ultra.SignIn;
import com.cloudappsync.ultra.Utilities.Common;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.opencsv.CSVReader;
import io.paperdb.Paper;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LicenceVerify extends AppCompatActivity {

    //widgets
    private EditText companyId, licenceKey;
    private RelativeLayout verifyButton;
    private TextView verifyText;
    private ProgressBar verifyProgress;

    //static values
    public static final int PERMISSION_REQUEST_CODE = 234;

    //data
    private File newFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licence_verify);

        //widgets
        companyId = findViewById(R.id.companyId);
        licenceKey = findViewById(R.id.licenceKey);
        verifyButton = findViewById(R.id.verifyButton);
        verifyText = findViewById(R.id.verifyText);
        verifyProgress = findViewById(R.id.verifyProgress);

        //init
        initUI();
    }

    private void initUI() {

        //request permission
        requestPermissions();

        //verify licence
        verifyButton.setOnClickListener(v -> {

            validateParams();

        });

    }





    //permissions
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





    //verify
    private void validateParams() {

        //extract strings
        String theId = companyId.getText().toString().trim();
        String theKey = licenceKey.getText().toString().trim();

        //check
        if (TextUtils.isEmpty(theId)){

            companyId.requestFocus();
            companyId.setError("Required");

        } else

        if (TextUtils.isEmpty(theKey)){

            licenceKey.requestFocus();
            licenceKey.setError("Required");

        } else {

            downloadVerificationFile(theId, theKey);

        }

    }

    private void downloadVerificationFile(String theId, String theKey) {

        //start loading
        verifyButton.setEnabled(false);
        verifyText.setVisibility(View.GONE);
        verifyProgress.setVisibility(View.VISIBLE);

        //dir
        File baseDir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
        File theDir = new File(baseDir.getAbsolutePath(), "/" + Common.SPLASH_FOLDER_NAME);

        //build url
        String theUrl = "http://cloudappsync.com/cloudappsync/admin/" + Common.VERIFICATION_FILE;

        //new download
        PRDownloader.download(theUrl, theDir.getAbsolutePath(), Common.VERIFICATION_FILE)
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

                        //read file
                        readVerificationFile(theId, theKey);

                    }

                    @Override
                    public void onError(Error error) {

                        //stop loading
                        verifyButton.setEnabled(true);
                        verifyProgress.setVisibility(View.GONE);
                        verifyText.setVisibility(View.VISIBLE);

                        //error
                        Toast.makeText(LicenceVerify.this, "Error occurred while downloading default settings. Please try again", Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void readVerificationFile(String theId, String theKey) {

        //check if file exist in directory and populate spinner
        File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME + "/" + Common.SPLASH_FOLDER_NAME);
        if (dir.exists()) {

            //set file to use
            newFile = new File(dir, Common.VERIFICATION_FILE);

            //read csv
            try {
                CSVReader reader = new CSVReader(new FileReader(newFile));
                String[] nextLine;
                int count = 0;
                boolean exists = false;

                //read off first
                reader.readNext();

                while ((nextLine = reader.readNext()) != null) {

                    //check if data is there
                    if (nextLine[0].equals(theId + "-" + theKey)){

                        //check exist
                        exists = true;

                        //set licence
                        Paper.book().write(Common.CURRENT_USER_TYPE, nextLine[1]);
                        break;

                    }

                    // nextLine[] is an array of values from the line
                    count++;
                }

                //if not exist
                if (!exists){

                    //stop loading
                    verifyButton.setEnabled(true);
                    verifyProgress.setVisibility(View.GONE);
                    verifyText.setVisibility(View.VISIBLE);

                    //error
                    Toast.makeText(this, "Sorry, you are not a verified customer yet", Toast.LENGTH_LONG).show();

                } else {

                    //stop loading
                    verifyButton.setEnabled(true);
                    verifyProgress.setVisibility(View.GONE);
                    verifyText.setVisibility(View.VISIBLE);

                    //check licence type
                    if (Paper.book().read(Common.CURRENT_USER_TYPE).equals(Common.USER_TYPE_ULTRA)){

                        Intent signIntent = new Intent(this, SignIn.class);
                        startActivity(signIntent);
                        finish();
                        this.overridePendingTransition(R.anim.slide_left, R.anim.slide_out_left);

                    } else

                    if (Paper.book().read(Common.CURRENT_USER_TYPE).equals(Common.USER_TYPE_BASIC)){

                        Intent signIntent = new Intent(this, SignInBasic.class);
                        startActivity(signIntent);
                        finish();
                        this.overridePendingTransition(R.anim.slide_left, R.anim.slide_out_left);

                    }

                }

            } catch (IOException e) {

                //stop loading
                verifyButton.setEnabled(true);
                verifyProgress.setVisibility(View.GONE);
                verifyText.setVisibility(View.VISIBLE);

                Toast.makeText(this, "CSV Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {

                //delete default domain
                newFile.delete();
            }

        } else {

            //stop loading
            verifyButton.setEnabled(true);
            verifyProgress.setVisibility(View.GONE);
            verifyText.setVisibility(View.VISIBLE);

            //create dir
            dir.mkdir();

            Toast.makeText(this, "Verification File Download Failed", Toast.LENGTH_SHORT).show();

        }

    }
}