package com.example.kovka;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class OrderAdapter extends ArrayAdapter<JSONObject> {
    int listLayout;
    ArrayList<JSONObject> usersList;
    Context context;

    public OrderAdapter(Context context, int listLayout, int field, ArrayList<JSONObject> usersList) {
        super(context, listLayout, field, usersList);
        this.context = context;
        this.listLayout = listLayout;
        this.usersList = usersList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View listViewItem = inflater.inflate(listLayout, null, false);
        TextView nomer = listViewItem.findViewById(R.id.nomer);
        TextView zakaz = listViewItem.findViewById(R.id.zakaz);
        TextView time = listViewItem.findViewById(R.id.time);

        try {
            JSONObject item = usersList.get(position);

            // ID
            nomer.setText(item.getString("id"));

            // product.name — это вложенный объект
            JSONObject product = item.optJSONObject("product");
            if (product != null) {
                zakaz.setText(product.optString("name", "Без названия"));
            } else {
                zakaz.setText("Товар не указан");
            }

            // Форматирование даты
            String orderDate = item.optString("order_date", "");
            if (!orderDate.isEmpty()) {
                String formattedDate = formatDate(orderDate);
                time.setText(formattedDate);
            } else {
                time.setText("");
            }

        } catch (JSONException je) {
            je.printStackTrace();
        }
        return listViewItem;
    }

    // Добавьте этот метод в класс OrderAdapter
    private String formatDate(String isoDate) {
        try {
            // Парсим ISO 8601 формат
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = isoFormat.parse(isoDate);

            // Форматируем в нужный вид (например, "08.08.2026")
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            // Если парсинг не удался, пробуем другой формат
            try {
                SimpleDateFormat isoFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = isoFormat2.parse(isoDate);
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                return outputFormat.format(date);
            } catch (ParseException e2) {
                e2.printStackTrace();
                return isoDate; // Возвращаем как есть, если не удалось распарсить
            }
        }
    }
}