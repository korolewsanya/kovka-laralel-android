package com.example.kovka;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class TokenManager {
    private static final String TAG = "TokenManager";
    private static final String PREF_NAME = "kovka_prefs";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_ROLE = "user_role";

    private SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Log.d(TAG, "TokenManager инициализирован");
    }

    public void saveToken(String token) {
        Log.d(TAG, "Сохранение токена");
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        Log.d(TAG, "Очистка токена и роли");
        prefs.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_ROLE)
                .apply();
    }

    public boolean hasToken() {
        return getToken() != null && !getToken().isEmpty();
    }

    // Методы для работы с ролью

    public void saveRole(String role) {
        Log.d(TAG, "Сохранение роли: " + role);
        prefs.edit().putString(KEY_ROLE, role).apply();
    }

    public String getRole() {
        String role = prefs.getString(KEY_ROLE, "employee");
        Log.d(TAG, "Получение роли: " + role);
        return role;
    }

    public boolean isAdmin() {
        String role = getRole();
        return "admin".equalsIgnoreCase(role) || "administrator".equalsIgnoreCase(role);
    }
}