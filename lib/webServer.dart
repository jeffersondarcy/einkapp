import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

final Stream<dynamic> plattformStream = EventChannel('e-ink.fitdev.io/screenshotStream')
    .receiveBroadcastStream();
dynamic image;

void handleNewImage(dynamic imageData) {
  image = imageData;
}

registerScreenshotStreamSubscription() {
  plattformStream.listen(handleNewImage);
}

Future webServer() async {
  registerScreenshotStreamSubscription();
  HttpServer.bind(InternetAddress.anyIPv6, 3000).then((server) {
    server.listen((HttpRequest request) async {
      if(WebSocketTransformer.isUpgradeRequest(request)) {
        handleWebsocketCommunication(request);
        return;
      }

      if(request.requestedUri.path == '/') return serveMain(request);
      if(request.requestedUri.path.startsWith('/screenshot')) return serveScreenshot(request);

      serveStatic(request);
    });
  });
}

handleWebsocketCommunication(HttpRequest request) async {
  WebSocket ws = await WebSocketTransformer.upgrade(request);
  ws.addStream(plattformStream);
}

serveScreenshot(HttpRequest request) async {
  request.response.headers.set('Content-Type', 'image/png');
  request.response.add(image);
  request.response.close();
}

serveMain(HttpRequest request) async {
  request.response.headers.contentType = ContentType.html;
  String html = await getFileContent('index.html');
  request.response.write(html);
  request.response.close();
}

serveStatic(HttpRequest request) async {
  String path = request.requestedUri.path;
  if (path.endsWith('.js')) {
    request.response.headers.set('Content-Type', 'text/javascript');
    request.response.write(await getFileContent(path.substring(1)));
    request.response.close();
  }
}

Future<String> getFileContent(String filename) async{
  try {
    return rootBundle.loadString('assets/webserver/$filename');
  } catch (e) {
    return '';
  }
}