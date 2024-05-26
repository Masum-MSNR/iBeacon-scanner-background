import 'package:ibeacon_scanner_background/ibeacon.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'ibeacon_scanner_background_method_channel.dart';

abstract class IbeaconScannerBackgroundPlatform extends PlatformInterface {
  /// Constructs a IbeaconScannerBackgroundPlatform.
  IbeaconScannerBackgroundPlatform() : super(token: _token);

  static final Object _token = Object();

  static IbeaconScannerBackgroundPlatform _instance =
      MethodChannelIbeaconScannerBackground();

  /// The default instance of [IbeaconScannerBackgroundPlatform] to use.
  ///
  /// Defaults to [MethodChannelIbeaconScannerBackground].
  static IbeaconScannerBackgroundPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [IbeaconScannerBackgroundPlatform] when
  /// they register themselves.
  static set instance(IbeaconScannerBackgroundPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<bool?> startScan() {
    throw UnimplementedError('startScan() has not been implemented.');
  }

  Future<bool?> stopScan() {
    throw UnimplementedError('stopScan() has not been implemented.');
  }

  Stream<dynamic> get iBeaconStream {
    throw UnimplementedError('iBeaconStream has not been implemented.');
  }
}
