package com.cloudappsync.ultra;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudappsync.ultra.Basic.BasicSetting;
import com.cloudappsync.ultra.Basic.BasicWebActivity;
import com.cloudappsync.ultra.Basic.SignInBasic;
import com.cloudappsync.ultra.Services.BubbleService;
import com.cloudappsync.ultra.Ultra.Setting;
import com.cloudappsync.ultra.Ultra.SignIn;
import com.cloudappsync.ultra.Ultra.WebActivity;
import com.cloudappsync.ultra.Utilities.Common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.cloudappsync.ultra.Utilities.Database;
import io.paperdb.Paper;

public class PasswordCheckPage extends AppCompatActivity {

    //values
    private String savedUrl = "";
    private String savedUsername = "";
    private String savedPassword = "";
    private String testMode;
    private String testStatus;


    //dialog
    private android.app.AlertDialog loadingDialog;
    private boolean isLoading = false;

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
        setContentView(R.layout.activity_password_check_page);

        //values
        testMode = Paper.book().read(Common.CURRENT_TEST_MODE);
        testStatus = Paper.book().read(Common.IS_IN_TEST_MODE);

        //init
        init();
    }

    private void init() {

        //build string
        String companyId = Paper.book().read(Common.COMPANY_ID);
        String licenceKey = Paper.book().read(Common.LICENCE_ID);

        //saved url
        try {
            File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME + "/" + Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey + "/" + Common.USER_CONFIG_FOLDER);
            BufferedReader reader = new BufferedReader(new FileReader(dir.getAbsoluteFile() + "/configFile.txt"));

            String line = "";
            if ((line = reader.readLine()) != null)
                savedUrl = line;
            if ((line = reader.readLine()) != null)
                savedUsername = line;
            if ((line = reader.readLine()) != null)
                savedPassword = line;
            reader.close();

            //set text
            //savedUrlTxt.setText(savedUrl);

            //save webpage to local
            //saveWebpageToLocal();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        doCheck();

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
    }

    private void doCheck() {

        requestFeaturePassword();

    }

    private void requestFeaturePassword() {

        //create dialog
        android.app.AlertDialog passwordDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.featured_password_dialog, null);

        //widgets
        final EditText appPassword = viewOptions.findViewById(R.id.appPassword);
        final TextView accessGrant = viewOptions.findViewById(R.id.accessGrant);
        final TextView launchWebView = viewOptions.findViewById(R.id.launchWebView);
        final TextView appSettingBtn = viewOptions.findViewById(R.id.appSettingBtn);
        final TextView deviceSettingBtn = viewOptions.findViewById(R.id.deviceSettingBtn);
        final TextView deviceHomeBtn = viewOptions.findViewById(R.id.deviceHomeBtn);
        final TextView testModeBtn = viewOptions.findViewById(R.id.testModeBtn);
        final TextView exitBtn = viewOptions.findViewById(R.id.exitBtn);
        final TextView resyncBtn = viewOptions.findViewById(R.id.resyncBtn);

        //dialog props
        passwordDialog.setView(viewOptions);
        passwordDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        passwordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //lock dialog
        passwordDialog.setCancelable(false);
        passwordDialog.setCanceledOnTouchOutside(false);

        //check password status
        if (Paper.book().read(Common.PASSWORD_REQUEST_TYPE) != null) {

            if (Paper.book().read(Common.PASSWORD_REQUEST_TYPE).equals(Common.PASSWORD_REQUEST_ONCE)) {

                if (Paper.book().read(Common.PASSWORD_STATUS) != null) {

                    if (Paper.book().read(Common.PASSWORD_STATUS).equals(Common.PASSWORD_NOT_PROVIDED)) {

                        appPassword.setText("");

                    } else {

                        appPassword.setText(savedPassword);

                    }

                } else {

                    Paper.book().write(Common.PASSWORD_STATUS, Common.PASSWORD_NOT_PROVIDED);

                    appPassword.setText("");

                }

            } else if (Paper.book().read(Common.PASSWORD_REQUEST_TYPE).equals(Common.PASSWORD_REQUEST_ALWAYS)) {

                appPassword.setText("");


            }

        } else {

            Paper.book().write(Common.PASSWORD_REQUEST_TYPE, Common.PASSWORD_REQUEST_ALWAYS);

        }

        //access
        accessGrant.setOnClickListener(v -> {

            //check password
            if (!TextUtils.isEmpty(appPassword.getText().toString().trim())){

                if (appPassword.getText().toString().trim().equals(savedPassword)){

                    //register password entry
                    Paper.book().write(Common.PASSWORD_STATUS, Common.PASSWORD_PROVIDED);

                    //dismiss
                    passwordDialog.dismiss();

                    //access home
                    Intent homeIntent = new Intent(PasswordCheckPage.this, Home.class);
                    startActivity(homeIntent);
                    finish();
                    this.overridePendingTransition(R.anim.slide_left, R.anim.slide_out_left);


                } else {

                    Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show();

                }

            } else {

                Toast.makeText(this, "Provide password", Toast.LENGTH_SHORT).show();

            }

        });

        //app settings
        appSettingBtn.setOnClickListener(v -> {

            //check password
            if (!TextUtils.isEmpty(appPassword.getText().toString().trim())){

                if (appPassword.getText().toString().trim().equals(savedPassword)){

                    //register password entry
                    Paper.book().write(Common.PASSWORD_STATUS, Common.PASSWORD_PROVIDED);

                    //dismiss
                    passwordDialog.dismiss();

                    //null
                    Intent settingIntent = null;

                    //intent
                    if (Paper.book().read(Common.CURRENT_USER_TYPE, Common.USER_TYPE_BASIC).equals(Common.USER_TYPE_ULTRA)) {
                        settingIntent = new Intent(this, Setting.class);
                    } else {
                        settingIntent = new Intent(this, BasicSetting.class);
                    }

                    startActivity(settingIntent);
                    this.overridePendingTransition(R.anim.slide_left, R.anim.slide_out_left);

                } else {

                    Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show();

                }

            } else {

                Toast.makeText(this, "Provide password", Toast.LENGTH_SHORT).show();

            }

        });

        //device settings
        deviceSettingBtn.setOnClickListener(v -> {

            //check password
            if (!TextUtils.isEmpty(appPassword.getText().toString().trim())){

                if (appPassword.getText().toString().trim().equals(savedPassword)){

                    //register password entry
                    Paper.book().write(Common.PASSWORD_STATUS, Common.PASSWORD_PROVIDED);

                    //dismiss
                    passwordDialog.dismiss();

                    //go to settings
                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(intent);

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
                        startService(new Intent(this, BubbleService.class).putExtra("activity_background", true));
                    }

                } else {

                    Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show();

                }

            } else {

                Toast.makeText(this, "Provide password", Toast.LENGTH_SHORT).show();

            }

        });

        //webview
        launchWebView.setOnClickListener(v -> {
            //check password
            if (!TextUtils.isEmpty(appPassword.getText().toString().trim())){

                if (appPassword.getText().toString().trim().equals(savedPassword)){

                    //register password entry
                    Paper.book().write(Common.PASSWORD_STATUS, Common.PASSWORD_PROVIDED);

                    //grant access
                    passwordDialog.dismiss();

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
                        webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_NORMAL_MODE);
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
                        webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_NORMAL_MODE);
                        webIntent.putExtra(Common.WEB_PAGE_TYPE, Common.LOAD_FROM_LOCAL);
                        webIntent.putExtra(Common.WEB_PAGE_INTENT, "file:///" + liDir.getAbsolutePath() + "/index.html");
                        startActivity(webIntent);
                        this.overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);

                    }

                } else {

                    Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show();

                }

            } else {

                Toast.makeText(this, "Provide password", Toast.LENGTH_SHORT).show();

            }
        });

        //device home
        deviceHomeBtn.setOnClickListener(v -> {

            //check password
            if (!TextUtils.isEmpty(appPassword.getText().toString().trim())){

                if (appPassword.getText().toString().trim().equals(savedPassword)){

                    //register password entry
                    Paper.book().write(Common.PASSWORD_STATUS, Common.PASSWORD_PROVIDED);

                    //dismiss
                    passwordDialog.dismiss();

                    //go home
                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(startMain);

                } else {

                    Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show();

                }

            } else {

                Toast.makeText(this, "Provide password", Toast.LENGTH_SHORT).show();

            }

        });

        //test mode option
        if (testMode.equals(Common.TEST_PERSISTENT)){

            if (testStatus.equals(Common.YES)){

                //set text
                testModeBtn.setText("APP MODE");

                //click
                testModeBtn.setOnClickListener(v -> {

                    //check password
                    if (!TextUtils.isEmpty(appPassword.getText().toString().trim())){

                        if (appPassword.getText().toString().trim().equals(savedPassword)){

                            //register password entry
                            Paper.book().write(Common.PASSWORD_STATUS, Common.PASSWORD_PROVIDED);

                            //cancel test mode
                            Paper.book().write(Common.IS_IN_TEST_MODE, Common.NO);

                            //set text
                            testModeBtn.setText("TEST MODE");

                            //grant access
                            passwordDialog.dismiss();

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
                                webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_NORMAL_MODE);
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
                                webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_NORMAL_MODE);
                                webIntent.putExtra(Common.WEB_PAGE_TYPE, Common.LOAD_FROM_LOCAL);
                                webIntent.putExtra(Common.WEB_PAGE_INTENT, "file:///" + liDir.getAbsolutePath() + "/index.html");
                                startActivity(webIntent);
                                this.overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);

                            }

                        } else {

                            Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show();

                        }

                    } else {

                        Toast.makeText(this, "Provide password", Toast.LENGTH_SHORT).show();

                    }

                });

            } else {

                //set text
                testModeBtn.setText("TEST MODE");

                //click
                testModeBtn.setOnClickListener(v -> {

                    //check password
                    if (!TextUtils.isEmpty(appPassword.getText().toString().trim())){

                        if (appPassword.getText().toString().trim().equals(savedPassword)){

                            //register password entry
                            Paper.book().write(Common.PASSWORD_STATUS, Common.PASSWORD_PROVIDED);

                            //cancel test mode
                            Paper.book().write(Common.IS_IN_TEST_MODE, Common.YES);

                            //set text
                            testModeBtn.setText("APP MODE");

                            //grant access
                            passwordDialog.dismiss();

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
                                webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_TEST_MODE);
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
                                webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_TEST_MODE);
                                webIntent.putExtra(Common.WEB_PAGE_TYPE, Common.LOAD_FROM_LOCAL);
                                webIntent.putExtra(Common.WEB_PAGE_INTENT, "file:///" + liDir.getAbsolutePath() + "/index.html");
                                startActivity(webIntent);
                                this.overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);

                            }

                        } else {

                            Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show();

                        }

                    } else {

                        Toast.makeText(this, "Provide password", Toast.LENGTH_SHORT).show();

                    }

                });

            }

        } else {

            if (testStatus.equals(Common.YES)){

                //set text
                testModeBtn.setText("APP MODE");

                //click
                testModeBtn.setOnClickListener(v -> {

                    //check password
                    if (!TextUtils.isEmpty(appPassword.getText().toString().trim())){

                        if (appPassword.getText().toString().trim().equals(savedPassword)){

                            //register password entry
                            Paper.book().write(Common.PASSWORD_STATUS, Common.PASSWORD_PROVIDED);

                            //cancel test mode
                            Paper.book().write(Common.IS_IN_TEST_MODE, Common.NO);

                            //set text
                            testModeBtn.setText("TEST MODE");

                            //grant access
                            passwordDialog.dismiss();

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
                                webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_NORMAL_MODE);
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
                                webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_NORMAL_MODE);
                                webIntent.putExtra(Common.WEB_PAGE_TYPE, Common.LOAD_FROM_LOCAL);
                                webIntent.putExtra(Common.WEB_PAGE_INTENT, "file:///" + liDir.getAbsolutePath() + "/index.html");
                                startActivity(webIntent);
                                this.overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);

                            }

                        } else {

                            Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show();

                        }

                    } else {

                        Toast.makeText(this, "Provide password", Toast.LENGTH_SHORT).show();

                    }

                });

            } else {

                //set text
                testModeBtn.setText("TEST MODE");

                //click
                testModeBtn.setOnClickListener(v -> {

                    //check password
                    if (!TextUtils.isEmpty(appPassword.getText().toString().trim())){

                        if (appPassword.getText().toString().trim().equals(savedPassword)){

                            //register password entry
                            Paper.book().write(Common.PASSWORD_STATUS, Common.PASSWORD_PROVIDED);

                            //cancel test mode
                            Paper.book().write(Common.IS_IN_TEST_MODE, Common.YES);

                            //set text
                            testModeBtn.setText("APP MODE");

                            //grant access
                            passwordDialog.dismiss();

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
                                webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_TEST_MODE);
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
                                webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_TEST_MODE);
                                webIntent.putExtra(Common.WEB_PAGE_TYPE, Common.LOAD_FROM_LOCAL);
                                webIntent.putExtra(Common.WEB_PAGE_INTENT, "file:///" + liDir.getAbsolutePath() + "/index.html");
                                startActivity(webIntent);
                                this.overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);

                            }

                        } else {

                            Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show();

                        }

                    } else {

                        Toast.makeText(this, "Provide password", Toast.LENGTH_SHORT).show();

                    }

                });

            }

        }

        //exit
        exitBtn.setOnClickListener(v -> {

            //check password
            if (!TextUtils.isEmpty(appPassword.getText().toString().trim())){

                if (appPassword.getText().toString().trim().equals(savedPassword)){

                    //register password entry
                    Paper.book().write(Common.PASSWORD_STATUS, Common.PASSWORD_PROVIDED);

                    //grant access
                    passwordDialog.dismiss();

                    Intent exitIntent = new Intent(PasswordCheckPage.this, Splash.class);
                    exitIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    exitIntent.putExtra(Common.EXIT_APP_INTENT, true);
                    startActivity(exitIntent);
                    finish();
                    this.overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);

                } else {

                    Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show();

                }

            } else {

                Toast.makeText(this, "Provide password", Toast.LENGTH_SHORT).show();

            }

        });

        //resync
        resyncBtn.setOnClickListener(v -> {

            resync();

        });

        //show dialog
        passwordDialog.show();

    }

    private void resync() {

        //clean database
        new Database(this).cleanFileHistory(Paper.book().read(Common.CURRENT_DB_LICENCE));


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

        //log out
        Intent exitIntent = null;

        if (Paper.book().read(Common.CURRENT_USER_TYPE, Common.USER_TYPE_BASIC).equals(Common.USER_TYPE_ULTRA)) {
            exitIntent = new Intent(this, SignIn.class);
        } else {
            exitIntent = new Intent(this, SignInBasic.class);
        }
        exitIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        exitIntent.putExtra(Common.EXIT_APP_INTENT, true);
        startActivity(exitIntent);
        finish();
        overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isLoading)
            loadingDialog.dismiss();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isLoading)
            loadingDialog.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService(new Intent(this, BubbleService.class));
    }
}