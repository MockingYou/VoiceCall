package com.telpo.voicecall.APIs

import retrofit2.Response
import retrofit2.http.GET

interface MyApi {
    @GET("/get_apartments")
    suspend fun getApartments(): Response<List<GetApartments>>
}