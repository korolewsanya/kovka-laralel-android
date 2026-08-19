package com.example.kovka;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ImgSelectFromServerActivity extends AppCompatActivity implements ImageAdapter.OnImageActionListener {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ImageAdapter adapter;
    private List<ImageModel> imageList = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_select_all_from_server);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ImageAdapter(this, imageList, this);
        recyclerView.setAdapter(adapter);

        apiService = ApiService.getInstance(this);

        loadImages();
    }

    private void loadImages() {
        progressBar.setVisibility(ProgressBar.VISIBLE);

        apiService.getImages(new ApiService.ImageListCallback() {
            @Override
            public void onSuccess(List<ImageModel> images) {
                progressBar.setVisibility(ProgressBar.GONE);
                imageList.clear();
                imageList.addAll(images);
                adapter.notifyDataSetChanged();

                if (imageList.isEmpty()) {
                    Toast.makeText(ImgSelectFromServerActivity.this, "Нет изображений", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(ProgressBar.GONE);
                Toast.makeText(ImgSelectFromServerActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDelete(ImageModel image, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Удаление")
                .setMessage("Удалить " + image.getName() + "?")
                .setPositiveButton("Да", (dialog, which) -> deleteImage(image, position))
                .setNegativeButton("Нет", null)
                .show();
    }

    private void deleteImage(ImageModel image, int position) {
        progressBar.setVisibility(ProgressBar.VISIBLE);

        apiService.deleteImage(image.getName(), new ApiService.SimpleCallback() {
            @Override
            public void onSuccess(ApiResponse response) {
                progressBar.setVisibility(ProgressBar.GONE);
                if (response.isSuccess()) {
                    adapter.removeItem(position);
                    Toast.makeText(ImgSelectFromServerActivity.this, "Удалено", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ImgSelectFromServerActivity.this,
                            response.getMessage() != null ? response.getMessage() : "Ошибка удаления",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(ProgressBar.GONE);
                Toast.makeText(ImgSelectFromServerActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}