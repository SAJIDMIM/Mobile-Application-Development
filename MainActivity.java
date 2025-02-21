package com.example.flight_booking_app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flight_booking_app.Model.Location;
import com.example.flight_booking_app.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private int adultPassenger = 1, childPassenger = 1;
    private ImageView locationBtn, sendButton, editProfile;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("d MM, yyyy", Locale.ENGLISH);
    private Calendar calendar = Calendar.getInstance();
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView welcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize UI elements
        welcomeTextView = findViewById(R.id.textViewName);
        locationBtn = findViewById(R.id.locationBtn);
        sendButton = findViewById(R.id.chatBtn);
        editProfile = findViewById(R.id.editProfileBtn);

        // Retrieve and display the user's name
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            retrieveUserName(user.getUid());
        }

        // Location button click - Open Location Tracking Activity
        locationBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ActivityLocationTracking.class);
            startActivity(intent);
        });

        // Chat button click - Open Chat Activity
        sendButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        // Edit Profile button click - Open Edit Profile Activity
        editProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditAccount.class);
            startActivity(intent);
        });

        iniLocation();
        initPassengers();
        initClassSeat();
        initDatePickup();
        setVariable();
    }

    // Fetch and display the user's name
    private void retrieveUserName(String userId) {
        DatabaseReference userRef = mDatabase.child(userId).child("name");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.getValue(String.class);
                    welcomeTextView.setText("Welcome, " + userName + "!");
                } else {
                    welcomeTextView.setText("Welcome, User!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error retrieving name: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setVariable() {
        binding.searchBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            intent.putExtra("from", ((Location) binding.spinnerFrom.getSelectedItem()).getName());
            intent.putExtra("to", ((Location) binding.spinnerTo.getSelectedItem()).getName());
            intent.putExtra("date", binding.departureDateTxt.getText().toString());
            intent.putExtra("numPassenger", adultPassenger + childPassenger);
            startActivity(intent);
        });
    }

    private void initDatePickup() {
        Calendar calendarToday = Calendar.getInstance();
        String currentDate = dateFormat.format(calendarToday.getTime());
        binding.departureDateTxt.setText(currentDate);
        Calendar calendarTomorrow = Calendar.getInstance();
        calendarTomorrow.add(Calendar.DAY_OF_YEAR, 1);
        String tomorrowDate = dateFormat.format(calendarTomorrow.getTime());
        binding.returnDateTxt.setText(tomorrowDate);
        binding.departureDateTxt.setOnClickListener(v -> showDatePickerDialog(binding.departureDateTxt));
        binding.returnDateTxt.setOnClickListener(v -> showDatePickerDialog(binding.returnDateTxt));
    }

    private void initClassSeat() {
        binding.progressBarClass.setVisibility(View.VISIBLE);
        ArrayList<String> list = new ArrayList<>();
        list.add("Business Class");
        list.add("First Class");
        list.add("Economy Class");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.sp_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.classSp.setAdapter(adapter);
        binding.progressBarClass.setVisibility(View.GONE);
    }

    private void initPassengers() {
        binding.plusAdultBtn.setOnClickListener(v -> {
            adultPassenger++;
            binding.AdultTxt.setText(adultPassenger + " Adult");
        });

        binding.minusAdultBtn.setOnClickListener(v -> {
            if (adultPassenger > 1) {
                adultPassenger--;
                binding.AdultTxt.setText(adultPassenger + " Adult");
            }
        });

        binding.plusChildBtn.setOnClickListener(v -> {
            childPassenger++;
            binding.childTxt.setText(childPassenger + " Child");
        });

        binding.minusChildBtn.setOnClickListener(v -> {
            if (childPassenger > 0) {
                childPassenger--;
                binding.childTxt.setText(childPassenger + " Child");
            }
        });
    }

    private void iniLocation() {
        binding.progressBarFrom.setVisibility(View.VISIBLE);
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Locations");
        ArrayList<Location> list = new ArrayList<>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(Location.class));
                    }
                    ArrayAdapter<Location> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.sp_item, list);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.spinnerFrom.setAdapter(adapter);
                    binding.spinnerTo.setAdapter(adapter);
                    binding.spinnerFrom.setSelection(1);
                    binding.progressBarFrom.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error loading locations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePickerDialog(TextView textView) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            calendar.set(selectedYear, selectedMonth, selectedDay);
            String formattedDate = dateFormat.format(calendar.getTime());
            textView.setText(formattedDate);
        }, year, month, day);

        datePickerDialog.show();
    }
}
