package com.example.lostfoundapp.ui;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostfoundapp.R;
import com.example.lostfoundapp.data.LostFoundItem;
import com.example.lostfoundapp.databinding.ItemLostFoundBinding;

import java.util.ArrayList;
import java.util.List;

public class LostFoundAdapter extends RecyclerView.Adapter<LostFoundAdapter.ItemViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(LostFoundItem item);
    }

    private final OnItemClickListener listener;
    private final List<LostFoundItem> items = new ArrayList<>();

    public LostFoundAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<LostFoundItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLostFoundBinding binding = ItemLostFoundBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private final ItemLostFoundBinding binding;

        ItemViewHolder(ItemLostFoundBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LostFoundItem item) {
            binding.textType.setText(item.getPostType());
            binding.textName.setText(item.getName());
            binding.textCategory.setText(item.getCategory());
            binding.textLocation.setText(item.getLocation());
            binding.textTimestamp.setText(item.getTimestamp());
            if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
                binding.imageThumb.setImageURI(Uri.parse(item.getImageUri()));
            } else {
                binding.imageThumb.setImageResource(R.drawable.ic_image_placeholder);
            }
            binding.getRoot().setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
