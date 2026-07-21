package com.example.cemil_feels

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// ── Data yang dikirim Android → Backend ──
data class SnapTokenRequest(
    val order_id: String,
    val amount: Long,
    val customer_name: String
)

// ── Data yang diterima dari Backend ──
data class SnapTokenResponse(
    val success: Boolean,
    val token: String?,
    val order_id: String?
)

// ── Daftar endpoint yang tersedia ──
interface MerchantApiService {
    @POST("api/payment/snap-token")
    suspend fun getSnapToken(@Body request: SnapTokenRequest): SnapTokenResponse
}

// ── Singleton koneksi ke backend ──
object RetrofitClient {

    private val logging: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    val merchantApiService: MerchantApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.MERCHANT_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MerchantApiService::class.java)
    }
}
