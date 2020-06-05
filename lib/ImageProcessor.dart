import 'dart:async';
import 'dart:isolate';

import 'package:flutter/foundation.dart';
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
    return controller.stream.asBroadcastStream();
  }

  handleNewImage(dynamic imageData) {
    if (busy == true) {
      print('skip image');
      return;
    }
    processImage(imageData);
  }

  getCurrentImage() {
    return _outputImage;
  }

  processImage(bitmapData) async{
    busy = true;
    print('start isolate');
    final imageData = await compute(
        imageOperations, bitmapData
    );
    print('finish image');
    _outputImage = imageData;
    controller.add(imageData);
    busy = false;
  }
}

imageOperations(bitmapData){
  print('process image');
  Image img = decodePng(bitmapData);
  img = adjustColor(img, gamma: 2, saturation: 0, exposure: 0.06);
  return encodePng(img);
}
