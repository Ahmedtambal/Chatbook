package com.example.chatbook.Ui;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chatbook.Data.AppUser;
import com.example.chatbook.R;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Register extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;


    private EditText nameEditText, musernameEditText, mpasswordEditText, mconfirmpasswordEditText;
    private Button mloginButton;
    private FirebaseFirestore db;

    private ImageView mProfileImageView;

    private TextView mregisterTextView;

    private Uri mImageUri;


    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        musernameEditText = findViewById(R.id.usernameEditText);
        mpasswordEditText = findViewById(R.id.passwordEditText);
        mconfirmpasswordEditText = findViewById(R.id.confirmPasswordEditText);
        mregisterTextView = findViewById(R.id.registerTextView);
        mloginButton = findViewById(R.id.loginButton);
        db = FirebaseFirestore.getInstance();
        nameEditText = findViewById(R.id.nameEditText);
        mProfileImageView = findViewById(R.id.profileImageView);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        mloginButton.setOnClickListener(view -> {
            String name = nameEditText.getText().toString().trim();
            String email = musernameEditText.getText().toString().trim();
            String password = mpasswordEditText.getText().toString().trim();
            String confirmPassword = mconfirmpasswordEditText.getText().toString().trim();

            if (password.equals(confirmPassword)) {
                registerWithEmailPassword(name,email, password);
            } else {
                Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            }
        });
        mProfileImageView.setOnClickListener(view -> openFileChooser());

    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            Glide.with(this).load(mImageUri).into(mProfileImageView);
        }
    }


    private void registerWithEmailPassword(String name,String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration success, update UI with the signed-up user's information
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String id = firebaseUser.getUid();
                            // Save user information to Firestore
                            uploadImageToFirebaseStorage(id, name, email, password);
                        }
                        Toast.makeText(Register.this, "Registration successful.",
                                Toast.LENGTH_SHORT).show();
                        // TODO: Start your logged-in activity or go back to the Login activity here
                        startActivity(new Intent(Register.this, chatActivity.class));
                        finish();
                    } else {
                        // If registration fails, display a message to the user.
                        Toast.makeText(Register.this, "Registration failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImageToFirebaseStorage(String id, String name, String email, String password) {

        if (mImageUri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("profile_images");
            StorageReference fileReference = storageReference.child(id + ".jpg");

            fileReference.putFile(mImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String urlPicture = downloadUri.toString();
                    saveUserToFirestore(id, name, email, password, urlPicture);
                } else {
                    Toast.makeText(Register.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            saveUserToFirestore(id, name, email, password, null);
        }
    }

    private void saveUserToFirestore(String id, String name, String email, String password, String urlPicture) {
        AppUser appUser = new AppUser(id, name, email,password,urlPicture);
        db.collection("users").document(id)
                .set(appUser)
                .addOnSuccessListener(aVoid -> {
                    // Log success if you want
                    // TODO: Start your logged-in activity or go back to the Login activity here
                    startActivity(new Intent(Register.this, chatActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Log failure if you want
                });
    }

}
