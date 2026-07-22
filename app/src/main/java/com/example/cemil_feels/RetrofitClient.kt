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
    val order_id: String?,
    val redirect_url: String?,
    val message: String?
)

// ── Daftar endpoint yang tersedia ──
interface MerchantApiService {
    @POST("api/payment/snap-token")
    suspend fun getSnapToken(@Body request: SnapTokenRequest): retrofit2.Response<SnapTokenResponse>
}

// ── Singleton koneksi ke backend ──
object RetrofitClient {

    private val logging: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            // ✅ FIX: Log body hanya saat debug build, none di release
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            // ✅ FIX: 60s untuk handle Render.com free tier cold start
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            // ✅ FIX: Total max call duration 90 detik
            .callTimeout(90, TimeUnit.SECONDS)
            // ✅ FIX: Retry otomatis jika koneksi awal gagal
            .retryOnConnectionFailure(true)
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
