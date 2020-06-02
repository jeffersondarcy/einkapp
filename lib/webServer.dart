import 'dart:async';
import 'dart:io';

import 'package:einkapp/ImageProcessor.dart';
import 'package:flutter/services.dart';

Future webServer() async {
  final imageProcessor = ImageProcessor();
  final Stream<dynamic> imageStream = imageProcessor.receiveBroadcastStream();

  handleWebsocketCommunication(HttpRequest request) async {
    WebSocket ws = await WebSocketTransformer.upgrade(request);
    ws.addStream(imageStream);
  }

  Future<String> getFileContent(String filename) async{
    try {
      return rootBundle.loadString('assets/webserver/$filename');
    } catch (e) {
      return '';
    }
  }

  serveScreenshot(HttpRequest request) async {
    request.response.headers.set('Content-Type', 'image/png');
    final image = imageProcessor.getCurrentImage();
    if(image != null ) request.response.add(image);
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