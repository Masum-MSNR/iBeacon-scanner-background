import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'ibeacon_scanner_background_platform_interface.dart';

/// An implementation of [IbeaconScannerBackgroundPlatform] that uses method channels.
class MethodChannelIbeaconScannerBackground
    extends IbeaconScannerBackgroundPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('iBeacon');
  final eventChannel = const EventChannel('iBeaconStream');

  @override
  Future<bool?> startScan() async {
    final res = await methodChannel.invokeMethod<bool>('start_scan');
    return res;
  }

  @override
  Future<bool?> stopScan() async {
    final res = await methodChannel.invokeMethod<bool>('stop_scan');
    return res;
  }

  @override
  Future<bool?> saveToken(String token) async {
    final res = await methodChannel.invokeMethod<bool>('save_token', token);
    return res;
  }

  @override
  Stream<dynamic> get iBeaconStream {
    return eventChannel.receiveBroadcastStream();
  }
}
