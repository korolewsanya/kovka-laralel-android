package com.example.kovka;

import android.content.Intent;
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

public class SalaryActivity extends AppCompatActivity {
    private static final String JSON_URL = Config.API_BASE + "salaries";
    private static final int REQUEST_SAVE_SALARY = 1;
    private static final int REQUEST_UPDATE_SALARY = 2;

    private ListView listView;
    private ArrayList<Salary> salaryList;
    private String manager;
    private Bundle arguments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary);

        arguments = getIntent().getExtras();
        if (arguments != null) {
            manager = arguments.getString("manager");
        }

        listView = findViewById(R.id.listView);
        loadJSONFromURL(JSON_URL);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (salaryList != null && position < salaryList.size()) {
                    Salary salary = salaryList.get(position);
                    Intent intent = new Intent(getApplicationContext(), SalaryChDelActivity.class);

                    intent.putExtra("id", String.valueOf(salary.getId()));
                    intent.putExtra("employee_id", String.valueOf(salary.getEmployee_id()));
                    intent.putExtra("date", salary.getDate());
                    intent.putExtra("spec", salary.getSpecialty());
                    intent.putExtra("name", salary.getEmployee_name());
                    intent.putExtra("nachis", String.valueOf(salary.getAccrued()));
                    intent.putExtra("poluch", String.valueOf(salary.getReceived()));
                    intent.putExtra("description", salary.getDescription());

                    if (manager != null) {
                        intent.putExtra("manager", manager);
                    }
                    startActivityForResult(intent, REQUEST_UPDATE_SALARY);
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
            Log.e("SalaryActivity", "Date formatting error: " + e.getMessage());
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
                        salaryList = new ArrayList<>();

                        JSONObject object = new JSONObject(response);

                        if (!object.getBoolean("error")) {
                            JSONArray jsonArray = object.getJSONArray("data");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);

                                Salary salary = new Salary();
                                salary.setId(obj.getInt("id"));

                                String dateStr = obj.optString("date", "");
                                salary.setDate(formatDateForDisplay(dateStr));

                                JSONObject employeeObj = obj.optJSONObject("employee");
                                if (employeeObj != null) {
                                    salary.setEmployee_id(employeeObj.optInt("id", 0));
                                    salary.setEmployee_name(employeeObj.optString("full_name", ""));
                                    salary.setSpecialty(employeeObj.optString("position", ""));
                                } else {
                                    salary.setEmployee_id(0);
                                    salary.setEmployee_name("");
                                    salary.setSpecialty("");
                                }

                                salary.setAccrued(obj.optDouble("accrued", 0));
                                salary.setReceived(obj.optDouble("received", 0));
                                salary.setDescription(obj.optString("description", ""));

                                salaryList.add(salary);
                            }

                            SalaryListAdapter adapter = new SalaryListAdapter(
                                    getApplicationContext(),
                                    R.layout.row_salary,
                                    salaryList
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
            Intent intent = new Intent(this, SalarySaveActivity.class);
            if (arguments != null) {
                intent.putExtra("manager", manager);
            }
            startActivityForResult(intent, REQUEST_SAVE_SALARY);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}