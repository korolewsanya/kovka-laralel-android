package com.example.kovka;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FinanceAdapter extends RecyclerView.Adapter<FinanceAdapter.ViewHolder> {
    private List<FinanceModel> financeList;
    private OnFinanceActionListener listener;
    private NumberFormat currencyFormat;

    public interface OnFinanceActionListener {
        void onEdit(FinanceModel finance, int position);
        void onDelete(FinanceModel finance, int position);
    }

    public FinanceAdapter(List<FinanceModel> financeList, OnFinanceActionListener listener) {
        this.financeList = financeList;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getInstance(new Locale("ru", "RU"));
        currencyFormat.setMaximumFractionDigits(0);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_finance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FinanceModel finance = financeList.get(position);

        // Форматируем дату из 2023-08-03T00:00:00.0000000Z в 2023-08-03
        String displayDate = formatDate(finance.getDate());
        holder.tvDate.setText(displayDate);

        // Доход
        if (finance.getIncome() > 0) {
            holder.tvIncome.setText("Доход: " + currencyFormat.format(finance.getIncome()) + " ₽");
            holder.tvIncome.setVisibility(View.VISIBLE);
        } else {
            holder.tvIncome.setVisibility(View.GONE);
        }

        // Расход
        if (finance.getExpense() > 0) {
            holder.tvExpense.setText("Расход: " + currencyFormat.format(finance.getExpense()) + " ₽");
            holder.tvExpense.setVisibility(View.VISIBLE);
        } else {
            holder.tvExpense.setVisibility(View.GONE);
        }

        // Прибыль
        double profit = finance.getProfit();
        holder.tvProfit.setText(currencyFormat.format(profit) + " ₽");
        if (profit >= 0) {
            holder.tvProfit.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvProfit.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
        }

        // Примечание
        if (finance.getNote() != null && !finance.getNote().isEmpty()) {
            holder.tvNote.setText(finance.getNote());
            holder.tvNote.setVisibility(View.VISIBLE);
        } else {
            holder.tvNote.setVisibility(View.GONE);
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(finance, position);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(finance, position);
            }
        });
    }

    // Метод для форматирования даты
    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "Дата не указана";
        }

        // Если строка содержит "T", берем только часть до "T"
        if (dateStr.contains("T")) {
            return dateStr.substring(0, dateStr.indexOf("T"));
        }

        return dateStr;
    }

    @Override
    public int getItemCount() {
        return financeList.size();
    }

    public void updateItem(int position, FinanceModel newFinance) {
        financeList.set(position, newFinance);
        notifyItemChanged(position);
    }

    public void removeItem(int position) {
        financeList.remove(position);
        notifyItemRemoved(position);
    }

    public void addItem(FinanceModel finance) {
        financeList.add(0, finance);
        notifyItemInserted(0);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvIncome, tvExpense, tvProfit, tvNote;
        Button btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvIncome = itemView.findViewById(R.id.tvIncome);
            tvExpense = itemView.findViewById(R.id.tvExpense);
            tvProfit = itemView.findViewById(R.id.tvProfit);
            tvNote = itemView.findViewById(R.id.tvNote);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}