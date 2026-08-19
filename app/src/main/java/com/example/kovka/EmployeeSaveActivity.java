package com.example.kovka;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class EmployeeSaveActivity extends AppCompatActivity {
    private EditText etSpec, etName, etPhone, etEmail, etAddress, etHireDate, etNotes, etPassword;
    private Button btnSave, btnCancel;
    private ProgressBar progressBar;
    private String manager;
    private Bundle arguments;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_save);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        etSpec = findViewById(R.id.spec);
        etName = findViewById(R.id.name);
        etPhone = findViewById(R.id.tel);
        etEmail = findViewById(R.id.email);
        etAddress = findViewById(R.id.adres);
        etHireDate = findViewById(R.id.data);
        etNotes = findViewById(R.id.proch);
        etPassword = findViewById(R.id.password);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        progressBar = findViewById(R.id.progressBar);

        calendar = Calendar.getInstance();
        setCurrentDate();

        etHireDate.setOnClickListener(v -> showDatePickerDialog());

        arguments = getIntent().getExtras();
        if (arguments != null) {
            manager = arguments.getString("manager");
        }

        btnSave.setOnClickListener(v -> saveEmployee());

        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        etHireDate.setText(sdf.format(calendar.getTime()));
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
                    etHireDate.setText(sdf.format(calendar.getTime()));
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private String formatDateForApi(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "";
        }
        try {
            dateStr = dateStr.trim();
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
            if (dateStr.contains("/")) {
                String[] parts = dateStr.split("/");
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
            Log.e("EmployeeSaveActivity", "Date formatting error: " + e.getMessage());
        }
        return dateStr;
    }

    private void saveEmployee() {
        String spec = etSpec.getText().toString().trim();
        if (spec.isEmpty()) {
            Toast.makeText(this, "Введите должность", Toast.LENGTH_SHORT).show();
            etSpec.requestFocus();
            return;
        }

        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Введите ФИО", Toast.LENGTH_SHORT).show();
            etName.requestFocus();
            return;
        }

        String phone = etPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            Toast.makeText(this, "Введите телефон", Toast.LENGTH_SHORT).show();
            etPhone.requestFocus();
            return;
        }

        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Введите Email", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return;
        }

        String password = etPassword.getText().toString().trim();
        if (password.isEmpty()) {
            Toast.makeText(this, "Введите пароль (минимум 6 символов)", Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
            return;
        }

        String url = Config.API_BASE + "employees";

        String dateStr = etHireDate.getText().toString().trim();
        String formattedDate = formatDateForApi(dateStr);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("position", spec);
            jsonBody.put("full_name", name);
            jsonBody.put("phone", phone);
            jsonBody.put("email", email);
            jsonBody.put("address", etAddress.getText().toString().trim());
            jsonBody.put("hire_date", formattedDate);
            jsonBody.put("notes", etNotes.getText().toString().trim());
            jsonBody.put("is_active", true);
            jsonBody.put("password", password);

            Log.d("EmployeeSaveActivity", "Sending data: " + jsonBody.toString());
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

                    Toast.makeText(EmployeeSaveActivity.this,
                            "Сотрудник успешно создан",
                            Toast.LENGTH_SHORT).show();

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
                            Log.e("EmployeeSaveActivity", "Error body: " + responseBody);
                            JSONObject errorJson = new JSONObject(responseBody);
                            if (errorJson.has("message")) {
                                errorMsg += " - " + errorJson.getString("message");
                            }
                            if (errorJson.has("errors")) {
                                JSONObject errors = errorJson.getJSONObject("errors");
                                errorMsg += " - " + errors.toString();
                            }
                        } catch (Exception e) {}
                    }
                    Toast.makeText(EmployeeSaveActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}