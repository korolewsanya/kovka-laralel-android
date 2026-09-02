package com.example.kovka;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static android.app.Activity.RESULT_OK;

public class OrderChDelActivity extends AppCompatActivity {

    private EditText id, data, izdelie, dlina, shirina, visota, prise, pay, proces, name, tel, email, coment;
    private ImageView imageView;

    private String idi1, data1, izdelie1, image1, dlina1, shirina1, visota1, prise1, pay1, proces1, name1, tel1, email1, coment1;
    private String manager;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_ch_del);

        // ПОЛУЧАЕМ РОЛЬ
        TokenManager tokenManager = new TokenManager(this);
        userRole = tokenManager.getRole();
        if (userRole == null) userRole = "employee";

        id = findViewById(R.id.id);
        data = findViewById(R.id.data);
        izdelie = findViewById(R.id.izdelie);
        imageView = findViewById(R.id.imageView);
        dlina = findViewById(R.id.dlina);
        shirina = findViewById(R.id.shirina);
        visota = findViewById(R.id.visota);
        prise = findViewById(R.id.prise);
        pay = findViewById(R.id.pay);
        proces = findViewById(R.id.proces);
        name = findViewById(R.id.name);
        tel = findViewById(R.id.tel);
        email = findViewById(R.id.email);
        coment = findViewById(R.id.coment);

        Intent intent = getIntent();
        idi1 = intent.getStringExtra("idi");
        data1 = intent.getStringExtra("date");
        izdelie1 = intent.getStringExtra("izdelie");
        image1 = intent.getStringExtra("image");
        dlina1 = intent.getStringExtra("dlina");
        shirina1 = intent.getStringExtra("shirina");
        visota1 = intent.getStringExtra("visota");
        prise1 = intent.getStringExtra("prise");
        pay1 = intent.getStringExtra("pay");
        proces1 = intent.getStringExtra("proces");
        name1 = intent.getStringExtra("name");
        tel1 = intent.getStringExtra("tel");
        email1 = intent.getStringExtra("email");
        coment1 = intent.getStringExtra("coment");

        if (intent.getStringExtra("manager") != null) {
            manager = intent.getStringExtra("manager");
        }

        // ОТОБРАЖЕНИЕ ДАННЫХ БЕЗ "null"
        id.setText(idi1 != null && !idi1.equals("null") ? idi1 : "");
        data.setText(formatDate(data1));
        izdelie.setText(izdelie1 != null && !izdelie1.equals("null") ? izdelie1 : "");
        dlina.setText(dlina1 != null && !dlina1.equals("null") ? dlina1 : "");
        shirina.setText(shirina1 != null && !shirina1.equals("null") ? shirina1 : "");
        visota.setText(visota1 != null && !visota1.equals("null") ? visota1 : "");
        prise.setText(prise1 != null && !prise1.equals("null") ? prise1 : "");
        pay.setText(pay1 != null && !pay1.equals("null") ? pay1 : "");
        proces.setText(proces1 != null && !proces1.equals("null") ? proces1 : "");
        name.setText(name1 != null && !name1.equals("null") ? name1 : "");
        tel.setText(tel1 != null && !tel1.equals("null") ? tel1 : "");
        email.setText(email1 != null && !email1.equals("null") ? email1 : "");
        coment.setText(coment1 != null && !coment1.equals("null") ? coment1 : "");

        loadImage();
    }

    private void loadImage() {
        if (image1 != null && !image1.isEmpty() && !image1.equals("null")) {
            try {
                String imageUrl = Config.getImageUrl(image1);

                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter()
                        .into(imageView);

            } catch (Exception e) {
                e.printStackTrace();
                Glide.with(this)
                        .load(android.R.drawable.ic_menu_camera)
                        .into(imageView);
            }
        } else {
            Glide.with(this)
                    .load(android.R.drawable.ic_menu_camera)
                    .into(imageView);
        }
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty() || isoDate.equals("null")) {
            return "";
        }
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = isoFormat.parse(isoDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            try {
                SimpleDateFormat isoFormat2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = isoFormat2.parse(isoDate);
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                return outputFormat.format(date);
            } catch (ParseException e2) {
                return isoDate;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_order_ch_del, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int nom = item.getItemId();

        switch (nom) {
            case R.id.change:

                // ПРОВЕРКА РОЛИ
                if (!userRole.equalsIgnoreCase("admin")) {
                    Toast.makeText(this, "Доступ запрещён", Toast.LENGTH_SHORT).show();
                    return true;
                }

                idi1 = id.getText().toString();
                data1 = data.getText().toString();
                izdelie1 = izdelie.getText().toString();
                dlina1 = dlina.getText().toString();
                shirina1 = shirina.getText().toString();
                visota1 = visota.getText().toString();
                prise1 = prise.getText().toString();
                pay1 = pay.getText().toString();
                proces1 = proces.getText().toString();
                name1 = name.getText().toString();
                tel1 = tel.getText().toString();
                email1 = email.getText().toString();
                coment1 = coment.getText().toString();

                if (TextUtils.isEmpty(izdelie1)) {
                    izdelie.setError("Пожалуйста, заполните это поле");
                } else if (TextUtils.isEmpty(prise1)) {
                    prise.setError("Пожалуйста, заполните это поле");
                } else if (TextUtils.isEmpty(tel1)) {
                    tel.setError("Пожалуйста, заполните это поле");
                } else {
                    updateOrder();
                }
                return true;

            case R.id.del:
                // ПРОВЕРКА РОЛИ
                if (!userRole.equalsIgnoreCase("admin")) {
                    Toast.makeText(this, "Доступ запрещён", Toast.LENGTH_SHORT).show();
                    return true;
                }
                deleteOrder();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateOrder() {
         String url = Config.API_BASE + "orders/" + idi1;
        RequestQueue queue = Volley.newRequestQueue(OrderChDelActivity.this);

        Map<String, String> params = new HashMap<>();
        params.put("product_id", "1");
        params.put("customer_name", name1);
        params.put("customer_phone", tel1);
        params.put("customer_email", email1);
        params.put("comment", coment1);
        params.put("price", prise1);
        params.put("paid", pay1);
        params.put("progress", proces1);
        params.put("length", dlina1);
        params.put("width", shirina1);
        params.put("height", visota1);
        params.put("status", "in_progress");

        JSONObject jsonBody = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, jsonBody,
                response -> {
                    try {
                        if (!response.getBoolean("error")) {
                            Toast.makeText(OrderChDelActivity.this, "Изменения сохранены", Toast.LENGTH_LONG).show();

                            // ВОЗВРАЩАЕМ РЕЗУЛЬТАТ
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("updated", true);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Toast.makeText(OrderChDelActivity.this,
                                    response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(OrderChDelActivity.this,
                                "Ошибка обработки ответа", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMsg = "Ошибка соединения. Попробуйте позже.";
                    if (error.networkResponse != null) {
                        try {
                            String responseBody = new String(error.networkResponse.data, "UTF-8");
                            JSONObject errorJson = new JSONObject(responseBody);
                            if (errorJson.has("message")) {
                                errorMsg = errorJson.getString("message");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(OrderChDelActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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

        queue.add(request);
    }

    private void deleteOrder() {
         String url = Config.API_BASE + "orders/" + idi1;
        RequestQueue queue = Volley.newRequestQueue(OrderChDelActivity.this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                response -> {
                    try {
                        if (!response.getBoolean("error")) {
                            Toast.makeText(OrderChDelActivity.this, "Заказ удалён", Toast.LENGTH_LONG).show();

                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("updated", true);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Toast.makeText(OrderChDelActivity.this,
                                    response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(OrderChDelActivity.this,
                                "Ошибка обработки ответа", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMsg = "Ошибка соединения. Попробуйте позже.";
                    if (error.networkResponse != null) {
                        try {
                            String responseBody = new String(error.networkResponse.data, "UTF-8");
                            JSONObject errorJson = new JSONObject(responseBody);
                            if (errorJson.has("message")) {
                                errorMsg = errorJson.getString("message");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(OrderChDelActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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

        queue.add(request);
    }
}