package com.sagar.networkdi

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.sagar.android.logutilmaster.LogUtil
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NetworkModule<T>(
    logUtil: LogUtil,
    private val baseUrl: String,
    private val connectionTimeoutSeconds: Long = 120L,
    private val createApiInterface: () -> Class<T>
) {

    var apiInterface: T

    init {
        apiInterface = getApiInterface(
            getRetrofit(
                getOkHttpClient(
                    getHttpLoggingInterceptor(
                        logUtil
                    )
                )
            )
        )
    }

    private fun getHttpLoggingInterceptor(logUtil: LogUtil): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor(
            object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    logUtil.logV(message)
                }
            }
        )
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }

    private fun getOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor
    ) =
        OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .connectTimeout(connectionTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(connectionTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(connectionTimeoutSeconds, TimeUnit.SECONDS)
            .build()

    private fun getRetrofit(okHttpClient: OkHttpClient) =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .build()

    private fun getApiInterface(retrofit: Retrofit) = retrofit.create(createApiInterface())
}