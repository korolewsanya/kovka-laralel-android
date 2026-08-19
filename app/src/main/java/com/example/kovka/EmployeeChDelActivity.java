package com.example.kovka;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EmployeeChDelActivity extends AppCompatActivity {
    private EditText etId, etSpec, etName, etPhone, etEmail, etAddress, etHireDate, etNotes;
    private EditText etNewPassword;
    private Button btnUpdate, btnDelete, btnBack, btnResetPassword;
    private ProgressBar progressBar;
    private String employeeId;
    private String employeeName;
    private String manager;
    private Bundle arguments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_ch_del);

        etId = findViewById(R.id.nom);
        etSpec = findViewById(R.id.spec);
        etName = findViewById(R.id.name);
        etPhone = findViewById(R.id.tel);
        etEmail = findViewById(R.id.email);
        etAddress = findViewById(R.id.adres);
        etHireDate = findViewById(R.id.data);
        etNotes = findViewById(R.id.proch);
        etNewPassword = findViewById(R.id.new_password);
        btnUpdate = findViewById(R.id.btn_update);
        btnDelete = findViewById(R.id.btn_delete);
        btnBack = findViewById(R.id.btn_back);
        btnResetPassword = findViewById(R.id.btn_reset_password);
        progressBar = findViewById(R.id.progressBar);

        arguments = getIntent().getExtras();
        if (arguments != null) {
            employeeId = arguments.getString("idi");
            employeeName = arguments.getString("name");

            etId.setText(employeeId);
            etSpec.setText(arguments.getString("spec"));
            etName.setText(employeeName);
            etPhone.setText(arguments.getString("tel"));
            etEmail.setText(arguments.getString("email"));
            etAddress.setText(arguments.getString("adres"));
            etHireDate.setText(arguments.getString("data"));
            etNotes.setText(arguments.getString("proch"));

            etId.setEnabled(false);
            etId.setFocusable(false);
            etId.setClickable(false);

            etHireDate.setEnabled(false);
            etHireDate.setFocusable(false);
            etHireDate.setClickable(false);

            manager = arguments.getString("manager");
        }

        btnUpdate.setOnClickListener(v -> updateEmployee());
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
        btnResetPassword.setOnClickListener(v -> resetPassword());
        btnBack.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void resetPassword() {
        String newPassword = etNewPassword.getText().toString().trim();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Подтверждение сброса пароля");
        builder.setMessage("Вы уверены, что хотите сбросить пароль для сотрудника \"" + employeeName + "\"?\n" +
                (newPassword.isEmpty() ? "Будет сгенерирован новый пароль." : "Будет установлен введенный пароль."));
        builder.setIcon(android.R.drawable.ic_dialog_info);

        builder.setPositiveButton("Да, сбросить", (dialog, which) -> performResetPassword(newPassword));
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    private void performResetPassword(String newPassword) {
        String url = Config.API_BASE + "employees/" + employeeId;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("reset_password", true);
            if (!newPassword.isEmpty()) {
                jsonBody.put("password", newPassword);
            }
            Log.d("EmployeeChDelActivity", "Resetting password: " + jsonBody.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка в данных", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnResetPassword.setEnabled(false);

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    btnResetPassword.setEnabled(true);

                    String generatedPassword = null;
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        if (responseJson.has("data")) {
                            JSONObject data = responseJson.getJSONObject("data");
                            if (data.has("new_password")) {
                                generatedPassword = data.getString("new_password");
                            }
                        }
                    } catch (JSONException e) {}

                    showPasswordDialog(generatedPassword);
                    etNewPassword.setText("");
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    btnResetPassword.setEnabled(true);
                    String errorMsg = "Ошибка сброса пароля";
                    if (error.networkResponse != null) {
                        errorMsg += ". Код: " + error.networkResponse.statusCode;
                        try {
                            String responseBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e("EmployeeChDelActivity", "Error body: " + responseBody);
                            JSONObject errorJson = new JSONObject(responseBody);
                            if (errorJson.has("message")) {
                                errorMsg += " - " + errorJson.getString("message");
                            }
                        } catch (Exception e) {}
                    }
                    Toast.makeText(EmployeeChDelActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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

    private void showPasswordDialog(String password) {
        if (password == null || password.isEmpty()) {
            Toast.makeText(this, "Пароль успешно сброшен!", Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✅ Пароль успешно сброшен!");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_password, null);
        TextView tvPassword = dialogView.findViewById(R.id.tv_password);
        Button btnCopy = dialogView.findViewById(R.id.btn_copy);
        Button btnOk = dialogView.findViewById(R.id.btn_ok);

        tvPassword.setText(password);

        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Password", password);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Пароль скопирован в буфер обмена ✅", Toast.LENGTH_SHORT).show();
        });

        btnOk.setOnClickListener(v -> {
            AlertDialog dialog = (AlertDialog) v.getParent().getParent().getParent();
            if (dialog != null) {
                dialog.dismiss();
            }
        });

        builder.setView(dialogView);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Подтверждение удаления");
        builder.setMessage("Вы уверены, что хотите удалить сотрудника \"" + employeeName + "\"?\nЭто действие нельзя отменить!\n\nВнимание! Будут удалены связанные записи (зарплаты, отчеты).");
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton("Да, удалить", (dialog, which) -> deleteEmployee());
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    private String formatDateForApi(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "";
        }
        try {
            dateStr = dateStr.trim();
            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return dateStr;
            }
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
            Log.e("EmployeeChDelActivity", "Date formatting error: " + e.getMessage());
        }
        return dateStr;
    }

    private void updateEmployee() {
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

        String url = Config.API_BASE + "employees/" + employeeId;

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

            Log.d("EmployeeChDelActivity", "Updating data: " + jsonBody.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка в данных", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpdate.setEnabled(false);

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);
                    Toast.makeText(EmployeeChDelActivity.this,
                            "Сотрудник успешно обновлен",
                            Toast.LENGTH_SHORT).show();

                    setResult(RESULT_OK);
                    finish();
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);
                    String errorMsg = "Ошибка обновления";
                    if (error.networkResponse != null) {
                        errorMsg += ". Код: " + error.networkResponse.statusCode;
                        try {
                            String responseBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e("EmployeeChDelActivity", "Error body: " + responseBody);
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
                    Toast.makeText(EmployeeChDelActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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

    private void deleteEmployee() {
        String url = Config.API_BASE + "employees/" + employeeId;

        progressBar.setVisibility(View.VISIBLE);
        btnDelete.setEnabled(false);

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    btnDelete.setEnabled(true);
                    Toast.makeText(EmployeeChDelActivity.this,
                            "Сотрудник \"" + employeeName + "\" успешно удален",
                            Toast.LENGTH_SHORT).show();

                    // ИСПРАВЛЕНО: возвращаем результат и закрываем
                    setResult(RESULT_OK);
                    finish();
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    btnDelete.setEnabled(true);
                    String errorMsg = "Ошибка удаления";
                    if (error.networkResponse != null) {
                        errorMsg += ". Код: " + error.networkResponse.statusCode;
                        try {
                            String responseBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e("EmployeeChDelActivity", "Error body: " + responseBody);
                            JSONObject errorJson = new JSONObject(responseBody);
                            if (errorJson.has("message")) {
                                errorMsg += " - " + errorJson.getString("message");
                            }
                        } catch (Exception e) {}
                    }
                    Toast.makeText(EmployeeChDelActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
        ) {
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