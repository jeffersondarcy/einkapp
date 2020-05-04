import 'dart:io';
import 'package:native_screenshot/native_screenshot.dart';

Future webServer() async {
  String path = '';
  File image;
  HttpServer.bind(InternetAddress.anyIPv6, 3000).then((server) {
    server.listen((HttpRequest request) async {
      path = await NativeScreenshot.takeScreenshot();
      if (path == null) return;
      image = new File(path);
      image.readAsBytes().then((raw) {
        request.response.headers.set('Content-Type', 'image/png');
        request.response.headers.set('Content-Length', raw.length);
        request.response.add(raw);
        request.response.close();
      });
      //request.response.write('Hello, worldдфгфдг! $path');
      //request.response.close();
    });
  });
}
