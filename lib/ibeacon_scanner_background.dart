import 'package:ibeacon_scanner_background/ibeacon.dart';

import 'ibeacon_scanner_background_platform_interface.dart';

class IbeaconScannerBackground {
  Future<bool?> startScan() {
    return IbeaconScannerBackgroundPlatform.instance.startScan();
  }

  Future<bool?> stopScan() {
    return IbeaconScannerBackgroundPlatform.instance.stopScan();
  }

  Stream<dynamic> get ibeaconStream {
    return IbeaconScannerBackgroundPlatform.instance.iBeaconStream;
  }
}
