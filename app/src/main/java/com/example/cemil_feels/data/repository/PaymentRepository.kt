package com.example.cemil_feels.data.repository

import com.example.cemil_feels.RetrofitClient
import com.example.cemil_feels.SnapTokenRequest
import com.example.cemil_feels.SnapTokenResponse
import java.io.IOException

sealed class PaymentResult {
    data class Success(val token: String, val redirectUrl: String) : PaymentResult()
    data class Error(val message: String) : PaymentResult()
    object NetworkTimeout : PaymentResult()
}

class PaymentRepository {

    suspend fun getSnapToken(
        orderId: String,
        amount: Int,
        customerName: String
    ): PaymentResult {
        return try {
            val response = RetrofitClient.merchantApiService.getSnapToken(
                SnapTokenRequest(orderId, amount.toLong(), customerName)
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && !body.token.isNullOrEmpty()) {
                    PaymentResult.Success(
                        token = body.token,
                        redirectUrl = body.redirect_url ?: ""
                    )
                } else {
                    PaymentResult.Error(body?.message ?: "Response tidak valid dari server")
                }
            } else {
                PaymentResult.Error("Server error: ${response.code()} - ${response.message()}")
            }

        } catch (e: java.net.SocketTimeoutException) {
            PaymentResult.NetworkTimeout
        } catch (e: java.net.ConnectException) {
            PaymentResult.Error("Tidak bisa terhubung ke server. Pastikan koneksi internet aktif.")
        } catch (e: IOException) {
            PaymentResult.Error("Gagal terhubung: ${e.message}")
        } catch (e: Exception) {
            PaymentResult.Error("Error tidak diketahui: ${e.message}")
        }
    }
}