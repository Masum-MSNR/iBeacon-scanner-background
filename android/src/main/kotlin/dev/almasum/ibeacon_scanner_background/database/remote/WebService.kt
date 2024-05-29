package dev.almasum.ibeacon_scanner_background.database.remote

import android.text.SpannableString
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.lang.reflect.Type


interface WebService {
    @POST("/inventory/api.php")
    suspend fun postBeacon(
        @Body data: RequestBody
    ): JsonElement

    class SpannableDeserializer : JsonDeserializer<SpannableString> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): SpannableString {
            return SpannableString(json.asString)
        }
    }

    companion object {
        private fun buildGsonConverterFactory(): GsonConverterFactory {
            val gsonBuilder = GsonBuilder().registerTypeAdapter(
                SpannableString::class.java, SpannableDeserializer()
            )
            return GsonConverterFactory.create(gsonBuilder.create())
        }

//        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
//        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
//        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//        clientBuilder.addInterceptor(loggingInterceptor);
//
//        Retrofit retrofit = new Retrofit.Builder()
//        .baseUrl(NetworkCalls.BASE_URL)
//        .client(clientBuilder.build())
//        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
//        .addConverterFactory(GsonConverterFactory.create())
//        .build();

        private var httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host("trcom.com.ar")
            .build()

        fun create(): WebService = create(httpUrl)
        private fun create(httpUrl: HttpUrl): WebService {
            val client = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    this.level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
            return Retrofit.Builder()
                .baseUrl(httpUrl)
                .client(client)
                .addConverterFactory(buildGsonConverterFactory())
                .build()
                .create(WebService::class.java)
        }
    }
}