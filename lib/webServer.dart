import 'dart:async';
import 'dart:io';
import 'package:shelf/shelf.dart' show Handler, Pipeline, Request, Response;
import 'package:shelf/shelf_io.dart' as io;

import 'package:flutter/services.dart';
import 'package:sse/server/sse_handler.dart';

const EventChannel eventChannel = EventChannel('e-ink.fitdev.io/screenshotStream');
dynamic image;

void handleNewImage(dynamic imageData) {
  image = imageData;
}

Future webServer() async {
  eventChannel.receiveBroadcastStream().listen(handleNewImage);
  var sseHandler = SseHandler(Uri.parse('/sse'));
  final handler = const Pipeline().addHandler(requestHandler);
  io.serve(handler, InternetAddress.anyIPv6, 3000).then((server) {
    print('Serving at http://${server.address.host}:${server.port}');
  });
}

SseHandler handler = SseHandler(Uri.parse('/sse'));
Future<Response> requestHandler(Request request) {
  switch(request.requestedUri.path) {
        case '/': return serveMain(request); break;
        case '/screenshot': return serveScreenshot(request); break;
        default : return serveMain(request);
        /*
        case '/test': serveExperiment(request); break;
        case '/screenshot': serveScreenshot(request); break;
        case '/sse': serveEvents(request); break;
        default: serveText(request);

         */
      }
}


Future<Response> serveScreenshot(Request request) async {
  const headers = {
    'content-type': 'image/png',
    'Cache-control': 'max-age=0, must-revalidate',
  };
  return Response.ok(image, headers: headers);
}

Future<Response> serveMain(Request request) async {
  String js = await getFileContent('functions.js');
  String html = await getFileContent('index.html');
  String content = html.replaceFirst('//AutoreplaceByServer', js);
  return Response.ok(content, headers: {'content-type': 'text/html'});
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