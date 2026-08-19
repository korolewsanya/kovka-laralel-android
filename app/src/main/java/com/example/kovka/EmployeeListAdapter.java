package com.example.kovka;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class EmployeeListAdapter extends ArrayAdapter<Employee> {
    private Context context;
    private List<Employee> employeeList;
    private int layoutResourceId;

    public EmployeeListAdapter(Context context, int layoutResourceId, List<Employee> employeeList) {
        super(context, layoutResourceId, employeeList);
        this.context = context;
        this.employeeList = employeeList;
        this.layoutResourceId = layoutResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ViewHolder();
            holder.id = row.findViewById(R.id.nomer);
            holder.position = row.findViewById(R.id.spec);
            holder.fullName = row.findViewById(R.id.name);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Employee employee = employeeList.get(position);

        holder.id.setText(String.valueOf(employee.getId()));
        holder.position.setText(employee.getPosition() != null ? employee.getPosition() : "");
        holder.fullName.setText(employee.getFull_name() != null ? employee.getFull_name() : "");

        return row;
    }

    public void updateData(List<Employee> newList) {
        this.employeeList.clear();
        this.employeeList.addAll(newList);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView id;
        TextView position;
        TextView fullName;
    }
}