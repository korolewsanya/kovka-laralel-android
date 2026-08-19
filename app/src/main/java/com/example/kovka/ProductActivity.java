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

public class ProductActivity extends AppCompatActivity {
    private static final String TAG = "ProductActivity";
    private static final String JSON_URL = Config.API_BASE + "products";
    private static final int REQUEST_SAVE_PRODUCT = 1;
    private static final int REQUEST_UPDATE_PRODUCT = 2;

    private ListView listView;
    private ArrayList<Product> productList;
    private ProgressBar progressBar;
    private String manager;
    private Bundle arguments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        arguments = getIntent().getExtras();
        if (arguments != null) {
            manager = arguments.getString("manager");
        }

        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);
        loadProducts();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (productList != null && position < productList.size()) {
                    Product product = productList.get(position);
                    Intent intent = new Intent(getApplicationContext(), ProductChDelActivity.class);

                    intent.putExtra("id", String.valueOf(product.getId()));
                    intent.putExtra("name", product.getName());
                    intent.putExtra("category", product.getCategory());
                    intent.putExtra("image", product.getImage());
                    intent.putExtra("length", product.getLength());
                    intent.putExtra("width", product.getWidth());
                    intent.putExtra("height", product.getHeight());
                    intent.putExtra("price", String.valueOf(product.getPrice()));
                    intent.putExtra("description", product.getDescription());
                    intent.putExtra("is_active", product.isActive());

                    if (manager != null) {
                        intent.putExtra("manager", manager);
                    }
                    startActivityForResult(intent, REQUEST_UPDATE_PRODUCT);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadProducts();
            Toast.makeText(this, "Данные обновлены", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProducts() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        StringRequest stringRequest = new StringRequest(Request.Method.GET, JSON_URL,
                response -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean("error")) {
                            JSONArray jsonArray = object.getJSONArray("data");
                            productList = new ArrayList<>();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);
                                Product product = new Product();
                                product.setId(obj.getInt("id"));
                                product.setName(obj.optString("name", ""));
                                product.setCategory(obj.optString("category", ""));
                                product.setImage(obj.optString("image", ""));
                                product.setLength(obj.optString("length", ""));
                                product.setWidth(obj.optString("width", ""));
                                product.setHeight(obj.optString("height", ""));
                                product.setPrice(obj.optDouble("price", 0));
                                product.setDescription(obj.optString("description", ""));
                                product.setActive(obj.optBoolean("is_active", true));
                                productList.add(product);
                            }

                            ProductAdapter adapter = new ProductAdapter(
                                    getApplicationContext(),
                                    R.layout.row_product,
                                    productList
                            );
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
                        progressBar.setVisibility(View.GONE);
                    }
                    Log.e(TAG, "Load error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(), "Ошибка соединения", Toast.LENGTH_SHORT).show();
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
        if (item.getItemId() == R.id.save_fin) {
            Intent intent = new Intent(this, ProductSaveActivity.class);
            if (arguments != null) {
                intent.putExtra("manager", manager);
            }
            startActivityForResult(intent, REQUEST_SAVE_PRODUCT);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}