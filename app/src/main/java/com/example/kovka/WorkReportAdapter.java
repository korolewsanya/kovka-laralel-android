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

public class WorkReportAdapter extends ArrayAdapter<JSONObject> {
    int listLayout;
    ArrayList<JSONObject> usersList;
    Context context;

    public WorkReportAdapter(Context context, int listLayout, int field, ArrayList<JSONObject> usersList) {
        super(context, listLayout, field, usersList);
        this.context = context;
        this.listLayout = listLayout;
        this.usersList = usersList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View listViewItem = inflater.inflate(listLayout, null, false);

        TextView tvDate = listViewItem.findViewById(R.id.date);
        TextView tvProf = listViewItem.findViewById(R.id.prof);
        TextView tvName = listViewItem.findViewById(R.id.name);
        TextView tvTz = listViewItem.findViewById(R.id.tz);
        TextView tvOtchet = listViewItem.findViewById(R.id.otchet);

        try {
            JSONObject obj = usersList.get(position);

            // Дата
            String dateStr = obj.optString("date", "");
            if (dateStr != null && dateStr.contains("T")) {
                dateStr = dateStr.substring(0, dateStr.indexOf("T"));
            }
            tvDate.setText(dateStr);

            // Должность (specialty) - теперь в корне объекта
            String specialty = obj.optString("specialty", "");
            tvProf.setText(specialty);

            // ФИО (employee_name) - теперь в корне объекта
            String employeeName = obj.optString("employee_name", "");
            tvName.setText(employeeName);

            // Тех.задание (task)
            tvTz.setText(obj.optString("task", ""));

            // Отчёт (report)
            tvOtchet.setText(obj.optString("report", ""));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return listViewItem;
    }
}