package com.example.chatbook.Data;

import androidx.annotation.Nullable;

public class AppUser{
    private String id;
    private String username;
    private String email;
    private String password;

    @Nullable
    private String profileImageUrl;


    public AppUser() {
    }



    public AppUser(String id, String username, String email, String password, String urlPicture ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.profileImageUrl = urlPicture;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Nullable
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(@Nullable String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
