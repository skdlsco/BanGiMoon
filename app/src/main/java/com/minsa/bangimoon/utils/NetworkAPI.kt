package com.minsa.bangimoon.utils

/**
 * Created by eka on 2018. 4. 1..
 */
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

/**
 * Created by eka on 2018. 2. 23..
 */
interface NetworkAPI {
    @GET("/getData")
    fun getDevices(): Call<ResponseBody>
}