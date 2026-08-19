package com.example.kovka;

public class Material {
    private int id;
    private String date;
    private String name;
    private double purchased;
    private double used;
    private double balance;
    private double price_per_unit;
    private double total_price;
    private String created_at;
    private String updated_at;

    // Конструктор по умолчанию
    public Material() {
    }

    // Конструктор с параметрами
    public Material(int id, String date, String name, double purchased,
                    double used, double balance, double price_per_unit,
                    double total_price) {
        this.id = id;
        this.date = date;
        this.name = name;
        this.purchased = purchased;
        this.used = used;
        this.balance = balance;
        this.price_per_unit = price_per_unit;
        this.total_price = total_price;
    }

    // Геттеры
    public int getId() { return id; }
    public String getDate() { return date; }
    public String getName() { return name; }
    public double getPurchased() { return purchased; }
    public double getUsed() { return used; }
    public double getBalance() { return balance; }
    public double getPrice_per_unit() { return price_per_unit; }
    public double getTotal_price() { return total_price; }
    public String getCreated_at() { return created_at; }
    public String getUpdated_at() { return updated_at; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setDate(String date) { this.date = date; }
    public void setName(String name) { this.name = name; }
    public void setPurchased(double purchased) { this.purchased = purchased; }
    public void setUsed(double used) { this.used = used; }
    public void setBalance(double balance) { this.balance = balance; }
    public void setPrice_per_unit(double price_per_unit) { this.price_per_unit = price_per_unit; }
    public void setTotal_price(double total_price) { this.total_price = total_price; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
    public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }
}