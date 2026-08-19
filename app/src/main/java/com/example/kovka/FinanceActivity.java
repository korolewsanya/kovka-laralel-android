package com.example.kovka;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class FinanceActivity extends AppCompatActivity implements FinanceAdapter.OnFinanceActionListener {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private MaterialButton btnAddFinance;
    private FinanceAdapter adapter;
    private List<FinanceModel> financeList = new ArrayList<>();
    private ApiService apiService;

    private static final int REQUEST_ADD = 1;
    private static final int REQUEST_EDIT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance);

        recyclerView = findViewById(R.id.recyclerViewFinance);
        progressBar = findViewById(R.id.progressBar);
        btnAddFinance = findViewById(R.id.btnAddFinance);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FinanceAdapter(financeList, this);
        recyclerView.setAdapter(adapter);

        apiService = ApiService.getInstance(this);

        btnAddFinance.setOnClickListener(v -> {
            Intent intent = new Intent(FinanceActivity.this, FinanceAddEditActivity.class);
            intent.putExtra("mode", "add");
            startActivityForResult(intent, REQUEST_ADD);
        });

        loadFinances();
    }

    private void loadFinances() {
        progressBar.setVisibility(ProgressBar.VISIBLE);

        apiService.getFinances(new ApiService.FinanceListCallback() {
            @Override
            public void onSuccess(List<FinanceModel> finances) {
                progressBar.setVisibility(ProgressBar.GONE);
                financeList.clear();
                financeList.addAll(finances);
                adapter.notifyDataSetChanged();

                if (financeList.isEmpty()) {
                    Toast.makeText(FinanceActivity.this, "Нет записей", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(ProgressBar.GONE);
                Toast.makeText(FinanceActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onEdit(FinanceModel finance, int position) {
        Intent intent = new Intent(FinanceActivity.this, FinanceAddEditActivity.class);
        intent.putExtra("mode", "edit");
        intent.putExtra("id", finance.getId());
        intent.putExtra("date", finance.getDate());
        intent.putExtra("income", finance.getIncome());
        intent.putExtra("expense", finance.getExpense());
        intent.putExtra("profit", finance.getProfit());
        intent.putExtra("note", finance.getNote());
        intent.putExtra("position", position);
        startActivityForResult(intent, REQUEST_EDIT);
    }

    @Override
    public void onDelete(FinanceModel finance, int position) {
        // Форматируем дату для сообщения
        String displayDate = formatDateForDisplay(finance.getDate());

        new AlertDialog.Builder(this)
                .setTitle("Удаление")
                .setMessage("Удалить запись от " + displayDate + "?")
                .setPositiveButton("Да", (dialog, which) -> deleteFinance(finance, position))
                .setNegativeButton("Нет", null)
                .show();
    }

    private String formatDateForDisplay(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "дата не указана";
        }
        if (dateStr.contains("T")) {
            return dateStr.substring(0, dateStr.indexOf("T"));
        }
        return dateStr;
    }

    private void deleteFinance(FinanceModel finance, int position) {
        progressBar.setVisibility(ProgressBar.VISIBLE);

        apiService.deleteFinance(finance.getId(), new ApiService.SimpleCallback() {
            @Override
            public void onSuccess(ApiResponse response) {
                progressBar.setVisibility(ProgressBar.GONE);
                if (response.isSuccess()) {
                    adapter.removeItem(position);
                    Toast.makeText(FinanceActivity.this, "Запись удалена", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FinanceActivity.this,
                            response.getMessage() != null ? response.getMessage() : "Ошибка удаления",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(ProgressBar.GONE);
                Toast.makeText(FinanceActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            loadFinances();
        }
    }
}