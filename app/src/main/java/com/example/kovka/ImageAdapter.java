package com.example.kovka;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private List<ImageModel> images;
    private OnImageActionListener listener;
    private Context context;

    public interface OnImageActionListener {
        void onDelete(ImageModel image, int position);
    }

    public ImageAdapter(Context context, List<ImageModel> images, OnImageActionListener listener) {
        this.context = context;
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageModel image = images.get(position);
        holder.tvFileName.setText(image.getName());

        // Загрузка изображения через Glide
        String imageUrl = Config.STORAGE_BASE + "products/" + image.getName();
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.ivImage);

        // КЛИК ПО ИЗОБРАЖЕНИЮ - ОТКРЫТИЕ ПОЛНОЭКРАННОГО РЕЖИМА
        holder.ivImage.setOnClickListener(v -> {
            Intent intent = new Intent(context, ImageFullScreenActivity.class);
            intent.putExtra(ImageFullScreenActivity.EXTRA_IMAGE_URL, imageUrl);
            intent.putExtra(ImageFullScreenActivity.EXTRA_IMAGE_NAME, image.getName());
            context.startActivity(intent);
        });

        // Кнопка Удалить
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(image, position);
            }
        });

        // Кнопка Переименовать - СКРЫТА
        holder.btnRename.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void removeItem(int position) {
        images.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvFileName;
        Button btnDelete, btnRename;

        ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnRename = itemView.findViewById(R.id.btnRename);
        }
    }
}