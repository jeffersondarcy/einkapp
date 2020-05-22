import 'dart:io';

import 'package:flutter/services.dart';

const EventChannel eventChannel = EventChannel('e-ink.fitdev.io/screenshotStream');
dynamic image;

void handleNewImage(dynamic imageData) {
  image = imageData;
}



Future webServer() async {
  eventChannel.receiveBroadcastStream().listen(handleNewImage);
  HttpServer.bind(InternetAddress.anyIPv6, 3000).then((server) {
    server.listen((HttpRequest request) async {
      if(WebSocketTransformer.isUpgradeRequest(request)) {
        handleWebsocketCommunication(request);
        return;
      }

      switch(request.requestedUri.path) {
        case '/': serveMain(request); break;
        case '/test': serveExperiment(request); break;
        case '/screenshot': serveScreenshot(request); break;
        default: serveText(request);
      }
    });
  });
}

handleWebsocketCommunication(HttpRequest request) async {
  WebSocket ws = await WebSocketTransformer.upgrade(request);
  /*
  switch(request.requestedUri.path) {
        case '/ws': serveMain(request); break;
        case '/test': serveExperiment(request); break;
        case '/screenshot': serveScreenshot(request); break;
        default: ws.addStream(eventChannel.receiveBroadcastStream());
      }

   */
  ws.addStream(eventChannel.receiveBroadcastStream());
}

serveScreenshot(HttpRequest request) async {
  request.response.headers.set('Content-Type', 'image/png');
  request.response.headers.set('Cache-control', 'max-age=0, must-revalidate');
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

serveExperiment(HttpRequest request) async {
  request.response.headers.contentType = ContentType.html;
  String js = await getFileContent('functions-experiment.js');
  String html = await getFileContent('experiment.html');
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