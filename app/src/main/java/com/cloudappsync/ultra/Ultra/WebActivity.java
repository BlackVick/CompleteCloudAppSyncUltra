package com.cloudappsync.ultra.Ultra;

import android.text.TextUtils;
import android.webkit.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudappsync.ultra.PasswordCheckPage;
import com.cloudappsync.ultra.Models.Domains;
import com.cloudappsync.ultra.Models.FileHistory;
import com.cloudappsync.ultra.Models.Schedule;
import com.cloudappsync.ultra.R;
import com.cloudappsync.ultra.Utilities.Database;
import com.cloudappsync.ultra.Utilities.DownloadFromUrl;
import com.cloudappsync.ultra.Utilities.Common;
import com.cloudappsync.ultra.Interface.DownloadHelper;
import com.cloudappsync.ultra.Utilities.Methods;

import com.cloudappsync.ultra.Utilities.ZipManager;
import com.opencsv.CSVReader;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import io.paperdb.Paper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebActivity extends AppCompatActivity {

    //widgets
    public static ImageView connectionIndicator;
    public static WebView myWebView;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    public ProgressBar progressBar;
    public ProgressBar syncProgressBar;

    //test layout
    private RelativeLayout testLayout;
    public static ImageView testOnlineIndicator;
    public TextView countDownTimer, filesCount, syncProgress, filesChanged;

    //file directories
    private static File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
    public static File localWebDirectory = new File(dir.getAbsolutePath(), Common.LICENCED_FOLDER_NAME + "/" + Paper.book().read(Common.COMPANY_ID) + "-" + Paper.book().read(Common.LICENCE_ID) + "/" + Common.USER_WEBPAGE_FOLDER);

    //values
    public static String pageType = "";
    public static String pageMode = "";
    public static String testMode = "";
    public static String testStatus = "";
    public boolean isSchedule = false;
    public int timeOut;
    public boolean timerActive = true;
    private String dbLicence;

    //settings
    private WebSettings webSettings;

    //test values
    public int filesNumb = 0;
    public int totalFiles = 0;
    private CountDownTimer mCountDownTimer;
    private long mTimeLeftInMillis = 0;

    //files count
    private int filesOnServer = 0;
    private int filesChangedOnServer = 0;
    private int filesDownloaded = 0;

    //pole
    private Handler handler = new Handler(Looper.myLooper());
    private Runnable runnable;

    //new values
    private static String url;














    //data
    private List<Schedule> tempList = new ArrayList<>();
    public List<Schedule> theSchedules = new ArrayList<>();
    public List<Schedule> setAlarms = new ArrayList<>();
    public List<String> enteredSchedules = new ArrayList<>();
    public List<String> enteredAlarms = new ArrayList<>();

    //values
    private Date providedDate = null;
    private Date currentDate = null;
    private long value = 0;
    private long value2 = 0;

    //sync check
    private int fl = 0;

    //data
    private File newFile;

    //values
    boolean isExist = false;
    public boolean isRunning = false;
    public boolean isScheduleCurrentlyRunning = false;

    //handler
    private Handler handlerSchedule = new Handler(Looper.getMainLooper());
    private Handler handlerRunningSchedule = new Handler(Looper.getMainLooper());
    private Runnable runnableSchedule, runnableRunningSchedule;

    //schedule indicator
    private TextView scheduleStart, scheduleEnd;

    //threads and parseing
    private Thread parseThread, secondaryParseThread;
    private List<String> theList = new ArrayList<>();

    //timer
    private Timer zipTimer;

    //values for off connection fix
    private Timer connTimer;
    private boolean isInternetAvailable = false;
    private boolean hasSyncStarted = false;
    private boolean lostDuringSync = false;









    public void onCreate(Bundle savedInstanceState) {
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
        setContentView(R.layout.activity_web);

        //values
        url = getIntent().getStringExtra(Common.WEB_PAGE_INTENT);
        pageType = getIntent().getStringExtra(Common.WEB_PAGE_TYPE);
        pageMode = getIntent().getStringExtra(Common.WEB_PAGE_MODE);
        testMode = Paper.book().read(Common.CURRENT_TEST_MODE);
        testStatus = Paper.book().read(Common.IS_IN_TEST_MODE);
        dbLicence = Paper.book().read(Common.CURRENT_DB_LICENCE);
        timeOut = Integer.parseInt(Paper.book().read(Common.PAGE_TIMEOUT_VALUE));

        //widgtes
        progressBar = findViewById(R.id.progressBar);
        myWebView = findViewById(R.id.webView);
        mySwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        connectionIndicator = findViewById(R.id.connectionIndicator);
        testLayout = findViewById(R.id.testLayout);
        testOnlineIndicator = findViewById(R.id.testOnlineIndicator);
        countDownTimer = findViewById(R.id.countDownTimer);
        filesCount = findViewById(R.id.filesCount);
        syncProgress = findViewById(R.id.syncProgress);
        filesChanged = findViewById(R.id.filesChanged);
        syncProgressBar = findViewById(R.id.syncProgressBar);
        scheduleStart = findViewById(R.id.scheduleStart);
        scheduleEnd = findViewById(R.id.scheduleEnd);

        //init ui
        init();
    }

    private void init() {

        //sync progress initializer
        syncProgressBar.setIndeterminate(false);
        syncProgressBar.setMax(100);
        syncProgressBar.setProgress(0);

        //load webpage
        loadWebPage();

        //online indicator
        if (Paper.book().read(Common.ONLINE_INDICATOR_STATE).equals(Common.ONLINE_INDICATOR_SHOW)) {

            if (pageMode.equals(Common.PAGE_TEST_MODE) || testStatus.equals(Common.YES)){

                connectionIndicator.setVisibility(View.GONE);

            } else {

                connectionIndicator.setVisibility(View.VISIBLE);

            }

            if (pageType.equals(Common.LOAD_FROM_ONLINE)){

                connectionIndicator.setImageResource(R.drawable.online);

            } else {

                connectionIndicator.setImageResource(R.drawable.local_indicator);

            }

        } else {

            connectionIndicator.setVisibility(View.GONE);

        }

        //test mode
        if (pageMode.equals(Common.PAGE_TEST_MODE) || testStatus.equals(Common.YES)){

            testLayout.setVisibility(View.VISIBLE);

        } else {

            testLayout.setVisibility(View.GONE);

        }

        //check service
        runScheduleCheck();

        //refresh layout
        this.mySwipeRefreshLayout.setOnRefreshListener(() -> {
            if (!isSchedule){
                if (isInternetAvailable) {
                    myWebView.loadUrl(url);
                } else {
                    myWebView.loadUrl("file:///" + localWebDirectory.getAbsolutePath() + "/index.html");
                }
            }
            mySwipeRefreshLayout.setRefreshing(false);
        });

        //launch
        if (pageType.equals(Common.LOAD_FROM_ONLINE)){

            if (Paper.book().read(Common.PAGE_REFRESH_STATUS, Common.PAGE_REFRESH_ACTIVE).equals(Common.PAGE_REFRESH_ACTIVE)) {

                refreshPage();

            }

        }

        if (Paper.book().read(Common.LOAD_STYLE).equals(Common.LOAD_FROM_ONLINE)){

            connectionIndicator.setImageResource(R.drawable.online);

        } else {

            connectionIndicator.setImageResource(R.drawable.local_indicator);

        }

        //set custom domains
        setCustomDomains();

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebPage() {

        //user agent
        if (Paper.book().read(Common.CURRENT_USER_AGENT, Common.USER_AGENT_DESKTOP).equals(Common.USER_AGENT_DESKTOP)){

            myWebView.getSettings().setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/37.0.2062.94 Chrome/37.0.2062.94 Safari/537.36");

        } else {

            myWebView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");

        }

        //web view
        myWebView.getSettings().setJavaScriptEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        myWebView.setFocusable(true);
        myWebView.setFocusableInTouchMode(true);
        myWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        myWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        myWebView.getSettings().setDomStorageEnabled(true);
        myWebView.getSettings().setDatabaseEnabled(true);
        myWebView.getSettings().setAppCacheEnabled(true);
        myWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        myWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        myWebView.getSettings().setAppCacheEnabled(false);
        myWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        //run check
        if (connTimer != null){

            connTimer.cancel();
            runFirstCheck();

        } else {

            runFirstCheck();
            Toast.makeText(this, "Ran", Toast.LENGTH_SHORT).show();

        }

        /*if (Paper.book().read(Common.IS_DEVICE_CONNECTED, "False").equals("True")){
            myWebView.loadUrl(url);
            Log.d("PageLoad", "Loading online");
        } else {
            myWebView.loadUrl("file:///" + localWebDirectory.getAbsolutePath() + "/index.html");
            Log.d("PageLoad", "Loading offline");
        }*/


        //webview client
        myWebView.setWebViewClient(new WebViewClient() {

            /*public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                Log.i("apperror", "404 apperrorOnReceiveTitle");
                if (errorResponse.getStatusCode() == 404) {
                    Log.i("apperror", "404 apperrorOnReceiveTitle");
                    Toast.makeText(view.getContext(), "HTTP error", Toast.LENGTH_SHORT).show();
                    myWebView.loadUrl("file:///" + WebActivity.localWebDirectory.getAbsolutePath() + "/index.html");
                    Toast.makeText(WebActivity.this, "RecievedHttp Error", Toast.LENGTH_SHORT).show();
                    WebActivity.this.progressBar.setVisibility(View.GONE);
                }
                Context context = view.getContext();
                Toast.makeText(context, "HTTP error " + errorResponse.getStatusCode(), Toast.LENGTH_LONG).show();
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                WebView access$000 = WebActivity.myWebView;
                myWebView.loadUrl("file:///" + WebActivity.localWebDirectory.getAbsolutePath() + "/index.html");
                Toast.makeText(WebActivity.this, "Crossed received", Toast.LENGTH_SHORT).show();
            }*/

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                progressBar.setVisibility(View.VISIBLE);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                progressBar.setVisibility(View.GONE);
                view.clearHistory();
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!isInternetAvailable) {
                    myWebView.loadUrl("file:///" + localWebDirectory.getAbsolutePath() + "/index.html");
                    Toast.makeText(WebActivity.this, "Couldn't connect , Loading Local files", Toast.LENGTH_LONG).show();
                }
                if (url == null || url.startsWith("http://") || url.startsWith("https://")) {
                    return false;
                }
                try {
                    view.getContext().startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
                    return true;
                } catch (Exception e) {
                    Toast.makeText(WebActivity.this, "App not Installed!", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }

        });

        //chrome web client
        myWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                /*webPageProgress.setProgress(newProgress);

                if (newProgress == 100){
                    webPageProgress.setVisibility(View.GONE);
                }*/
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (title.contains("Object not found!") || title.contains("not found!")) {
                    Toast.makeText(view.getContext(), "HTTP error 404", Toast.LENGTH_SHORT).show();

                    //load local
                    myWebView.loadUrl("file:///" + WebActivity.localWebDirectory.getAbsolutePath() + "/index.html");

                    Toast.makeText(WebActivity.this, "OnReceived Title", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
                //webPageIcon.setImageBitmap(icon);

            }

        });

        //clear history
        myWebView.clearHistory();

    }

    public void refreshPage(){

        long interval = Integer.parseInt(Paper.book().read(Common.PAGE_RESYNC_VALUE));

        handler.postDelayed(runnable = () -> {
            if (!isSchedule) {
                myWebView.reload();
                refreshPage();
            }
        }, interval * 60 * 1000);

    }





    //schedule
    public  void loadScheduleUrl(String theId, String theUrl, String stopTime, boolean isOneOff, int position) {

        //register type
        isSchedule = true;

        //load page
        if (isInternetAvailable) {
            myWebView.loadUrl(theUrl);
        } else {
            if (theUrl.contains("file://")){
                myWebView.loadUrl(theUrl);
            } else {
                myWebView.loadUrl("file:///" + localWebDirectory.getAbsolutePath() + "/index.html");
                Toast.makeText(this, "Internet Unavailable", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void stopSchedule(String scheduleId, int position){

        Log.d("ServiceStop", "stopSchedule: stopped");

        //register type
        isSchedule = false;

        //load former webpage
        if (isInternetAvailable) {
            myWebView.loadUrl(url);
        } else {
            myWebView.loadUrl("file:///" + localWebDirectory.getAbsolutePath() + "/index.html");
        }

        //remove
        theSchedules.remove(position);
        enteredSchedules.remove(scheduleId);

    }






    //internet check
    public void runFirstCheck(){

        if (Methods.isConnected(this)){

            //internet available
            Log.i("ConnectionStatus", "Now Online" + url);

            //set values
            isInternetAvailable = true;

            //load online url
            myWebView.loadUrl(url);

            //set indicator
            if (pageType.equals(Common.LOAD_FROM_ONLINE)) {
                connectionIndicator.setImageResource(R.drawable.online);
            }
            testOnlineIndicator.setImageResource(R.drawable.online);

            //sync
            if (pageMode.equals(Common.PAGE_NO_SYNC_MODE)) {

                synchronizeAfterInterval();

            } else if (pageMode.equals(Common.PAGE_NORMAL_MODE) || pageMode.equals(Common.PAGE_TEST_MODE)) {

                syncFirstThenInterval();

            }

            //run continuous check
            runContinuousCheck();

        } else {

            //no connectivity
            Log.i("ConnectionStatus", "No Connectivity Detected: Loading Local");

            //set values
            isInternetAvailable = false;

            //load local page
            if (pageType.equals(Common.LOAD_FROM_ONLINE)) {
                myWebView.loadUrl("file:///" + localWebDirectory.getAbsolutePath() + "/index.html");
            } else {
                myWebView.loadUrl(url);
            }

            //set indicator
            if (pageType.equals(Common.LOAD_FROM_ONLINE)) {
                connectionIndicator.setImageResource(R.drawable.offline);
            }
            testOnlineIndicator.setImageResource(R.drawable.offline);

            //synchronization
            synchronizeAfterInterval();

            //run continuous check
            runContinuousCheck();

        }

    }

    public void runContinuousCheck(){

        connTimer = new Timer();
        connTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(() -> {

                    if (Methods.isConnected(WebActivity.this)){

                        //check for connectivity
                        if (!isInternetAvailable) {

                            //internet available
                            Log.i("ConnectionStatus", "Now Online" + url);

                            //set values
                            isInternetAvailable = true;

                            //load online url
                            myWebView.loadUrl(url);

                            //set indicator
                            if (pageType.equals(Common.LOAD_FROM_ONLINE)) {
                                connectionIndicator.setImageResource(R.drawable.online);
                            }
                            testOnlineIndicator.setImageResource(R.drawable.online);

                            //sync
                            if (hasSyncStarted && lostDuringSync){

                                //set new state
                                lostDuringSync = false;

                                //clear countdown
                                if (mCountDownTimer != null){
                                    mCountDownTimer.cancel();
                                }

                                //restart sync
                                synchronizeAfterInterval();

                            }

                        } else {

                            //sync
                            if (hasSyncStarted && lostDuringSync){

                                //set new state
                                lostDuringSync = false;

                                //clear countdown
                                if (mCountDownTimer != null){
                                    mCountDownTimer.cancel();
                                }

                                //restart sync
                                synchronizeAfterInterval();

                            }

                        }

                        //run continuous check
                        runContinuousCheck();

                    } else {

                        //no connectivity
                        if (isInternetAvailable) {

                            Log.i("ConnectionStatus", "No Connectivity Detected: Loading Local");

                            //set values
                            isInternetAvailable = false;

                            //load local page
                            if (pageType.equals(Common.LOAD_FROM_ONLINE)) {
                                myWebView.loadUrl("file:///" + localWebDirectory.getAbsolutePath() + "/index.html");
                            } else {
                                myWebView.loadUrl(url);
                            }

                            //set indicator
                            if (pageType.equals(Common.LOAD_FROM_ONLINE)) {
                                connectionIndicator.setImageResource(R.drawable.offline);
                            }
                            testOnlineIndicator.setImageResource(R.drawable.offline);

                            //sync
                            if (hasSyncStarted){
                                lostDuringSync = true;
                            }

                        }

                        //run continuous check
                        runContinuousCheck();

                    }

                });
            }
        }, 2500);

    }








    //life cycle
    @Override
    protected void onStop() {
        super.onStop();

        /*if (mCountDownTimer != null)
            mCountDownTimer.cancel();*/

        handlerSchedule.removeCallbacks(runnableSchedule);
        handlerRunningSchedule.removeCallbacks(runnableRunningSchedule);

    }

    public void onDestroy() {
        super.onDestroy();

        if (mCountDownTimer != null)
            mCountDownTimer.cancel();

        //check test mode
        switchTestMode();
    }

    public void onBackPressed() {
        if (!myWebView.canGoBack()) {
            super.onBackPressed();
            this.timerActive = false;
            myWebView.destroy();
            Intent settingsIntent = new Intent(WebActivity.this, PasswordCheckPage.class);
            startActivity(settingsIntent);
            finish();
            overridePendingTransition(R.anim.slide_right, R.anim.slide_out_right);
            return;
        }
        //myWebView.goBack();
    }

    public void onPause() {
        super.onPause();
        this.timerActive = false;

        //check test mode
        switchTestMode();

        handlerSchedule.removeCallbacks(runnableSchedule);
        handlerRunningSchedule.removeCallbacks(runnableRunningSchedule);

    }

    public void onResume() {
        super.onResume();
        this.timerActive = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //check mode
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.UltraDarkTheme);
        } else {
            setTheme(R.style.UltraLightTheme);
        }
    }












    //sync
    private void syncFirstThenInterval() {

        //set defaults
        filesCount.setText(0 + " / " + filesNumb);
        syncProgress.setText(0 + " %");
        countDownTimer.setText("00:00");
        filesChanged.setText("0");
        totalFiles = 0;
        filesNumb = 0;
        filesOnServer = 0;
        filesDownloaded = 0;
        filesChangedOnServer = 0;
        syncProgressBar.setProgress(0);

        //synchronize
        if (isInternetAvailable) {
            synchroniseApp();
        } else {
            synchronizeAfterInterval();
        }

    }

    private void synchronizeAfterInterval() {

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }

        //set defaults
        filesCount.setText(0 + " / " + filesNumb);
        syncProgress.setText(0 + " %");
        filesChanged.setText("0");
        totalFiles = 0;
        filesNumb = 0;
        filesOnServer = 0;
        filesDownloaded = 0;
        filesChangedOnServer = 0;
        syncProgressBar.setProgress(0);
        hasSyncStarted = false;

        //get sync time
        long theInterval = Long.parseLong(Paper.book().read(Common.APP_SYNC_INTERVAL));
        mTimeLeftInMillis = theInterval * 60 * 1000;


        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;

                //update text view
                int hours = (int) mTimeLeftInMillis / (1000 * 60 * 60) % 24;
                int minutes = (int) mTimeLeftInMillis / (60 * 1000) % 60;
                int seconds = (int) mTimeLeftInMillis / 1000 % 60;

                String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

                countDownTimer.setText(timeLeftFormatted);
            }

            @Override
            public void onFinish() {

                mCountDownTimer.cancel();

                //synchronize
                if (isInternetAvailable) {
                    synchroniseApp();
                } else {
                    synchronizeAfterInterval();
                }
            }
        }.start();

    }

    private void synchroniseApp() {

        if (pageMode.equals(Common.PAGE_TEST_MODE)){
            Toast.makeText(this, "Sync Started", Toast.LENGTH_SHORT).show();
        }

        //sync type
        String syncScope = Paper.book().read(Common.CURRENT_SYNC_TYPE);

        //set state
        hasSyncStarted = true;

        if (syncScope != null) {
            switch (syncScope) {

                case Common.SYNC_TYPE_FTP_FOLDER:
                    countFiles();
                    break;

                case Common.SYNC_TYPE_FTP_ZIP:
                    countZip();
                    break;

                case Common.SYNC_TYPE_URL_ZIP:
                    countURLZip();
                    break;

                case Common.SYNC_TYPE_INDEX:
                    checkIndexFiles();
                    break;

                case Common.SYNC_TYPE_PARSE:
                    //string
                    String selectedMasterDomain = Paper.book().read(Common.CURRENT_MASTER_DOMAIN);
                    String theCompany = Paper.book().read(Common.COMPANY_ID);
                    String theLicence = Paper.book().read(Common.LICENCE_ID);
                    //page url
                    String pageUrl = selectedMasterDomain + "/" + theCompany + "/" + theLicence + "/App/Application/index.html";
                    try {
                        getParsedItems(theCompany, theLicence, pageUrl);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;

            }
        } else {

            Toast.makeText(this, "A fatal error has occurred. Please resync now", Toast.LENGTH_LONG).show();

        }

    }

    public void activeCount(){

        WebActivity.this.runOnUiThread(() -> {

            //files count
            filesOnServer++;
            filesCount.setText(filesDownloaded + " / " + filesOnServer);

        });

    }









    //parsing
    private void getParsedItems(String theCompany, String theLicence, String pageUrl) throws InterruptedException {

        //string
        String selectedMasterDomain = Paper.book().read(Common.CURRENT_MASTER_DOMAIN);

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

                //print progress
                WebActivity.this.runOnUiThread(() -> {

                    filesCount.setText(filesDownloaded + " / " + filesOnServer);

                    //start download
                    initializeParseDownload();

                });
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        //start thread
        parseThread.start();

    }

    private void getSecondaryParsedItems(String theCompany, String theLicence, String pageUrl) throws InterruptedException {

        //string
        String selectedMasterDomain = Paper.book().read(Common.CURRENT_MASTER_DOMAIN);

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

        //check sync type
        String syncType = Paper.book().read(Common.APP_SYNC_TYPE, Common.APP_SYNC_DOWNLOAD);

        //check file
        boolean isExist = new Database(getBaseContext()).fileExists(licenceStuff, filePath, fileName);
        boolean isChange = new Database(getBaseContext()).fileChanged(licenceStuff, filePath, fileName, timeFromCalender);

        //check sync type and update
        if (syncType.equals(Common.APP_SYNC_DOWNLOAD)) {

            if (!fileName.equals("configFile.txt")) {

                if (isExist){
                    //if file exists in database, update it
                    new Database(getBaseContext()).updateFileToHistory(licenceStuff, filePath, fileName, timeFromCalender, fileStatusPending);

                    //active count
                    //activeCount();

                } else {
                    //if file doesnt exist, add file to database
                    new Database(getBaseContext()).addFileToHistory(licenceStuff, filePath, localDir, fileName, downloadUrl, timeFromCalender, fileStatusPending);

                    //active count
                    activeCount();
                }

            }

        } else if (syncType.equals(Common.APP_SYNC_CHANGE)) {

            if (!fileName.equals("configFile.txt")) {

                if (isExist){

                    if (isChange){

                        new Database(getBaseContext()).updateFileToHistory(licenceStuff, filePath, fileName, timeFromCalender, fileStatusPending);

                        //active count
                        activeCount();

                    }

                } else {

                    new Database(getBaseContext()).addFileToHistory(licenceStuff, filePath, localDir, fileName, downloadUrl, timeFromCalender, fileStatusPending);

                    //active count
                    activeCount();

                }

            }

        }

    }

    private void initializeParseDownload() {



        //check sync type
        String syncType = Paper.book().read(Common.APP_SYNC_TYPE);
        String theCompany = Paper.book().read(Common.COMPANY_ID);
        String theLicence = Paper.book().read(Common.LICENCE_ID);

        //fetch all files to download
        List<FileHistory> allFilesToDownload = new Database(this).getIncompleteFiles(dbLicence);

        //initialize directory
        File baseDir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
        File licenceDir = new File(baseDir.getAbsolutePath(), Common.LICENCED_FOLDER_NAME);
        File theDir = new File(licenceDir.getAbsolutePath(),  theCompany + "-" + theLicence);


        //check type
        if (syncType.equals(Common.APP_SYNC_DOWNLOAD)){

            //update ui elements
            filesOnServer = allFilesToDownload.size();
            filesCount.setText(filesDownloaded + " / " + filesOnServer);
            filesChanged.setText("0");

        } else

        if (syncType.equals(Common.APP_SYNC_CHANGE)){

            //update ui elements
            filesOnServer = allFilesToDownload.size();
            filesCount.setText(filesDownloaded + " / " + filesOnServer);
            filesChanged.setText(String.valueOf(filesOnServer));

        }

        //check if list is empty
        if (allFilesToDownload.size() > 0){

            for (FileHistory downloadFile : allFilesToDownload){

                //directory
                File cre = new File(theDir.getAbsolutePath(), downloadFile.getFile_dir());
                if (!cre.exists()) {
                    cre.mkdirs();
                }

                //start download
                new DownloadFromUrl(new DownloadHelper() {
                    @Override
                    public void afterExecutionIsComplete() {

                        //download finished
                        runOnUiThread(() -> {

                            //calc
                            filesDownloaded++;

                            //ui update
                            filesCount.setText(filesDownloaded + " / " + filesOnServer);

                            //progress update
                            double temp = 100.0 * filesDownloaded;
                            syncProgress.setText((int) (temp / (double) filesOnServer) + " %");
                            syncProgressBar.setProgress((int) (temp / (double) filesOnServer));

                            //update download status
                            new Database(WebActivity.this).updateFileForCompleteDownload(dbLicence, downloadFile.getId(), Common.FILE_STATUS_DOWNLOADED);

                            //check if download is complete
                            if (filesDownloaded == filesOnServer){

                                //set new master domain list
                                setCustomDomains();

                                //reload
                                if (!isSchedule) {
                                    myWebView.reload();
                                }

                                //start countdown
                                synchronizeAfterInterval();

                            }


                        });

                    }

                    @Override
                    public void whenExecutionStarts() {

                    }

                    @Override
                    public void whileInProgress(int i) {

                    }

                }).execute(downloadFile.getFile_url(), cre.getAbsolutePath(), downloadFile.getFile_name());

            }

        } else {

            synchronizeAfterInterval();

        }

    }











    //folder type
    private void countFiles() {

        //strings
        String theHost = Paper.book().read(Common.FTP_HOST);
        String theDomain = Paper.book().read(Common.CURRENT_MASTER_DOMAIN);
        String theUsername = Paper.book().read(Common.FTP_USERNAME);
        String thePassword = Paper.book().read(Common.FTP_PASSWORD);
        int thePort = Integer.parseInt(Paper.book().read(Common.FTP_PORT));
        String theCompany = Paper.book().read(Common.COMPANY_ID);
        String theLicence = Paper.book().read(Common.LICENCE_ID);

        new Thread(() -> {

            try {
                final FTPClient ftp = new FTPClient();

                //init ftp file list
                FTPFile[] filesList = null;

                //FTP Server connection
                ftp.connect(InetAddress.getByName(theHost), thePort);
                ftp.login(theUsername, thePassword);
                ftp.enterLocalPassiveMode();

                //set buffer size and ftp directory
                ftp.setBufferSize(1024);
                filesList = ftp.listFiles("/" + theCompany + "/" + theLicence + "/App/");
                ftp.setFileType(FTP.BINARY_FILE_TYPE);

                for (FTPFile theFile : filesList){

                    if (!theFile.getName().equals(".") && !theFile.getName().equals("..")) {

                        if (theFile.isDirectory()){

                            String filePath = theCompany + "/" + theLicence + "/App/" + theFile.getName();
                            String saveDirToPath = "/" + theFile.getName();

                            //sort folder
                            countFilesInDirectory(ftp, filePath, "", saveDirToPath, theUsername, thePassword, theCompany, theLicence);

                        } else {

                            //create ftp download
                            String fileName = theFile.getName();
                            String filePath = theCompany + "/" + theLicence + "/App/" + theFile.getName();

                            //generate url
                            String downloadUrl = theDomain + "/" + filePath;

                            //add file to db
                            fillInFileList(dbLicence, filePath, fileName, downloadUrl, Methods.getTimeFromCalender(this, theFile.getTimestamp()), Common.FILE_STATUS_PENDING, "");

                        }

                    }

                }

                //print progress
                WebActivity.this.runOnUiThread(() -> {

                    filesCount.setText(filesDownloaded + " / " + filesOnServer);

                    //start download
                    initializeDownload();

                });

                ftp.logout();
                ftp.disconnect();
            } catch (Exception e) {

                WebActivity.this.runOnUiThread(() -> {

                    //error
                    Toast.makeText(this, "Check Internet Connection", Toast.LENGTH_LONG).show();

                });

                e.printStackTrace();
            }

        }).start();

    }

    private void countFilesInDirectory(FTPClient ftpClient, String parentDir, String currentDir, String saveDir, String theUsername, String thePassword, String theCompany, String theLicence) {

        //get domain
        String theDomain = Paper.book().read(Common.CURRENT_MASTER_DOMAIN);

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

                        //generate url
                        String downloadUrl = theDomain + "/" + filePath;

                        //update file list
                        fillInFileList(dbLicence, filePath, fileName, downloadUrl, Methods.getTimeFromCalender(this, aFile.getTimestamp()), Common.FILE_STATUS_PENDING, saveDir);

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

            WebActivity.this.runOnUiThread(() -> {
                Toast.makeText(this, "Check Internet Connection", Toast.LENGTH_LONG).show();
            });
        }

    }

    private void fillInFileList(String licenceStuff, String filePath, String fileName, String downloadUrl, String timeFromCalender, String fileStatusPending, String localDir) {

        //check sync type
        String syncType = Paper.book().read(Common.APP_SYNC_TYPE, Common.APP_SYNC_DOWNLOAD);

        //check file
        boolean isExist = new Database(getBaseContext()).fileExists(licenceStuff, filePath, fileName);
        boolean isChange = new Database(getBaseContext()).fileChanged(licenceStuff, filePath, fileName, timeFromCalender);

        //check sync type and update
        if (syncType.equals(Common.APP_SYNC_DOWNLOAD)) {

            if (!fileName.equals("configFile.txt")) {

                if (isExist){
                    //if file exists in database, update it
                    new Database(getBaseContext()).updateFileToHistory(licenceStuff, filePath, fileName, timeFromCalender, fileStatusPending);

                    //active count
                    activeCount();

                } else {
                    //if file doesnt exist, add file to database
                    new Database(getBaseContext()).addFileToHistory(licenceStuff, filePath, localDir, fileName, downloadUrl, timeFromCalender, fileStatusPending);

                    //active count
                    activeCount();
                }

            }

        } else if (syncType.equals(Common.APP_SYNC_CHANGE)) {

            if (!fileName.equals("configFile.txt")) {

                if (isExist){

                    if (isChange){

                        new Database(getBaseContext()).updateFileToHistory(licenceStuff, filePath, fileName, timeFromCalender, fileStatusPending);

                        //active count
                        activeCount();

                    }

                } else {

                    new Database(getBaseContext()).addFileToHistory(licenceStuff, filePath, localDir, fileName, downloadUrl, timeFromCalender, fileStatusPending);

                    //active count
                    activeCount();

                }

            }

        }


    }

    private void initializeDownload() {



        //check sync type
        String syncType = Paper.book().read(Common.APP_SYNC_TYPE);
        String theCompany = Paper.book().read(Common.COMPANY_ID);
        String theLicence = Paper.book().read(Common.LICENCE_ID);

        //fetch all files to download
        List<FileHistory> allFilesToDownload = new Database(this).getIncompleteFiles(dbLicence);

        //initialize directory
        File baseDir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
        File licenceDir = new File(baseDir.getAbsolutePath(), Common.LICENCED_FOLDER_NAME);
        File theDir = new File(licenceDir.getAbsolutePath(),  theCompany + "-" + theLicence);


        //check type
        if (syncType.equals(Common.APP_SYNC_DOWNLOAD)){

            //update ui elements
            filesOnServer = allFilesToDownload.size();
            filesCount.setText(filesDownloaded + " / " + filesOnServer);
            filesChanged.setText("0");

        } else

        if (syncType.equals(Common.APP_SYNC_CHANGE)){

            //update ui elements
            filesOnServer = allFilesToDownload.size();
            filesCount.setText(filesDownloaded + " / " + filesOnServer);
            filesChanged.setText(String.valueOf(filesOnServer));

        }

        //check if list is empty
        if (allFilesToDownload.size() > 0){

            for (FileHistory downloadFile : allFilesToDownload){

                //directory
                File cre = new File(theDir.getAbsolutePath(), downloadFile.getFile_dir());
                if (!cre.exists()) {
                    cre.mkdir();
                }

                //start download
                new DownloadFromUrl(new DownloadHelper() {
                    @Override
                    public void afterExecutionIsComplete() {

                        //download finished
                        runOnUiThread(() -> {

                            //calc
                            filesDownloaded++;

                            //ui update
                            filesCount.setText(filesDownloaded + " / " + filesOnServer);

                            //progress update
                            double temp = 100.0 * filesDownloaded;
                            syncProgress.setText((int) (temp / (double) filesOnServer) + " %");
                            syncProgressBar.setProgress((int) (temp / (double) filesOnServer));

                            //update download status
                            new Database(WebActivity.this).updateFileForCompleteDownload(dbLicence, downloadFile.getId(), Common.FILE_STATUS_DOWNLOADED);

                            //check if download is complete
                            if (filesDownloaded == filesOnServer){

                                //set new master domain list
                                setCustomDomains();

                                //reload
                                if (!isSchedule) {
                                    myWebView.reload();
                                }

                                //start countdown
                                synchronizeAfterInterval();

                            }


                        });

                    }

                    @Override
                    public void whenExecutionStarts() {

                    }

                    @Override
                    public void whileInProgress(int i) {

                    }

                }).execute(downloadFile.getFile_url(), cre.getAbsolutePath(), downloadFile.getFile_name());

            }

        } else {

            synchronizeAfterInterval();

        }

    }









    //index
    private void checkIndexFiles() {

        //get domain
        String theDomain = Paper.book().read(Common.CURRENT_MASTER_DOMAIN);
        String theCompany = Paper.book().read(Common.COMPANY_ID);
        String theLicence = Paper.book().read(Common.LICENCE_ID);

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
                ftp.connect(InetAddress.getByName(Paper.book().read(Common.FTP_HOST)), 21);
                ftp.login(Paper.book().read(Common.FTP_USERNAME), Paper.book().read(Common.FTP_PASSWORD));
                ftp.enterLocalPassiveMode();

                //get licence
                String companyId = Paper.book().read(Common.COMPANY_ID);
                String licenceKey = Paper.book().read(Common.LICENCE_ID);

                //set buffer size and ftp directory
                ftp.setBufferSize(1024);
                filesList = ftp.listFiles( companyId + "/" + licenceKey + "/App/Application/");
                ftp.setFileType(FTP.BINARY_FILE_TYPE);

                for (FTPFile theFile : filesList){

                    if (!theFile.getName().equals(".") && !theFile.getName().equals("..")) {

                        if (theFile.getName().equals("index.html")) {

                            //create ftp download
                            String fileName = theFile.getName();
                            String filePath = theCompany + "/" + theLicence + "/App/Application/" + theFile.getName();

                            //generate url
                            String downloadUrl = theDomain + "/" + filePath;

                            //check file
                            boolean isExist = new Database(getBaseContext()).fileExists(dbLicence, filePath, fileName);
                            boolean isChange = new Database(getBaseContext()).fileChanged(dbLicence, filePath, fileName, Methods.getTimeFromCalender(WebActivity.this, theFile.getTimestamp()));

                            //check sync type and update
                            if (isExist){

                                if (isChange){

                                    countIndexFiles();

                                } else {

                                    WebActivity.this.runOnUiThread(this::synchronizeAfterInterval);

                                }

                            } else {

                                countIndexFiles();

                            }
                        }

                    }

                }

                ftp.logout();
                ftp.disconnect();
            } catch (Exception e) {

                WebActivity.this.runOnUiThread(() -> {

                    //error
                    Toast.makeText(this, "Check Internet Connection", Toast.LENGTH_LONG).show();

                });

                e.printStackTrace();
            }

        }).start();

    }

    private void countIndexFiles() {

        //strings
        String theHost = Paper.book().read(Common.FTP_HOST);
        String theDomain = Paper.book().read(Common.CURRENT_MASTER_DOMAIN);
        String theUsername = Paper.book().read(Common.FTP_USERNAME);
        String thePassword = Paper.book().read(Common.FTP_PASSWORD);
        int thePort = Integer.parseInt(Paper.book().read(Common.FTP_PORT));
        String theCompany = Paper.book().read(Common.COMPANY_ID);
        String theLicence = Paper.book().read(Common.LICENCE_ID);

        new Thread(() -> {

            try {
                final FTPClient ftp = new FTPClient();

                //init ftp file list
                FTPFile[] filesList = null;

                //FTP Server connection
                ftp.connect(InetAddress.getByName(theHost), thePort);
                ftp.login(theUsername, thePassword);
                ftp.enterLocalPassiveMode();

                //set buffer size and ftp directory
                ftp.setBufferSize(1024);
                filesList = ftp.listFiles("/" + theCompany + "/" + theLicence + "/App/");
                ftp.setFileType(FTP.BINARY_FILE_TYPE);

                for (FTPFile theFile : filesList){

                    if (!theFile.getName().equals(".") && !theFile.getName().equals("..")) {

                        if (theFile.isDirectory()){

                            String filePath = theCompany + "/" + theLicence + "/App/" + theFile.getName();
                            String saveDirToPath = "/" + theFile.getName();

                            //sort folder
                            countIndexFilesInDirectory(ftp, filePath, "", saveDirToPath, theUsername, thePassword, theCompany, theLicence);

                        } else {

                            //create ftp download
                            String fileName = theFile.getName();
                            String filePath = theCompany + "/" + theLicence + "/App/" + theFile.getName();

                            //generate url
                            String downloadUrl = theDomain + "/" + filePath;

                            //add file to db
                            fillInIndexFileList(dbLicence, filePath, fileName, downloadUrl, Methods.getTimeFromCalender(this, theFile.getTimestamp()), Common.FILE_STATUS_PENDING, "");

                        }

                    }

                }

                //print progress
                WebActivity.this.runOnUiThread(() -> {

                    filesCount.setText(filesDownloaded + " / " + filesOnServer);

                    //start download
                    initializeDownload();

                });

                ftp.logout();
                ftp.disconnect();
            } catch (Exception e) {

                WebActivity.this.runOnUiThread(() -> {

                    //error
                    Toast.makeText(this, "Check Internet Connection", Toast.LENGTH_LONG).show();

                });

                e.printStackTrace();
            }

        }).start();

    }

    private void countIndexFilesInDirectory(FTPClient ftpClient, String parentDir, String currentDir, String saveDir, String theUsername, String thePassword, String theCompany, String theLicence) {

        //get domain
        String theDomain = Paper.book().read(Common.CURRENT_MASTER_DOMAIN);

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
                        countIndexFilesInDirectory(ftpClient, dirToList, newDirToList,
                                newDirOnLocal, theUsername, thePassword, theCompany, theLicence);
                    } else {
                        //create ftp download
                        String fileName = aFile.getName();

                        //generate url
                        String downloadUrl = theDomain + "/" + filePath;

                        //update file list
                        fillInIndexFileList(dbLicence, filePath, fileName, downloadUrl, Methods.getTimeFromCalender(this, aFile.getTimestamp()), Common.FILE_STATUS_PENDING, saveDir);

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

            WebActivity.this.runOnUiThread(() -> {
                Toast.makeText(this, "Check Internet Connection", Toast.LENGTH_LONG).show();
            });
        }

    }

    private void fillInIndexFileList(String licenceStuff, String filePath, String fileName, String downloadUrl, String timeFromCalender, String fileStatusPending, String localDir) {

        //check file
        boolean isExist = new Database(getBaseContext()).fileExists(licenceStuff, filePath, fileName);

        if (!fileName.equals("configFile.txt")) {
            if (isExist) {
                //if file exists in database, update it
                new Database(getBaseContext()).updateFileToHistory(licenceStuff, filePath, fileName, timeFromCalender, fileStatusPending);

                //active count
                activeCount();

            } else {
                //if file doesnt exist, add file to database
                new Database(getBaseContext()).addFileToHistory(licenceStuff, filePath, localDir, fileName, downloadUrl, timeFromCalender, fileStatusPending);

                //active count
                activeCount();
            }
        }


    }









    //zip type
    private void countZip() {

        //get domain
        String theDomain = Paper.book().read(Common.CURRENT_MASTER_DOMAIN);
        String theCompany = Paper.book().read(Common.COMPANY_ID);
        String theLicence = Paper.book().read(Common.LICENCE_ID);

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
                ftp.connect(InetAddress.getByName(Paper.book().read(Common.FTP_HOST)), 21);
                ftp.login(Paper.book().read(Common.FTP_USERNAME), Paper.book().read(Common.FTP_PASSWORD));
                ftp.enterLocalPassiveMode();

                //get licence
                String companyId = Paper.book().read(Common.COMPANY_ID);
                String licenceKey = Paper.book().read(Common.LICENCE_ID);

                //set buffer size and ftp directory
                ftp.setBufferSize(1024);
                filesList = ftp.listFiles( companyId + "/" + licenceKey + "/Zip/");
                ftp.setFileType(FTP.BINARY_FILE_TYPE);

                for (FTPFile theFile : filesList){

                    if (!theFile.getName().equals(".") && !theFile.getName().equals("..")) {

                        if (theFile.getName().endsWith(".zip")) {

                            //create ftp download
                            String fileName = theFile.getName();
                            String filePath = theCompany + "/" + theLicence + "/Zip/" + theFile.getName();

                            //generate url
                            String downloadUrl = theDomain + "/" + filePath;

                            //add file to db
                            fillInZipFileList(dbLicence, filePath, fileName, downloadUrl, Methods.getTimeFromCalender(this, theFile.getTimestamp()), Common.FILE_STATUS_PENDING, "");

                        }

                    }

                }

                //print progress
                WebActivity.this.runOnUiThread(() -> {

                    filesCount.setText(filesDownloaded + " / " + filesOnServer);

                    //start download
                    initializeZipDownload();

                });

                ftp.logout();
                ftp.disconnect();
            } catch (Exception e) {

                WebActivity.this.runOnUiThread(() -> {

                    //error
                    Toast.makeText(this, "Check Internet Connection", Toast.LENGTH_LONG).show();

                });

                e.printStackTrace();
            }

        }).start();

    }

    private void countURLZip() {

        String domain = Paper.book().read(Common.CURRENT_MASTER_DOMAIN);
        String company = Paper.book().read(Common.COMPANY_ID);
        String licence = Paper.book().read(Common.LICENCE_ID);

        new Thread(() -> {

            try {
                //build the url
                String file_url =  domain + "/" + company + "/" + licence + "/Zip/App.zip";

                URL url = new URL(file_url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int code = connection.getResponseCode();

                if(code == 200) {

                    this.runOnUiThread(() -> {

                        try {
                            connection.getInputStream();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        long theFileSize = connection.getContentLength();

                        String filePath = company + "/" + licence + "/Zip/App.zip";
                        String fileName = "App.zip";

                        //update file list
                        fillInURLZipFileList(dbLicence, filePath, fileName, file_url, String.valueOf(theFileSize), Common.FILE_STATUS_PENDING, Common.LICENCE_FOLDER_DOWNLOAD_NAME);

                        //initalize zip download
                        initializeZipDownload();

                    });

                } else {

                    this.runOnUiThread(() -> {

                        //Toast
                        Toast.makeText(this, "404 File non fount", Toast.LENGTH_SHORT).show();

                    });

                }
            } catch (Exception jayWalk){

                jayWalk.printStackTrace();

            }

        }).start();

    }

    private void fillInZipFileList(String licenceStuff, String filePath, String fileName, String downloadUrl, String timeFromCalender, String fileStatusPending, String localDir) {

        //check sync type
        String syncType = Paper.book().read(Common.APP_SYNC_TYPE, Common.APP_SYNC_DOWNLOAD);

        //check file
        boolean isExist = new Database(getBaseContext()).fileExists(licenceStuff, filePath, fileName);
        boolean isChange = new Database(getBaseContext()).fileChanged(licenceStuff, filePath, fileName, timeFromCalender);

        //check sync type and update
        if (syncType.equals(Common.APP_SYNC_DOWNLOAD)) {

            if (isExist){
                //if file exists in database, update it
                new Database(getBaseContext()).updateFileToHistory(licenceStuff, filePath, fileName, timeFromCalender, fileStatusPending);

                //active count
                activeCount();

            } else {
                //if file doesnt exist, add file to database
                new Database(getBaseContext()).addFileToHistory(licenceStuff, filePath, localDir, fileName, downloadUrl, timeFromCalender, fileStatusPending);

                //active count
                activeCount();
            }

        } else if (syncType.equals(Common.APP_SYNC_CHANGE)) {

            if (isExist){

                if (isChange){

                    new Database(getBaseContext()).updateFileToHistory(licenceStuff, filePath, fileName, timeFromCalender, fileStatusPending);

                    //active count
                    activeCount();

                }

            } else {

                new Database(getBaseContext()).addFileToHistory(licenceStuff, filePath, localDir, fileName, downloadUrl, timeFromCalender, fileStatusPending);

                //active count
                activeCount();

            }

        }


    }

    private void fillInURLZipFileList(String licenceStuff, String filePath, String fileName, String downloadUrl, String timeFromCalender, String fileStatusPending, String localDir) {

        //check sync type
        String syncType = Paper.book().read(Common.APP_SYNC_TYPE, Common.APP_SYNC_DOWNLOAD);

        //check file
        boolean isExist = new Database(getBaseContext()).fileExists(licenceStuff, filePath, fileName);
        boolean isChange = new Database(getBaseContext()).fileChanged(licenceStuff, filePath, fileName, timeFromCalender);


        //check sync type and update
        //check sync type and update
        if (syncType.equals(Common.APP_SYNC_DOWNLOAD)) {

            if (isExist){
                //if file exists in database, update it
                new Database(getBaseContext()).updateFileToHistory(licenceStuff, filePath, fileName, timeFromCalender, fileStatusPending);

                //active count
                activeCount();

            } else {
                //if file doesnt exist, add file to database
                new Database(getBaseContext()).addFileToHistory(licenceStuff, filePath, localDir, fileName, downloadUrl, timeFromCalender, fileStatusPending);

                //active count
                activeCount();
            }

        } else if (syncType.equals(Common.APP_SYNC_CHANGE)) {

            if (isExist){

                if (isChange){

                    new Database(getBaseContext()).updateFileToHistory(licenceStuff, filePath, fileName, timeFromCalender, fileStatusPending);

                    //active count
                    activeCount();

                }

            } else {

                new Database(getBaseContext()).addFileToHistory(licenceStuff, filePath, localDir, fileName, downloadUrl, timeFromCalender, fileStatusPending);

                //active count
                activeCount();

            }

        }


    }

    private void initializeZipDownload() {

        //check sync type
        String syncType = Paper.book().read(Common.APP_SYNC_TYPE);
        String theCompany = Paper.book().read(Common.COMPANY_ID);
        String theLicence = Paper.book().read(Common.LICENCE_ID);

        //fetch all files to download
        List<FileHistory> allFilesToDownload = new Database(this).getIncompleteFiles(dbLicence);

        //check type
        if (syncType.equals(Common.APP_SYNC_DOWNLOAD)){

            //update ui elements
            filesOnServer = allFilesToDownload.size();
            filesCount.setText(filesDownloaded + " / " + filesOnServer);
            filesChanged.setText("1");

        } else

        if (syncType.equals(Common.APP_SYNC_CHANGE)){

            //update ui elements
            filesOnServer = allFilesToDownload.size();
            filesCount.setText(filesDownloaded + " / " + filesOnServer);
            filesChanged.setText(String.valueOf(filesOnServer));

        }

        //check if list is empty
        if (allFilesToDownload.size() > 0){

            for (FileHistory downloadFile : allFilesToDownload){

                //initialize directory
                File baseDir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
                File licenceDir = new File(baseDir.getAbsolutePath(), Common.LICENCE_FOLDER_DOWNLOAD_NAME);

                //directory
                File cre = new File(licenceDir.getAbsolutePath(), downloadFile.getFile_dir());

                //start download
                new DownloadFromUrl(new DownloadHelper() {
                    @Override
                    public void afterExecutionIsComplete() {

                        //download finished
                        runOnUiThread(() -> {

                            //calc
                            filesDownloaded++;

                            //ui update
                            filesCount.setText(filesDownloaded + " / " + filesOnServer);

                            //update download status
                            new Database(WebActivity.this).updateFileForCompleteDownload(dbLicence, downloadFile.getId(), Common.FILE_STATUS_DOWNLOADED);

                            //check if download is complete
                            if (filesDownloaded == filesOnServer){
                                //initialize directory
                                File baseDir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
                                File licenceDir = new File(baseDir.getAbsolutePath(), Common.LICENCE_FOLDER_DOWNLOAD_NAME);

                                //extract zip
                                File downloadedFile = new File(licenceDir.getAbsolutePath(), downloadFile.getFile_name());
                                extractDownload(downloadedFile);

                            }


                        });

                    }

                    @Override
                    public void whenExecutionStarts() {

                    }

                    @Override
                    public void whileInProgress(int i) {

                        runOnUiThread(() -> {
                            //progress update
                            syncProgress.setText(i + " %");
                            syncProgressBar.setProgress(i);
                        });

                    }

                }).execute(downloadFile.getFile_url(), licenceDir.getAbsolutePath(), downloadFile.getFile_name());
            }

        } else {

            synchronizeAfterInterval();

        }

    }

    private void extractDownload(File downloadFile) {

        //get saved licence
        String companyId = Paper.book().read(Common.COMPANY_ID);
        String licenceKey = Paper.book().read(Common.LICENCE_ID);

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
            if (!destinationFolder.exists()) {
                destinationFolder.mkdirs();
            }

            //unzip files
            ZipManager zipManager = new ZipManager();
            zipManager.unzip(downloadFile, destinationFolder, false);

            //set timer for extraction
            zipTimer = new Timer();
            zipTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    new Handler(Looper.getMainLooper()).post(() -> {

                        WebActivity.this.runOnUiThread(() -> {

                            //set new master domain list
                            setCustomDomains();

                            //reload
                            if (!isSchedule) {
                                myWebView.loadUrl(url);
                            }

                            //start countdown again
                            synchronizeAfterInterval();

                        });


                    });
                }
            }, 25000);

        } else {

            Toast.makeText(this, "File doesn't exist", Toast.LENGTH_SHORT).show();

        }

    }

    public static boolean unzip(File zipFile, File destinationDir) {
        ZipFile zip = null;
        try {
            destinationDir.mkdirs();
            zip = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();
            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = zipFileEntries.nextElement();
                String entryName = entry.getName();
                File destFile = new File(destinationDir, entryName);
                File destinationParent = destFile.getParentFile();
                if (destinationParent != null && !destinationParent.exists()) {
                    destinationParent.mkdirs();
                }
                if (!entry.isDirectory()) {

                    if (!entry.getName().equals("configFile.txt")) {
                        BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                        int currentByte;
                        byte data[] = new byte[8192];
                        FileOutputStream fos = new FileOutputStream(destFile);
                        BufferedOutputStream dest = new BufferedOutputStream(fos, 8192);
                        while ((currentByte = is.read(data, 0, 8192)) != -1) {
                            dest.write(data, 0, currentByte);
                        }
                        dest.flush();
                        dest.close();
                        is.close();
                    }
                }
            }
        } catch (Exception e) {
            return false;
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                    zipFile.delete();

                } catch (IOException ignored) {
                }
            }
        }
        return true;
    }






    //switch test mode
    private void switchTestMode(){

        if (pageMode.equals(Common.PAGE_TEST_MODE)) {

            if (testMode.equals(Common.TEST_ONE_OFF)) {

                Paper.book().write(Common.IS_IN_TEST_MODE, Common.NO);

            }

        }

    }






    //set custom domains
    private void setCustomDomains(){

        String companyId = Paper.book().read(Common.COMPANY_ID);
        String licenceKey = Paper.book().read(Common.LICENCE_ID);

        //check if file exist in directory and populate spinner
        File domainFile = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME + "/" + Common.LICENCED_FOLDER_NAME +
                "/" + companyId + "-" + licenceKey + "/" + Common.USER_CONFIG_FOLDER + "/" + Common.CUSTOM_DOMAINS_FILE);
        if (domainFile.exists()) {

            //create file
            List<Domains> domainData = new ArrayList<>();
            domainData.add(new Domains("", "", "", "", ""));

            //read csv
            try {
                CSVReader reader = new CSVReader(new FileReader(domainFile.getAbsolutePath()));
                String[] nextLine;

                //skip one
                reader.readNext();

                while ((nextLine = reader.readNext()) != null) {

                    //populate spinner and background data
                    if (nextLine.length == 5) {
                        domainData.add(new Domains(nextLine[0], nextLine[1], nextLine[2], nextLine[3], nextLine[4]));
                    }

                }

            } catch (IOException e) {
                Toast.makeText(this, "CSV Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {

                //save domains
                Paper.book().write(Common.MASTER_DOMAINS, domainData);

                //delete default domain
                domainFile.delete();
            }

        } else {

            Log.d("DomainCsv", "Domain File Not Found");

        }

    }




















    //schedule
    private void runScheduleCheck(){

        //check
        if (Paper.book().read(Common.CURRENT_DAY) == null || TextUtils.isEmpty(Paper.book().read(Common.CURRENT_DAY))){

            Paper.book().write(Common.CURRENT_DAY, today());

        }

        //get saved licence
        String companyId = Paper.book().read(Common.COMPANY_ID);
        String licenceKey = Paper.book().read(Common.LICENCE_ID);

        //get all
        File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME + "/" + Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey + "/" + Common.USER_SCHEDULE_FOLDER);
        if (!dir.exists()) {
            dir.mkdir();
        }

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

        //change day and clear lists
        if (!Paper.book().read(Common.CURRENT_DAY).equals(today())){

            //set day to current day
            Paper.book().write(Common.CURRENT_DAY, today());

            //clear arrays
            tempList.clear();
            theSchedules.clear();
            setAlarms.clear();
            enteredSchedules.clear();
            enteredAlarms.clear();

        }

        //read csv
        try {
            CSVReader reader = new CSVReader(new FileReader(newFile));
            String[] nextLine;
            int count = 0;

            //read csv file
            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                count++;

                tempList.add(new Schedule(nextLine[0], nextLine[1], Boolean.parseBoolean(nextLine[2].toLowerCase()), Boolean.parseBoolean(nextLine[3].toLowerCase()), Boolean.parseBoolean(nextLine[4].toLowerCase()), nextLine[5], nextLine[6], nextLine[7], nextLine[8], nextLine[9], nextLine[10]));

            }

            //populate actual schedules list when they meet set conditions
            for (int i = 1; i < tempList.size(); i++) {

                if (tempList.get(i).getDay().equals(today()) || tempList.get(i).isDaily() || tempList.get(i).getDate().equals(todayDate()) || (tempList.get(i).isWeekly() && tempList.get(i).getDay().equals(today()))){

                    if (timeDifference(tempList.get(i).getStartTime()) > 0 || dayDifference(tempList.get(i).getStartTime()) > 0){

                        if (!enteredSchedules.contains(tempList.get(i).getId())) {
                            theSchedules.add(tempList.get(i));
                            enteredSchedules.add(tempList.get(i).getId());
                        }

                    }

                }

            }

            //sort
            //Collections.sort(theSchedules, (o1, o2) -> o1.getStartTime().compareTo(o2.getStartTime()));
            theSchedules.sort(Comparator.comparing(Schedule::getStartTime));

            //text
            if (theSchedules.size() > 0){

                scheduleStart.setText(theSchedules.get(0).getStartTime());
                scheduleEnd.setText(theSchedules.get(0).getStopTime());

            } else {

                scheduleStart.setText("None");
                scheduleEnd.setText("None");

            }

            //set alarm
            setAlarm(theSchedules);

        } catch (IOException e) {

        }

    }

    private String today(){

        //get today in millis
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        return new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());

    }

    private String todayDate(){

        return  new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());

    }

    public long timeDifference(String providedTime){

        long currentTimeMillis = System.currentTimeMillis();

        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String currentTime = format.format(currentTimeMillis);

        Date providedDate = null;
        Date currentDate = null;
        long difference = 0;

        try {

            providedDate = format.parse(providedTime);
            currentDate = format.parse(currentTime);
            difference = providedDate.getTime() - currentDate.getTime();


        } catch (ParseException e) {
            e.printStackTrace();
            Log.d("Schedule", "Time Difference: " + e.getMessage());
        }

        return difference;
    }

    public long dayDifference(String providedTime){

        long currentTimeMillis = System.currentTimeMillis();

        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String currentTime = format.format(currentTimeMillis);

        Date providedDate = null;
        Date currentDate = null;
        long difference = 0;

        try {

            providedDate = format.parse(providedTime);
            currentDate = format.parse(currentTime);
            difference = providedDate.getTime() - currentDate.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
            Log.d("Schedule", "Day Difference: " + e.getMessage());
        }

        return difference;

    }

    private void setAlarm(List<Schedule> theSchedules) {

        //check if empty
        if (theSchedules.size() > 0) {

            //current time
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            String currentTime = format.format(System.currentTimeMillis());

            //loop through today schedules
            for (int i = 0; i < theSchedules.size(); i++) {

                if (theSchedules.get(i).getStartTime().equals(currentTime) && !isScheduleCurrentlyRunning){

                    String theId = theSchedules.get(i).getId();
                    String theUrl = theSchedules.get(i).getRedirect_url();
                    String theEndTime = theSchedules.get(i).getStopTime();
                    boolean isOneOff = theSchedules.get(i).isOneTime();
                    String theDuration = theSchedules.get(i).getDuration();

                    //start schedule
                    loadScheduleUrl(theId, theUrl, theEndTime, isOneOff, i);

                    //print
                    Toast.makeText(getApplicationContext(), "Scheduled Started", Toast.LENGTH_SHORT).show();

                    //change state
                    isScheduleCurrentlyRunning = true;

                    //start end time check
                    startEndTimeCheck(theEndTime, theId, i);
                    break;

                }

            }

        }

        //repeat
        handlerSchedule.postDelayed(runnableSchedule = this::runScheduleCheck, 32000);

    }

    private void startEndTimeCheck(String scheduleEndTime, String scheduleId, int position){

        //current time
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String currentTime = format.format(System.currentTimeMillis());

        if (currentTime.equals(scheduleEndTime)){

            Log.d("ServiceStop", "Service is ready to stop stopped");

            //remove timer
            handlerRunningSchedule.removeCallbacks(runnableRunningSchedule);

            //stop schedule
            stopSchedule(scheduleId, position);

            //print
            Toast.makeText(getApplicationContext(), "Scheduled Finished", Toast.LENGTH_SHORT).show();

        } else {

            Log.d("ServiceStop", "Service not yet time");

            handlerRunningSchedule.postDelayed(runnableRunningSchedule = () -> startEndTimeCheck(scheduleEndTime, scheduleId, position), 32000);

        }

    }

}