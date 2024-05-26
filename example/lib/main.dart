import 'dart:async';
import 'dart:io';

import 'package:device_info_plus/device_info_plus.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:ibeacon_scanner_background/ibeacon_scanner_background.dart';
import 'package:permission_handler/permission_handler.dart';

import 'home_page.dart';

main() async {
  WidgetsFlutterBinding.ensureInitialized();
  DeviceInfoPlugin deviceInfo = DeviceInfoPlugin();
  AndroidDeviceInfo androidInfo = await deviceInfo.androidInfo;

  if (Platform.isAndroid && androidInfo.version.sdkInt <= 30) {
    [
      Permission.bluetooth,
      Permission.bluetoothConnect,
      Permission.bluetoothScan,
      Permission.notification,
    ].request().then((status) {
      runApp(const MyApp());
    });
  } else if (Platform.isAndroid && androidInfo.version.sdkInt >= 31) {
    [
      Permission.bluetoothConnect,
      Permission.bluetoothScan,
      Permission.location,
      Permission.notification,
    ].request().then((status) {
      runApp(const MyApp());
    });
  }
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      debugShowCheckedModeBanner: false,
      home: HomePage(),
    );
  }
}
