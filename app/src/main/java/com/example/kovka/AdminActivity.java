package com.example.kovka;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class AdminActivity extends AppCompatActivity {

    private ApiClient apiClient;
    private TokenManager tokenManager;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        apiClient = ApiClient.getInstance(this);
        tokenManager = new TokenManager(this);

        if (!tokenManager.hasToken()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userRole = tokenManager.getRole();
        if (userRole == null || userRole.isEmpty()) {
            userRole = "employee";
        }

        Toast.makeText(this, "Добро пожаловать, " + userRole + "!", Toast.LENGTH_SHORT).show();
    }

    private boolean isAdmin() {
        return "admin".equalsIgnoreCase(userRole) || "administrator".equalsIgnoreCase(userRole);
    }

    private void showAccessDenied() {
        Toast.makeText(this, "Доступ запрещён для вашей роли", Toast.LENGTH_SHORT).show();
    }

    // --- Обработчики кликов ---

    public void zakaz(View view) {
        startActivity(new Intent(this, OrderActivity.class));
    }

    public void mater(View view) {
        startActivity(new Intent(this, MaterActivity.class));
    }

    public void zp(View view) {
        if (!isAdmin()) {
            showAccessDenied();
            return;
        }
        startActivity(new Intent(this, SalaryActivity.class));
    }

    public void fin(View view) {
        if (!isAdmin()) {
            showAccessDenied();
            return;
        }
        startActivity(new Intent(this, FinanceActivity.class));
    }

    public void wokers(View view) {
        if (!isAdmin()) {
            showAccessDenied();
            return;
        }
        startActivity(new Intent(this, EmployeeActivity.class));
    }

    public void otchet(View view) {
        startActivity(new Intent(this, WorkReportsActivity.class));
    }

    public void tz(View view) {
        startActivity(new Intent(this, TzActivity.class));
    }

    // --- Меню ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin, menu);

        MenuItem imgItem = menu.findItem(R.id.img);
        MenuItem img3Item = menu.findItem(R.id.img3);

        if (!isAdmin()) {
            if (imgItem != null) imgItem.setVisible(false);
            if (img3Item != null) img3Item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.img:
                if (!isAdmin()) {
                    showAccessDenied();
                    return true;
                }
                startActivity(new Intent(this, ImgUploadToServerActivity.class));
                return true;

            case R.id.img3:
                if (!isAdmin()) {
                    showAccessDenied();
                    return true;
                }
                startActivity(new Intent(this, ImgSelectFromServerActivity.class));
                return true;

            case R.id.logout:
                logout();
                return true;

            case R.id.products:
                startActivity(new Intent(this, ProductActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        tokenManager.clearToken();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Вы вышли из системы", Toast.LENGTH_SHORT).show();
    }
}