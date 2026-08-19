package com.example.kovka;

public class Employee {
    private int id;
    private String full_name;
    private String position;
    private String phone;
    private String email;
    private String address;
    private String hire_date;
    private String notes;
    private boolean is_active;

    // Конструктор по умолчанию
    public Employee() {
    }

    // Конструктор с параметрами
    public Employee(int id, String full_name, String position) {
        this.id = id;
        this.full_name = full_name;
        this.position = position;
    }

    // Геттеры
    public int getId() { return id; }
    public String getFull_name() { return full_name; }
    public String getPosition() { return position; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getHire_date() { return hire_date; }
    public String getNotes() { return notes; }
    public boolean isIs_active() { return is_active; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setFull_name(String full_name) { this.full_name = full_name; }
    public void setPosition(String position) { this.position = position; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setAddress(String address) { this.address = address; }
    public void setHire_date(String hire_date) { this.hire_date = hire_date; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setIs_active(boolean is_active) { this.is_active = is_active; }

    @Override
    public String toString() {
        return full_name + " - " + position;
    }
}