package com.example.kovka;

public class FinanceModel {
    private int id;
    private String date;
    private double income;
    private double expense;
    private double profit;
    private String note;

    public FinanceModel() {}

    public FinanceModel(int id, String date, double income, double expense, double profit, String note) {
        this.id = id;
        this.date = date;
        this.income = income;
        this.expense = expense;
        this.profit = profit;
        this.note = note;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getIncome() { return income; }
    public void setIncome(double income) { this.income = income; }

    public double getExpense() { return expense; }
    public void setExpense(double expense) { this.expense = expense; }

    public double getProfit() { return profit; }
    public void setProfit(double profit) { this.profit = profit; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}