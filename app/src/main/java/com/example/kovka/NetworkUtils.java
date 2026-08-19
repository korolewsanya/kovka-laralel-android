package com.example.kovka;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.android.volley.RequestQueue;

public class NetworkUtils {

    // КОНСТАНТЫ ДЛЯ НАСТРОЕК ТАЙМАУТА
    private static final int TIMEOUT_MS = 15000;      // ТАЙМАУТ 15 СЕКУНД
    private static final int MAX_RETRIES = 3;         // МАКСИМУМ ПОВТОРНЫХ ПОПЫТОК
    private static final float BACKOFF_MULT = 1f;     // МНОЖИТЕЛЬ ЗАДЕРЖКИ

    // ---1) Проверка наличия интернета ---
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    // ПРИМЕНЕНИЕ 1: НАСТРОЙКА ТАЙМАУТА ДЛЯ ЛЮБОГО ЗАПРОСА
    // Метод применяет единые настройки таймаута ко всем запросам
    public static <T> void configureTimeout(Request<T> request) {
        request.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,      // таймаут в миллисекундах
                MAX_RETRIES,     // количество повторных попыток
                BACKOFF_MULT     // множитель задержки
        ));
    }

    // ПРИМЕНЕНИЕ 2: НАСТРОЙКА ТАЙМАУТА С ПОЛЬЗОВАТЕЛЬСКИМИ ЗНАЧЕНИЯМИ (при необходимости)
    // Перегруженный метод для случаев, когда нужны другие настройки
    public static <T> void configureTimeout(Request<T> request, int timeoutMs, int maxRetries) {
        request.setRetryPolicy(new DefaultRetryPolicy(
                timeoutMs,
                maxRetries,
                BACKOFF_MULT
        ));
    }

    // ---2) Получение понятного сообщения об ошибке ---
    public static String getErrorMessage(VolleyError error) {
        if (error instanceof NoConnectionError) {
            return "Нет подключения к интернету";
        } else if (error instanceof TimeoutError) {
            return "Сервер не отвечает. Проверьте соединение";
        } else if (error instanceof ServerError) {
            return "Ошибка сервера. Попробуйте позже";
        } else if (error instanceof AuthFailureError) {
            return "Ошибка авторизации";
        } else if (error instanceof ParseError) {
            return "Ошибка обработки данных";
        } else {
            String message = "Ошибка соединения. Попробуйте позже.";
            return message != null ? message : "Неизвестная ошибка";
        }
    }

    // ---3) Показ сообщения об ошибке ---
    public static void showErrorMessage(Context context, VolleyError error) {
        String message = getErrorMessage(error);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    // ---4) Получение очереди запросов с настройками ---
    public static RequestQueue getRequestQueue(Context context) {
        return Volley.newRequestQueue(context);
    }
}