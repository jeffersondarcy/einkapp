import 'dart:async';

import 'package:flutter/services.dart';
import 'package:image/image.dart';

final Stream<dynamic> plattformStream =
    EventChannel('e-ink.fitdev.io/screenshotStream').receiveBroadcastStream();

class ImageProcessor {
  final controller = StreamController<dynamic>();
  bool busy = false;
  var _outputImage;

  ImageProcessor() {
    plattformStream.listen(handleNewImage);
  }

  receiveBroadcastStream() {
    return controller.stream;
  }

  handleNewImage(dynamic imageData) {
    if (busy == true) {
      print('skip image');
      return;
    }
    busy = true;
    _outputImage = processImage(imageData);
    controller.add(_outputImage);
    busy = false;
  }

  getCurrentImage() {
    return _outputImage;
  }
}

processImage(bitmapData) {
  //return bitmapData;
  Image img = decodePng(bitmapData);
  return encodePng(invert(img));
}
