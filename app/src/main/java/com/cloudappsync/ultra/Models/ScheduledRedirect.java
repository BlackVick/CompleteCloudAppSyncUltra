package com.cloudappsync.ultra.Models;

public class ScheduledRedirect {

    private String redirect_url;
    private boolean isDaily;
    private boolean isWeekly;
    private boolean isOneTime;
    private String day;
    private long time;
    private int duration;

    public ScheduledRedirect() {
    }

    public ScheduledRedirect(String redirect_url, boolean isDaily, boolean isWeekly, boolean isOneTime, String day, long time, int duration) {
        this.redirect_url = redirect_url;
        this.isDaily = isDaily;
        this.isWeekly = isWeekly;
        this.isOneTime = isOneTime;
        this.day = day;
        this.time = time;
        this.duration = duration;
    }

    public String getRedirect_url() {
        return redirect_url;
    }

    public void setRedirect_url(String redirect_url) {
        this.redirect_url = redirect_url;
    }

    public boolean isDaily() {
        return isDaily;
    }

    public void setDaily(boolean daily) {
        isDaily = daily;
    }

    public boolean isWeekly() {
        return isWeekly;
    }

    public void setWeekly(boolean weekly) {
        isWeekly = weekly;
    }

    public boolean isOneTime() {
        return isOneTime;
    }

    public void setOneTime(boolean oneTime) {
        isOneTime = oneTime;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
