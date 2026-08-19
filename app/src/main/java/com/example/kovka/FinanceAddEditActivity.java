package com.example.kovka;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class FinanceAddEditActivity extends AppCompatActivity {
    private TextInputEditText etDate, etIncome, etExpense, etProfit, etNote;
    private MaterialButton btnSave;
    private TextView tvTitle;
    private ApiService apiService;
    private FinanceModel editingFinance;
    private int editingPosition = -1;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance_add_edit);

        etDate = findViewById(R.id.etDate);
        etIncome = findViewById(R.id.etIncome);
        etExpense = findViewById(R.id.etExpense);
        etProfit = findViewById(R.id.etProfit);
        etNote = findViewById(R.id.etNote);
        btnSave = findViewById(R.id.btnSave);
        tvTitle = findViewById(R.id.tvTitle);

        apiService = ApiService.getInstance(this);

        mode = getIntent().getStringExtra("mode");

        if ("edit".equals(mode)) {
            tvTitle.setText("✏️ Изменить запись");
            editingPosition = getIntent().getIntExtra("position", -1);

            editingFinance = new FinanceModel();
            editingFinance.setId(getIntent().getIntExtra("id", 0));
            editingFinance.setDate(getIntent().getStringExtra("date"));
            editingFinance.setIncome(getIntent().getDoubleExtra("income", 0));
            editingFinance.setExpense(getIntent().getDoubleExtra("expense", 0));
            editingFinance.setProfit(getIntent().getDoubleExtra("profit", 0));
            editingFinance.setNote(getIntent().getStringExtra("note"));

            if (editingFinance != null) {
                // Показываем только дату без времени
                String date = editingFinance.getDate();
                if (date != null && date.contains("T")) {
                    date = date.substring(0, date.indexOf("T"));
                }
                etDate.setText(date);
                etIncome.setText(String.valueOf(editingFinance.getIncome()));
                etExpense.setText(String.valueOf(editingFinance.getExpense()));
                etProfit.setText(String.valueOf(editingFinance.getProfit()));
                etNote.setText(editingFinance.getNote());
            }

            btnSave.setText("💾 Обновить");
        } else {
            tvTitle.setText("➕ Добавить запись");
            // Устанавливаем сегодняшнюю дату в формате YYYY-MM-DD
            String today = getTodayDate();
            etDate.setText(today);
            btnSave.setText("💾 Сохранить");
        }

        btnSave.setOnClickListener(v -> {
            if (validateFields()) {
                if ("edit".equals(mode)) {
                    updateFinance();
                } else {
                    createFinance();
                }
            }
        });
    }

    // Получить сегодняшнюю дату в формате YYYY-MM-DD
    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new java.util.Date());
    }

    private boolean validateFields() {
        String date = etDate.getText().toString().trim();
        String incomeStr = etIncome.getText().toString().trim();
        String expenseStr = etExpense.getText().toString().trim();
        String profitStr = etProfit.getText().toString().trim();

        if (date.isEmpty()) {
            etDate.setError("Введите дату в формате ГГГГ-ММ-ДД");
            etDate.requestFocus();
            return false;
        }

        // Проверяем формат даты YYYY-MM-DD
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            etDate.setError("Используйте формат ГГГГ-ММ-ДД (например: 2023-06-03)");
            etDate.requestFocus();
            return false;
        }

        if (incomeStr.isEmpty() && expenseStr.isEmpty() && profitStr.isEmpty()) {
            Toast.makeText(this, "Заполните хотя бы одно поле: доход, расход или прибыль", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void createFinance() {
        FinanceModel finance = getFinanceFromFields();

        btnSave.setEnabled(false);
        btnSave.setText("⏳ Сохранение...");

        apiService.createFinance(finance, new ApiService.SimpleCallback() {
            @Override
            public void onSuccess(ApiResponse response) {
                btnSave.setEnabled(true);
                btnSave.setText("💾 Сохранить");

                if (response.isSuccess()) {
                    Toast.makeText(FinanceAddEditActivity.this, "Запись создана", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(FinanceAddEditActivity.this,
                            response.getMessage() != null ? response.getMessage() : "Ошибка",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String error) {
                btnSave.setEnabled(true);
                btnSave.setText("💾 Сохранить");
                Toast.makeText(FinanceAddEditActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateFinance() {
        FinanceModel finance = getFinanceFromFields();
        finance.setId(editingFinance.getId());

        btnSave.setEnabled(false);
        btnSave.setText("⏳ Обновление...");

        apiService.updateFinance(finance.getId(), finance, new ApiService.SimpleCallback() {
            @Override
            public void onSuccess(ApiResponse response) {
                btnSave.setEnabled(true);
                btnSave.setText("💾 Обновить");

                if (response.isSuccess()) {
                    Toast.makeText(FinanceAddEditActivity.this, "Запись обновлена", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(FinanceAddEditActivity.this,
                            response.getMessage() != null ? response.getMessage() : "Ошибка",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String error) {
                btnSave.setEnabled(true);
                btnSave.setText("💾 Обновить");
                Toast.makeText(FinanceAddEditActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private FinanceModel getFinanceFromFields() {
        FinanceModel finance = new FinanceModel();

        // Дата в формате YYYY-MM-DD
        finance.setDate(etDate.getText().toString().trim());

        String incomeStr = etIncome.getText().toString().trim();
        if (!incomeStr.isEmpty()) {
            finance.setIncome(Double.parseDouble(incomeStr));
        } else {
            finance.setIncome(0);
        }

        String expenseStr = etExpense.getText().toString().trim();
        if (!expenseStr.isEmpty()) {
            finance.setExpense(Double.parseDouble(expenseStr));
        } else {
            finance.setExpense(0);
        }

        String profitStr = etProfit.getText().toString().trim();
        if (!profitStr.isEmpty()) {
            finance.setProfit(Double.parseDouble(profitStr));
        } else {
            finance.setProfit(finance.getIncome() - finance.getExpense());
        }

        String note = etNote.getText().toString().trim();
        finance.setNote(note);

        return finance;
    }
}