package com.example.kovka;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProductSaveActivity extends AppCompatActivity {
    private static final String TAG = "ProductSaveActivity";

    private EditText etName, etLength, etWidth, etHeight, etPrice, etDescription;
    private Spinner spinnerCategory;
    private ImageView ivImage;
    private Button selectPhotoButton;
    private ProgressBar progressBar;

    private String manager;
    private Uri selectedImageUri = null;
    private String selectedImageBase64 = null;

    private String[] categories = {"vorota", "zabor", "mangal", "kozirek", "lavo4ki", "ogradki", "reshetki", "mebel", "melo4i", "other"};
    private String[] categoryNames = {"Ворота", "Заборы", "Мангалы", "Козырьки", "Лавочки", "Оградки", "Решетки", "Мебель", "Полезные мелочи", "Другое"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_save);

        // Инициализация Views
        etName = findViewById(R.id.et_name);
        spinnerCategory = findViewById(R.id.spinner_category);
        etLength = findViewById(R.id.et_length);
        etWidth = findViewById(R.id.et_width);
        etHeight = findViewById(R.id.et_height);
        etPrice = findViewById(R.id.et_price);
        etDescription = findViewById(R.id.et_description);
        ivImage = findViewById(R.id.iv_image);
        selectPhotoButton = findViewById(R.id.select_photo_button);
        progressBar = findViewById(R.id.progressBar);

        // Устанавливаем placeholder для изображения
        ivImage.setImageResource(android.R.drawable.ic_menu_gallery);

        // Настройка Spinner для категорий
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Получение данных
        Bundle arguments = getIntent().getExtras();
        if (arguments != null) {
            manager = arguments.getString("manager");
        }

        selectPhotoButton.setOnClickListener(v -> {
            selectImageLauncher.launch("image/*");
        });
    }

    private String getCategoryValue(int position) {
        if (position >= 0 && position < categories.length) {
            return categories[position];
        }
        return "other";
    }

    private String convertImageToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream");
                return null;
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            inputStream.close();
            byteArrayOutputStream.close();

            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            String mimeType = getContentResolver().getType(uri);
            if (mimeType == null) {
                mimeType = "image/jpeg";
            }

            Log.d(TAG, "Image converted, size: " + imageBytes.length + " bytes");
            return "data:" + mimeType + ";base64," + base64Image;

        } catch (IOException e) {
            Log.e(TAG, "Error converting image: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tz_sohranit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sohr) {
            saveProduct();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProduct() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Введите название товара", Toast.LENGTH_SHORT).show();
            etName.requestFocus();
            return;
        }

        String url = Config.API_BASE + "products";

        JSONObject params = new JSONObject();
        try {
            params.put("name", name);
            params.put("category", getCategoryValue(spinnerCategory.getSelectedItemPosition()));
            params.put("length", etLength.getText().toString().trim());
            params.put("width", etWidth.getText().toString().trim());
            params.put("height", etHeight.getText().toString().trim());
            params.put("price", parseDoubleOrDefault(etPrice.getText().toString(), 0));
            params.put("description", etDescription.getText().toString().trim());
            params.put("is_active", true);

            if (selectedImageBase64 != null && !selectedImageBase64.isEmpty()) {
                params.put("image", selectedImageBase64);
                Log.d(TAG, "Sending image as base64");
            }

            Log.d(TAG, "Params: " + params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка в данных", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.POST,
                url,
                params,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        boolean error = response.optBoolean("error", false);
                        if (!error) {
                            Toast.makeText(ProductSaveActivity.this,
                                    "Товар успешно создан", Toast.LENGTH_LONG).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            String message = response.optString("message", "Ошибка");
                            Toast.makeText(ProductSaveActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Network error: " + error.getMessage());
                    if (error.networkResponse != null) {
                        try {
                            String responseBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e(TAG, "Error body: " + responseBody);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(ProductSaveActivity.this,
                            "Ошибка соединения", Toast.LENGTH_SHORT).show();
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

    private final ActivityResultLauncher<String> selectImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    Log.d(TAG, "Image selected: " + uri.toString());
                    ivImage.setImageURI(uri);
                    ivImage.setVisibility(View.VISIBLE);
                    selectedImageUri = uri;
                    selectedImageBase64 = convertImageToBase64(uri);
                    if (selectedImageBase64 != null) {
                        Log.d(TAG, "Image converted to base64");
                        Toast.makeText(this, "Изображение загружено", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Failed to convert image to base64");
                        Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "Image selection cancelled");
                }
            });

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