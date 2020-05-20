import 'dart:io';

import 'package:flutter/services.dart';

const EventChannel eventChannel = EventChannel('e-ink.fitdev.io/screenshotStream');
dynamic image;
WebSocket ws;

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
      if(WebSocketTransformer.isUpgradeRequest(request)) {
        handleWebsocketCommunication(request);
        return;
      }

      switch(request.requestedUri.path) {
        case '/': serveMain(request); break;
        case '/screenshot': serveScreenshot(request); break;
        default: serveText(request);
      }
    });
  });
}

handleWebsocketCommunication(HttpRequest request) async {
  ws = await WebSocketTransformer.upgrade(request);
  void imageUpdated(dynamic imageData){
    print('ws:new image');
    ws.add('new image');
  }

  eventChannel.receiveBroadcastStream().listen(imageUpdated);
}

serveScreenshot(HttpRequest request) async {
  request.response.headers.set('Content-Type', 'image/png');
  request.response.add(image);
  request.response.close();
}

serveMain(HttpRequest request) async {
  request.response.headers.contentType = ContentType.html;
  String js = await getFileContent('functions.js');
  String html = await getFileContent('index.html');
  String content = html.replaceFirst('//AutoreplaceByServer', js);
  request.response.write(content);
  request.response.close();
}

serveText(HttpRequest request) async {
  request.response.write('Hello, world! ${request.uri.queryParameters['q']}');
  request.response.close();
}

Future<String> getFileContent(String filename) {
  return rootBundle.loadString('assets/webserver/$filename');
}