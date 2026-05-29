package com.example.safelink.network;

public class ApiClient {

    public static final String API_KEY = com.example.safelink.BuildConfig.VIRUSTOTAL_API_KEY;

    private static ApiService apiService = null;

    public static ApiService getService() {
        if (apiService == null) {
            apiService = RetrofitInstance.getInstance().create(ApiService.class);
        }
        return apiService;
    }
}
