package com.example.safelink.network;

import com.example.safelink.models.ApiResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {

    /**
     * GET /api/v3/urls/{id}
     *
     * @param urlId Base64url encoded URL string without padding.
     * Headers (like x-apikey) are added globally via OkHttp interceptor in RetrofitInstance.
     */
    @GET("urls/{id}")
    Call<ApiResponse> checkUrl(@Path("id") String urlId);
}
