package com.example.overlayqrcodeapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    protected void onPause() {
        super.onPause();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        for (int i =0; i < 10; i++) {
            doBackgroundToast();
        }
    }

    private void checkForQRCode() {

    }

    private void doBackgroundToast() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.qr_overlay_toast,
                (ViewGroup) findViewById(R.id.qr_overlay));
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0,50);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();

    }
}
