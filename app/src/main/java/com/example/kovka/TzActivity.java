package com.example.kovka;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
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

public class TzActivity extends AppCompatActivity {
    private static final String JSON_URL = Config.API_BASE + "work-reports";
    private static final int REQUEST_SAVE_REPORT = 1;
    private static final int REQUEST_UPDATE_REPORT = 2;

    ListView listView;
    ArrayList<JSONObject> infoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tz);

        listView = findViewById(R.id.listView);
        loadJSONFromURL(JSON_URL);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject item = infoList.get(position);
                String idi = item.optString("id");
                String task = item.optString("task");
                String report = item.optString("report");
                String date = item.optString("date");
                String image = item.optString("image");

                JSONObject employee = item.optJSONObject("employee");
                String employeeName = "";
                String specialty = "";
                if (employee != null) {
                    employeeName = employee.optString("full_name", "");
                    specialty = employee.optString("position", "");
                }

                Intent intent = new Intent(getApplicationContext(), WorkReportsSaveActivity.class);
                intent.putExtra("idi", idi);
                intent.putExtra("task", task);
                intent.putExtra("report", report);
                intent.putExtra("date", date);
                intent.putExtra("image", image);
                intent.putExtra("employee_name", employeeName);
                intent.putExtra("specialty", specialty);
                startActivityForResult(intent, REQUEST_UPDATE_REPORT);
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

    private void loadJSONFromURL(String url) {
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                    try {
                        JSONObject object = new JSONObject(response);
                        boolean error = object.optBoolean("error", false);
                        if (!error) {
                            JSONArray jsonArray = object.getJSONArray("data");
                            ArrayList<JSONObject> listItems = getArrayListFromJSONArray(jsonArray);
                            infoList = listItems;
                            ListAdapter adapter = new TzAdapter(getApplicationContext(), R.layout.row_new_order, R.id.nomer, listItems);
                            listView.setAdapter(adapter);
                        } else {
                            String message = object.optString("message", "Ошибка загрузки");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Ошибка данных", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                    Toast.makeText(getApplicationContext(), "Ошибка соединения. Попробуйте позже.", Toast.LENGTH_SHORT).show();
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

    private ArrayList<JSONObject> getArrayListFromJSONArray(JSONArray jsonArray) {
        ArrayList<JSONObject> aList = new ArrayList<>();
        try {
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    aList.add(jsonArray.getJSONObject(i));
                }
            }
        } catch (JSONException js) {
            js.printStackTrace();
        }
        return aList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tz, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.give_tz) {
            Intent intent = new Intent(this, TzGiveActivity.class);
            startActivityForResult(intent, REQUEST_SAVE_REPORT);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}