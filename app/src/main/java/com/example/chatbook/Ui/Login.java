package com.example.chatbook.Ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatbook.Data.AppUser;
import com.example.chatbook.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;

    private EditText musernameEditText, mpasswordEditText;
    private Button mloginButton;
    private SignInButton mgoogleSignInButton;
    private TextView mregisterTextView;

    private GoogleSignInClient mGoogleSignInClient;

    private FirebaseFirestore db;



    // Add FirebaseAuth instance
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        musernameEditText = findViewById(R.id.usernameEditText);
        mpasswordEditText = findViewById(R.id.passwordEditText);
        mregisterTextView = findViewById(R.id.registerTextView);
        mgoogleSignInButton = findViewById(R.id.googleSignInButton);
        mloginButton = findViewById(R.id.loginButton);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mloginButton.setOnClickListener(view -> {
            String email = musernameEditText.getText().toString().trim();
            String password = mpasswordEditText.getText().toString().trim();
            signInWithEmailPassword(email, password);
        });

        mgoogleSignInButton.setOnClickListener(view -> signInWithGoogle());
    }

    private void signInWithEmailPassword(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            Toast.makeText(this, "Email and password should not be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        // TODO: Start your logged-in activity here
                        // Navigate to the main activity or other desired activity
                        startActivity(new Intent(Login.this, chatActivity.class));
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(Login.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(Login.this, "Google sign in failed", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveGoogleUserToFirestore(user);
                            // TODO: Start your logged-in activity here
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveGoogleUserToFirestore(FirebaseUser user) {
        String id = user.getUid();
        String email = user.getEmail();
        String name = user.getDisplayName();
        String urlPicture = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";

        AppUser appUser = new AppUser(id, name, email, null ,urlPicture);
        db.collection("users").document(id)
                .set(appUser)
                .addOnSuccessListener(aVoid -> {
                    // Log success if you want
                    Toast.makeText(Login.this, "Google sign in success", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Login.this, chatActivity.class));
                    finish();

                })
                .addOnFailureListener(e -> {
                    // Log failure if you want
                });
    }


    public void openLactivity(View view) {
        // create an intent to start the Numbers activity
        Intent Lintent = new Intent(this, Register.class);

        // start the Numbers activity
        startActivity(Lintent);
    }
}