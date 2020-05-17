import 'package:einkapp/camera_image.dart';
import 'package:flutter/services.dart';

const EventChannel eventChannel = EventChannel('e-ink.fitdev.io/screenshotStream');
CameraImage image;

void handleNewImage(dynamic imageData) {
  image = CameraImage.fromPlatformData(imageData);
  print(image.height);
}

registerScreenshotStreamSubscription() {
  eventChannel.receiveBroadcastStream().listen(handleNewImage);
}
