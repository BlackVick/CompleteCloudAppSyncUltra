package com.cloudappsync.ultra.Receivers;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.cloudappsync.ultra.Ultra.WebActivity;

public class TimedRedirectReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //get intent data
        if (intent != null){
            //intent.getStringExtra()
        }


        try {
            if (isOnline(context)) {
                WebActivity.isInternetConnected(true);
                return;
            }
            WebActivity.isInternetConnected(false);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private boolean isOnline(Context context) {
        NetworkInfo[] info;
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Service.CONNECTIVITY_SERVICE);
        if (manager == null || (info = manager.getAllNetworkInfo()) == null) {
            return false;
        }
        for (NetworkInfo state : info) {
            if (state.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        }
        return false;
    }
}
