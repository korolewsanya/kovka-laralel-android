package com.example.kovka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TzGiveActivity extends AppCompatActivity implements Spinner.OnItemSelectedListener {
    private Spinner spinner;
    private ArrayList<String> workers;
    private JSONArray result;
    private EditText tz;
    private String employeeId;
    private TextView cd;
    private TextView cw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tz_give);

        tz = findViewById(R.id.tz);
        workers = new ArrayList<>();
        spinner = findViewById(R.id.spinner);
        cd = findViewById(R.id.cod);
        cw = findViewById(R.id.class_work);

        spinner.setOnItemSelectedListener(this);
        getData();
    }

    private void getData() {
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
                                result = dataArray;
                                workers.clear();
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject obj = dataArray.getJSONObject(i);
                                    String position = obj.optString("position", "");
                                    String fullName = obj.optString("full_name", "");
                                    workers.add(position + ": " + fullName);
                                }
                                spinner.setAdapter(new ArrayAdapter<>(
                                        TzGiveActivity.this,
                                        android.R.layout.simple_spinner_dropdown_item,
                                        workers
                                ));
                            }
                        } else {
                            String message = response.optString("message", "Ошибка загрузки");
                            Toast.makeText(TzGiveActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(TzGiveActivity.this, "Ошибка данных", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(TzGiveActivity.this, "Ошибка соединения", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        try {
            JSONObject json = result.getJSONObject(position);
            employeeId = json.optString("id", "");
            String positionText = json.optString("position", "");
            String fullName = json.optString("full_name", "");
            String workClass = json.optString("work_class", "1");

            cd.setText(positionText + ": " + fullName);
            cw.setText(workClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        cd.setText("");
        cw.setText("");
    }

    private void addDataToDatabase(String tzText, String date, String empId) {
        String url = Config.API_BASE + "work-reports";

        JSONObject params = new JSONObject();
        try {
            params.put("employee_id", empId);
            params.put("task", tzText);
            params.put("date", date);

            Log.d("GIVE_TZ", "Params: " + params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                params,
                response -> {
                    try {
                        boolean error = response.optBoolean("error", false);
                        if (!error) {
                            Toast toast = Toast.makeText(TzGiveActivity.this, "Тех.задание успешно отправлено", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();

                            // возвращаем результат и закрываем
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            String message = response.optString("message", "Ошибка");
                            Toast.makeText(TzGiveActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(TzGiveActivity.this, "Нет подключения к интернету", Toast.LENGTH_SHORT).show();
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

    public void onMyButtonClick(View view) {
        String tzText = tz.getText().toString().trim();
        if (tzText.isEmpty()) {
            Toast.makeText(this, "Введите техническое задание", Toast.LENGTH_SHORT).show();
            return;
        }
        if (employeeId == null || employeeId.isEmpty()) {
            Toast.makeText(this, "Выберите сотрудника", Toast.LENGTH_SHORT).show();
            return;
        }
        String date = new SimpleDateFormat("yyyy-MM-dd", new Locale("ru")).format(Calendar.getInstance().getTime());
        addDataToDatabase(tzText, date, employeeId);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}