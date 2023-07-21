package com.example.chatbook.Data;

import com.google.firebase.Timestamp;

import java.util.Date;


public class ChatMessage {
    private String id;
    private String message;
    private Timestamp timestamp;
    private String imageUrl;



    // Add this no-argument constructor
    public ChatMessage() {
    }

    public ChatMessage(String id, AppUser sender, String message, Timestamp timestamp, String imageUrl) {
        this.id = id;
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl != null ? imageUrl : "";
    }


    public AppUser getSender() {
        return sender;
    }

    public void setSender(AppUser sender) {
        this.sender = sender;
    }

    private AppUser sender;



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {if (timestamp != null) {
        return timestamp.toDate();
    }
        return null;}

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
