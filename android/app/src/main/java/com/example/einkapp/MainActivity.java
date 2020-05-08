package com.example.einkapp;

import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
  private static final String CHANNEL = "e-ink.fitdev.io/screen-capture";

  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    super.configureFlutterEngine(flutterEngine);
    new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
      .setMethodCallHandler(
        (call, result) -> {
          if (call.method.equals("getScreenImage")) {
                result.success("hallo bla vbla");
            } else {
              result.notImplemented();
            }
        }
      );
  }
}
