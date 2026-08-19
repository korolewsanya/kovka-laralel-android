package com.example.kovka;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WorkReportsSaveActivity extends AppCompatActivity {
    private static final String TAG = "WorkReportsSave";

    private EditText etDate, etTz, etOt, etImg;
    private Spinner spinnerEmployee;
    private ImageView selectedImageView;
    private Button selectPhotoButton;
    private Uri selectedImageUri = null;
    private String selectedImageBase64 = null;

    private String reportId;
    private boolean isEditMode = false;
    private String existingImageName = null;

    // Для выбора сотрудника
    private List<Employee> employeeList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private int selectedEmployeeId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_reports_save);

        etDate = findViewById(R.id.data);
        etTz = findViewById(R.id.teh_zd);
        etOt = findViewById(R.id.order);
        etImg = findViewById(R.id.img);
        spinnerEmployee = findViewById(R.id.spinner_employee);
        selectedImageView = findViewById(R.id.selected_image);
        selectPhotoButton = findViewById(R.id.select_photo_button);

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
                    Log.d(TAG, "Selected employee: " + selected.getFull_name() + " (ID: " + selectedEmployeeId + ")");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Intent intent = getIntent();
        reportId = intent.getStringExtra("idi");

        Log.d(TAG, "onCreate: reportId = " + reportId);

        if (reportId != null && !reportId.isEmpty()) {
            isEditMode = true;
            etTz.setText(intent.getStringExtra("task"));
            etOt.setText(intent.getStringExtra("report"));

            String dateStr = intent.getStringExtra("date");
            etDate.setText(formatDateForDisplay(dateStr));

            existingImageName = intent.getStringExtra("image");
            Log.d(TAG, "Existing image name: " + existingImageName);

            // проверяем, что не null и не "null"
            if (existingImageName != null && !existingImageName.isEmpty() && !existingImageName.equals("null")) {
                etImg.setText(existingImageName);
            } else {
                etImg.setText(""); // пусто
            }

            // Загружаем сотрудника и выбираем его в Spinner
            loadEmployees();
        } else {
            isEditMode = false;
            etDate.setText(new SimpleDateFormat("dd.MM.yyyy", new Locale("ru")).format(Calendar.getInstance().getTime()));
            // Загружаем сотрудников
            loadEmployees();
        }

        selectPhotoButton.setOnClickListener(v -> {
            selectImageLauncher.launch("image/*");
        });
    }

    private void loadEmployees() {
        String url = Config.API_BASE + "employees";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        boolean error = response.optBoolean("error", false);
                        if (!error) {
                            JSONArray dataArray = response.optJSONArray("data");
                            if (dataArray != null) {
                                employeeList.clear();
                                List<String> employeeNames = new ArrayList<>();

                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject obj = dataArray.getJSONObject(i);
                                    Employee employee = new Employee();
                                    employee.setId(obj.getInt("id"));
                                    employee.setFull_name(obj.optString("full_name", ""));
                                    employee.setPosition(obj.optString("position", ""));
                                    employeeList.add(employee);
                                    employeeNames.add(employee.getFull_name() + " (" + employee.getPosition() + ")");
                                }

                                spinnerAdapter.clear();
                                spinnerAdapter.addAll(employeeNames);
                                spinnerAdapter.notifyDataSetChanged();

                                if (!employeeList.isEmpty()) {
                                    selectedEmployeeId = employeeList.get(0).getId();
                                    spinnerEmployee.setSelection(0);
                                    Log.d(TAG, "Default employee: " + employeeList.get(0).getFull_name() + " (ID: " + selectedEmployeeId + ")");
                                }
                            }
                        } else {
                            String message = response.optString("message", "Ошибка загрузки");
                            Toast.makeText(WorkReportsSaveActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(WorkReportsSaveActivity.this, "Ошибка данных", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(WorkReportsSaveActivity.this, "Ошибка соединения", Toast.LENGTH_SHORT).show();
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

        Volley.newRequestQueue(this).add(request);
    }

    private String formatDateForDisplay(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return new SimpleDateFormat("dd.MM.yyyy", new Locale("ru")).format(Calendar.getInstance().getTime());
        }
        try {
            if (dateStr.contains("-") && dateStr.length() == 10) {
                String[] parts = dateStr.split("-");
                if (parts.length == 3) {
                    return parts[2] + "." + parts[1] + "." + parts[0];
                }
            }
            if (dateStr.contains("T")) {
                dateStr = dateStr.substring(0, dateStr.indexOf("T"));
                String[] parts = dateStr.split("-");
                if (parts.length == 3) {
                    return parts[2] + "." + parts[1] + "." + parts[0];
                }
            }
            return dateStr;
        } catch (Exception e) {
            return dateStr;
        }
    }

    private String formatDateForServer(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return new SimpleDateFormat("yyyy-MM-dd", new Locale("ru")).format(Calendar.getInstance().getTime());
        }
        try {
            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return dateStr;
            }
            if (dateStr.contains(".")) {
                String[] parts = dateStr.split("\\.");
                if (parts.length == 3) {
                    return parts[2] + "-" + parts[1] + "-" + parts[0];
                }
            }
            return dateStr;
        } catch (Exception e) {
            return dateStr;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tz_sohranit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sohr) {
            saveReport();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveReport() {
        // Проверяем, что выбран сотрудник
        if (selectedEmployeeId == -1) {
            Toast.makeText(this, "Выберите сотрудника", Toast.LENGTH_SHORT).show();
            return;
        }

        String date = etDate.getText().toString().trim();
        String task = etTz.getText().toString().trim();
        String report = etOt.getText().toString().trim();

        Log.d(TAG, "saveReport: employee_id=" + selectedEmployeeId + ", date=" + date + ", task=" + task + ", report=" + report);

        if (TextUtils.isEmpty(date)) {
            etDate.setError("Введите дату");
            return;
        }
        if (TextUtils.isEmpty(report)) {
            etOt.setError("Введите отчет");
            return;
        }

        String serverDate = formatDateForServer(date);

        // Если есть новое изображение - отправляем base64
        if (selectedImageBase64 != null && !selectedImageBase64.isEmpty()) {
            Log.d(TAG, "Sending new image as base64, length: " + selectedImageBase64.length());
            if (isEditMode) {
                updateReport(reportId, selectedEmployeeId, task, report, serverDate, selectedImageBase64);
            } else {
                createReport(selectedEmployeeId, task, report, serverDate, selectedImageBase64);
            }
        } else {
            // Нет нового изображения
            String imageName = etImg.getText().toString().trim();
            Log.d(TAG, "No new image, using existing: " + imageName);
            if (isEditMode) {
                updateReport(reportId, selectedEmployeeId, task, report, serverDate, imageName);
            } else {
                createReport(selectedEmployeeId, task, report, serverDate, imageName);
            }
        }
    }

    private String convertImageToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream");
                return null;
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            inputStream.close();
            byteArrayOutputStream.close();

            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            String mimeType = getContentResolver().getType(uri);
            if (mimeType == null) {
                mimeType = "image/jpeg";
            }

            Log.d(TAG, "Image converted, size: " + imageBytes.length + " bytes");
            return "data:" + mimeType + ";base64," + base64Image;

        } catch (IOException e) {
            Log.e(TAG, "Error converting image: " + e.getMessage());
            return null;
        }
    }

    private void createReport(int employeeId, String task, String report, String date, String imageData) {
        String url = Config.API_BASE + "work-reports";

        Log.d(TAG, "createReport: url=" + url);
        Log.d(TAG, "createReport: employee_id=" + employeeId);

        JSONObject params = new JSONObject();
        try {
            params.put("employee_id", employeeId);
            params.put("task", task);
            params.put("report", report);
            params.put("date", date);
            if (imageData != null && !imageData.isEmpty()) {
                params.put("image", imageData);
            }
            Log.d(TAG, "Params: " + params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.POST,
                url,
                params,
                response -> {
                    try {
                        Log.d(TAG, "Response: " + response.toString());
                        boolean error = response.optBoolean("error", false);
                        if (!error) {
                            Toast.makeText(WorkReportsSaveActivity.this,
                                    "Отчет создан", Toast.LENGTH_LONG).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            String message = response.optString("message", "Ошибка");
                            Log.e(TAG, "Error response: " + message);
                            Toast.makeText(WorkReportsSaveActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Response error: " + e.getMessage());
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e(TAG, "Network error: " + error.getMessage());
                    if (error.networkResponse != null) {
                        try {
                            String responseBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e(TAG, "Error body: " + responseBody);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(WorkReportsSaveActivity.this,
                            "Ошибка соединения", Toast.LENGTH_SHORT).show();
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

        Volley.newRequestQueue(this).add(request);
    }

    private void updateReport(String id, int employeeId, String task, String report, String date, String imageData) {
        String url = Config.API_BASE + "work-reports/" + id;

        Log.d(TAG, "updateReport: url=" + url);
        Log.d(TAG, "updateReport: employee_id=" + employeeId);

        JSONObject params = new JSONObject();
        try {
            params.put("employee_id", employeeId);
            params.put("task", task);
            params.put("report", report);
            params.put("date", date);
            if (imageData != null && !imageData.isEmpty()) {
                params.put("image", imageData);
            }
            Log.d(TAG, "Params: " + params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.PUT,
                url,
                params,
                response -> {
                    try {
                        Log.d(TAG, "Response: " + response.toString());
                        boolean error = response.optBoolean("error", false);
                        if (!error) {
                            Toast.makeText(WorkReportsSaveActivity.this,
                                    "Отчет сохранен", Toast.LENGTH_LONG).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            String message = response.optString("message", "Ошибка");
                            Log.e(TAG, "Error response: " + message);
                            Toast.makeText(WorkReportsSaveActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Response error: " + e.getMessage());
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e(TAG, "Network error: " + error.getMessage());
                    if (error.networkResponse != null) {
                        try {
                            String responseBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e(TAG, "Error body: " + responseBody);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(WorkReportsSaveActivity.this,
                            "Ошибка соединения", Toast.LENGTH_SHORT).show();
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

        Volley.newRequestQueue(this).add(request);
    }

    private final ActivityResultLauncher<String> selectImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    Log.d(TAG, "Image selected: " + uri.toString());
                    selectedImageView.setImageURI(uri);
                    selectedImageView.setVisibility(View.VISIBLE);

                    String fileName = getFileName(uri);
                    Log.d(TAG, "File name: " + fileName);
                    etImg.setText(fileName);

                    selectedImageUri = uri;
                    selectedImageBase64 = convertImageToBase64(uri);
                    if (selectedImageBase64 != null) {
                        Log.d(TAG, "Image converted to base64, length: " + selectedImageBase64.length());
                        Toast.makeText(this, "Изображение загружено", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Failed to convert image to base64");
                        Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "Image selection cancelled");
                }
            });

    private String getFileName(Uri uri) {
        String name = "";
        try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (displayNameIndex >= 0) {
                    name = cursor.getString(displayNameIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (name.isEmpty()) {
            name = uri.getLastPathSegment();
            if (name == null) name = "unknown";
        }
        return name;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}