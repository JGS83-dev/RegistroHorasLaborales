package com.dsm.registro.biometrico.service

import com.dsm.registro.biometrico.clases.DataPost
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface MailerApiService {
    @POST("mailer")
    fun callMailer(@Body dataPost: DataPost): Call<ResponseBody>
}