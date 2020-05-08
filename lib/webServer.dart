import 'dart:io';

import 'package:einkapp/functions.dart';

Future webServer() async {
  String path = '';
  File image;
  HttpServer.bind(InternetAddress.anyIPv6, 3000).then((server) {
    server.listen((HttpRequest request) async {
      //request.response.write('Hello, worldдфгфдг! ${getScreenImage()}');
      request.response.write('Hello, worldдфгфдг!');
      request.response.close();
    });
  });
}
