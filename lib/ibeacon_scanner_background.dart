import 'ibeacon_scanner_background_platform_interface.dart';

class IbeaconScannerBackground {
  Future<bool?> startScan() {
    return IbeaconScannerBackgroundPlatform.instance.startScan();
  }

  Future<bool?> stopScan() {
    return IbeaconScannerBackgroundPlatform.instance.stopScan();
  }

  Future<bool?> saveToken(String token) {
    return IbeaconScannerBackgroundPlatform.instance.saveToken(token);
  }

  Stream<dynamic> get ibeaconStream {
    return IbeaconScannerBackgroundPlatform.instance.iBeaconStream;
  }
}
