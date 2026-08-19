package com.example.kovka;

public class EndPoints {
    // Базовый URL для Laravel API
    private static final String ROOT_URL = Config.API_BASE;

    // Laravel маршруты для изображений
    public static final String UPLOAD_URL = ROOT_URL + "images/upload";
    public static final String IMAGES_URL = ROOT_URL + "images";
    public static final String DELETE_URL = ROOT_URL + "images/delete";
    public static final String RENAME_URL = ROOT_URL + "images/rename";
}
