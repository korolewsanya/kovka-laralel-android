package com.example.kovka;

public class ImageModel {
    private String name;
    private String url;
    private long size;
    private String modified;

    public ImageModel() {}

    public ImageModel(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public ImageModel(String name, String url, long size, String modified) {
        this.name = name;
        this.url = url;
        this.size = size;
        this.modified = modified;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getModified() { return modified; }
    public void setModified(String modified) { this.modified = modified; }
}