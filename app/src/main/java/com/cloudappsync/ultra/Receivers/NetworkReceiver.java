package com.cloudappsync.ultra.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.cloudappsync.ultra.Utilities.Common;
import io.paperdb.Paper;

import static com.cloudappsync.ultra.Basic.BasicWebActivity.updateNetworkData;
import static com.cloudappsync.ultra.Ultra.WebActivity.updateUltraNetworkData;

public class NetworkReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        try {

            if (Paper.book().read(Common.CURRENT_USER_TYPE, Common.USER_TYPE_BASIC).equals(Common.USER_TYPE_ULTRA)) {
                if (isOnline(context)) {
                    updateUltraNetworkData(true);
                    Paper.book().write(Common.IS_DEVICE_CONNECTED, "True");
                    Log.e("ConnectionStatus", "Online Connect Intenet ");
                } else {
                    updateUltraNetworkData(false);
                    Paper.book().write(Common.IS_DEVICE_CONNECTED, "False");
                    Log.e("ConnectionStatus", "Conectivity Failure !!! ");
                }
            } else {
                if (isOnline(context)) {
                    updateNetworkData(true);
                    Paper.book().write(Common.IS_DEVICE_CONNECTED, "True");
                    Log.e("ConnectionStatus", "Online Connect Intenet ");
                } else {
                    updateNetworkData(false);
                    Paper.book().write(Common.IS_DEVICE_CONNECTED, "False");
                    Log.e("ConnectionStatus", "Conectivity Failure !!! ");
                }
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }
}
