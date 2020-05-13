package com.example.einkapp;

import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;
// import com.example.einkapp.ScreenshotHandler;

public class MainActivity extends FlutterActivity {
  private static final String CHANNEL = "e-ink.fitdev.io/screen-capture";

  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    super.configureFlutterEngine(flutterEngine);
    ScreenshotHandler screenshotHandler = new ScreenshotHandler(this);

    new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
      .setMethodCallHandler(
        (call, result) -> {
          if (call.method.equals("getScreenImage")) {
              screenshotHandler.takeScreenshot();
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
