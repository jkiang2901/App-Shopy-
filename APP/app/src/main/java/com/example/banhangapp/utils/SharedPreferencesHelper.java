package com.example.banhangapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
    private static final String PREFS_NAME = "BanHangAppPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SharedPreferencesHelper(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void saveUser(String userId, String name, String email, String role) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_ROLE, role);
        editor.apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, null);
    }

    public void clear() {
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }
}

