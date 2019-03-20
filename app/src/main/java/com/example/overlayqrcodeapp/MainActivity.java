package com.example.overlayqrcodeapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
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

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.SYSTEM_ALERT_WINDOW;

public class MainActivity extends AppCompatActivity {

    private String DEBUG_TAG="Action: ";
    private String TAG = "Screenshot Tag: ";
    private static final int PERMISSION_REQUEST_CODE = 1;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkCanDrawOverlays();
    }

    protected void onStop() {
        super.onStop();
        //checkCanDrawOverlays();

        /*
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (checkForQRCode()) {
                    doBackgroundToast();
                    cancel();
                }
            }
        },500,5000);
        */
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }


        if (checkForQRCode()) {
            for (int i =0; i < 10; i++) {
                doBackgroundToast();
            }
        }

    }

    public void checkCanDrawOverlays(){
        if (Build.VERSION.SDK_INT >= 23) {
            if(!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivityForResult(intent,0);
            } else {
                requestCapturePermission();
            }
        }
    }

    private void requestCapturePermission() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent it = mediaProjectionManager.createScreenCaptureIntent();
        it.addFlags(Intent.FLAG_FROM_BACKGROUND);
        it.setAction(Intent.ACTION_USER_BACKGROUND);
        startActivityForResult(it, 1);
        //moveTaskToBack(true);
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

    private BarcodeDetector barcodeDetector = null;
    private Frame frame = null;
    private SparseArray<Barcode> barcodes = null;

    public boolean checkForQRCode()  {

        String filePath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + "/Screenshot.jpg";
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        Log.d("Directory: ", filePath);

        barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        frame = new Frame.Builder().setBitmap(bitmap).build();
        barcodes = barcodeDetector.detect(frame);

        if (barcodes.size() != 0) {
            Log.d("QR URL is : ", barcodes.valueAt(0).displayValue);
            return true;
        } else {
            Log.d("QR URL is : ", "No QR URL detected");
        }
        return false;
    }

    public void doBackgroundToast() {
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
