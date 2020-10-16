import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ability_media_projection_plugin/ability_media_projection_plugin.dart';

void main() {
  const MethodChannel channel = MethodChannel('ability_media_projection_plugin');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await AbilityMediaProjectionPlugin.platformVersion, '42');
  });
}
