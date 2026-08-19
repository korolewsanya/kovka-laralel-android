package com.example.kovka;

import android.app.AlertDialog;
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

public class SalaryChDelActivity extends AppCompatActivity {
    private EditText etId, etDate, etSpec, etName, etAccrued, etReceived, etDescription;
    private Button btnUpdate, btnDelete, btnBack;
    private ProgressBar progressBar;
    private String salaryId;
    private String employeeId;
    private String salaryName;
    private String manager;
    private Bundle arguments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary_ch_del);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        etId = findViewById(R.id.et_id);
        etDate = findViewById(R.id.et_date);
        etSpec = findViewById(R.id.et_spec);
        etName = findViewById(R.id.et_name);
        etAccrued = findViewById(R.id.et_accrued);
        etReceived = findViewById(R.id.et_received);
        etDescription = findViewById(R.id.et_description);
        btnUpdate = findViewById(R.id.btn_update);
        btnDelete = findViewById(R.id.btn_delete);
        btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progressBar);

        arguments = getIntent().getExtras();
        if (arguments != null) {
            salaryId = arguments.getString("id");
            employeeId = arguments.getString("employee_id");
            salaryName = arguments.getString("name");

            etId.setText(salaryId);
            etDate.setText(arguments.getString("date"));
            etDate.setEnabled(false);
            etDate.setFocusable(false);
            etDate.setClickable(false);

            etSpec.setText(arguments.getString("spec"));
            etName.setText(salaryName);
            etAccrued.setText(arguments.getString("nachis"));
            etReceived.setText(arguments.getString("poluch"));
            etDescription.setText(arguments.getString("description"));

            manager = arguments.getString("manager");
        }

        btnUpdate.setOnClickListener(v -> updateSalary());
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
        btnBack.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Подтверждение удаления");
        builder.setMessage("Вы уверены, что хотите удалить запись зарплаты для \"" + salaryName + "\"?\nЭто действие нельзя отменить!");
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton("Да, удалить", (dialog, which) -> deleteSalary());
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
            Log.e("SalaryChDelActivity", "Date formatting error: " + e.getMessage());
        }
        return dateStr;
    }

    private void updateSalary() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Введите ФИО", Toast.LENGTH_SHORT).show();
            etName.requestFocus();
            return;
        }

        String spec = etSpec.getText().toString().trim();
        if (spec.isEmpty()) {
            Toast.makeText(this, "Введите должность", Toast.LENGTH_SHORT).show();
            etSpec.requestFocus();
            return;
        }

        // ПРОВЕРКА НА МАКСИМАЛЬНОЕ ЗНАЧЕНИЕ
        double accrued = parseDoubleOrDefault(etAccrued.getText().toString(), 0);
        double received = parseDoubleOrDefault(etReceived.getText().toString(), 0);

        if (accrued > 99999999.99) {
            Toast.makeText(this, "Сумма начисления слишком большая. Максимум: 99 999 999.99", Toast.LENGTH_LONG).show();
            etAccrued.requestFocus();
            return;
        }

        if (received > 99999999.99) {
            Toast.makeText(this, "Сумма получения слишком большая. Максимум: 99 999 999.99", Toast.LENGTH_LONG).show();
            etReceived.requestFocus();
            return;
        }

        String url = Config.API_BASE + "salaries/" + salaryId;

        String dateStr = etDate.getText().toString().trim();
        String formattedDate = formatDateForApi(dateStr);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("employee_id", Integer.parseInt(employeeId));
            jsonBody.put("date", formattedDate);
            jsonBody.put("accrued", parseDoubleOrDefault(etAccrued.getText().toString(), 0));
            jsonBody.put("received", parseDoubleOrDefault(etReceived.getText().toString(), 0));
            jsonBody.put("description", etDescription.getText().toString().trim());

            Log.d("SalaryChDelActivity", "Updating data: " + jsonBody.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка в данных", Toast.LENGTH_SHORT).show();
            return;
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Проверьте правильность числовых полей", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpdate.setEnabled(false);

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);
                    Toast.makeText(SalaryChDelActivity.this,
                            "Запись зарплаты успешно обновлена",
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
                            Log.e("SalaryChDelActivity", "Error body: " + responseBody);
                            JSONObject errorJson = new JSONObject(responseBody);
                            if (errorJson.has("message")) {
                                errorMsg += " - " + errorJson.getString("message");
                            }
                        } catch (Exception e) {}
                    }
                    Toast.makeText(SalaryChDelActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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

    private void deleteSalary() {
        String url = Config.API_BASE + "salaries/" + salaryId;

        progressBar.setVisibility(View.VISIBLE);
        btnDelete.setEnabled(false);

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    btnDelete.setEnabled(true);
                    Toast.makeText(SalaryChDelActivity.this,
                            "Запись зарплаты для \"" + salaryName + "\" успешно удалена",
                            Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    btnDelete.setEnabled(true);
                    String errorMsg = "Ошибка удаления";
                    if (error.networkResponse != null) {
                        errorMsg += ". Код: " + error.networkResponse.statusCode;
                    }
                    Toast.makeText(SalaryChDelActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}