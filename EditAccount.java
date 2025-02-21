package com.example.flight_booking_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditAccount extends AppCompatActivity {

    private EditText emailEditText, newPasswordEditText, confirmPasswordEditText, nameEditText;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private ImageView profileImageView;
    private Uri imageUri;
    private static final int IMAGE_PICK_REQUEST = 1;

    private ImageView homeView,chatView,locationView,profileView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        emailEditText = findViewById(R.id.editEmail);
        newPasswordEditText = findViewById(R.id.newPassword2);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        nameEditText = findViewById(R.id.editTextName);
        profileImageView = findViewById(R.id.imageViewProfile2);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");
        currentUser = mAuth.getCurrentUser();

        profileImageView.setOnClickListener(v -> openGalleryForImage());

        if (currentUser != null) {
            emailEditText.setText(currentUser.getEmail());
            emailEditText.setEnabled(false);
        }

        Button saveButton = findViewById(R.id.updatePasswordButton);
        saveButton.setOnClickListener(v -> updateAccountDetails());

        Button deleteButton = findViewById(R.id.deleteAccountButton);
        deleteButton.setOnClickListener(v -> deleteAccount());

        homeView = findViewById(R.id.homeBtn);
        homeView.setOnClickListener(v -> {
            Intent intent = new Intent(EditAccount.this, MainActivity.class);
            startActivity(intent);
        });

        chatView = findViewById(R.id.chatBtn);
        chatView.setOnClickListener(v -> {
            Intent intent = new Intent(EditAccount.this, ChatActivity.class);
            startActivity(intent);
        });

        locationView = findViewById(R.id.locationBtn);
        locationView.setOnClickListener(v -> {
            Intent intent = new Intent(EditAccount.this, ActivityLocationTracking.class);
            startActivity(intent);
        });

        profileView = findViewById(R.id.editProfileBtn);
        profileView.setOnClickListener(v -> {
            Intent intent = new Intent(EditAccount.this, EditAccount.class);
            startActivity(intent);
        });
    }


    private void openGalleryForImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    private void updateAccountDetails() {
        String newName = nameEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (newName.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields must be filled!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        mDatabase.child(currentUser.getUid()).child("name").setValue(newName);
        currentUser.updatePassword(newPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mDatabase.child(currentUser.getUid()).child("password").setValue(newPassword);
                Toast.makeText(this, "Account updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Password update failed", Toast.LENGTH_SHORT).show();
            }
        });

        if (imageUri != null) {
            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            StorageReference fileRef = FirebaseStorage.getInstance().getReference()
                    .child("profileImages/" + currentUser.getUid() + ".jpg");

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        mDatabase.child(currentUser.getUid()).child("profileImage").setValue(uri.toString());
                        Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    }))
                    .addOnFailureListener(e -> Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void deleteAccount() {
        currentUser.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mDatabase.child(currentUser.getUid()).removeValue();
                Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, Login.class));
                finish();
            } else {
                Toast.makeText(this, "Account deletion failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
