package com.example.chatbook.Ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chatbook.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openLactivity(View view) {
        // create an intent to start the Numbers activity
        Intent Lintent = new Intent(this, Login.class);

        // start the Numbers activity
        startActivity(Lintent);
    }

    public void openRactivity(View view) {
        // create an intent to start the Numbers activity
        Intent Rintent = new Intent(this, Register.class);

        // start the Numbers activity
        startActivity(Rintent);
    }
}
