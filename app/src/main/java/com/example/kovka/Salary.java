package com.example.kovka;

public class Salary {
    private int id;
    private int employee_id;
    private String date;
    private String specialty;
    private String employee_name;
    private double accrued;
    private double received;
    private String description;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployee_id() { return employee_id; }
    public void setEmployee_id(int employee_id) { this.employee_id = employee_id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getEmployee_name() { return employee_name; }
    public void setEmployee_name(String employee_name) { this.employee_name = employee_name; }

    public double getAccrued() { return accrued; }
    public void setAccrued(double accrued) { this.accrued = accrued; }

    public double getReceived() { return received; }
    public void setReceived(double received) { this.received = received; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}