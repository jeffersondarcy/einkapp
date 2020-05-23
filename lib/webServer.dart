import 'dart:async';
import 'dart:io';
import 'package:shelf/shelf.dart' show Handler, Pipeline, Request;
import 'package:shelf/shelf_io.dart' as io;

import 'package:flutter/services.dart';
import 'package:sse/server/sse_handler.dart';

const EventChannel eventChannel = EventChannel('e-ink.fitdev.io/screenshotStream');
dynamic image;

void handleNewImage(dynamic imageData) {
  image = imageData;
}

Future shelfWebServer() async {
  var sseHandler = SseHandler(Uri.parse('/sse'));
  final handler = const Pipeline().addHandler(requestHandler);
  io.serve(handler, InternetAddress.anyIPv6, 3000).then((server) {
    print('Serving at http://${server.address.host}:${server.port}');
  });
}

SseHandler handler = SseHandler(Uri.parse('/sse'));
Future<dynamic> requestHandler(Request request) {
  switch(request.requestedUri.path) {
        case '/': serveMain(request); break;
        case '/test': serveExperiment(request); break;
        case '/screenshot': serveScreenshot(request); break;
        case '/sse': serveEvents(request); break;
        default: serveText(request);
      }
}

SseHandler handler = SseHandler(Uri.parse('/sse'));
Future<void>serveEvents(HttpRequest request) async {
  response = await handler.handler(shelf.Request)
  request.response.headers.set('Cache-control', 'no-cache');
  request.response.headers.set('Content-Type', 'text/event-stream');
  request.response.write('bla');
  request.response.flush();
  Timer fut = Timer.periodic(Duration(seconds: 1), (timer) {
    print('out');
  request.response.write('bla');
    request.response.flush();
  });
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