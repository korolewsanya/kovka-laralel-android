package com.example.kovka;

public class ApiResponse {
    private boolean error;
    private String message;
    private Object data;
    private String new_name;
    private String fileName;
    private String fileUrl;

    public boolean isError() { return error; }
    public void setError(boolean error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public String getNewName() { return new_name; }
    public void setNewName(String new_name) { this.new_name = new_name; }

    public boolean isSuccess() { return !error; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
}