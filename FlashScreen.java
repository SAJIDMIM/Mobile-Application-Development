package com.example.flight_booking_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class FlashScreen extends AppCompatActivity {

    private Button getStartedButton;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_screen);

        // Initialize buttons
        getStartedButton = findViewById(R.id.getStartedButton);
        loginButton = findViewById(R.id.loginButton);

        // Get Started button click listener
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the LoginActivity when the button is clicked
                Intent intent = new Intent(FlashScreen.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Login button click listener
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch the login activity
                Intent loginIntent = new Intent(FlashScreen.this, Login.class); // Replace with your login activity class
                startActivity(loginIntent);
            }
        });
    }
}
