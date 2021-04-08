package com.cloudappsync.ultra.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.cloudappsync.ultra.Interface.EventCallback;
import com.cloudappsync.ultra.Utilities.Common;
import com.cloudappsync.ultra.Utilities.NotificationTools;
import com.cloudappsync.ultra.Utilities.PageSaver;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;

public class SaveWebService extends Service {

    private final String TAG = "SaveService";

    private ThreadPoolExecutor executor;
    private PageSaver pageSaver;
    private NotificationTools notificationTools;



    @Override
    public void onCreate() {
        executor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        pageSaver = new PageSaver(new PageSaveEventCallback());
        notificationTools = new NotificationTools(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getBooleanExtra("USER_CANCELLED", false) || intent.getBooleanExtra("USER_CANCELLED_ALL", false)) {
            if (intent.getBooleanExtra("USER_CANCELLED_ALL", false)) {
                executor.getQueue().clear();
            }
            //cancelling okhttp seems to cause networkOnMainThreadException, hence this.
            Log.w(TAG, "Cancelled");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    pageSaver.cancel();
                }
            }).start();

            return START_NOT_STICKY;
        }

        String pageUrl = intent.getStringExtra(Intent.EXTRA_TEXT);

        if (pageUrl != null && pageUrl.startsWith("http")) {
            executor.submit(new PageSaveTask(pageUrl));
        } else {
            if (pageUrl == null) {
                notificationTools.notifyFailure("URL null, this is probably a bug", null);
            } else {
                notificationTools.notifyFailure("URL not valid: " + pageUrl, null);
            }
        }

        return START_NOT_STICKY;
    }

    private class PageSaveTask implements Runnable {
        private final String pageUrl;
        private String destinationDirectory;

        public PageSaveTask(String pageUrl) {
            this.pageUrl = pageUrl;
            this.destinationDirectory = Paper.book().read(Common.LICENCED_LOCAL_DIR);
        }

        @Override
        public void run() {
            try {
                pageSaver.resetState();

                notificationTools.notifySaveStarted(executor.getQueue().size());

                pageSaver.getOptions().setUserAgent("Mozilla/5.0 (Linux; U; Android 4.4.3; en-us; mobile) AppleWebKit/537.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
                //cache is leaking and growing forever for some reason, so disable cache for now.
                //pageSaver.getOptions().setCache(getApplicationContext().getExternalCacheDir(), 1024 * 1024 * 15);
                boolean success = pageSaver.getPage(pageUrl, destinationDirectory, "index.html");

                if (pageSaver.isCancelled() || !success) {
                    if (pageSaver.isCancelled()) { //user cancelled, remove the notification, and delete files.
                        Log.e("SaveService", "Stopping Service, (Cancelled). Deleting files in: " + destinationDirectory + ", from: " + pageUrl);
                        notificationTools.cancelAll();
                        stopService();
                    } else if (!success) { //something went wrong, leave the notification, and delete files.
                        Log.e("SaveService", "Failed. Deleting files in: " + destinationDirectory + ", from: " + pageUrl);
                    }
                    return;
                }

                notificationTools.updateText(null, "Finishing...", executor.getQueue().size());

                File oldSavedPageDirectory = new File(destinationDirectory);
                File newSavedPageDirectory = new File(getNewDirectoryPath(pageSaver.getPageTitle(), oldSavedPageDirectory.getPath()));
                oldSavedPageDirectory.renameTo(newSavedPageDirectory);

                stopService();

                notificationTools.notifyFinished(pageSaver.getPageTitle(), newSavedPageDirectory.getPath());
            } catch (Exception e) {  //so that exceptions don't fpget swallowed and we see them.
                Toast.makeText(SaveWebService.this, "SaveService Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }

        private String getNewDirectoryPath(String title, String oldDirectoryPath) {
            String returnString = title.replaceAll("[^a-zA-Z0-9-_\\.]", "_"); //TODO: Fix this to support non A-Z & 0-9 characters

            File f = new File(oldDirectoryPath);
            return f.getParentFile().getAbsolutePath() + File.separator  + returnString + File.separator;
        }
    }

    private class PageSaveEventCallback implements EventCallback {

        @Override
        public void onFatalError(final Throwable e, String pageUrl) {
            Log.e("PageSaverService", e.getMessage(), e);
            stopService();

            notificationTools.notifyFailure(e.getMessage(), pageUrl);
        }

        @Override
        public void onProgressChanged(final int progress, final int maxProgress, final boolean indeterminate) {
            notificationTools.updateProgress(progress, maxProgress, indeterminate, executor.getQueue().size());
        }

        @Override
        public void onProgressMessage(final String message) {
            notificationTools.updateText(null, message, executor.getQueue().size());
        }

        @Override
        public void onPageTitleAvailable(String pageTitle) {
            notificationTools.updateText(pageTitle, null, executor.getQueue().size());
        }

        @Override
        public void onLogMessage(final String message) {
            Log.d("PageSaverService", message);
        }

        @Override
        public void onError(final Throwable e) {
            Log.e("PageSaverService", e.getMessage(), e);
        }

        @Override
        public void onError(String errorMessage) {
            Log.e(TAG, errorMessage);
        }
    }

    private void stopService() {
        if (executor.getQueue().isEmpty()) {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent i) {
        return null;
    }
}