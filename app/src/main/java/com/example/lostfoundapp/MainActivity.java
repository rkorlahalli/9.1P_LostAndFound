package com.example.lostfoundapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lostfoundapp.databinding.ActivityMainBinding;
import com.example.lostfoundapp.ui.CreateAdvertActivity;
import com.example.lostfoundapp.ui.ItemListActivity;
import com.example.lostfoundapp.ui.MapActivity;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonCreateAdvert.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CreateAdvertActivity.class)));

        binding.buttonShowItems.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ItemListActivity.class)));

        binding.buttonShowOnMap.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MapActivity.class)));
    }
}
