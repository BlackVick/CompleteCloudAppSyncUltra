package com.cloudappsync.ultra.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cloudappsync.ultra.Utilities.Common;

public class ScheduleAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null){

            String theId = intent.getStringExtra(Common.SCHEDULE_INTENT_ID);
            String theUrl = intent.getStringExtra(Common.SCHEDULE_INTENT_URL);
            String theEndTime = intent.getStringExtra(Common.SCHEDULE_INTENT_END);
            String theDuration = intent.getStringExtra(Common.SCHEDULE_INTENT_DURATION);
            boolean isOneOff = intent.getBooleanExtra(Common.SCHEDULE_INTENT_TYPE, false);

            //WebActivity.loadScheduleUrl(theId, theUrl, theEndTime, isOneOff, theDuration);

        }

    }

}