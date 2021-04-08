package com.cloudappsync.ultra.Models;

public class Schedule {

    private String id;
    private String redirect_url;
    private boolean isDaily;
    private boolean isWeekly;
    private boolean isOneTime;
    private String day;
    private String startTime;
    private String stopTime;
    private String duration;
    private String date;
    private String priority;

    public Schedule() {
    }

    public Schedule(String id, String redirect_url, boolean isDaily, boolean isWeekly, boolean isOneTime, String day, String startTime, String stopTime, String duration, String date, String priority) {
        this.id = id;
        this.redirect_url = redirect_url;
        this.isDaily = isDaily;
        this.isWeekly = isWeekly;
        this.isOneTime = isOneTime;
        this.day = day;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.duration = duration;
        this.date = date;
        this.priority = priority;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStopTime() {
        return stopTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
