import 'package:flutter/material.dart';
import 'dart:async';

import 'package:ability_media_projection_plugin/ability_media_projection_plugin.dart';
import 'package:flutter/services.dart';

//void main() => runApp(MyApp());

void main(){
  WidgetsFlutterBinding.ensureInitialized();
  SystemChrome.setPreferredOrientations([
    DeviceOrientation.landscapeLeft,
    DeviceOrientation.landscapeRight
  ]);
  runApp(MyApp());
}


class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _opResult = "";

  @override
  void initState() {
    super.initState();
  }

  void startRecord() {
    AbilityMediaProjectionPlugin.startRecord.then((value) => setState((){
      _opResult = value;
    }));
  }

  void stopRecord() {
    AbilityMediaProjectionPlugin.stopRecord.then((value) => setState((){
      _opResult = value;
    }));
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('录屏测试'),
        ),
        body: Column(
          children: <Widget>[
            Text("录屏能力验证："),
            RaisedButton(
              onPressed: () => startRecord(),
              color: Colors.lightBlueAccent,
              child: Text('开始录屏', style: TextStyle(fontSize: 10)),
            ),
            RaisedButton(
              onPressed: () => stopRecord(),
              color: Colors.lightBlueAccent,
              child: Text('结束录屏', style: TextStyle(fontSize: 10)),
            ),
            Text('$_opResult'),
          ],
        ),
      ),
    );
  }
}
