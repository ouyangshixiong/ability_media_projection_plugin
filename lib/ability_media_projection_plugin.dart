import 'dart:async';

import 'package:flutter/services.dart';

class AbilityMediaProjectionPlugin {
  static const MethodChannel _channel =
      const MethodChannel('ability_media_projection_plugin');

  static Future<String> get startRecord async {
    final String version = await _channel.invokeMethod('startRecordScreen');
    return version;
  }

  static Future<String> get stopRecord async {
    final String version = await _channel.invokeMethod('stopRecordScreen');
    return version;
  }
}
