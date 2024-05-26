import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:ibeacon_scanner_background/ibeacon.dart';
import 'package:ibeacon_scanner_background/ibeacon_scanner_background.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('iBeacon Scanner'),
      ),
      body: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              ElevatedButton(
                onPressed: () {
                  IbeaconScannerBackground().startScan();
                },
                child: Text('Start Scan'),
              ),
              SizedBox(width: 50),
              ElevatedButton(
                onPressed: () {
                  IbeaconScannerBackground().stopScan();
                },
                child: Text('Stop Scan'),
              ),
            ],
          ),
          Expanded(
            child: StreamBuilder<dynamic>(
              stream: IbeaconScannerBackground().ibeaconStream,
              builder: (context, snapshot) {
                if (snapshot.hasData) {
                  List<IBeacon> beacons = [];
                  var events = jsonDecode(snapshot.data);
                  for (var beacon in events) {
                    beacons.add(IBeacon.fromJson(beacon));
                  }
                  print(beacons.length);
                  return ListView.builder(
                    itemCount: beacons.length,
                    itemBuilder: (context, index) {
                      return ListTile(
                        title: Text(beacons[index].uuid!),
                        subtitle: Text(beacons[index].major.toString() +
                            ' ' +
                            beacons[index].minor.toString()),
                        trailing: Text(beacons[index].rssi.toString()),
                      );
                    },
                  );
                } else {
                  return const Center(child: Text("No beacons found"));
                }
              },
            ),
          )
        ],
      ),
    );
  }
}
