package com.example.kovka;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class OrderSaveActivity extends AppCompatActivity {
    private static final String TAG = "ZakazSave";

    private EditText data, image, dlina, shirina, visota, prise, pay, proces, name, tel, email, coment;
    private Spinner spinnerProduct;
    private WebView webBrowser;
    private String data1, image1, dlina1, shirina1, visota1, prise1, pay1, proces1, name1, tel1, email1, coment1;
    private String selectedProductId = "0";
    private String selectedProductImage = "";

    private Bundle arguments;
    private String manager;
    private ApiClient apiClient;

    private List<ProductItem> productList = new ArrayList<>();
    private boolean isCreating = false;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_save);

        apiClient = ApiClient.getInstance(this);

        arguments = getIntent().getExtras();
        if (arguments != null) {
            manager = arguments.get("manager").toString();
        }

        data = findViewById(R.id.data);
        spinnerProduct = findViewById(R.id.spinner_product);
        image = findViewById(R.id.image);
        webBrowser = findViewById(R.id.webBrowser);
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

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Создание заказа...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        setupWebView();

        String today = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("ru")).format(Calendar.getInstance().getTime());
        data.setText(today);

        loadProducts();
    }

    private void setupWebView() {
        webBrowser.getSettings().setLoadWithOverviewMode(true);
        webBrowser.getSettings().setUseWideViewPort(true);
        webBrowser.getSettings().setBuiltInZoomControls(true);
        webBrowser.getSettings().setDisplayZoomControls(false);
        webBrowser.getSettings().setJavaScriptEnabled(true);
        webBrowser.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Можно добавить обработку после загрузки
            }
        });
    }

    private void loadProducts() {
        apiClient.get("products", new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    if (!result.getBoolean("error")) {
                        JSONArray dataArray = result.getJSONArray("data");
                        productList.clear();
                        productList.add(new ProductItem("0", "Выберите товар", ""));

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject product = dataArray.getJSONObject(i);
                            String id = product.getString("id");
                            String name = product.getString("name");
                            String imagePath = product.optString("image", "");
                            productList.add(new ProductItem(id, name, imagePath));
                        }

                        runOnUiThread(() -> setupSpinner());
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Ошибка: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(OrderSaveActivity.this,
                                "Ошибка загрузки товаров: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void setupSpinner() {
        ArrayAdapter<ProductItem> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                productList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProduct.setAdapter(adapter);

        spinnerProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ProductItem selected = productList.get(position);
                selectedProductId = selected.getId();
                selectedProductImage = selected.getImage();

                if (!selectedProductImage.isEmpty()) {
                    image.setText(selectedProductImage);
                    loadImageInWebView(selectedProductImage);
                } else {
                    image.setText("");
                    showEmptyImage();
                }

                Log.d(TAG, "Выбран товар: " + selected.getName() +
                        " (ID: " + selectedProductId +
                        ", image: " + selectedProductImage + ")");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedProductId = "0";
                selectedProductImage = "";
                image.setText("");
                showEmptyImage();
            }
        });
    }

    private void loadImageInWebView(String imagePath) {
        String fullImageUrl = Config.getImageUrl(imagePath);
        Log.d(TAG, "Загрузка изображения: " + fullImageUrl);

        String html = "<html>" +
                "<head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "body { margin: 0; padding: 0; display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f5f5f5; }" +
                "img { max-width: 100%; max-height: 100%; object-fit: contain; }" +
                ".error { color: #999; font-family: sans-serif; font-size: 18px; text-align: center; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<img src='" + fullImageUrl + "' alt='Товар' onerror='this.style.display=\"none\"; document.getElementById(\"error\").style.display=\"block\";' />" +
                "<div id='error' class='error' style='display:none;'>Изображение не найдено</div>" +
                "</body>" +
                "</html>";

        webBrowser.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    private void showEmptyImage() {
        String html = "<html>" +
                "<head>" +
                "<style>" +
                "body { margin: 0; padding: 0; display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f5f5f5; }" +
                "p { color: #999; font-family: sans-serif; font-size: 18px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<p>Нет изображения</p>" +
                "</body>" +
                "</html>";
        webBrowser.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_order_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.sozd) {
            if (isCreating) {
                Toast.makeText(this, "Подождите, заказ создается...", Toast.LENGTH_SHORT).show();
                return true;
            }

            data1 = data.getText().toString();
            image1 = image.getText().toString();
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

            if (selectedProductId.equals("0")) {
                Toast.makeText(this, "Пожалуйста, выберите товар", Toast.LENGTH_SHORT).show();
                return true;
            }
            if (TextUtils.isEmpty(dlina1)) {
                dlina.setError("Пожалуйста, заполните это поле");
                return true;
            }
            if (TextUtils.isEmpty(shirina1)) {
                shirina.setError("Пожалуйста, заполните это поле");
                return true;
            }
            if (TextUtils.isEmpty(visota1)) {
                visota.setError("Пожалуйста, заполните это поле");
                return true;
            }
            if (TextUtils.isEmpty(prise1)) {
                prise.setError("Пожалуйста, заполните это поле");
                return true;
            }
            if (TextUtils.isEmpty(pay1)) {
                pay.setError("Пожалуйста, заполните это поле");
                return true;
            }
            if (TextUtils.isEmpty(proces1)) {
                proces.setError("Пожалуйста, заполните это поле");
                return true;
            }
            if (TextUtils.isEmpty(name1)) {
                name.setError("Пожалуйста, заполните это поле");
                return true;
            }
            if (TextUtils.isEmpty(tel1)) {
                tel.setError("Пожалуйста, заполните это поле");
                return true;
            }
            if (TextUtils.isEmpty(email1)) {
                email.setError("Пожалуйста, заполните это поле");
                return true;
            }

            createOrder();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createOrder() {
        isCreating = true;

        runOnUiThread(() -> {
            if (progressDialog != null && !progressDialog.isShowing()) {
                progressDialog.show();
            }
        });

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("product_id", Integer.parseInt(selectedProductId));
            jsonBody.put("customer_name", name1);
            jsonBody.put("customer_phone", tel1);
            jsonBody.put("customer_email", email1);
            jsonBody.put("comment", coment1);
            jsonBody.put("price", prise1);
            jsonBody.put("paid", pay1);
            jsonBody.put("progress", proces1);
            jsonBody.put("length", dlina1);
            jsonBody.put("width", shirina1);
            jsonBody.put("height", visota1);
            jsonBody.put("status", "new");
            jsonBody.put("order_date", data1);
            jsonBody.put("image", image1);

            Log.d(TAG, "Отправка заказа: " + jsonBody.toString());

            apiClient.post("orders", jsonBody, new ApiClient.ApiCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject result) {
                    isCreating = false;
                    runOnUiThread(() -> {
                        hideProgressDialog();
                        Toast.makeText(OrderSaveActivity.this, "Заказ создан! Проверьте список.", Toast.LENGTH_LONG).show();
                        navigateBack();
                    });
                }

                @Override
                public void onError(String error) {
                    isCreating = false;
                    runOnUiThread(() -> {
                        hideProgressDialog();
                        Toast.makeText(OrderSaveActivity.this, "Заказ отправлен. Проверьте список.", Toast.LENGTH_LONG).show();
                        navigateBack();
                    });
                }
            });

        } catch (JSONException e) {
            isCreating = false;
            runOnUiThread(() -> {
                hideProgressDialog();
                Log.e(TAG, "JSON ошибка: " + e.getMessage());
                Toast.makeText(OrderSaveActivity.this, "Ошибка формирования данных", Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            isCreating = false;
            runOnUiThread(() -> {
                hideProgressDialog();
                Log.e(TAG, "Ошибка: " + e.getMessage());
                Toast.makeText(OrderSaveActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
    }

    private void navigateBack() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("created", true);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private static class ProductItem {
        private String id;
        private String name;
        private String image;

        public ProductItem(String id, String name, String image) {
            this.id = id;
            this.name = name;
            this.image = image;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getImage() { return image; }

        @Override
        public String toString() {
            return name;
        }
    }
}