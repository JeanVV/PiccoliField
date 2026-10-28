package com.piccolifield;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.piccolifield.databinding.ActivityLoginBinding;
import com.piccolifield.sharedpreferences.SplashStorage;

import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.ResponseBody;
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
        apiService.iniciarSesion(new LoginRequest(email, password)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                binding.btnIniciarSesion.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String rawJson = response.body().string();
                        Log.d("LoginActivity", "Respuesta Server Login: " + rawJson);

                        JSONObject json = new JSONObject(rawJson);
                        String token = "";

                        // Extraer token de cualquier formato que devuelva Flask
                        if (json.has("data")) {
                            Object dataObj = json.get("data");
                            if (dataObj instanceof JSONObject) {
                                JSONObject dataJson = (JSONObject) dataObj;
                                token = dataJson.optString("access_token", dataJson.optString("token", ""));
                            } else if (dataObj instanceof String) {
                                token = (String) dataObj;
                            }
                        }

                        if (token.isEmpty() && json.has("access_token")) {
                            token = json.getString("access_token");
                        }

                        if (token.isEmpty() && json.has("token")) {
                            token = json.getString("token");
                        }

                        // Si encontramos el token o la respuesta de Flask es HTTP 200
                        if (!token.isEmpty() || response.code() == 200) {
                            if (!token.isEmpty()) {
                                RetrofitClient.API_TOKEN = token;
                                SplashStorage.guardarToken(LoginActivity.this, token);
                            }

                            // Obtener el perfil para saber exactamente el ROL antes de ir al Menú
                            obtenerPerfilYRol();

                            // Intentar guardar datos de usuario si vienen en la respuesta
                            if (json.has("user") || json.has("usuario") || json.has("data")) {
                                JSONObject userObj = json.optJSONObject("user");
                                if (userObj == null) userObj = json.optJSONObject("usuario");
                                if (userObj == null && json.opt("data") instanceof JSONObject) {
                                    userObj = json.optJSONObject("data");
                                }
                                if (userObj != null) {
                                    String rol = userObj.optString("rol", userObj.optString("role", ""));
                                    int idUsuario = userObj.optInt("idUsuario", userObj.optInt("id_usuario", userObj.optInt("id", 0)));
                                    String nombre = userObj.optString("nombres", userObj.optString("nombre", ""));
                                    String correo = userObj.optString("correo", userObj.optString("email", ""));

                                    SplashStorage.guardarRol(LoginActivity.this, rol);
                                    SplashStorage.guardarUsuarioId(LoginActivity.this, idUsuario);
                                    SplashStorage.guardarNombreUsuario(LoginActivity.this, nombre);
                                    SplashStorage.guardarCorreoUsuario(LoginActivity.this, correo);
                                }
                            }

                            SplashStorage.registrarSesionActiva(LoginActivity.this);

                            Toast.makeText(LoginActivity.this, "¡Bienvenido a Piccoli!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                            startActivity(intent);
                            finish();
                            return;
                        }

                        binding.btnIniciarSesion.setEnabled(true);
                        String msg = json.optString("message", json.optString("msg", "Error al iniciar sesión"));
                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        Log.e("LoginActivity", "Error al procesar JSON de login", e);
                        Toast.makeText(LoginActivity.this, "Error en el formato de respuesta del servidor", Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMsg = "Credenciales incorrectas";
                    if (response.errorBody() != null) {
                        try {
                            String errorJson = response.errorBody().string();
                            Log.e("LoginActivity", "Error Body: " + errorJson);
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
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                binding.btnIniciarSesion.setEnabled(true);
                Log.e("LoginActivity", "Error de red al iniciar sesión", t);

                String errorText;
                if (t instanceof UnknownHostException) {
                    errorText = "Sin conexión a Internet o servidor no disponible.";
                } else if (t instanceof SocketTimeoutException) {
                    errorText = "El servidor en Render tardó demasiado en responder. Intenta de nuevo.";
                } else {
                    errorText = "Error de conexión: " + (t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName());
                }

                Toast.makeText(LoginActivity.this, errorText, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void obtenerPerfilYRol() {
        ApiService apiService = RetrofitClient.createService();
        apiService.obtenerPerfil().enqueue(new Callback<com.piccolifield.data.remote.dto.response.ApiResponse<com.piccolifield.data.model.Usuario>>() {
            @Override
            public void onResponse(@NonNull Call<com.piccolifield.data.remote.dto.response.ApiResponse<com.piccolifield.data.model.Usuario>> call, @NonNull Response<com.piccolifield.data.remote.dto.response.ApiResponse<com.piccolifield.data.model.Usuario>> response) {
                binding.btnIniciarSesion.setEnabled(true);

                String rol = "SUPERVISOR"; // Default por seguridad
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    rol = response.body().getData().getRol();
                }

                Log.d("LoginActivity", "Rol guardado en memoria: " + rol);

                SplashStorage.registrarSesionActiva(LoginActivity.this, rol);
                Toast.makeText(LoginActivity.this, "¡Bienvenido a Piccoli!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<com.piccolifield.data.remote.dto.response.ApiResponse<com.piccolifield.data.model.Usuario>> call, @NonNull Throwable t) {
                binding.btnIniciarSesion.setEnabled(true);
                // Aún si falla el perfil, dejamos pasar con el rol por defecto
                Log.e("LoginActivity", "Fallo al obtener perfil", t);
                SplashStorage.registrarSesionActiva(LoginActivity.this, "SUPERVISOR");
                Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}