package com.minsa.bangimoon.utils

/**
 * Created by eka on 2018. 4. 1..
 */

import android.content.Context
import android.net.ConnectivityManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by eka on 2018. 2. 23..
 */
object NetworkHelper {
    private const val url = "http://soylatte.kr:4102/"

    private var retrofit: Retrofit? = null
    val retrofitInstance: NetworkAPI
        get() {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                        .baseUrl(url)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
            }
            return retrofit!!.create<NetworkAPI>(NetworkAPI::class.java)
        }

    fun returnNetworkState(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo.isConnected
    }
}