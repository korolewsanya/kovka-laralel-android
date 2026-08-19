package com.example.kovka;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MaterialListAdapter extends ArrayAdapter<Material> {
    private Context context;
    private List<Material> materials;
    private int layoutResourceId;

    public MaterialListAdapter(Context context, int layoutResourceId, List<Material> materials) {
        super(context, layoutResourceId, materials);
        this.context = context;
        this.materials = materials;
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
            holder.name = row.findViewById(R.id.name);
            holder.kup = row.findViewById(R.id.kup);
            holder.izras = row.findViewById(R.id.izras);
            holder.ost = row.findViewById(R.id.ost);
            holder.prise = row.findViewById(R.id.prise);
            holder.itogo = row.findViewById(R.id.itogo);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Material material = materials.get(position);

        // Заполняем данные
        holder.id.setText(String.valueOf(material.getId()));
        holder.date.setText(material.getDate() != null ? material.getDate() : "");
        holder.name.setText(material.getName() != null ? material.getName() : "");
        holder.kup.setText(String.valueOf(material.getPurchased()));
        holder.izras.setText(String.valueOf(material.getUsed()));
        holder.ost.setText(String.valueOf(material.getBalance()));
        holder.prise.setText(String.valueOf(material.getPrice_per_unit()));
        holder.itogo.setText(String.valueOf(material.getTotal_price()));

        return row;
    }

    // Обновление данных
    public void updateData(List<Material> newMaterials) {
        this.materials.clear();
        this.materials.addAll(newMaterials);
        notifyDataSetChanged();
    }

    // ViewHolder для оптимизации производительности
    static class ViewHolder {
        TextView id;
        TextView date;
        TextView name;
        TextView kup;
        TextView izras;
        TextView ost;
        TextView prise;
        TextView itogo;
    }
}