package com.example.chatbook.Ui;

import static android.content.ContentValues.TAG;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatbook.Data.AppUser;
import com.example.chatbook.Data.ChatMessage;
import com.example.chatbook.Data.MessageAdapter;
import com.example.chatbook.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.ArrayList;
import java.util.List;


public class chatActivity  extends AppCompatActivity {


    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private ListenerRegistration chatMessageListener;


    RecyclerView recycler_open_channel_chat;

    EditText edittext_chat_message;

    private String imageURL;

    private ActionBarDrawerToggle actionBarDrawerToggle;


    Button button_open_channel_chat_send;
    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference mStorageRef;
    private CollectionReference messagesRef;

    String currentUsername = "myUsername";


    private ArrayList<ChatMessage> chatMessages;
    private MessageAdapter messageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        recycler_open_channel_chat = findViewById(R.id.recycler_open_channel_chat);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        edittext_chat_message = findViewById(R.id.edittext_chat_message);
        button_open_channel_chat_send = findViewById(R.id.button_open_channel_chat_send);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mStorageRef = storage.getReference("message_images");
        messagesRef = db.collection("chat");

        chatMessages = new ArrayList<>();
        messageAdapter = new MessageAdapter(chatMessages, this,currentUsername);

        recycler_open_channel_chat.setLayoutManager(new LinearLayoutManager(this));
        recycler_open_channel_chat.setAdapter(messageAdapter);

        // Set up the ActionBarDrawerToggle
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        // Set the ActionBarDrawerToggle as the DrawerListener
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // Enable the home button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Set up the NavigationView item click listener
        mNavigationView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.nav_item_profile:
                    // Navigate to the profile activity
                    startActivity(new Intent(chatActivity.this, ProfileActivity.class));
                    break;

                case R.id.nav_item_logout:
                    // Handle the logout process
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(chatActivity.this, Login.class));
                    finish();
                    break;
            }

            // Close the navigation drawer
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        );

        button_open_channel_chat_send.setOnClickListener(v -> getUserDetailsAndSendMessage());
        setUpChatMessageListener();

    }

    private void sendMessage(AppUser sender, String messageText, String imageUrl) {
        ChatMessage chatMessage = new ChatMessage(
                mUser.getUid(),
                sender,
                messageText,
                Timestamp.now(),
                imageUrl

        );

        messagesRef.add(chatMessage).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                edittext_chat_message.setText("");
                messageAdapter.addMessage(chatMessage);
            } else {
                Toast.makeText(chatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void getUserDetailsAndSendMessage() {
        String messageText = edittext_chat_message.getText().toString().trim();

        if (messageText.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userRef = db.collection("users").document(mUser.getUid());
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            String username = documentSnapshot.getString("username");
            String profileImageUrl = documentSnapshot.getString("profileImageUrl");
            String userId = mUser.getUid();
            String email = mUser.getEmail();
            String password = ""; // You should not store passwords in Firebase Firestore

            AppUser sender = new AppUser(userId, username, email, password, profileImageUrl);

            // Set imageURL to null if there is no image
            imageURL = imageURL != null && !imageURL.isEmpty() ? imageURL : null;
            sendMessage(sender, messageText, imageURL);

        }).addOnFailureListener(e -> {
            Toast.makeText(chatActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
        });
    }

    private void setUpChatMessageListener() {
        CollectionReference chatRef = db.collection("chat");

        chatMessageListener = chatRef
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "listen:error", e);
                        return;
                    }

                    for (DocumentChange change : snapshots.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            ChatMessage message = change.getDocument().toObject(ChatMessage.class);
                            messageAdapter.addMessage(message);
                            recycler_open_channel_chat.scrollToPosition(messageAdapter.getItemCount() - 1);
                        }
                    }
                });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatMessageListener != null) {
            chatMessageListener.remove();
        }
    }






}