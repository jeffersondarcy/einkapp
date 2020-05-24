import 'dart:async';
import 'dart:io';
import 'dart:convert';
import 'package:shelf/shelf.dart' show Handler, Pipeline, Request, Response;
import 'package:shelf/shelf_io.dart' as io;

import 'package:flutter/services.dart';
import 'package:sse/server/sse_handler.dart';

const EventChannel eventChannel = EventChannel('e-ink.fitdev.io/screenshotStream');
dynamic image;
SseHandler sseHandler = SseHandler(Uri.parse('/sse'), keepAlive: Duration(minutes: 10));

void handleNewImage(dynamic imageData) async {
  image = imageData;
  sendSSEUpdate();
}

sendSSEUpdate() async{
  var connections = sseHandler.connections;
  while(await connections.hasNext) {
    var connection = await connections.next;
      connection.sink.add('update');
      //connection.stream.listen(print);
  }
}

Future webServer() async {
  eventChannel.receiveBroadcastStream().listen(handleNewImage);
  final handler = const Pipeline().addHandler(requestHandler);
  io.serve(handler, InternetAddress.anyIPv6, 3000).then((server) {
    print('Serving at http://${server.address.host}:${server.port}');
  });
}

Future<Response> requestHandler(Request request) {
  switch(request.requestedUri.path) {
        case '/': return serveMain(request); break;
        case '/screenshot': return serveScreenshot(request); break;
        case '/sse': return sseHandler.handler(request); break;
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

Future<String> getFileContent(String filename) {
  return rootBundle.loadString('assets/webserver/$filename');
}