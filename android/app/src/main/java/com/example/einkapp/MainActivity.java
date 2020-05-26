package com.example.einkapp;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;

import android.media.projection.MediaProjectionManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.view.Display;
import android.view.Surface;
import android.util.Log;
import android.widget.Toast;
import android.hardware.display.DisplayManager;
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.graphics.Point;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends FlutterActivity {
    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private EventChannel eventChannel;
    int width, height, density;
    ImageReader mImageReader;
    EventChannel imageStreamChannel;
    private static final String CHANNEL = "e-ink.fitdev.io/screen-capture";
    private static final String TAG = "MediaProjectionDemo";
    private static final int PERMISSION_CODE = 1;

    private void initDisplayParameters() {
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        Point size = new Point();
        display.getRealSize(size);
        width = size.x / 4;
        height = size.y / 4;
        density = metrics.densityDpi;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        final String channelName = "e-ink.fitdev.io/screenshotStream";
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
        final int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR;
        mMediaProjection.createVirtualDisplay("screen-mirror", width, height, density, flags, mImageReader.getSurface(), null, null);
        Log.w("mylog", "dsdfsfs serfsf");
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

        mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 10);
        final String channelName = "e-ink.fitdev.io/screenshotStream";
        eventChannel = new EventChannel(getFlutterEngine().getDartExecutor().getBinaryMessenger(), channelName);
        setStreamHandler();
    }

    private void setStreamHandler() {
        eventChannel.setStreamHandler(
                new EventChannel.StreamHandler() {
                    @Override
                    public void onListen(Object o, EventChannel.EventSink imageStreamSink) {
                        setImageStreamImageAvailableListener(imageStreamSink);
                    }

                    @Override
                    public void onCancel(Object o) {
                        mImageReader.setOnImageAvailableListener(null, null);
                    }
                });
    }

    private void setImageStreamImageAvailableListener(final EventChannel.EventSink imageStreamSink) {
        mImageReader.setOnImageAvailableListener(
                reader -> {
                    Image img = reader.acquireLatestImage();
                    if (img == null) return;

                    final Image.Plane[] planes = img.getPlanes();
                    final ByteBuffer buffer = planes[0].getBuffer();
                    int offset = 0;
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * width;
// create bitmap
                    Bitmap bitmap = Bitmap.createBitmap(width+rowPadding/pixelStride, height, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);
                    ByteArrayOutputStream baos=new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] newPng=baos.toByteArray();

                    imageStreamSink.success(newPng);
                    img.close();
                },
                null);
    }
}
