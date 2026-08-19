package com.example.kovka;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MaterSaveActivity extends AppCompatActivity {
    private EditText etDate, etName, etPurchased, etUsed, etBalance, etPricePerUnit, etTotalPrice;
    private Button btnSave, btnCancel;
    private ProgressBar progressBar;
    private String manager;
    private Bundle arguments;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mater_save);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Инициализация Views
        etDate = findViewById(R.id.et_date);
        etName = findViewById(R.id.et_name);
        etPurchased = findViewById(R.id.et_purchased);
        etUsed = findViewById(R.id.et_used);
        etBalance = findViewById(R.id.et_balance);
        etPricePerUnit = findViewById(R.id.et_price_per_unit);
        etTotalPrice = findViewById(R.id.et_total_price);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        progressBar = findViewById(R.id.progressBar);

        // Инициализация календаря и установка текущей даты
        calendar = Calendar.getInstance();
        setCurrentDate();

        // Обработчик клика на поле даты - открывает DatePicker
        etDate.setOnClickListener(v -> showDatePickerDialog());

        // Получаем данные из Intent
        arguments = getIntent().getExtras();
        if (arguments != null) {
            manager = arguments.getString("manager");
        }

        // Обработчик кнопки "Сохранить"
        btnSave.setOnClickListener(v -> saveMaterial());

        // ИСПРАВЛЕНО: кнопка "Отмена" - просто закрываем активность
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        etDate.setText(sdf.format(calendar.getTime()));
    }

    private void showDatePickerDialog() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                    etDate.setText(sdf.format(calendar.getTime()));
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    // Метод для преобразования даты из ДД.ММ.ГГГГ в YYYY-MM-DD для API
    private String formatDateForApi(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "";
        }
        try {
            dateStr = dateStr.trim();

            // Если дата в формате ДД.ММ.ГГГГ
            if (dateStr.contains(".")) {
                String[] parts = dateStr.split("\\.");
                if (parts.length == 3) {
                    String day = parts[0].trim();
                    String month = parts[1].trim();
                    String year = parts[2].trim();
                    if (year.length() == 2) {
                        year = "20" + year;
                    }
                    return year + "-" + month + "-" + day;
                }
            }
        } catch (Exception e) {
            Log.e("MaterSaveActivity", "Date formatting error: " + e.getMessage());
        }
        return dateStr;
    }

    private void saveMaterial() {
        // Проверяем обязательное поле
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Введите наименование материала", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Config.API_BASE + "materials";

        // Получаем и форматируем дату для API
        String dateStr = etDate.getText().toString().trim();
        String formattedDate = formatDateForApi(dateStr);

        // Создаем JSON объект с данными
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("date", formattedDate);
            jsonBody.put("name", name);
            jsonBody.put("purchased", parseDoubleOrDefault(etPurchased.getText().toString(), 0));
            jsonBody.put("used", parseDoubleOrDefault(etUsed.getText().toString(), 0));
            jsonBody.put("balance", parseDoubleOrDefault(etBalance.getText().toString(), 0));
            jsonBody.put("price_per_unit", parseDoubleOrDefault(etPricePerUnit.getText().toString(), 0));
            jsonBody.put("total_price", parseDoubleOrDefault(etTotalPrice.getText().toString(), 0));

            Log.d("MaterSaveActivity", "Sending data: " + jsonBody.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка в данных", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);

                    Toast.makeText(MaterSaveActivity.this,
                            "Материал успешно создан",
                            Toast.LENGTH_SHORT).show();

                    // ИСПРАВЛЕНО: возвращаем результат и закрываем
                    setResult(RESULT_OK);
                    finish();
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);

                    String errorMsg = "Ошибка создания";
                    if (error.networkResponse != null) {
                        errorMsg += ". Код: " + error.networkResponse.statusCode;
                        try {
                            String responseBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e("MaterSaveActivity", "Error body: " + responseBody);
                            JSONObject errorJson = new JSONObject(responseBody);
                            if (errorJson.has("message")) {
                                errorMsg += " - " + errorJson.getString("message");
                            }
                        } catch (Exception e) {
                            // Игнорируем
                        }
                    }
                    Toast.makeText(MaterSaveActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public byte[] getBody() {
                return jsonBody.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                TokenManager tokenManager = new TokenManager(getApplicationContext());
                String token = tokenManager.getToken();
                if (token != null && !token.isEmpty()) {
                    headers.put("Authorization", "Bearer " + token);
                }
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        NetworkUtils.configureTimeout(request);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private double parseDoubleOrDefault(String value, double defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ДОБАВЛЕНО: переопределяем onBackPressed для правильного возврата
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}