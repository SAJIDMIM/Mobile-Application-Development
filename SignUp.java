package com.example.flight_booking_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUp extends AppCompatActivity {

    private EditText usernameOrEmailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView loginRedirectText;
    private DatabaseReference databaseReference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Bind UI components
        usernameOrEmailEditText = findViewById(R.id.inputEmail);
        passwordEditText = findViewById(R.id.inputPassword);
        confirmPasswordEditText = findViewById(R.id.inputConfirmPassword);
        registerButton = findViewById(R.id.signupButton);
        loginRedirectText = findViewById(R.id.loginTxt);

        // Set click listeners
        registerButton.setOnClickListener(v -> registerUser());
        loginRedirectText.setOnClickListener(v -> {
            startActivity(new Intent(SignUp.this, Login.class));
            finish();
        });
    }

    // Main registration logic
    private void registerUser() {
        String usernameOrEmail = usernameOrEmailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validate inputs
        if (!isValidInput(usernameOrEmail, password, confirmPassword)) return;

        // Check if user already exists
        databaseReference.orderByChild("usernameOrEmail").equalTo(usernameOrEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            showToast("User already exists! Try logging in.");
                            usernameOrEmailEditText.setError("Username or Email already in use!");
                        } else {
                            // Proceed with registration if user doesn't exist
                            registerNewUser(usernameOrEmail, password);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        showToast("Database error: " + databaseError.getMessage());
                    }
                });
    }

    // Register a new user and save to Firebase
    private void registerNewUser(String usernameOrEmail, String password) {
        String userId = databaseReference.push().getKey(); // Generate unique ID for the user
        User user = new User(usernameOrEmail, password);   // Create user object

        databaseReference.child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("User registered successfully!");
                        clearInputFields();
                    } else {
                        showToast("Registration failed. Please try again.");
                    }
                });
    }

    // Validate user input
    private boolean isValidInput(String usernameOrEmail, String password, String confirmPassword) {
        if (TextUtils.isEmpty(usernameOrEmail)) {
            usernameOrEmailEditText.setError("Username or Email is required!");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required!");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match!");
            return false;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters long!");
            return false;
        }

        // Advanced validation for email format using regular expression
        if (!Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches()) {
            usernameOrEmailEditText.setError("Invalid email format!");
            return false;
        }

        return true;
    }

    // Clear input fields after successful registration
    private void clearInputFields() {
        usernameOrEmailEditText.setText("");
        passwordEditText.setText("");
        confirmPasswordEditText.setText("");
    }

    // Utility function to show Toast message
    private void showToast(String message) {
        Toast.makeText(SignUp.this, message, Toast.LENGTH_SHORT).show();
    }
}