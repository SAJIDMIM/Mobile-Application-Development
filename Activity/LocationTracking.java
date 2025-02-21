package com.example.flight_booking_app.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.flight_booking_app.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationTracking extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText txtAddress;
    private Button btnGetLocation, btnFetchLocation, btnCancel;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_track);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnFetchLocation = findViewById(R.id.btnGetLocation); // New button for address input
        btnCancel = findViewById(R.id.btnCancel);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapContainer);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Button to fetch and display current location
        btnGetLocation.setOnClickListener(v -> getCurrentLocation());

        // Button to fetch location from entered address
        btnFetchLocation.setOnClickListener(v -> fetchLocationFromAddress());

        // Cancel button functionality
        btnCancel.setOnClickListener(v -> finish());
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            // Request location permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Fetch the last known location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        updateMapWithLocation(location);
                    } else {
                        Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchLocationFromAddress() {
        String addressInput = txtAddress.getText().toString().trim();

        if (addressInput.isEmpty()) {
            Toast.makeText(this, "Please enter a valid address", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(addressInput, 1);
            if (addresses == null || addresses.isEmpty()) {
                Toast.makeText(this, "Unable to find location", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the first address result
            Address address = addresses.get(0);
            LatLng locationLatLng = new LatLng(address.getLatitude(), address.getLongitude());

            // Update the map with the new location
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(locationLatLng).title("Location: " + addressInput));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 15));

        } catch (IOException e) {
            Toast.makeText(this, "Error fetching location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMapWithLocation(Location location) {
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        // Add a marker and move the camera
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(currentLatLng).title("You are here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

        // Update the EditText with the location details
        txtAddress.setText("Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Enable map gestures
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Check permissions for current location display
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
