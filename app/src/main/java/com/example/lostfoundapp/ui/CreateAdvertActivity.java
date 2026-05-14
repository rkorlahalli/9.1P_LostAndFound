package com.example.lostfoundapp.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lostfoundapp.data.DatabaseHelper;
import com.example.lostfoundapp.data.LostFoundItem;
import com.example.lostfoundapp.databinding.ActivityCreateAdvertBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateAdvertActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 101;

    private ActivityCreateAdvertBinding binding;
    private DatabaseHelper databaseHelper;
    private Uri selectedImageUri;
    private final Calendar selectedDate = Calendar.getInstance();
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;
    private ArrayAdapter<String> locationSuggestionsAdapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Runnable pendingSearchRunnable;
    private boolean suppressLocationTextChanges = false;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    selectedImageUri = result.getData().getData();
                    try {
                        getContentResolver().takePersistableUriPermission(
                                selectedImageUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (SecurityException ignored) {
                        // Some providers do not grant persistable permissions. The URI can still be used in this session.
                    }
                    binding.imagePreview.setImageURI(selectedImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateAdvertBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this,
                com.example.lostfoundapp.R.array.categories,
                android.R.layout.simple_spinner_item
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(categoryAdapter);

        setupLocationAutocomplete();

        binding.buttonSelectDate.setOnClickListener(v -> showDatePicker());
        binding.buttonUploadImage.setOnClickListener(v -> openImagePicker());
        binding.buttonGetCurrentLocation.setOnClickListener(v -> getCurrentLocation());
        binding.buttonSave.setOnClickListener(v -> saveAdvert());
        binding.buttonBackHome.setOnClickListener(v -> finish());
    }

    private void setupLocationAutocomplete() {
        locationSuggestionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        binding.editLocation.setAdapter(locationSuggestionsAdapter);
        binding.editLocation.setThreshold(3);

        binding.editLocation.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (suppressLocationTextChanges) {
                    return;
                }
                selectedLatitude = 0.0;
                selectedLongitude = 0.0;
                binding.textCoordinates.setText("Coordinates not selected yet");
                queueLocationSearch(s.toString());
            }

            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        binding.editLocation.setOnItemClickListener((parent, view, position, id) -> {
            String selectedAddress = parent.getItemAtPosition(position).toString();
            binding.editLocation.setText(selectedAddress);
            geocodeExactAddress(selectedAddress);
        });
    }

    private void queueLocationSearch(String query) {
        if (pendingSearchRunnable != null) {
            searchHandler.removeCallbacks(pendingSearchRunnable);
        }

        if (query.trim().length() < 3) {
            return;
        }

        pendingSearchRunnable = () -> autocompleteAddress(query.trim());
        searchHandler.postDelayed(pendingSearchRunnable, 600);
    }

    private void autocompleteAddress(String query) {
        executorService.execute(() -> {
            List<String> suggestions = new ArrayList<>();
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(query, 5);
                if (addresses != null) {
                    for (Address address : addresses) {
                        String display = address.getAddressLine(0);
                        if (display != null && !display.trim().isEmpty() && !suggestions.contains(display)) {
                            suggestions.add(display);
                        }
                    }
                }
            } catch (IOException ignored) {
                // Network/geocoder service may be unavailable. The user can still type a location manually.
            }

            runOnUiThread(() -> {
                locationSuggestionsAdapter.clear();
                locationSuggestionsAdapter.addAll(suggestions);
                locationSuggestionsAdapter.notifyDataSetChanged();
                if (!suggestions.isEmpty()) {
                    binding.editLocation.showDropDown();
                }
            });
        });
    }

    private void geocodeExactAddress(String addressText) {
        executorService.execute(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(addressText, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    selectedLatitude = address.getLatitude();
                    selectedLongitude = address.getLongitude();
                    runOnUiThread(() -> updateCoordinateLabel(selectedLatitude, selectedLongitude));
                }
            } catch (IOException ignored) {
                runOnUiThread(() -> Snackbar.make(binding.getRoot(), "Could not find coordinates for this location", Snackbar.LENGTH_SHORT).show());
            }
        });
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
                applyCurrentLocation(bestLocation);
                return;
            }

            String provider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    ? LocationManager.GPS_PROVIDER
                    : LocationManager.NETWORK_PROVIDER;

            Snackbar.make(binding.getRoot(), "Getting current location...", Snackbar.LENGTH_SHORT).show();
            locationManager.requestSingleUpdate(provider, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    applyCurrentLocation(location);
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

    private void applyCurrentLocation(Location location) {
        selectedLatitude = location.getLatitude();
        selectedLongitude = location.getLongitude();
        updateCoordinateLabel(selectedLatitude, selectedLongitude);
        reverseGeocodeCurrentLocation(selectedLatitude, selectedLongitude);
    }

    private void reverseGeocodeCurrentLocation(double latitude, double longitude) {
        executorService.execute(() -> {
            String locationText = "Current location";
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty() && addresses.get(0).getAddressLine(0) != null) {
                    locationText = addresses.get(0).getAddressLine(0);
                }
            } catch (IOException ignored) {}

            String finalLocationText = locationText;
            runOnUiThread(() -> {
                suppressLocationTextChanges = true;
                binding.editLocation.setText(finalLocationText);
                binding.editLocation.dismissDropDown();
                suppressLocationTextChanges = false;
                updateCoordinateLabel(selectedLatitude, selectedLongitude);
                Snackbar.make(binding.getRoot(), "Current location selected", Snackbar.LENGTH_SHORT).show();
            });
        });
    }

    private void updateCoordinateLabel(double latitude, double longitude) {
        binding.textCoordinates.setText(String.format(Locale.getDefault(), "Coordinates: %.5f, %.5f", latitude, longitude));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (hasLocationPermission()) {
                loadCurrentLocation();
            } else {
                Snackbar.make(binding.getRoot(), "Location permission is required for current location", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            binding.editDate.setText(formatter.format(selectedDate.getTime()));
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        imagePickerLauncher.launch(intent);
    }

    private void saveAdvert() {
        String postType = binding.radioLost.isChecked() ? "Lost" : "Found";
        String name = binding.editName.getText().toString().trim();
        String phone = binding.editPhone.getText().toString().trim();
        String description = binding.editDescription.getText().toString().trim();
        String dateText = binding.editDate.getText().toString().trim();
        String location = binding.editLocation.getText().toString().trim();
        String category = binding.spinnerCategory.getSelectedItem().toString();

        if (name.isEmpty() || phone.isEmpty() || description.isEmpty() || dateText.isEmpty() || location.isEmpty()) {
            Snackbar.make(binding.getRoot(), "Please fill in all fields", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (selectedLatitude == 0.0 && selectedLongitude == 0.0) {
            Snackbar.make(binding.getRoot(), "Please select a location from autocomplete or use current location", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Snackbar.make(binding.getRoot(), "Please upload an image", Snackbar.LENGTH_SHORT).show();
            return;
        }

        String timestamp = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        LostFoundItem item = new LostFoundItem(
                postType,
                name,
                phone,
                description,
                dateText,
                location,
                category,
                selectedImageUri.toString(),
                timestamp,
                selectedLatitude,
                selectedLongitude
        );

        long result = databaseHelper.insertItem(item);
        if (result != -1) {
            Snackbar.make(binding.getRoot(), "Advert saved", Snackbar.LENGTH_SHORT).show();
            clearForm();
        } else {
            Snackbar.make(binding.getRoot(), "Failed to save advert", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        binding.radioLost.setChecked(true);
        binding.editName.setText("");
        binding.editPhone.setText("");
        binding.editDescription.setText("");
        binding.editDate.setText("");
        suppressLocationTextChanges = true;
        binding.editLocation.setText("");
        suppressLocationTextChanges = false;
        binding.textCoordinates.setText("Coordinates not selected yet");
        binding.spinnerCategory.setSelection(0);
        binding.imagePreview.setImageResource(com.example.lostfoundapp.R.drawable.ic_image_placeholder);
        selectedImageUri = null;
        selectedLatitude = 0.0;
        selectedLongitude = 0.0;
    }
}
