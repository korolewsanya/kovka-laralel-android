package com.example.kovka;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TzAdapter extends ArrayAdapter<JSONObject> {
    int listLayout;
    ArrayList<JSONObject> usersList;
    Context context;

    public TzAdapter(Context context, int listLayout, int field, ArrayList<JSONObject> usersList) {
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
            JSONObject obj = usersList.get(position);
            nomer.setText(obj.optString("id", ""));
            zakaz.setText(obj.optString("task", ""));

            // Форматируем дату
            String dateStr = obj.optString("date", "");
            time.setText(formatDate(dateStr));
        } catch (Exception je) {
            je.printStackTrace();
        }
        return listViewItem;
    }

    // Форматируем дату из 2024-05-25 в 25.05.2024
    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "";
        }
        try {
            if (dateStr.contains("-") && dateStr.length() == 10) {
                String[] parts = dateStr.split("-");
                if (parts.length == 3) {
                    return parts[2] + "." + parts[1] + "." + parts[0];
                }
            }
            if (dateStr.contains("T")) {
                dateStr = dateStr.substring(0, dateStr.indexOf("T"));
                String[] parts = dateStr.split("-");
                if (parts.length == 3) {
                    return parts[2] + "." + parts[1] + "." + parts[0];
                }
            }
            return dateStr;
        } catch (Exception e) {
            return dateStr;
        }
    }
}