package com.example.overlayqrcodeapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import static android.Manifest.permission.SYSTEM_ALERT_WINDOW;

public class MainActivity extends AppCompatActivity {

    private String DEBUG_TAG="Action: ";
    private String TAG = "Screenshot Tag: ";
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkCanDrawOverlays();
    }

    protected void onPause() {
        super.onPause();
        checkForQRCode();
        //checkCanDrawOverlays();
        /*
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        */
        for (int i =0; i < 5; i++) {
            //doBackgroundToast();
        }
    }

    public void checkCanDrawOverlays(){
        if (Build.VERSION.SDK_INT >= 23) {
            if(!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivityForResult(intent,0);
            }
            else{
                requestCapturePermission();
            }
        }
    }

    private void requestCapturePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent it = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(it, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent it) {
        super.onActivityResult(requestCode, resultCode, it);

        switch (requestCode) {
            case 0:
                if(resultCode == RESULT_OK){
                    requestCapturePermission();
                }
                break;
            case 1:
                if (resultCode == RESULT_OK && it != null) {
                    ScreenShotService.setResultIntent(it);
                    startService(new Intent(getApplicationContext(), ScreenShotService.class));
                }
                break;

        }
    }

    private void checkForQRCode() {
        String filePath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + "/Screenshot.jpg";
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        Log.d("Directory: ", filePath);

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> barcodes = barcodeDetector.detect(frame);

        if (barcodes.size() != 0) {
            Log.d("QR URL is : ", barcodes.valueAt(0).displayValue);
        } else {
            Log.d("QR URL is : ", "No QR URL detected");
        }
    }

    private void doBackgroundToast() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.qr_overlay_toast,
                (ViewGroup) findViewById(R.id.qr_overlay_root));
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0,0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}
