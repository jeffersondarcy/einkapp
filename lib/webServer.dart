import 'dart:io';

import 'package:flutter/services.dart';

const EventChannel eventChannel = EventChannel('e-ink.fitdev.io/screenshotStream');
dynamic image;

void handleNewImage(dynamic imageData) {
  image = imageData;
}

registerScreenshotStreamSubscription() {
  eventChannel.receiveBroadcastStream().listen(handleNewImage);
}

Future webServer() async {
  registerScreenshotStreamSubscription();
  HttpServer.bind(InternetAddress.anyIPv6, 3000).then((server) {
    server.listen((HttpRequest request) async {
      request.response.headers.set('Content-Type', 'image/png');
      request.response.add(image);
      request.response.close();
    });
  });
}
