package com.example.einkapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

import android.media.projection.MediaProjectionManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.content.Context;
import android.content.Intent;
import android.view.Display;
import android.view.Surface;
import android.util.Log;
import android.widget.Toast;
import android.hardware.display.DisplayManager;
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.graphics.Point;

public class MainActivity extends FlutterActivity {
    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private Surface mSurface;
    int width, height, density;
    private static final String CHANNEL = "e-ink.fitdev.io/screen-capture";
    private static final String TAG = "MediaProjectionDemo";
    private static final int PERMISSION_CODE = 1;

    private void initDisplayParameters() {
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        Point size = new Point();
        display.getRealSize(size);
        width = size.x;
        height = size.y;
        density = metrics.densityDpi;
    }

    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("ScreenSharingDemo",
                width, height, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mSurface, null /*Callbacks*/, null /*Handler*/);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initDisplayParameters();
        super.onCreate(savedInstanceState);
        mProjectionManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(),
                PERMISSION_CODE);
    }

    @Override
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        mVirtualDisplay.release();
    }

    @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    super.configureFlutterEngine(flutterEngine);
    //ScreenshotHandler screenshotHandler = new ScreenshotHandler(this);
        //initDisplayParameters();

       //shareScreen();
    new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
      .setMethodCallHandler(
        (call, result) -> {
          if (call.method.equals("getScreenImage")) {
              //screenshotHandler.takeScreenshot();
             // handler = ScreenshotHandler.getInstance();
             // handler.takeScreenShot();
                result.success("hallo bla vbla");
            } else {
              result.notImplemented();
            }
        }
      );
  }
}
