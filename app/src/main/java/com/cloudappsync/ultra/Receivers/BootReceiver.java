package com.cloudappsync.ultra.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cloudappsync.ultra.Splash;
import com.cloudappsync.ultra.Utilities.Common;

import io.paperdb.Paper;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (Paper.book().read(Common.CURRENT_START_MODE, Common.START_MODE_BOOT).equals(Common.START_MODE_BOOT)) {
                Intent i = new Intent(context, Splash.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(i);
            }
        }
    }
}