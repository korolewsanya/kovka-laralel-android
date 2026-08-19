package com.example.kovka;

import android.app.DatePickerDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SalarySaveActivity extends AppCompatActivity {
    private EditText etDate, etSpec, etName, etAccrued, etReceived, etDescription;
    private Spinner spinnerEmployee;
    private Button btnSave, btnCancel;
    private ProgressBar progressBar;
    private String manager;
    private Bundle arguments;
    private Calendar calendar;

    private List<Employee> employeeList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private int selectedEmployeeId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary_save);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Инициализация Views
        etDate = findViewById(R.id.et_date);
        etSpec = findViewById(R.id.et_spec);
        etName = findViewById(R.id.et_name);
        etAccrued = findViewById(R.id.et_accrued);
        etReceived = findViewById(R.id.et_received);
        etDescription = findViewById(R.id.et_description);
        spinnerEmployee = findViewById(R.id.spinner_employee);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        progressBar = findViewById(R.id.progressBar);

        // Настройка Spinner
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEmployee.setAdapter(spinnerAdapter);

        spinnerEmployee.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < employeeList.size()) {
                    Employee selected = employeeList.get(position);
                    selectedEmployeeId = selected.getId();
                    etName.setText(selected.getFull_name());
                    etSpec.setText(selected.getPosition());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Установка текущей даты
        calendar = Calendar.getInstance();
        setCurrentDate();

        // Обработчик клика на поле даты
        etDate.setOnClickListener(v -> showDatePickerDialog());

        // Получение данных
        arguments = getIntent().getExtras();
        if (arguments != null) {
            manager = arguments.getString("manager");
        }

        // Загрузка списка сотрудников
        loadEmployees();

        // Обработчики кнопок
        btnSave.setOnClickListener(v -> saveSalary());
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void loadEmployees() {
        String url = Config.API_BASE + "employees";

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);

                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean("error")) {
                            JSONArray jsonArray = object.getJSONArray("data");
                            employeeList.clear();

                            List<String> employeeNames = new ArrayList<>();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);
                                Employee employee = new Employee();
                                employee.setId(obj.getInt("id"));
                                employee.setFull_name(obj.getString("full_name"));
                                employee.setPosition(obj.optString("position", ""));
                                employeeList.add(employee);

                                employeeNames.add(employee.getFull_name() + " (" + employee.getPosition() + ")");
                            }

                            spinnerAdapter.clear();
                            spinnerAdapter.addAll(employeeNames);
                            spinnerAdapter.notifyDataSetChanged();

                            if (!employeeList.isEmpty()) {
                                selectedEmployeeId = employeeList.get(0).getId();
                                etName.setText(employeeList.get(0).getFull_name());
                                etSpec.setText(employeeList.get(0).getPosition());
                                spinnerEmployee.setSelection(0);
                            }

                            Toast.makeText(SalarySaveActivity.this,
                                    "Загружено " + employeeList.size() + " сотрудников",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(SalarySaveActivity.this,
                                "Ошибка загрузки сотрудников", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    String errorMsg = "Ошибка загрузки сотрудников";
                    if (error.networkResponse != null) {
                        errorMsg += ". Код: " + error.networkResponse.statusCode;
                    }
                    Toast.makeText(SalarySaveActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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
                return headers;
            }
        };

        NetworkUtils.configureTimeout(request);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
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
        } catch (Exception e) {
            Log.e("SalarySaveActivity", "Date formatting error: " + e.getMessage());
        }
        return dateStr;
    }

    private void saveSalary() {
        if (selectedEmployeeId == -1) {
            Toast.makeText(this, "Выберите сотрудника", Toast.LENGTH_SHORT).show();
            return;
        }

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


        String url = Config.API_BASE + "salaries";

        String dateStr = etDate.getText().toString().trim();
        String formattedDate = formatDateForApi(dateStr);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("employee_id", selectedEmployeeId);
            jsonBody.put("date", formattedDate);
            jsonBody.put("accrued", parseDoubleOrDefault(etAccrued.getText().toString(), 0));
            jsonBody.put("received", parseDoubleOrDefault(etReceived.getText().toString(), 0));
            jsonBody.put("description", etDescription.getText().toString().trim());

            Log.d("SalarySaveActivity", "Sending data: " + jsonBody.toString());
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

                    Toast.makeText(SalarySaveActivity.this,
                            "Запись зарплаты успешно создана",
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
                            Log.e("SalarySaveActivity", "Error body: " + responseBody);
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
                    Toast.makeText(SalarySaveActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}