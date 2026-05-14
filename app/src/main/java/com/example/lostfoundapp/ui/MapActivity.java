package com.example.lostfoundapp.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lostfoundapp.data.DatabaseHelper;
import com.example.lostfoundapp.data.LostFoundItem;
import com.example.lostfoundapp.databinding.ActivityMapBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 201;

    private ActivityMapBinding binding;
    private DatabaseHelper databaseHelper;
    private double userLatitude = 0.0;
    private double userLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        setupWebView();

        binding.buttonUseCurrentLocation.setOnClickListener(v -> getCurrentLocation());
        binding.buttonApplyRadius.setOnClickListener(v -> loadMapWithRadius());
        binding.buttonBackHome.setOnClickListener(v -> finish());

        loadEmptyMap("Tap GET CURRENT LOCATION to show nearby lost and found items.");
        getCurrentLocation();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = binding.webMap.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
    }

    private void getCurrentLocation() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST
            );
            return;
        }
        loadCurrentLocation();
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void loadCurrentLocation() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location bestLocation = getBestLastKnownLocation(locationManager);
            if (bestLocation != null) {
                applyUserLocation(bestLocation);
                return;
            }

            String provider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    ? LocationManager.GPS_PROVIDER
                    : LocationManager.NETWORK_PROVIDER;

            binding.textMapStatus.setText("Getting current location...");
            locationManager.requestSingleUpdate(provider, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    applyUserLocation(location);
                }

                @Override public void onProviderEnabled(@NonNull String provider) {}
                @Override public void onProviderDisabled(@NonNull String provider) {}
            }, Looper.getMainLooper());
        } catch (SecurityException | IllegalArgumentException ex) {
            Snackbar.make(binding.getRoot(), "Unable to access current location", Snackbar.LENGTH_SHORT).show();
        }
    }

    private Location getBestLastKnownLocation(LocationManager locationManager) {
        Location gps = null;
        Location network = null;
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                gps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                network = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        } catch (SecurityException ignored) {}

        if (gps == null) return network;
        if (network == null) return gps;
        return gps.getAccuracy() <= network.getAccuracy() ? gps : network;
    }

    private void applyUserLocation(Location location) {
        userLatitude = location.getLatitude();
        userLongitude = location.getLongitude();
        binding.textMapStatus.setText(String.format(Locale.getDefault(), "Current location: %.5f, %.5f", userLatitude, userLongitude));
        loadMapWithRadius();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (hasLocationPermission()) {
                loadCurrentLocation();
            } else {
                Snackbar.make(binding.getRoot(), "Location permission is required for the map radius search", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private double getRadiusKm() {
        String radiusText = binding.editRadiusKm.getText().toString().trim();
        if (radiusText.isEmpty()) {
            return 10.0;
        }
        try {
            return Math.max(0.1, Double.parseDouble(radiusText));
        } catch (NumberFormatException ex) {
            Snackbar.make(binding.getRoot(), "Invalid radius, using 10 km", Snackbar.LENGTH_SHORT).show();
            return 10.0;
        }
    }

    private void loadMapWithRadius() {
        if (userLatitude == 0.0 && userLongitude == 0.0) {
            Snackbar.make(binding.getRoot(), "Please get your current location first", Snackbar.LENGTH_SHORT).show();
            return;
        }

        double radiusKm = getRadiusKm();
        List<LostFoundItem> nearbyItems = databaseHelper.getItemsWithinRadius(userLatitude, userLongitude, radiusKm);
        binding.textMapStatus.setText(String.format(Locale.getDefault(), "Showing %d item(s) within %.1f km", nearbyItems.size(), radiusKm));
        binding.webMap.loadDataWithBaseURL("https://leafletjs.com/", buildMapHtml(nearbyItems, radiusKm), "text/html", "UTF-8", null);
    }

    private void loadEmptyMap(String message) {
        binding.textMapStatus.setText(message);
        binding.webMap.loadDataWithBaseURL("https://leafletjs.com/", buildMapHtml(new java.util.ArrayList<>(), 10.0), "text/html", "UTF-8", null);
    }

    private String buildMapHtml(List<LostFoundItem> items, double radiusKm) {
        double centerLat = userLatitude == 0.0 ? -37.8136 : userLatitude;
        double centerLng = userLongitude == 0.0 ? 144.9631 : userLongitude;

        StringBuilder markers = new StringBuilder();
        for (LostFoundItem item : items) {
            if (!item.hasValidCoordinates()) continue;
            String title = escapeJs(item.getPostType() + ": " + item.getName());
            String details = escapeJs(item.getCategory() + "<br>" + item.getLocation() + "<br>Posted: " + item.getTimestamp());
            markers.append("L.marker([")
                    .append(item.getLatitude()).append(",")
                    .append(item.getLongitude()).append("])")
                    .append(".addTo(map).bindPopup('")
                    .append("<b>").append(title).append("</b><br>").append(details)
                    .append("');\n");
        }

        return "<!DOCTYPE html><html><head>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>"
                + "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>"
                + "<style>html,body,#map{height:100%;margin:0;padding:0;} .leaflet-popup-content{font-size:14px;}</style>"
                + "</head><body><div id='map'></div><script>"
                + "var map = L.map('map').setView([" + centerLat + "," + centerLng + "], 13);"
                + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {maxZoom: 19, attribution: '© OpenStreetMap'}).addTo(map);"
                + "L.circle([" + centerLat + "," + centerLng + "], {radius:" + (radiusKm * 1000.0) + ", color:'#6A4FB3', fillOpacity:0.08}).addTo(map);"
                + "L.marker([" + centerLat + "," + centerLng + "], {title:'Your current location'}).addTo(map).bindPopup('<b>Your current location</b>');"
                + markers
                + "</script></body></html>";
    }

    private String escapeJs(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}
