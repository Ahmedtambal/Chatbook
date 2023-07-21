package com.example.chatbook.Ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chatbook.Data.AppUser;
import com.example.chatbook.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity  extends AppCompatActivity{

    private static final int PICK_IMAGE_REQUEST = 1;

    ImageView profileImageView;

    EditText usernameEditText, emailEditText;

    TextView emailTextView;

    Button updateButton, deleteButton;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore db;
    private StorageReference mStorageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        profileImageView = findViewById(R.id.profileImageView);
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        emailTextView = findViewById(R.id.emailTextView);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);
        
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference("profile_images");
        
        loadData();

        profileImageView.setOnClickListener(view -> chooseImage());
        updateButton.setOnClickListener(view -> updateProfile());


    }
    private void updateProfile() {
        if (mUser != null) {
            String userId = mUser.getUid();
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();

            Map<String, Object> updates = new HashMap<>();
            updates.put("username", username);
            updates.put("email", email);

            // Update Firestore
            db.collection("users").document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        // Update the email in Firebase Authentication
                        mUser.updateEmail(email).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Email update failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Profile update failed", Toast.LENGTH_SHORT).show());

            // Update the profile image
            profileImageView.setDrawingCacheEnabled(true);
            profileImageView.buildDrawingCache();
            Bitmap bitmap = profileImageView.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            StorageReference profileImageRef = mStorageRef.child(userId + ".jpg");

            UploadTask uploadTask = profileImageRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL for the updated image
                        taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(uriTask -> {
                            if (uriTask.isSuccessful()) {
                                // Update the profile image URL in Firestore
                                String imageURL = uriTask.getResult().toString();
                                db.collection("users").document(userId).update("profileImageUrl", imageURL);
                            } else {
                                Toast.makeText(ProfileActivity.this, "Profile image update failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Profile image update failed", Toast.LENGTH_SHORT).show());
        }
    }


    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadData() {
        if (mUser != null) {
            String userId = mUser.getUid();
            DocumentReference docRef = db.collection("users").document(userId);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    AppUser user = task.getResult().toObject(AppUser.class);
                    if (user != null) {
                        usernameEditText.setText(user.getUsername());
                        emailEditText.setText(user.getEmail());
                        emailTextView.setText(user.getEmail());
                        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                            // Check if the profile image is hosted on Firebase Storage or Google servers
                            if (user.getProfileImageUrl().startsWith("https://")) {
                                // Load the image from the URL (Google Sign-in user)
                                Glide.with(ProfileActivity.this)
                                        .load(user.getProfileImageUrl())
                                        .into(profileImageView);
                            } else {
                                // Load the image from Firebase Storage (Email/password user)
                                StorageReference profileImageRef = mStorageRef.child(user.getProfileImageUrl());
                                profileImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    profileImageView.setImageBitmap(bmp);
                                });
                            }
                        }
                    }
                }
            });
        }
    }

}


