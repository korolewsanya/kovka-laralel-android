package com.example.kovka;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EmployeeActivity extends AppCompatActivity {
    private static final String JSON_URL = Config.API_BASE + "employees";
    private static final int REQUEST_SAVE_EMPLOYEE = 1;
    private static final int REQUEST_UPDATE_EMPLOYEE = 2;

    private ListView listView;
    private ArrayList<Employee> employeeList;
    private String manager;
    private Bundle arguments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        arguments = getIntent().getExtras();
        if (arguments != null) {
            manager = arguments.getString("manager");
        }

        listView = findViewById(R.id.listView);
        loadJSONFromURL(JSON_URL);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (employeeList != null && position < employeeList.size()) {
                    Employee employee = employeeList.get(position);
                    Intent intent = new Intent(getApplicationContext(), EmployeeChDelActivity.class);

                    intent.putExtra("idi", String.valueOf(employee.getId()));
                    intent.putExtra("spec", employee.getPosition());
                    intent.putExtra("name", employee.getFull_name());
                    intent.putExtra("tel", employee.getPhone());
                    intent.putExtra("email", employee.getEmail());
                    intent.putExtra("adres", employee.getAddress());
                    intent.putExtra("data", employee.getHire_date());
                    intent.putExtra("proch", employee.getNotes());

                    if (manager != null) {
                        intent.putExtra("manager", manager);
                    }
                    startActivityForResult(intent, REQUEST_UPDATE_EMPLOYEE);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadJSONFromURL(JSON_URL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadJSONFromURL(JSON_URL);
            Toast.makeText(this, "Данные обновлены", Toast.LENGTH_SHORT).show();
        }
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
            Log.e("EmployeeActivity", "Date formatting error: " + e.getMessage());
        }
        return dateStr;
    }

    private void loadJSONFromURL(String url) {
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    try {
                        employeeList = new ArrayList<>();

                        JSONObject object = new JSONObject(response);

                        if (!object.getBoolean("error")) {
                            JSONArray jsonArray = object.getJSONArray("data");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);

                                Employee employee = new Employee();
                                employee.setId(obj.getInt("id"));
                                employee.setPosition(obj.optString("position", ""));
                                employee.setFull_name(obj.optString("full_name", ""));
                                employee.setPhone(obj.optString("phone", ""));
                                employee.setEmail(obj.optString("email", ""));
                                employee.setAddress(obj.optString("address", ""));

                                String dateStr = obj.optString("hire_date", "");
                                employee.setHire_date(formatDateForDisplay(dateStr));

                                employee.setNotes(obj.optString("notes", ""));

                                employeeList.add(employee);
                            }

                            EmployeeListAdapter adapter = new EmployeeListAdapter(
                                    getApplicationContext(),
                                    R.layout.row_employee,
                                    employeeList
                            );
                            listView.setAdapter(adapter);

                        } else {
                            String message = object.optString("message", "Неизвестная ошибка");
                            Toast.makeText(getApplicationContext(),
                                    message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),
                                "Ошибка обработки данных: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    String errorMsg = "Ошибка соединения. Попробуйте позже.";
                    if (error.networkResponse != null) {
                        errorMsg += " Код: " + error.networkResponse.statusCode;
                    }
                    Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
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

        NetworkUtils.configureTimeout(stringRequest);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fin_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save_fin) {
            Intent intent = new Intent(this, EmployeeSaveActivity.class);
            if (arguments != null) {
                intent.putExtra("manager", manager);
            }
            startActivityForResult(intent, REQUEST_SAVE_EMPLOYEE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}