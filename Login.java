package com.example.flight_booking_app;

import static com.example.flight_booking_app.R.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private TextView signupRedirectText;
    private Button loginButton;
    private ImageView passViewIcon;
    private FirebaseAuth auth;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_login_page);

        // Initialize views
        loginEmail = findViewById(id.inputEmail);
        loginPassword = findViewById(id.inputPassword);
        loginButton = findViewById(id.loginButton);
        signupRedirectText = findViewById(id.registerTxt);
        passViewIcon = findViewById(id.passViewIcon);

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Toggle password visibility
        passViewIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    loginPassword.setInputType(129); // 129 = TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD
                    passViewIcon.setImageResource(drawable.passview);
                } else {
                    loginPassword.setInputType(1); // 1 = TYPE_CLASS_TEXT
                    passViewIcon.setImageResource(drawable.passview);
                }
                isPasswordVisible = !isPasswordVisible;
                loginPassword.setSelection(loginPassword.getText().length()); // Move cursor to end
            }
        });

        // Set click listener for login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmail.getText().toString().trim();
                String pass = loginPassword.getText().toString().trim();

                if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (!pass.isEmpty()) {
                        auth.signInWithEmailAndPassword(email, pass)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Login.this, MainActivity.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Login.this, "Login Failed. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        loginPassword.setError("Password is required");
                    }
                } else if (email.isEmpty()) {
                    loginEmail.setError("Please enter your email");
                } else {
                    loginEmail.setError("Please enter a valid email address");
                }
            }
        });

        // Redirect to signup page
        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, SignUp.class));
            }
        });
    }
}
