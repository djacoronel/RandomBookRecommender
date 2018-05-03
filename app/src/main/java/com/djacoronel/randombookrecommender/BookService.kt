package com.djacoronel.randombookrecommender

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by djacoronel on 5/3/18.
 */

interface BookService {
    @GET("volumes")
    fun queryBooks(
            @Query("q") keyword: String,
            @Query("startIndex") startIndex: Int
    ): Single<BooksResponse>
}
