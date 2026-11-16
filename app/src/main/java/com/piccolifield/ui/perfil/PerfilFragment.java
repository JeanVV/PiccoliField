package com.piccolifield.ui.perfil;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.piccolifield.MenuActivity;
import com.piccolifield.R;
import com.piccolifield.data.model.Usuario;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.databinding.FragmentPerfilBinding;
import com.piccolifield.sharedpreferences.SplashStorage;
import com.piccolifield.ui.CurvedBottomNavigationView;
import com.piccolifield.util.UserPhotoLoader;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private FragmentPerfilBinding binding;
    private Uri cameraPhotoUri;

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    abrirCamaraParaFoto();
                } else {
                    Toast.makeText(getContext(), "Se requiere permiso de cámara para tomar una foto", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraPhotoUri != null) {
                    subirFotoPerfil(cameraPhotoUri);
                }
            });

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    subirFotoPerfil(uri);
                }
            });

    public PerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
            RetrofitClient.API_TOKEN = SplashStorage.obtenerToken(requireContext());
        }

        actualizarFechaYHora();
        cargarDatosPerfil();
        configurarBotonesNavegacion();

        View.OnClickListener cambiarFotoListener = v -> mostrarOpcionesCambiarFoto();
        binding.ivFotoPerfilHome.setOnClickListener(cambiarFotoListener);
        binding.flFotoPerfilHome.setOnClickListener(cambiarFotoListener);
    }

    private void mostrarOpcionesCambiarFoto() {
        if (!isAdded()) return;
        String[] opciones = {"Tomar foto con la cámara", "Elegir foto de la galería", "Ir a Mi Perfil"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Foto de Perfil")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            abrirCamaraParaFoto();
                        } else {
                            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                        }
                    } else if (which == 1) {
                        pickImageLauncher.launch("image/*");
                    } else if (which == 2) {
                        navegarDesdeElMenu(4);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void abrirCamaraParaFoto() {
        try {
            File photoFile = new File(requireContext().getCacheDir(), "home_perfil_cam_" + System.currentTimeMillis() + ".jpg");
            cameraPhotoUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile
            );
            takePictureLauncher.launch(cameraPhotoUri);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al abrir cámara: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void subirFotoPerfil(Uri uri) {
        if (!isAdded() || binding == null) return;
        Toast.makeText(getContext(), "Actualizando foto de perfil...", Toast.LENGTH_SHORT).show();
        int idUsuario = SplashStorage.obtenerUsuarioId(requireContext());
        UserPhotoLoader.subirFotoUsuario(requireContext(), idUsuario, uri, true, new UserPhotoLoader.OnFotoActualizadaListener() {
            @Override
            public void onSuccess(Bitmap nuevoBitmap, String nuevaUrlFoto, String mensaje) {
                if (!isAdded() || binding == null) return;
                if (nuevoBitmap != null) {
                    ImageViewCompat.setImageTintList(binding.ivFotoPerfilHome, null);
                    binding.ivFotoPerfilHome.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    binding.ivFotoPerfilHome.setImageBitmap(nuevoBitmap);
                }
                Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String mensajeError) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), mensajeError, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void configurarBotonesNavegacion() {
        binding.cardSupervision.setOnClickListener(v -> navegarDesdeElMenu(2));
        binding.cardAsistencias.setOnClickListener(v -> navegarDesdeElMenu(1));
        binding.cardPuntosDespacho.setOnClickListener(v -> navegarDesdeElMenu(3));
        binding.cardProspectos.setOnClickListener(v -> navegarDesdeElMenu(4));
    }

    private void navegarDesdeElMenu(int index) {
        if (getActivity() instanceof MenuActivity) {
            MenuActivity menuActivity = (MenuActivity) getActivity();
            CurvedBottomNavigationView bottomNav = menuActivity.findViewById(R.id.curvedBottomNav);
            if (bottomNav != null) {
                bottomNav.selectTab(index, true);
            }
        }
    }

    private void actualizarFechaYHora() {
        Date currentDate = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-ES"));
        String fecha = dateFormat.format(currentDate);
        fecha = fecha.substring(0, 1).toUpperCase() + fecha.substring(1);
        binding.tvFecha.setText(fecha);

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String hora = timeFormat.format(currentDate).toLowerCase();
        binding.tvHora.setText(hora);
    }

    private void cargarDatosPerfil() {
        String nombreGuardado = SplashStorage.obtenerNombreUsuario(requireContext());
        String rolGuardado = SplashStorage.obtenerRol(requireContext());

        if (nombreGuardado != null && !nombreGuardado.isEmpty()) {
            binding.tvNombre.setText(nombreGuardado);
        }
        if (rolGuardado != null && !rolGuardado.isEmpty()) {
            binding.tvRol.setText("Rol: " + rolGuardado);
        }

        // Cargar foto actual del usuario y sincronizar GET /api/perfil (con soporte Base64 directo)
        UserPhotoLoader.loadMyProfilePhoto(requireContext(), binding.ivFotoPerfilHome, R.drawable.ic_avatar_placeholder);
        UserPhotoLoader.sincronizarPerfilRaw(requireContext(), binding.ivFotoPerfilHome, R.drawable.ic_avatar_placeholder,
                (idUsuario, nombreCompleto, rol, correo, urlFoto) -> {
                    if (!isAdded() || binding == null) return;
                    if (nombreCompleto != null && !nombreCompleto.isEmpty()) {
                        binding.tvNombre.setText(nombreCompleto);
                    }
                    if (rol != null && !rol.isEmpty()) {
                        binding.tvRol.setText("Rol: " + rol);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
