package dev.almasum.ibeacon_scanner_background.ibeacon


class BeaconModel(mac: String?) {

    var macAddress = mac
    var manufacturer: String? = null
    var type: String? = null
    var uuid: String? = null
    var major: Int? = null
    var minor: Int? = null
    var namespace: String? = null
    var instance: String? = null
    var rssi: Int? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var timestamp: Long? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BeaconModel) return false

        if (macAddress != other.macAddress) return false

        return true
    }

    override fun hashCode(): Int {
        return macAddress?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "{\"mac\":\"$macAddress\", \"manufacturer\":\"$manufacturer\", \"type\":\"$type\", \"uuid\":\"$uuid\", \"major\":$major, \"minor\":$minor, \"namespace\":\"$namespace\", \"instance\":\"$instance\", \"rssi\":$rssi, \"latitude\":$latitude, \"longitude\":$longitude}"
    }

}