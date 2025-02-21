package com.example.flight_booking_app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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

public class ActivityLocationTracking extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private Button btnGetLocation, btnCancel;
    private ImageView backBtn; // Back button

    private ImageView homeView,chatView,locationView,profileView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_track);

        btnCancel = findViewById(R.id.btnCancel);
        backBtn = findViewById(R.id.backbtn); // Initialize back button

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapContainer);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);  // 10-second updates
        locationRequest.setFastestInterval(5000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    updateMapLocation(location);  // Update map with the new location
                }
            }
        };

        btnCancel.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.clear();
            }
        });

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityLocationTracking.this, MainActivity.class);
            startActivity(intent);
            finish();
        });



        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnGetLocation.setOnClickListener(v -> getCurrentLocation());


        homeView = findViewById(R.id.homeBtn);
        homeView.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityLocationTracking.this, MainActivity.class);
            startActivity(intent);
        });

        chatView = findViewById(R.id.chatBtn);
        chatView.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityLocationTracking.this, ChatActivity.class);
            startActivity(intent);
        });

        locationView = findViewById(R.id.locationBtn);
        locationView.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityLocationTracking.this, ActivityLocationTracking.class);
            startActivity(intent);
        });

        profileView = findViewById(R.id.editProfileBtn);
        profileView.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityLocationTracking.this, EditAccount.class);
            startActivity(intent);
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        requestLocationPermission();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableUserLocation();
        }
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, request permission
            requestLocationPermission();
            return;
        }

        // Get the last known location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        updateMapLocation(location);  // Update map with the last known location
                    } else {
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error fetching location: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void updateMapLocation(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        Log.d("LocationDebug", "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(userLocation).title(getPlaceName(location)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
    }

    private String getPlaceName(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 5);
            if (!addresses.isEmpty()) {
                Address bestMatch = addresses.get(0);
                return bestMatch.getFeatureName() + ", " + bestMatch.getSubLocality() + ", " + bestMatch.getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown Location";
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);  // Stop receiving location updates when the activity is paused
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
            } else {
                Toast.makeText(this, "Location permission is required to use this feature", Toast.LENGTH_LONG).show();
            }
        }
    }
}
