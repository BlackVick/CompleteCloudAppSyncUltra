package com.cloudappsync.ultra.Receivers;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.cloudappsync.ultra.Basic.BasicWebActivity;
import com.cloudappsync.ultra.Ultra.WebActivity;
import com.cloudappsync.ultra.Utilities.Common;
import io.paperdb.Paper;

public class NetworkReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        try {

            if (Paper.book().read(Common.CURRENT_USER_TYPE, Common.USER_TYPE_BASIC).equals(Common.USER_TYPE_ULTRA)) {
                if (isOnline(context)) {
                    WebActivity.isInternetConnected(true);
                    return;
                }
                WebActivity.isInternetConnected(false);
            } else {
                BasicWebActivity bwa = new BasicWebActivity();
                if (isOnline(context)) {
                    bwa.isInternetConnected(true);
                    return;
                }
                bwa.isInternetConnected(false);
            }

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
