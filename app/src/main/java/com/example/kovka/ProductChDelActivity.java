package com.example.kovka;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProductChDelActivity extends AppCompatActivity {
    private static final String TAG = "ProductChDelActivity";

    private EditText etId, etName, etLength, etWidth, etHeight, etPrice, etDescription;
    private Spinner spinnerCategory;
    private ImageView ivImage;
    private Button btnUpdate, btnDelete, btnBack, btnViewImage;
    private ProgressBar progressBar;

    private String productId;
    private String productName;
    private String currentImage;
    private String manager;
    private String userRole;

    private ArrayAdapter<String> categoryAdapter;
    private String[] categories = {"vorota", "zabor", "mangal", "kozirek", "lavo4ki", "ogradki", "reshetki", "mebel", "melo4i", "other"};
    private String[] categoryNames = {"Ворота", "Заборы", "Мангалы", "Козырьки", "Лавочки", "Оградки", "Решетки", "Мебель", "Полезные мелочи", "Другое"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_ch_del);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Проверка роли
        TokenManager tokenManager = new TokenManager(this);
        userRole = tokenManager.getRole();
        if (userRole == null) userRole = "employee";

        // Инициализация Views
        etId = findViewById(R.id.et_id);
        etName = findViewById(R.id.et_name);
        spinnerCategory = findViewById(R.id.spinner_category);
        etLength = findViewById(R.id.et_length);
        etWidth = findViewById(R.id.et_width);
        etHeight = findViewById(R.id.et_height);
        etPrice = findViewById(R.id.et_price);
        etDescription = findViewById(R.id.et_description);
        ivImage = findViewById(R.id.iv_image);
        btnUpdate = findViewById(R.id.btn_update);
        btnDelete = findViewById(R.id.btn_delete);
        btnBack = findViewById(R.id.btn_back);
        btnViewImage = findViewById(R.id.btn_view_image);
        progressBar = findViewById(R.id.progressBar);

        // Настройка Spinner для категорий
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Получение данных
        Bundle arguments = getIntent().getExtras();
        if (arguments != null) {
            productId = arguments.getString("id");
            productName = arguments.getString("name");
            currentImage = arguments.getString("image");

            etId.setText(productId);
            etId.setEnabled(false);
            etName.setText(productName);

            // Устанавливаем категорию
            String category = arguments.getString("category", "other");
            int position = getCategoryPosition(category);
            spinnerCategory.setSelection(position);

            etLength.setText(arguments.getString("length", ""));
            etWidth.setText(arguments.getString("width", ""));
            etHeight.setText(arguments.getString("height", ""));
            etPrice.setText(arguments.getString("price", "0"));
            etDescription.setText(arguments.getString("description", ""));

            // Загружаем изображение со стандартными placeholder
            loadImage(currentImage);

            manager = arguments.getString("manager");
        }

        btnUpdate.setOnClickListener(v -> updateProduct());
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
        btnBack.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        btnViewImage.setOnClickListener(v -> viewFullImage());
    }

    private int getCategoryPosition(String category) {
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(category)) {
                return i;
            }
        }
        return categories.length - 1;
    }

    private String getCategoryValue(int position) {
        if (position >= 0 && position < categories.length) {
            return categories[position];
        }
        return "other";
    }

    private void loadImage(String imageName) {
        if (imageName != null && !imageName.isEmpty()) {
            String imageUrl = Config.STORAGE_BASE + imageName;
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(ivImage);
        } else {
            ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private void viewFullImage() {
        if (currentImage != null && !currentImage.isEmpty()) {
            String imageUrl = Config.STORAGE_BASE + currentImage;
            Intent intent = new Intent(this, ImageFullScreenActivity.class);
            intent.putExtra(ImageFullScreenActivity.EXTRA_IMAGE_URL, imageUrl);
            intent.putExtra(ImageFullScreenActivity.EXTRA_IMAGE_NAME, currentImage);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Нет изображения", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isAdmin() {
        return "admin".equalsIgnoreCase(userRole) || "administrator".equalsIgnoreCase(userRole);
    }

    private void showDeleteConfirmationDialog() {
        if (!isAdmin()) {
            Toast.makeText(this, "Доступ запрещён", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Подтверждение удаления");
        builder.setMessage("Вы уверены, что хотите удалить товар \"" + productName + "\"?\nЭто действие нельзя отменить!");
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton("Да, удалить", (dialog, which) -> deleteProduct());
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateProduct() {
        if (!isAdmin()) {
            Toast.makeText(this, "Доступ запрещён", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show();
            etName.requestFocus();
            return;
        }

        String url = Config.API_BASE + "products/" + productId;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("category", getCategoryValue(spinnerCategory.getSelectedItemPosition()));
            jsonBody.put("length", etLength.getText().toString().trim());
            jsonBody.put("width", etWidth.getText().toString().trim());
            jsonBody.put("height", etHeight.getText().toString().trim());
            jsonBody.put("price", parseDoubleOrDefault(etPrice.getText().toString(), 0));
            jsonBody.put("description", etDescription.getText().toString().trim());
            jsonBody.put("is_active", true);

            Log.d(TAG, "Updating data: " + jsonBody.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка в данных", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpdate.setEnabled(false);

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);
                    Toast.makeText(ProductChDelActivity.this,
                            "Товар успешно обновлен", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);
                    String errorMsg = "Ошибка обновления";
                    if (error.networkResponse != null) {
                        errorMsg += ". Код: " + error.networkResponse.statusCode;
                        try {
                            String responseBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e(TAG, "Error body: " + responseBody);
                            JSONObject errorJson = new JSONObject(responseBody);
                            if (errorJson.has("message")) {
                                errorMsg += " - " + errorJson.getString("message");
                            }
                        } catch (Exception e) {}
                    }
                    Toast.makeText(ProductChDelActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public byte[] getBody() {
                return jsonBody.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

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

        NetworkUtils.configureTimeout(request);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void deleteProduct() {
        if (!isAdmin()) {
            Toast.makeText(this, "Доступ запрещён", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Config.API_BASE + "products/" + productId;

        progressBar.setVisibility(View.VISIBLE);
        btnDelete.setEnabled(false);

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    btnDelete.setEnabled(true);
                    Toast.makeText(ProductChDelActivity.this,
                            "Товар \"" + productName + "\" успешно удален", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    btnDelete.setEnabled(true);
                    String errorMsg = "Ошибка удаления";
                    if (error.networkResponse != null) {
                        errorMsg += ". Код: " + error.networkResponse.statusCode;
                    }
                    Toast.makeText(ProductChDelActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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

        NetworkUtils.configureTimeout(request);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private double parseDoubleOrDefault(String value, double defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}