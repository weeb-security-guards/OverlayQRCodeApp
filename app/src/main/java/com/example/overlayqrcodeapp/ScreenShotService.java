package com.example.overlayqrcodeapp;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v4.os.AsyncTaskCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScreenShotService extends Service{

    private final static String TAG = "ScreenShotService";

    private static Intent resultIntent;

    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;

    private ImageReader imageReader;
    private Handler handler;
    private ImageView igv = null;

    public ScreenShotService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initWindow();
        initHandler();
        createImageReader();
        initMediaProjection();
    }

    private WindowManager windowManager =null;
    private WindowManager.LayoutParams params;

    private int screenWidth, screenHeight, screenDensity;

    public void initWindow() {

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        screenDensity = metrics.densityDpi;
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        Log.d(TAG,"density:"+screenDensity+", width:" + screenWidth + ", height:" + screenHeight);

        igv = new ImageView(this);
        igv.setImageResource(R.mipmap.ic_launcher);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = screenHeight/4;
        igv.setImageAlpha(10);

        igv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScreenShot();
            }
        });

        windowManager.addView(igv, params);

    }

    public void initHandler(){
        handler = new Handler();
    }

    public void createImageReader() {
        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 1);
    }

    public void initMediaProjection(){

        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, resultIntent);

        virtualDisplay = mediaProjection.createVirtualDisplay("screen-mirror",
                screenWidth, screenHeight, screenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(), null, null);

    }

    public static void setResultIntent(Intent it){
        resultIntent = it;
    }

    public void startScreenShot(){

        igv.setVisibility(View.GONE);

        handler.post(new Runnable() {
            public void run() {
                startCapture();
            }
        });
    }

    private void startCapture() {
        Image image = imageReader.acquireLatestImage();
        new SaveTask().execute(image);
        doBackgroundToast();
        Log.d("Background toast: ", "Toasted");
    }

    public class SaveTask extends AsyncTask<Image, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Image... params) {

            if (params == null || params.length < 1 || params[0] == null) {
                return false;
            }

            Boolean success = false;


            Image image = params[0];

            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();

            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * screenWidth;
            Bitmap bitmap = Bitmap.createBitmap(screenWidth + rowPadding / pixelStride,
                    screenHeight, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight);
            image.close();
            File fileImage;
            if (bitmap != null) {
                try {
                    /*
                    String fileName = String.format(Locale.getDefault(), "debug_screenshot_%s.jpg",
                            new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault())
                                    .format(new Date(System.currentTimeMillis())));
                                    */
                    String fileName = "Screenshot.jpg";
                    fileImage = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES), fileName);
                    Log.d(TAG, fileName);
                    FileOutputStream out = new FileOutputStream(fileImage);
                    if (out != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                        success = true;
                        Log.d(TAG, "Saved: " + fileName);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    bitmap.recycle();
                }
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);

            if (bool) {
                Toast.makeText(getApplicationContext(), "Got it", Toast.LENGTH_SHORT).show();
            } else {
                startCapture();
            }
            igv.setVisibility(View.VISIBLE);

        }
    }

    public void doBackgroundToast() {
        View layout = new LinearLayout(getApplicationContext());
        ((LinearLayout) layout).setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                240));
        ImageView qrCodeOverlayImage = new ImageView(getApplicationContext());
        qrCodeOverlayImage.setImageResource(R.drawable.jasmine_qr_code);
        RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(560, 560);
        lparams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lparams.addRule(Gravity.CENTER);
        lparams.setMargins(5,5,5,5);
        qrCodeOverlayImage.setLayoutParams(lparams);
        ((LinearLayout) layout).addView(qrCodeOverlayImage);
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0,0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}
