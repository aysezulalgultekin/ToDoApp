package com.example.todoappdemo;

import java.util.List;

public class Tasks {
    private String title;
    private String backgroundColor;
    private String repeatFrequency;
    private String date;
    private String time;
    private boolean isAllDay, isCompleted;
    private String userId;
    private List<String> chips;
    public Tasks(String title, String backgroundColor, String repeatFrequency, String date, String time, boolean isAllDay, String userId, List<String> chips, boolean isCompleted) {
        this.title = title;
        this.backgroundColor = backgroundColor;
        this.repeatFrequency = repeatFrequency;
        this.date = date;
        this.time = time;
        this.isAllDay = isAllDay;
        this.userId = userId;
        this.chips = chips;
        this.isCompleted = isCompleted;
    }
    public Tasks() {}
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getBackgroundColor() {
        return backgroundColor;
    }
    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    public String getRepeatFrequency() {
        return repeatFrequency;
    }
    public void setRepeatFrequency(String repeatFrequency) {
        this.repeatFrequency = repeatFrequency;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public boolean isAllDay() {
        return isAllDay;
    }
    public void setAllDay(boolean allDay) {
        isAllDay = allDay;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public List<String> getChips() {
        return chips;
    }

    public void setChips(List<String> chips) {
        this.chips = chips;
    }
    public boolean isCompleted() {
        return isCompleted;
    }
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}



