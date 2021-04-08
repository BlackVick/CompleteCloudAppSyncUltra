package com.cloudappsync.ultra.Utilities;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatDelegate;

import com.cloudappsync.ultra.R;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.github.anrwatchdog.ANRWatchDog;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;

import io.paperdb.Paper;

public class AppClass extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //context paper db init
        Paper.init(getApplicationContext());

        //picasso cache mode
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        //create default channel
        createNotificationChannel();

        //initialize anr watcher
        new ANRWatchDog(1000).setANRListener(error -> {

            StringWriter sw = new StringWriter();
            error.printStackTrace(new PrintWriter(sw));

            //check if file exist in directory and create if not
            File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
            if (!dir.exists()) {
                dir.mkdir();
            }

            //crash folder
            File crashDir = new File(dir.getAbsolutePath(), Common.CRASH_FOLDER_NAME);
            if (!crashDir.exists()) {
                crashDir.mkdir();
            }

            if (Build.VERSION.SDK_INT >= 26) {
                //check if config file exists and create file if not
                File newFile = new File(crashDir,  "Crash Reports.txt");

                if (!newFile.exists()){
                    try {
                        FileOutputStream fos = new FileOutputStream(newFile);
                        fos.write("".getBytes());
                        fos.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {

                    Files.write(Paths.get(newFile.getAbsolutePath()), "\n\n\n\n\n\n\n\n\n".getBytes(), StandardOpenOption.APPEND);
                    Files.write(Paths.get(newFile.getAbsolutePath()), new SimpleDateFormat("dd/MM/yyyy_HH:mm").format(System.currentTimeMillis()).getBytes(), StandardOpenOption.APPEND);
                    Files.write(Paths.get(newFile.getAbsolutePath()), "\n\n".getBytes(), StandardOpenOption.APPEND);
                    Files.write(Paths.get(newFile.getAbsolutePath()), sw.toString().getBytes(), StandardOpenOption.APPEND);

                } catch (IOException e) {
                    e.printStackTrace();
                    //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }



        }).start();

        //report main thread alone
        new ANRWatchDog().setReportMainThreadOnly();

        //pr download
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setReadTimeout(60_000)
                .setConnectTimeout(60_000)
                .build();
        PRDownloader.initialize(getApplicationContext(), config);

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    Common.DEFAULT_NOTIFICATION_CHANNEL,
                    "DefaultChannel",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setShowBadge(false);
            serviceChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void setupActivityListener() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                //orientation


                //night mode
                if (Paper.book().read(Common.VISUAL_STYLE, Common.DAY_MODE).equals(Common.NIGHT_MODE)){
                    activity.setTheme(R.style.UltraDarkTheme);
                } else {
                    activity.setTheme(R.style.UltraLightTheme);
                }
            }
            @Override
            public void onActivityStarted(Activity activity) {



                //night mode
                if (Paper.book().read(Common.VISUAL_STYLE, Common.DAY_MODE).equals(Common.NIGHT_MODE)){
                    activity.setTheme(R.style.UltraDarkTheme);
                } else {
                    activity.setTheme(R.style.UltraLightTheme);
                }

            }
            @Override
            public void onActivityResumed(Activity activity) {

                

                //night mode
                if (Paper.book().read(Common.VISUAL_STYLE, Common.DAY_MODE).equals(Common.NIGHT_MODE)){
                    activity.setTheme(R.style.UltraDarkTheme);

                    AppCompatDelegate
                            .setDefaultNightMode(
                                    AppCompatDelegate
                                            .MODE_NIGHT_YES);
                } else {
                    activity.setTheme(R.style.UltraLightTheme);

                    AppCompatDelegate
                            .setDefaultNightMode(
                                    AppCompatDelegate
                                            .MODE_NIGHT_NO);
                }

            }
            @Override
            public void onActivityPaused(Activity activity) {

            }
            @Override
            public void onActivityStopped(Activity activity) {
            }
            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }
            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }
}


