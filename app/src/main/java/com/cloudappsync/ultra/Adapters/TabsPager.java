package com.cloudappsync.ultra.Adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.cloudappsync.ultra.HomeTabs.DeviceTab;
import com.cloudappsync.ultra.HomeTabs.ScheduleTab;
import com.cloudappsync.ultra.HomeTabs.SettingsTab;
import com.cloudappsync.ultra.HomeTabs.WebData;

public class TabsPager extends FragmentStatePagerAdapter {

    String[] titles = new String[]{"DATA", "SCHEDULE", "SETUP", "DEVICE"};

    public TabsPager(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                WebData data = new WebData();
                return data;
            case 1:
                ScheduleTab schedule = new ScheduleTab();
                return schedule;
            case 2:
                SettingsTab settings = new SettingsTab();
                return settings;
            case 3:
                DeviceTab device = new DeviceTab();
                return device;
        }

        return null;
    }

    @Override
    public int getCount() {
        return 4;
    }
}
