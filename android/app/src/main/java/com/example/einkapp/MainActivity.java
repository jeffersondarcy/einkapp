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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends FlutterActivity implements OnImageAvailableListener {
    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    int width, height, density;
    ImageReader mImageReader;
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
        mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 10);
        mImageReader.setOnImageAvailableListener(this, null);
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

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image = reader.acquireLatestImage();
        Log.w("mylog", reader.toString());

        FileOutputStream fos = null;
            Bitmap bitmap = null;
            Image img = null;
            try {
                img = reader.acquireLatestImage();
                if (img != null) {
                    Image.Plane[] planes = img.getPlanes();
                    if (planes[0].getBuffer() == null) {
                        return;
                    }
                    int width = img.getWidth();
                    int height = img.getHeight();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * width;
                    byte[] newData = new byte[width * height * 4];

                    int offset = 0;
                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    ByteBuffer buffer = planes[0].getBuffer();
                    for (int i = 0; i < height; ++i) {
                        for (int j = 0; j < width; ++j) {
                            int pixel = 0;
                            pixel |= (buffer.get(offset) & 0xff) << 16;     // R
                            pixel |= (buffer.get(offset + 1) & 0xff) << 8;  // G
                            pixel |= (buffer.get(offset + 2) & 0xff);       // B
                            pixel |= (buffer.get(offset + 3) & 0xff) << 24; // A
                            bitmap.setPixel(j, i, pixel);
                            offset += pixelStride;
                        }
                        offset += rowPadding;
                    }
                    String name = "/myscreen.jpg";
                    File file = new File(Environment.DIRECTORY_PICTURES, name);
                    fos = new FileOutputStream(file.getAbsolutePath());
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    img.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != fos) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (null != bitmap) {
                    bitmap.recycle();
                }
                if (null != img) {
                    img.close();
                }

            }
        image.close();

    }
}
