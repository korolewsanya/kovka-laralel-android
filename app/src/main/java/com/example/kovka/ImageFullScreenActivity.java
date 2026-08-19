package com.example.kovka;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ImageFullScreenActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URL = "image_url";
    public static final String EXTRA_IMAGE_NAME = "image_name";

    private ImageView fullscreenImageView;
    private TextView tvFileName;
    private ImageView btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_full_screen);

        fullscreenImageView = findViewById(R.id.fullscreenImageView);
        tvFileName = findViewById(R.id.tvFileName);
        btnClose = findViewById(R.id.btnClose);

        // Получаем данные из Intent
        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        String imageName = getIntent().getStringExtra(EXTRA_IMAGE_NAME);

        // Показываем имя файла
        if (imageName != null) {
            tvFileName.setText(imageName);
        }

        // Загружаем изображение через Glide
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error)
                    .into(fullscreenImageView);
        }

        // Закрытие по кнопке
        btnClose.setOnClickListener(v -> finish());

        // Закрытие по нажатию на изображение (опционально)
        fullscreenImageView.setOnClickListener(v -> finish());
    }
}
