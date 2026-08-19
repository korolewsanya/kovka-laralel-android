package com.example.kovka;

import android.app.AlertDialog;
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

import java.util.HashMap;
import java.util.Map;

public class MaterChDelActivity extends AppCompatActivity {
    private EditText etId, etDate, etName, etPurchased, etUsed, etBalance, etPricePerUnit, etTotalPrice;
    private Button btnUpdate, btnDelete, btnBack;
    private ProgressBar progressBar;
    private String materialId;
    private String materialName;
    private String manager;
    private Bundle arguments;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mater_ch_del);

        TokenManager tokenManager = new TokenManager(this);
        userRole = tokenManager.getRole();
        if (userRole == null) userRole = "employee";

        etId = findViewById(R.id.et_id);
        etDate = findViewById(R.id.et_date);
        etName = findViewById(R.id.et_name);
        etPurchased = findViewById(R.id.et_purchased);
        etUsed = findViewById(R.id.et_used);
        etBalance = findViewById(R.id.et_balance);
        etPricePerUnit = findViewById(R.id.et_price_per_unit);
        etTotalPrice = findViewById(R.id.et_total_price);
        btnUpdate = findViewById(R.id.btn_update);
        btnDelete = findViewById(R.id.btn_delete);
        btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progressBar);

        arguments = getIntent().getExtras();
        if (arguments != null) {
            materialId = arguments.getString("id");
            materialName = arguments.getString("name");

            etId.setText(materialId);
            String dateStr = arguments.getString("date");
            etDate.setText(formatDateForDisplay(dateStr));
            etDate.setEnabled(false);
            etDate.setFocusable(false);
            etDate.setClickable(false);

            etName.setText(materialName);
            etPurchased.setText(arguments.getString("kup"));
            etUsed.setText(arguments.getString("izras"));
            etBalance.setText(arguments.getString("ost"));
            etPricePerUnit.setText(arguments.getString("prise"));
            etTotalPrice.setText(arguments.getString("itogo"));
            manager = arguments.getString("manager");
        }

        btnUpdate.setOnClickListener(v -> updateMaterial());
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());

        btnBack.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private boolean isAdmin() {
        return "admin".equalsIgnoreCase(userRole) || "administrator".equalsIgnoreCase(userRole);
    }

    private void showDeleteConfirmationDialog() {
        if (!isAdmin()) {
            Toast.makeText(this, "Доступ запрещён", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Подтверждение удаления");
        builder.setMessage("Вы уверены, что хотите удалить материал \"" + materialName + "\"?\nЭто действие нельзя отменить!");
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton("Да, удалить", (dialog, which) -> deleteMaterial());
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    private String formatDateForDisplay(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "";
        }
        try {
            if (dateStr.contains("T")) {
                dateStr = dateStr.split("T")[0];
            }
            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                String[] parts = dateStr.split("-");
                if (parts.length == 3) {
                    return parts[2] + "." + parts[1] + "." + parts[0];
                }
            }
        } catch (Exception e) {
            Log.e("MaterChDelActivity", "Date formatting error: " + e.getMessage());
        }
        return dateStr;
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
            Log.e("MaterChDelActivity", "Date formatting error: " + e.getMessage());
        }
        return dateStr;
    }

    private void updateMaterial() {
        String url = Config.API_BASE + "materials/" + materialId;

        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Введите наименование материала", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateStr = etDate.getText().toString().trim();
        String formattedDate = formatDateForApi(dateStr);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("date", formattedDate);
            jsonBody.put("name", name);
            jsonBody.put("purchased", parseDoubleOrDefault(etPurchased.getText().toString(), 0));
            jsonBody.put("used", parseDoubleOrDefault(etUsed.getText().toString(), 0));
            jsonBody.put("balance", parseDoubleOrDefault(etBalance.getText().toString(), 0));
            jsonBody.put("price_per_unit", parseDoubleOrDefault(etPricePerUnit.getText().toString(), 0));
            jsonBody.put("total_price", parseDoubleOrDefault(etTotalPrice.getText().toString(), 0));

            Log.d("MaterChDelActivity", "Updating data: " + jsonBody.toString());
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
                    Toast.makeText(MaterChDelActivity.this,
                            "Материал успешно обновлен",
                            Toast.LENGTH_SHORT).show();

                    // ИСПРАВЛЕНО: возвращаем результат и закрываем
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
                            Log.e("MaterChDelActivity", "Error body: " + responseBody);
                            JSONObject errorJson = new JSONObject(responseBody);
                            if (errorJson.has("message")) {
                                errorMsg += " - " + errorJson.getString("message");
                            }
                        } catch (Exception e) {}
                    }
                    Toast.makeText(MaterChDelActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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

    private void deleteMaterial() {
        if (!isAdmin()) {
            Toast.makeText(this, "Доступ запрещён", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Config.API_BASE + "materials/" + materialId;

        progressBar.setVisibility(View.VISIBLE);
        btnDelete.setEnabled(false);

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    btnDelete.setEnabled(true);
                    Toast.makeText(MaterChDelActivity.this,
                            "Материал \"" + materialName + "\" успешно удален",
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
                            Log.e("MaterChDelActivity", "Error body: " + responseBody);
                            JSONObject errorJson = new JSONObject(responseBody);
                            if (errorJson.has("message")) {
                                errorMsg += " - " + errorJson.getString("message");
                            }
                        } catch (Exception e) {}
                    }
                    Toast.makeText(MaterChDelActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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

    // переопределяем onBackPressed для правильного возврата
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}