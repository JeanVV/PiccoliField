package com.piccolifield.ui.perfil;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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

import com.piccolifield.LoginActivity;
import com.piccolifield.R;
import com.piccolifield.data.model.Usuario;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.sharedpreferences.SplashStorage;
import com.piccolifield.util.UserPhotoLoader;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MiPerfilFragment extends Fragment {

    private ImageView ivFotoPerfil;
    private View btnEditarFotoBadge;
    private Button btnCambiarFotoPerfil;
    private ProgressBar pbSubiendoFoto;
    private TextView tvNombrePerfil;
    private TextView tvRolPerfil;
    private TextView tvCorreoPerfil;
    private Button btnCerrarSesion;

    private Uri cameraPhotoUri;

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    abrirCamaraParaFotoPerfil();
                } else {
                    Toast.makeText(getContext(), "Se requiere permiso de cámara para tomar una foto", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraPhotoUri != null) {
                    subirNuevaFotoPerfil(cameraPhotoUri);
                }
            });

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    subirNuevaFotoPerfil(uri);
                }
            });

    public MiPerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mi_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivFotoPerfil = view.findViewById(R.id.ivFotoPerfil);
        btnEditarFotoBadge = view.findViewById(R.id.btnEditarFotoBadge);
        btnCambiarFotoPerfil = view.findViewById(R.id.btnCambiarFotoPerfil);
        pbSubiendoFoto = view.findViewById(R.id.pbSubiendoFoto);
        tvNombrePerfil = view.findViewById(R.id.tvNombrePerfil);
        tvRolPerfil = view.findViewById(R.id.tvRolPerfil);
        tvCorreoPerfil = view.findViewById(R.id.tvCorreoPerfil);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);

        if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
            RetrofitClient.API_TOKEN = SplashStorage.obtenerToken(requireContext());
        }

        cargarDatos();
        sincronizarPerfilConApi();

        View.OnClickListener selectorFotoListener = v -> mostrarOpcionesCambiarFoto();
        if (ivFotoPerfil != null) ivFotoPerfil.setOnClickListener(selectorFotoListener);
        if (btnEditarFotoBadge != null) btnEditarFotoBadge.setOnClickListener(selectorFotoListener);
        if (btnCambiarFotoPerfil != null) btnCambiarFotoPerfil.setOnClickListener(selectorFotoListener);

        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    private void cargarDatos() {
        String nombre = SplashStorage.obtenerNombreUsuario(requireContext());
        String rol = SplashStorage.obtenerRol(requireContext());
        String correo = SplashStorage.obtenerCorreoUsuario(requireContext());

        if (nombre != null && !nombre.isEmpty()) {
            tvNombrePerfil.setText(nombre);
        } else {
            tvNombrePerfil.setText("Usuario Registrado");
        }

        if (rol != null && !rol.isEmpty()) {
            tvRolPerfil.setText("Rol: " + rol);
        } else {
            tvRolPerfil.setText("Rol: No definido");
        }

        if (correo != null && !correo.isEmpty()) {
            tvCorreoPerfil.setText("Correo: " + correo);
        } else {
            tvCorreoPerfil.setText("Correo: No disponible");
        }

        if (ivFotoPerfil != null) {
            UserPhotoLoader.loadMyProfilePhoto(requireContext(), ivFotoPerfil, R.drawable.ic_avatar_placeholder);
        }
    }

    private void sincronizarPerfilConApi() {
        UserPhotoLoader.sincronizarPerfilRaw(requireContext(), ivFotoPerfil, R.drawable.ic_avatar_placeholder,
                (idUsuario, nombreCompleto, rol, correo, urlFoto) -> {
                    if (!isAdded()) return;
                    if (nombreCompleto != null && !nombreCompleto.isEmpty()) {
                        tvNombrePerfil.setText(nombreCompleto);
                    }
                    if (rol != null && !rol.isEmpty()) {
                        tvRolPerfil.setText("Rol: " + rol);
                    }
                    if (correo != null && !correo.isEmpty()) {
                        tvCorreoPerfil.setText("Correo: " + correo);
                    }
                });
    }

    private void mostrarOpcionesCambiarFoto() {
        if (!isAdded()) return;
        String[] opciones = {"Tomar foto con la cámara", "Elegir foto de la galería"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Actualizar foto de perfil")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        verificarPermisoYAbrirCamara();
                    } else if (which == 1) {
                        pickImageLauncher.launch("image/*");
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void verificarPermisoYAbrirCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamaraParaFotoPerfil();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void abrirCamaraParaFotoPerfil() {
        try {
            File photoFile = new File(requireContext().getCacheDir(), "perfil_cam_" + System.currentTimeMillis() + ".jpg");
            cameraPhotoUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile
            );
            takePictureLauncher.launch(cameraPhotoUri);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al abrir la cámara: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void subirNuevaFotoPerfil(Uri uri) {
        if (!isAdded()) return;
        if (pbSubiendoFoto != null) pbSubiendoFoto.setVisibility(View.VISIBLE);
        if (btnCambiarFotoPerfil != null) {
            btnCambiarFotoPerfil.setEnabled(false);
            btnCambiarFotoPerfil.setText("Actualizando foto...");
        }

        int idUsuario = SplashStorage.obtenerUsuarioId(requireContext());
        UserPhotoLoader.subirFotoUsuario(requireContext(), idUsuario, uri, true, new UserPhotoLoader.OnFotoActualizadaListener() {
            @Override
            public void onSuccess(Bitmap nuevoBitmap, String nuevaUrlFoto, String mensaje) {
                if (!isAdded()) return;
                if (pbSubiendoFoto != null) pbSubiendoFoto.setVisibility(View.GONE);
                if (btnCambiarFotoPerfil != null) {
                    btnCambiarFotoPerfil.setEnabled(true);
                    btnCambiarFotoPerfil.setText("Cambiar foto de perfil");
                }
                if (ivFotoPerfil != null && nuevoBitmap != null) {
                    ImageViewCompat.setImageTintList(ivFotoPerfil, null);
                    ivFotoPerfil.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    ivFotoPerfil.setImageBitmap(nuevoBitmap);
                }
                Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String mensajeError) {
                if (!isAdded()) return;
                if (pbSubiendoFoto != null) pbSubiendoFoto.setVisibility(View.GONE);
                if (btnCambiarFotoPerfil != null) {
                    btnCambiarFotoPerfil.setEnabled(true);
                    btnCambiarFotoPerfil.setText("Cambiar foto de perfil");
                }
                Toast.makeText(getContext(), mensajeError, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cerrarSesion() {
        UserPhotoLoader.clearCache();
        SplashStorage.limpiarSesionActiva(requireContext());
        RetrofitClient.API_TOKEN = null;

        Toast.makeText(getContext(), "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
