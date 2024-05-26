class IBeacon {
  String? mac;
  String? manufacturer;
  String? type;
  String? uuid;
  int? major;
  int? minor;
  String? namespace;
  String? instance;
  int? rssi;

  IBeacon(
      {this.mac,
      this.manufacturer,
      this.type,
      this.uuid,
      this.major,
      this.minor,
      this.namespace,
      this.instance,
      this.rssi});

  factory IBeacon.fromJson(Map<String, dynamic> json) {
    return IBeacon(
      mac: json['mac'],
      manufacturer: json['manufacturer'],
      type: json['type'],
      uuid: json['uuid'],
      major: json['major'],
      minor: json['minor'],
      namespace: json['namespace'],
      instance: json['instance'],
      rssi: json['rssi'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'mac': mac,
      'manufacturer': manufacturer,
      'type': type,
      'uuid': uuid,
      'major': major,
      'minor': minor,
      'namespace': namespace,
      'instance': instance,
      'rssi': rssi,
    };
  }
}
