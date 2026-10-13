package com.piccolifield;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.piccolifield.databinding.ActivityMainBinding;
import com.piccolifield.sharedpreferences.SplashStorage;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ObjectAnimator floatingAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Iconos oscuros legibles en la barra de estado y de navegación sobre el fondo claro
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);

        ViewCompat.setOnApplyWindowInsetsListener(binding.contentContainer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomPadding = Math.max(systemBars.bottom, imeInsets.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);
            return insets;
        });

        boolean yaIngreso = SplashStorage.consultarIngreso(this);

        if (!yaIngreso) {
            // Primera vez: mostrar botón Comenzar y esperar click
            binding.btnComenzar.setVisibility(View.VISIBLE);
            ejecutarAnimacionesEntrada(true);

            binding.btnComenzar.setOnClickListener(v -> {
                // Registrar que ya ingresó por primera vez
                SplashStorage.registrarIngreso(MainActivity.this);

                // Transición al LoginActivity
                irALogin();
            });
        } else {
            // Ya instaló e ingresó antes: ocultar botón y redirigir tras splash
            binding.btnComenzar.setVisibility(View.GONE);
            ejecutarAnimacionesEntrada(false);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                boolean sesionActiva = SplashStorage.consultarSesionActiva(MainActivity.this);
                Intent intent = new Intent(MainActivity.this, sesionActiva ? MenuActivity.class : LoginActivity.class);
                startActivity(intent);
                finish();
            }, 2300);
        }
    }

    private void ejecutarAnimacionesEntrada(boolean mostrarBoton) {
        // Animación del Logo: Fade-in + Desplazamiento desde arriba
        binding.imgLogo.setAlpha(0f);
        binding.imgLogo.setTranslationY(-60f);
        binding.imgLogo.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(900)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Animación de las Botellas: Fade-in + Escala suave
        binding.imgBottles.setAlpha(0f);
        binding.imgBottles.setScaleX(0.85f);
        binding.imgBottles.setScaleY(0.85f);
        binding.imgBottles.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1100)
                .setStartDelay(200)
                .setInterpolator(new OvershootInterpolator(1.1f))
                .withEndAction(this::iniciarAnimacionFlotante)
                .start();

        // Animación del Botón Comenzar
        if (mostrarBoton) {
            binding.btnComenzar.setAlpha(0f);
            binding.btnComenzar.setTranslationY(80f);
            binding.btnComenzar.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .setStartDelay(600)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    private void iniciarAnimacionFlotante() {
        if (binding == null) return;

        // Animación continua de flotación sutil para las botellas
        floatingAnimator = ObjectAnimator.ofFloat(binding.imgBottles, "translationY", 0f, -22f, 0f);
        floatingAnimator.setDuration(2600);
        floatingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        floatingAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        floatingAnimator.start();
    }

    private void irALogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (floatingAnimator != null && floatingAnimator.isRunning()) {
            floatingAnimator.cancel();
        }
        binding = null;
    }
}