import 'dart:async';
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
      switch(request.requestedUri.path) {
        case '/': serveMain(request); break;
        case '/screenshot': serveScreenshot(request); break;
        case '/sse': serveSSE(request); break;
      }
    });
  });
}

serveScreenshot(HttpRequest request) async {
  request.response.headers.set('Content-Type', 'image/png');
  request.response.add(image);
  request.response.close();
}

int counter =0;
serveSSE(HttpRequest request) async {
  request.response.headers.set('Connection', 'keep-alive');
  //request.response.headers.contentType = ContentType.html;
  request.response.headers.set('Content-Type', 'text/event-stream');
  request.response.headers.set('Cache-Control', 'no-cache');
  //request.response.write('bla1');
  request.response.write('\n');
  request.response.write('id: $counter\n');
  counter++;
  request.response.write('data: rerdgtdg\n\n');
  //request.response.flush();
  request.response.close();
  //request.response.close();
  //ptimer(request.response);
}

serveMain(HttpRequest request) async {
  request.response.headers.contentType = ContentType.html;
  String js = await getFileContent('functions.js');
  String html = await getFileContent('index.html');
  String content = html.replaceFirst('//AutoreplaceByServer', js);
  request.response.write(content);
  request.response.close();
}

Future<String> getFileContent(String filename) {
  return rootBundle.loadString('assets/webserver/$filename');
}