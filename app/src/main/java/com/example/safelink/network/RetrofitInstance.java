package com.example.safelink.network;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitInstance {

    private static final String BASE_URL = "https://www.virustotal.com/api/v3/";

    private static volatile Retrofit retrofit = null;

    public static Retrofit getInstance() {
        if (retrofit == null) {
            synchronized (RetrofitInstance.class) {
                if (retrofit == null) {
                    // Only log in debug builds — never log API keys/body in release
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(com.example.safelink.BuildConfig.DEBUG
                            ? HttpLoggingInterceptor.Level.BASIC
                            : HttpLoggingInterceptor.Level.NONE);

                    OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(chain -> {
                            Request original = chain.request();
                            Request request = original.newBuilder()
                                .header("x-apikey", ApiClient.API_KEY)
                                .header("accept", "application/json")
                                .method(original.method(), original.body())
                                .build();
                            return chain.proceed(request);
                        })
                        .addInterceptor(logging)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build();

                    retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                }
            }
        }
        return retrofit;
    }
}
