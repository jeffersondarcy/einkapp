import 'package:flutter/services.dart';

const channelName = 'e-ink.fitdev.io/screen-capture';
final methodChannel = MethodChannel('e-ink.fitdev.io/screen-capture');

Future<void> handleNewImage(MethodCall call) {
  // type inference will work here avoiding an explicit cast
  switch (call.method) {
    case "newImageAvailable":
      print(call.arguments);
  }
}

registerImageHandler() {
  methodChannel.setMethodCallHandler(handleNewImage);
}
