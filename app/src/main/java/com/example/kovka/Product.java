package com.example.kovka;

public class Product {
    private int id;
    private String name;
    private String category;
    private String image;
    private String length;
    private String width;
    private String height;
    private double price;
    private String description;
    private boolean isActive;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getLength() { return length; }
    public void setLength(String length) { this.length = length; }

    public String getWidth() { return width; }
    public void setWidth(String width) { this.width = width; }

    public String getHeight() { return height; }
    public void setHeight(String height) { this.height = height; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getImageUrl() {
        if (image != null && !image.isEmpty()) {
            return Config.getImageUrl(image);
        }
        return null;

    }
}