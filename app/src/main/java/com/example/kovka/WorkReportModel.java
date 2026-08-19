package com.example.kovka;

public class WorkReportModel {
    private int id;
    private int employeeId;
    private String employeeName;
    private String specialty;
    private int workClass;
    private String task;
    private String report;
    private String date;
    private String image;
    private String accessCode;

    public WorkReportModel() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public int getWorkClass() { return workClass; }
    public void setWorkClass(int workClass) { this.workClass = workClass; }

    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }

    public String getReport() { return report; }
    public void setReport(String report) { this.report = report; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getAccessCode() { return accessCode; }
    public void setAccessCode(String accessCode) { this.accessCode = accessCode; }
}