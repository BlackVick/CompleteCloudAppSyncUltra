package com.cloudappsync.ultra.Services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.cloudappsync.ultra.Basic.BasicWebActivity;
import com.cloudappsync.ultra.R;
import com.cloudappsync.ultra.Utilities.Common;
import com.cloudappsync.ultra.Ultra.WebActivity;
import com.squareup.picasso.Picasso;

import java.io.File;

import io.paperdb.Paper;

public class BubbleService extends Service {


    private WindowManager mWindowManager;
    private View mOverlayView;
    int mWidth;
    private ImageView counterFab;
    boolean activity_background;
    public boolean isRunning = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        if (intent != null) {
            activity_background = intent.getBooleanExtra("activity_background", false);
        }

        if (mOverlayView == null) {

            mOverlayView = LayoutInflater.from(this).inflate(R.layout.float_btn_layout, null);


            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);


            //Specify the view position
            params.gravity = Gravity.CENTER;        //Initially view will be added to top-left corner
            params.x = 0;
            params.y = 0;


            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            mWindowManager.addView(mOverlayView, params);

            Display display = mWindowManager.getDefaultDisplay();
            final Point size = new Point();
            display.getSize(size);

            counterFab = (ImageView) mOverlayView.findViewById(R.id.fabHead);

            //branding
            //custom image
            if (Paper.book().read(Common.CUSTOM_VISIT_IMAGE) != null && !TextUtils.isEmpty(Paper.book().read(Common.CUSTOM_VISIT_IMAGE))){

                String uri = Paper.book().read(Common.CUSTOM_VISIT_IMAGE);

                //display image
                Picasso.get()
                        .load(uri)
                        .config(Bitmap.Config.RGB_565)
                        .fit().centerCrop()
                        .into(counterFab);

            }


            final RelativeLayout layout = (RelativeLayout) mOverlayView.findViewById(R.id.layout);
            ViewTreeObserver vto = layout.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int width = layout.getMeasuredWidth();

                    //To get the accurate middle of the screen we subtract the width of the floating widget.
                    mWidth = size.x - width;

                }
            });

            counterFab.setOnTouchListener(new View.OnTouchListener() {
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            //remember the initial position.
                            initialX = params.x;
                            initialY = params.y;


                            //get the touch location
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();


                            return true;
                        case MotionEvent.ACTION_UP:

                            //Only start the activity if the application is in background. Pass the current badge_count to the activityif(activity_background){
                            float xDiff = event.getRawX() - initialTouchX;
                            float yDiff = event.getRawY() - initialTouchY;

                            if ((Math.abs(xDiff) < 10) && (Math.abs(yDiff) < 10)) {
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
                                        webIntent = new Intent(getBaseContext(), WebActivity.class);
                                    } else {
                                        webIntent = new Intent(getBaseContext(), BasicWebActivity.class);
                                    }
                                    webIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_NO_SYNC_MODE);
                                    webIntent.putExtra(Common.WEB_PAGE_TYPE, Common.LOAD_FROM_ONLINE);
                                    webIntent.putExtra(Common.WEB_PAGE_INTENT, theUrl);
                                    startActivity(webIntent);

                                    stopSelf();

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
                                        webIntent = new Intent(getBaseContext(), WebActivity.class);
                                    } else {
                                        webIntent = new Intent(getBaseContext(), BasicWebActivity.class);
                                    }
                                    webIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_NO_SYNC_MODE);
                                    webIntent.putExtra(Common.WEB_PAGE_TYPE, Common.LOAD_FROM_LOCAL);
                                    webIntent.putExtra(Common.WEB_PAGE_INTENT, "file:///" + liDir.getAbsolutePath() + "/index.html");
                                    startActivity(webIntent);

                                    stopSelf();

                                }

                                //close the service and remove the fab view
                            }

                            //Logic to auto-position the widget based on where it is positioned currently w.r.t middle of the screen.
                            int middle = mWidth / 2;
                            float nearestXWall = params.x >= middle ? mWidth : 0;
                            params.x = (int) nearestXWall;


                            mWindowManager.updateViewLayout(mOverlayView, params);
                            return true;
                        case MotionEvent.ACTION_MOVE:


                            int xDiff2 = Math.round(event.getRawX() - initialTouchX);
                            int yDiff2 = Math.round(event.getRawY() - initialTouchY);


                            //Calculate the X and Y coordinates of the view.
                            params.x = initialX + xDiff2;
                            params.y = initialY + yDiff2;

                            //Update the layout with new X & Y coordinates
                            mWindowManager.updateViewLayout(mOverlayView, params);


                            return true;
                    }
                    return false;
                }
            });
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setTheme(R.style.UltraLightTheme);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (mOverlayView != null)
            mWindowManager.removeView(mOverlayView);
    }

}