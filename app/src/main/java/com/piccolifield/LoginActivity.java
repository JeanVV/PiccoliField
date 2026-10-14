package com.piccolifield;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.RetrofitClient;
import com.piccolifield.data.remote.dto.request.LoginRequest;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.data.remote.dto.response.LoginResponse;
import com.piccolifield.databinding.ActivityLoginBinding;
import com.piccolifield.sharedpreferences.SplashStorage;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);

        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollContainer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomPadding = Math.max(systemBars.bottom, imeInsets.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);
            return insets;
        });

        binding.btnIniciarSesion.setOnClickListener(v -> realizarLogin());
    }

    private void realizarLogin() {
        String email = binding.etUser.getText() != null ? binding.etUser.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnIniciarSesion.setEnabled(false);

        ApiService apiService = RetrofitClient.createService();
        apiService.iniciarSesion(new LoginRequest(email, password)).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<LoginResponse>> call, @NonNull Response<ApiResponse<LoginResponse>> response) {
                binding.btnIniciarSesion.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<LoginResponse> apiResponse = response.body();

                    String token = "";
                    if (apiResponse.getData() != null && apiResponse.getData().getToken() != null) {
                        token = apiResponse.getData().getToken();
                    }

                    if (!token.isEmpty()) {
                        RetrofitClient.API_TOKEN = token;
                        SplashStorage.guardarToken(LoginActivity.this, token);
                    }

                    SplashStorage.registrarSesionActiva(LoginActivity.this);

                    Toast.makeText(LoginActivity.this, "¡Bienvenido a Piccoli!", Toast.LENGTH_SHORT).show();

                    // Navegar directamente al MenuActivity
                    Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Correo o contraseña incorrectos";
                    if (response.errorBody() != null) {
                        try {
                            String errorJson = response.errorBody().string();
                            JSONObject jsonObject = new JSONObject(errorJson);
                            if (jsonObject.has("message")) {
                                errorMsg = jsonObject.getString("message");
                            } else if (jsonObject.has("msg")) {
                                errorMsg = jsonObject.getString("msg");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<LoginResponse>> call, @NonNull Throwable t) {
                binding.btnIniciarSesion.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}