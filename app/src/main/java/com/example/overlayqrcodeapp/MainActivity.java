package com.example.overlayqrcodeapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    protected void onPause() {
        super.onPause();
        for (int i =0; i < 100; i++) {
            Toast.makeText(this, "Hehehehehee", Toast.LENGTH_LONG).show();
        }
    }
}
