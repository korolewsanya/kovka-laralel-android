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

public class OrderActivity extends AppCompatActivity {
    private static final String JSON_URL = Config.API_BASE + "orders";
    private ListView listView;
    private ArrayList<JSONObject> infoList;
    private String manager;
    private Bundle arguments;
    private TokenManager tokenManager;
    private String userRole;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        tokenManager = new TokenManager(this);
        userRole = tokenManager.getRole();
        if (userRole == null) userRole = "employee";

        arguments = getIntent().getExtras();
        if (arguments != null) {
            manager = arguments.getString("manager");
        }

        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);

        loadJSONFromURL(JSON_URL);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    JSONObject order = infoList.get(position);

                    String idi = order.optString("id");
                    String date = order.optString("order_date");
                    JSONObject product = order.optJSONObject("product");
                    String izdelie = (product != null) ? product.optString("name", "Без названия") : "Товар не указан";
                    String image = order.optString("image");
                    String dlina = order.optString("length");
                    String shirina = order.optString("width");
                    String visota = order.optString("height");
                    String prise = order.optString("price");
                    String pay = order.optString("paid");
                    String proces = order.optString("progress");
                    String name = order.optString("customer_name");
                    String tel = order.optString("customer_phone");
                    String email = order.optString("customer_email");
                    String coment = order.optString("comment");

                    Intent intent = new Intent(getApplicationContext(), OrderChDelActivity.class);
                    intent.putExtra("idi", idi);
                    intent.putExtra("date", date);
                    intent.putExtra("izdelie", izdelie);
                    intent.putExtra("image", image);
                    intent.putExtra("dlina", dlina);
                    intent.putExtra("shirina", shirina);
                    intent.putExtra("visota", visota);
                    intent.putExtra("prise", prise);
                    intent.putExtra("pay", pay);
                    intent.putExtra("proces", proces);
                    intent.putExtra("name", name);
                    intent.putExtra("tel", tel);
                    intent.putExtra("email", email);
                    intent.putExtra("coment", coment);
                    intent.putExtra("userRole", userRole);

                    if (manager != null) {
                        intent.putExtra("manager", manager);
                    }
                    startActivity(intent);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(OrderActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // ДОБАВЛЕНО: onResume для обновления списка
    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем список каждый раз при возврате
        loadJSONFromURL(JSON_URL);
    }

    private void loadJSONFromURL(String url) {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean("error")) {
                            JSONArray jsonArray = object.getJSONArray("data");
                            ArrayList<JSONObject> listItems = getArrayListFromJSONArray(jsonArray);
                            infoList = listItems;
                            ListAdapter adapter = new OrderAdapter(
                                    getApplicationContext(),
                                    R.layout.row_new_order,
                                    R.id.nomer,
                                    listItems
                            );
                            listView.setAdapter(adapter);
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    object.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),
                                "Ошибка обработки данных", Toast.LENGTH_SHORT).show();
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
                String token = new TokenManager(getApplicationContext()).getToken();
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

    private boolean isAdmin() {
        return "admin".equalsIgnoreCase(userRole) || "administrator".equalsIgnoreCase(userRole);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_order, menu);

        // Показываем кнопку "Добавить" только админу
        MenuItem saveItem = menu.findItem(R.id.save);
        if (saveItem != null) {
            saveItem.setVisible(isAdmin());
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save) {
            if (!isAdmin()) {
                Toast.makeText(this, "Доступ запрещён", Toast.LENGTH_SHORT).show();
                return true;
            }
            Intent intent = new Intent(this, OrderSaveActivity.class);
            if (arguments != null) {
                intent.putExtra("manager", manager);
            }
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}