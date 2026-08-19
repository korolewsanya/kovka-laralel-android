package com.example.kovka;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class ProductAdapter extends ArrayAdapter<Product> {
    private Context context;
    private List<Product> productList;
    private int layoutResourceId;

    public ProductAdapter(Context context, int layoutResourceId, List<Product> productList) {
        super(context, layoutResourceId, productList);
        this.context = context;
        this.productList = productList;
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
            holder.image = row.findViewById(R.id.image);
            holder.name = row.findViewById(R.id.name);
            holder.category = row.findViewById(R.id.category);
            holder.price = row.findViewById(R.id.price);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Product product = productList.get(position);

        holder.id.setText(String.valueOf(product.getId()));
        holder.name.setText(product.getName() != null ? product.getName() : "");
        holder.category.setText(formatCategory(product.getCategory()));
        holder.price.setText(product.getPrice() > 0 ? product.getPrice() + " ₽" : "");

        // Загрузка изображения со стандартными placeholder
        String imageUrl = product.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(holder.image);
        } else {
            holder.image.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        return row;
    }

    private String formatCategory(String category) {
        if (category == null) return "";
        switch (category) {
            case "vorota": return "Ворота";
            case "zabor": return "Заборы";
            case "mangal": return "Мангалы";
            case "kozirek": return "Козырьки";
            case "lavo4ki": return "Лавочки";
            case "ogradki": return "Оградки";
            case "reshetki": return "Решетки";
            case "mebel": return "Мебель";
            case "melo4i": return "Полезные мелочи";
            default: return category;
        }
    }

    public void updateData(List<Product> newProductList) {
        this.productList.clear();
        this.productList.addAll(newProductList);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView id;
        ImageView image;
        TextView name;
        TextView category;
        TextView price;
    }
}