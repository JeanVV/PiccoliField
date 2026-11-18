package com.piccolifield.data.remote;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.piccolifield.data.remote.api.ApiService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    public static final String BASE_SERVER_URL = "https://piccolifield-api-smkc.onrender.com/";
    public static final String URL_API_SERVICE = "https://piccolifield-api-smkc.onrender.com/api/";
    public static String API_TOKEN;

    private static class AuthInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .header("Accept", "application/json, image/*, */*")
                    .header("X-Operation-Id", java.util.UUID.randomUUID().toString());

            boolean esMultipart = false;
            if (original.body() != null && original.body().contentType() != null) {
                MediaType contentType = original.body().contentType();
                if ("multipart".equalsIgnoreCase(contentType.type())) {
                    esMultipart = true;
                }
            }
            if (!esMultipart && original.body() != null && original.header("Content-Type") == null) {
                requestBuilder.header("Content-Type", "application/json");
            }

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
