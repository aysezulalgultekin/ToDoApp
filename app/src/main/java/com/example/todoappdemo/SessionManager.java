package com.example.todoappdemo;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_LEVEL = "level";
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    public static void init(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
    }
    public static void setCurrentUser(User user) {
        editor.putString(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_NAME, user.getName());
        editor.putInt(KEY_LEVEL, user.getLevel());
        editor.apply();
    }
    public static User getCurrentUser() {
        String userId = sharedPreferences.getString(KEY_USER_ID, null);
        String email = sharedPreferences.getString(KEY_EMAIL, null);
        String name = sharedPreferences.getString(KEY_NAME, null);
        int level = sharedPreferences.getInt(KEY_LEVEL, 0);
        if (userId != null && email != null && name != null) {
            return new User(userId, email, name, level);
        }
        return null;
    }
    public static boolean isUserLoggedIn() {
        return sharedPreferences.contains(KEY_USER_ID) && sharedPreferences.contains(KEY_EMAIL);
    }
    public static void logout() {
        editor.clear();
        editor.apply();
    }
    public static void setBunnyName(String bunnyName) {
        editor.putString("bunnyName", bunnyName);
        editor.apply();
    }
    public static String getBunnyName() {
        return sharedPreferences.getString("bunnyName", "");
    }
}

