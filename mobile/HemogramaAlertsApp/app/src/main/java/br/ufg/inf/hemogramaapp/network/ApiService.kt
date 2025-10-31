package br.ufg.inf.hemogramaapp.network

import br.ufg.inf.hemogramaapp.model.Alert
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
interface ApiService {
    @GET("alerts")
    suspend fun getAlerts(): List<Alert>

    companion object {
        private const val BASE_URL = "http://10.0.2.2:8080/hemograma-api/"
        // 10.0.2.2 = localhost do computador host (para o emulador Android)
        fun create(): ApiService {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(ApiService::class.java)
        }
    }
}
