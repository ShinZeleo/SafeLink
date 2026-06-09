package com.example.safelink.network;

import com.example.safelink.models.ApiResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
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

    /**
     * POST /api/v3/urls
     * Submit a new URL for scanning.
     */
    @FormUrlEncoded
    @POST("urls")
    Call<ResponseBody> submitUrl(@Field("url") String url);
}
