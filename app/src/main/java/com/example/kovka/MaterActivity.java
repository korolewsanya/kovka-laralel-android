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

public class MaterActivity extends AppCompatActivity {
    private static final String JSON_URL = Config.API_BASE + "materials";
    private ListView listView;
    private ArrayList<Material> materialList;
    private String manager;
    private Bundle arguments;
    private static final int REQUEST_UPDATE_MATERIAL = 1;
    private static final int REQUEST_SAVE_MATERIAL = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mater);

        arguments = getIntent().getExtras();
        if (arguments != null) {
            manager = arguments.getString("manager");
        }

        listView = findViewById(R.id.listView);
        loadJSONFromURL(JSON_URL);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (materialList != null && position < materialList.size()) {
                    Material material = materialList.get(position);
                    Intent intent = new Intent(getApplicationContext(), MaterChDelActivity.class);

                    intent.putExtra("id", String.valueOf(material.getId()));
                    intent.putExtra("date", material.getDate());
                    intent.putExtra("name", material.getName());
                    intent.putExtra("kup", String.valueOf(material.getPurchased()));
                    intent.putExtra("izras", String.valueOf(material.getUsed()));
                    intent.putExtra("ost", String.valueOf(material.getBalance()));
                    intent.putExtra("prise", String.valueOf(material.getPrice_per_unit()));
                    intent.putExtra("itogo", String.valueOf(material.getTotal_price()));

                    if (manager != null) {
                        intent.putExtra("manager", manager);
                    }
                    startActivityForResult(intent, REQUEST_UPDATE_MATERIAL);
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
            // Обновляем список при успешном результате
            loadJSONFromURL(JSON_URL);
        }
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "";
        }
        try {
            if (dateStr.contains("T")) {
                dateStr = dateStr.split("T")[0];
            }
            String[] parts = dateStr.split("-");
            if (parts.length == 3) {
                return parts[2] + "." + parts[1] + "." + parts[0];
            }
        } catch (Exception e) {
            Log.e("MaterActivity", "Date formatting error: " + e.getMessage());
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
                        materialList = new ArrayList<>();

                        JSONObject object = new JSONObject(response);

                        if (!object.getBoolean("error")) {
                            JSONArray jsonArray = object.getJSONArray("data");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);

                                Material material = new Material();
                                material.setId(obj.getInt("id"));

                                String dateStr = obj.optString("date", "");
                                material.setDate(formatDate(dateStr));

                                material.setName(obj.getString("name"));
                                material.setPurchased(obj.optDouble("purchased", 0));
                                material.setUsed(obj.optDouble("used", 0));
                                material.setBalance(obj.optDouble("balance", 0));
                                material.setPrice_per_unit(obj.optDouble("price_per_unit", 0));
                                material.setTotal_price(obj.optDouble("total_price", 0));
                                materialList.add(material);
                            }

                            MaterialListAdapter adapter = new MaterialListAdapter(
                                    getApplicationContext(),
                                    R.layout.row_mater,
                                    materialList
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
            Intent intent = new Intent(this, MaterSaveActivity.class);
            if (arguments != null) {
                intent.putExtra("manager", manager);
            }
            // ИЗМЕНЕНО: используем startActivityForResult
            startActivityForResult(intent, REQUEST_SAVE_MATERIAL);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}