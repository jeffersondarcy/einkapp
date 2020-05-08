import 'package:flutter/services.dart';

const platform = const MethodChannel('e-ink.fitdev.io/screen-capture');

Future<String> getScreenImage() async {
  String image;
  try {
    final String result = await platform.invokeMethod('getScreenImage');
    image = result;
  } on PlatformException catch (e) {}

  return image;
}
