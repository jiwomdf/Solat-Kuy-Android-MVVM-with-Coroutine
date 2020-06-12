package com.programmergabut.solatkuy.data.remote

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/*
 * Created by Katili Jiwo Adi Wiyono on 02/06/20.
 */

class RetrofitBuilder {

    companion object{
        inline fun <reified T> build(strApi: String): T {
            return Retrofit.Builder()
                .baseUrl(strApi)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .build()
                .create(T::class.java)
        }
    }

}