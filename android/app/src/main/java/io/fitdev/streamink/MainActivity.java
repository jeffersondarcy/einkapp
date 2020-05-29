package io.fitdev.streamink;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.EventChannel;

import android.media.projection.MediaProjectionManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.content.Context;
import android.content.Intent;
import android.view.Display;
import android.view.Surface;
import android.widget.Toast;
import android.hardware.display.DisplayManager;
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.graphics.Point;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class MainActivity extends FlutterActivity {
  Display display;
  private final int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR;
  private MediaProjectionManager mProjectionManager;
  private MediaProjection mMediaProjection;
  private VirtualDisplay mVirtualDisplay;
  private EventChannel eventChannel;
  private DisplayMetrics metrics = new DisplayMetrics();
  int width, height, density, rotation;
  ImageReader mImageReader;
  private static final String TAG = "MediaProjectionDemo";
  private static final int PERMISSION_CODE = 1;

  private void refreshDisplayParameters() {
    display.getMetrics(metrics);
    Point size = new Point();
    display.getRealSize(size);
    width = size.x;
    height = size.y;
    density = metrics.densityDpi;
    rotation = display.getRotation();
  }

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (newConfig.orientation != rotation) {
      refreshDisplayParameters();
      mVirtualDisplay.release();
      //mImageReader.close();
      mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
      mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror", width, height, density, flags, mImageReader.getSurface(), null, null);
      //setStreamHandler();
    }
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

    refreshDisplayParameters();
    super.onCreate(savedInstanceState);
    Intent intent = new Intent(this, MediaProjectionService.class);
    startForegroundService(intent);
    mProjectionManager =
      (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    startActivityForResult(mProjectionManager.createScreenCaptureIntent(),
      PERMISSION_CODE);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {

    if (requestCode != PERMISSION_CODE) {
      Toast.makeText(this,
        "Unknown request code: " + requestCode, Toast.LENGTH_SHORT).show();
      return;
    }
    if (resultCode != RESULT_OK) {
      Toast.makeText(this,
        "User denied screen sharing permission", Toast.LENGTH_SHORT).show();
      return;
    }
    mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);

    mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror", width, height, density, flags, mImageReader.getSurface(), null, null);
  }

  private void pauseProjection() {
    mVirtualDisplay.setSurface(null);
  }

  private void resumeProjection() {
    mVirtualDisplay.setSurface(mImageReader.getSurface());
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (isFinishing()) {
      if (mMediaProjection != null) {
        mMediaProjection.stop();
        mMediaProjection = null;
      }
      mVirtualDisplay.release();
    }
  }

  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    super.configureFlutterEngine(flutterEngine);

    mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
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

        Bitmap bufferedBmp = getBufferedBitmap(img);
        Bitmap fixedBmp = bufferedBmp;
        //Bitmap fixedBmp = getFixedBitmap(bufferedBmp);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fixedBmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        imageStreamSink.success(imageData);
        img.close();
      },
      null);
  }

  private Bitmap getBufferedBitmap(Image img) {
    final Image.Plane[] planes = img.getPlanes();
    final ByteBuffer buffer = planes[0].getBuffer();
    int pixelStride = planes[0].getPixelStride();
    int rowStride = planes[0].getRowStride();
    int rowPadding = rowStride - pixelStride * img.getWidth();
    Bitmap bitmap = Bitmap.createBitmap(img.getWidth() + rowPadding / pixelStride, img.getHeight(), Bitmap.Config.ARGB_8888);
    bitmap.copyPixelsFromBuffer(buffer);

    return bitmap;
  }
}
