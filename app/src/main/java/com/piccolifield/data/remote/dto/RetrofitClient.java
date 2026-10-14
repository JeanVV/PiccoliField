package com.piccolifield.data.remote.dto;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.piccolifield.data.remote.api.ApiService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    public static final String URL_API_SERVICE = "https://piccolifield-api.onrender.com/api/";
    public static String API_TOKEN;

    private static class AuthInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");

            String token = API_TOKEN;
            if (token != null && !token.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }

            Request request = requestBuilder.build();
            return chain.proceed(request);
        }
    }

    private static class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Log.d("RetrofitClient", "--> " + request.method() + " " + request.url());
            try {
                Response response = chain.proceed(request);
                Log.d("RetrofitClient", "<-- " + response.code() + " " + response.message() + " (" + response.request().url() + ")");
                return response;
            } catch (IOException e) {
                Log.e("RetrofitClient", "<-- FAILED " + request.url() + ": " + e.getMessage());
                throw e;
            }
        }
    }

    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(new LoggingInterceptor())
            .addInterceptor(new AuthInterceptor())
            .build();

    public static final Retrofit API_SERVICE = new Retrofit.Builder()
            .baseUrl(URL_API_SERVICE)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static ApiService createService() {
        return API_SERVICE.create(ApiService.class);
    }
}