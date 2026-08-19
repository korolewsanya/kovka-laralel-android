package com.example.kovka;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class SalaryListAdapter extends ArrayAdapter<Salary> {
    private Context context;
    private List<Salary> salaryList;
    private int layoutResourceId;

    public SalaryListAdapter(Context context, int layoutResourceId, List<Salary> salaryList) {
        super(context, layoutResourceId, salaryList);
        this.context = context;
        this.salaryList = salaryList;
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
            holder.id = row.findViewById(R.id.id);
            holder.date = row.findViewById(R.id.date);
            holder.spec = row.findViewById(R.id.spec);
            holder.name = row.findViewById(R.id.name);
            holder.nachis = row.findViewById(R.id.nachis);
            holder.poluch = row.findViewById(R.id.poluch);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Salary salary = salaryList.get(position);

        holder.id.setText(String.valueOf(salary.getId()));
        holder.date.setText(salary.getDate() != null ? salary.getDate() : "");
        holder.spec.setText(salary.getSpecialty() != null ? salary.getSpecialty() : "");
        holder.name.setText(salary.getEmployee_name() != null ? salary.getEmployee_name() : "");
        holder.nachis.setText(String.valueOf(salary.getAccrued()));
        holder.poluch.setText(String.valueOf(salary.getReceived()));

        return row;
    }

    public void updateData(List<Salary> newSalaryList) {
        this.salaryList.clear();
        this.salaryList.addAll(newSalaryList);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView id;
        TextView date;
        TextView spec;
        TextView name;
        TextView nachis;
        TextView poluch;
    }
}