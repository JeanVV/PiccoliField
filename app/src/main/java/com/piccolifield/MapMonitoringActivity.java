package com.piccolifield;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.piccolifield.databinding.ActivityMapMonitoringBinding;

public class MapMonitoringActivity extends AppCompatActivity {

    private ActivityMapMonitoringBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMapMonitoringBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(true);
            controller.setAppearanceLightNavigationBars(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.mapMonitoringRootLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        setupUI();
    }

    private void setupUI() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_map);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, AttendanceHomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_map) {
                return true;
            } else if (itemId == R.id.nav_collaborators) {
                startActivity(new Intent(this, CollaboratorsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_reports) {
                Toast.makeText(this, "Reportes", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        binding.btnViewDetails.setOnClickListener(v -> 
            startActivity(new Intent(this, AttendanceDetailActivity.class))
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
