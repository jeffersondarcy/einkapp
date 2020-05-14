package com.example.einkapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.ImageReader;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;

public class ScreenshotHandler {
    final private int flags =
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
                    | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
                    | DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR;

    private Context context;
    Intent mediaProjectionIntent;
    int width, height, density;
    ImageReader imageReader;
    MediaProjection mProjection;

    ScreenshotHandler(Context context) {
        this.context = context;
        init();
    }

    private void initDisplayParameters() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        final DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        Point size = new Point();
        display.getRealSize(size);
        width = size.x;
        height = size.y;
        density = metrics.densityDpi;
    }

    private void init() {
        initDisplayParameters();

        //Create a imageReader for catch result
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);

        MediaProjectionManager projectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mediaProjectionIntent = projectionManager.createScreenCaptureIntent();
        context.startActivity(mediaProjectionIntent);
        mProjection = projectionManager.getMediaProjection(mediaProjectionIntent.);
    }

    private void createVirtualDisplay() {
        mProjection.createVirtualDisplay("screen-mirror", width, height, density, flags, imageReader.getSurface(), null, null);
    }

    private MediaProjection getMediaProjection() {
        MediaProjectionManager projectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        return projectionManager.getMediaProjection(Activity.RESULT_OK, (Intent) mediaProjectionIntent.clone());
    }

    public void takeScreenshot() {
        createVirtualDisplay();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != PERMISSION_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this,
                    "User denied screen sharing permission", Toast.LENGTH_SHORT).show();
            return;
        }
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mVirtualDisplay = createVirtualDisplay();
    }

}
