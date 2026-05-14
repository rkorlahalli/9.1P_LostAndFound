package com.example.lostfoundapp.ui;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lostfoundapp.R;
import com.example.lostfoundapp.data.DatabaseHelper;
import com.example.lostfoundapp.data.LostFoundItem;
import com.example.lostfoundapp.databinding.ActivityItemDetailBinding;
import com.google.android.material.snackbar.Snackbar;

public class ItemDetailActivity extends AppCompatActivity {

    private ActivityItemDetailBinding binding;
    private DatabaseHelper databaseHelper;
    private int itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        itemId = getIntent().getIntExtra("itemId", -1);

        loadItem();

        binding.buttonRemove.setOnClickListener(v -> removeItem());
        binding.buttonBack.setOnClickListener(v -> finish());
    }

    private void loadItem() {
        LostFoundItem item = databaseHelper.getItemById(itemId);
        if (item == null) {
            finish();
            return;
        }

        binding.textType.setText(item.getPostType() + " Item");
        binding.textName.setText(item.getName());
        binding.textPhone.setText(item.getPhone());
        binding.textDescription.setText(item.getDescription());
        binding.textDate.setText(item.getDateText());
        binding.textLocation.setText(item.getLocation());
        binding.textCategory.setText(item.getCategory());
        binding.textCoordinates.setText(String.format(java.util.Locale.getDefault(), "%.5f, %.5f", item.getLatitude(), item.getLongitude()));
        binding.textTimestamp.setText(item.getTimestamp());

        if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
            binding.imageItem.setImageURI(Uri.parse(item.getImageUri()));
        } else {
            binding.imageItem.setImageResource(R.drawable.ic_image_placeholder);
        }
    }

    private void removeItem() {
        int deleted = databaseHelper.deleteItem(itemId);
        if (deleted > 0) {
            Snackbar.make(binding.getRoot(), "Item removed", Snackbar.LENGTH_SHORT).show();
            binding.getRoot().postDelayed(this::finish, 600);
        } else {
            Snackbar.make(binding.getRoot(), "Unable to remove item", Snackbar.LENGTH_SHORT).show();
        }
    }
}
