package com.example.todoappdemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChipManager {
    private static final String PREFS_NAME = "chip_prefs";
    private final SharedPreferences sharedPreferences;

    public ChipManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveChips(String taskId, List<String> chipList) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> chipSet = new HashSet<>(chipList);
        editor.putStringSet(taskId, chipSet);
        editor.apply();
    }
    public List<String> loadChips(String taskId) {
        Set<String> chipSet = sharedPreferences.getStringSet(taskId, new HashSet<>());
        return new java.util.ArrayList<>(chipSet);
    }

    public String[] getChips(String taskId) {
        Set<String> chipSet = sharedPreferences.getStringSet(taskId, new HashSet<>());
        return chipSet.toArray(new String[0]);
    }
}

