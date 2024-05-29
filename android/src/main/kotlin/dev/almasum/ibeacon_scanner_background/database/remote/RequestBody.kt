package dev.almasum.ibeacon_scanner_background.database.remote

import retrofit2.http.Field

data class RequestBody(
    @Field("action") val action: String,
    @Field("token") val token: String,
    @Field("timestamp") val timestamp: String,
    @Field("latitude") val latitude: String,
    @Field("longitude") val longitude: String,
    @Field("uuid") val uuid: String
){
    override fun toString(): String {
        return "RequestBody(action='$action', token='$token', timestamp='$timestamp', latitude='$latitude', longitude='$longitude', uuid='$uuid')"
    }
}
