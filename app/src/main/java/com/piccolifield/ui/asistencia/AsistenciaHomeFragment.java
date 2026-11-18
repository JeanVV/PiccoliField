package com.piccolifield.ui.asistencia;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.piccolifield.R;
import com.piccolifield.adapter.RecentPersonnelAdapter;
import com.piccolifield.data.model.RegistroAsistencia;
import com.piccolifield.data.remote.Resource;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.databinding.FragmentAsistenciaInicioBinding;
import com.piccolifield.sharedpreferences.SplashStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AsistenciaHomeFragment extends Fragment {

    private FragmentAsistenciaInicioBinding binding;
    private AsistenciaViewModel viewModel;
    private RecentPersonnelAdapter adapter;
    private String estadoFiltroActual = "TODOS";

    public AsistenciaHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAsistenciaInicioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Asegurar que el token de autenticación esté cargado
        if (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty()) {
            RetrofitClient.API_TOKEN = SplashStorage.obtenerToken(requireContext());
        }

        // 2. Inicializar ViewModel y suscribirse a observadores MVVM
        viewModel = new ViewModelProvider(requireActivity()).get(AsistenciaViewModel.class);
        observarViewModel();

        // 3. Configurar eventos de navegación interna y búsqueda
        setupListeners();

        // 4. Solicitar carga de datos al ViewModel
        cargarDatos();
    }

    private void observarViewModel() {
        viewModel.getColaboradoresLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (!isAdded() || binding == null || resource == null) return;

            switch (resource.status) {
                case LOADING:
                    if (binding.pbLoadingHome != null) binding.pbLoadingHome.setVisibility(View.VISIBLE);
                    break;

                case SUCCESS:
                    if (binding.pbLoadingHome != null) binding.pbLoadingHome.setVisibility(View.GONE);
                    List<RegistroAsistencia> listaRemote = resource.data != null ? resource.data : new ArrayList<>();

                    verificarJustificacionesPendientes(listaRemote);
                    actualizarMetricasFaltas(listaRemote);

                    if (listaRemote.isEmpty()) {
                        binding.layoutEmptyRecentState.setVisibility(View.VISIBLE);
                        binding.rvRecentPersonnel.setVisibility(View.GONE);
                    } else {
                        binding.layoutEmptyRecentState.setVisibility(View.GONE);
                        binding.rvRecentPersonnel.setVisibility(View.VISIBLE);
                        binding.rvRecentPersonnel.setLayoutManager(new LinearLayoutManager(getContext()));

                        adapter = new RecentPersonnelAdapter(listaRemote);
                        adapter.setOnItemClickListener(item -> {
                            DetalleAsistenciaFragment fragment = DetalleAsistenciaFragment.newInstance(item);
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragmentContainer, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        });
                        binding.rvRecentPersonnel.setAdapter(adapter);
                    }
                    break;

                case ERROR:
                    if (binding.pbLoadingHome != null) binding.pbLoadingHome.setVisibility(View.GONE);
                    Log.e("AsistenciaHome", "Error cargando personal: " + resource.message);
                    binding.layoutEmptyRecentState.setVisibility(View.VISIBLE);
                    binding.rvRecentPersonnel.setVisibility(View.GONE);
                    break;
            }
        });
    }

    private void setupListeners() {
        // Botón Retroceder (Flecha arriba izquierda)
        if (binding.btnBack != null) {
            binding.btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else if (getActivity() instanceof com.piccolifield.MenuActivity) {
                    ((com.piccolifield.MenuActivity) getActivity()).navegarAPantallaPrincipal();
                }
            });
        }

        // Botón/Card Ver Mapa
        if (binding.cardGoToMap != null) {
            binding.cardGoToMap.setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new MapaFragment())
                    .addToBackStack(null)
                    .commit()
            );
        }

        // Botón/Card Colaboradores
        if (binding.cardGoToCollaborators != null) {
            binding.cardGoToCollaborators.setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ColaboradoresFragment())
                    .addToBackStack(null)
                    .commit()
            );
        }

        // Botón Notificación
        if (binding.btnNotification != null) {
            binding.btnNotification.setOnClickListener(v ->
                Toast.makeText(getContext(), "Notificaciones de asistencias", Toast.LENGTH_SHORT).show()
            );
        }

        // Búsqueda en tiempo real
        if (binding.etSearchCollaborator != null) {
            binding.etSearchCollaborator.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) {
                        adapter.filtrar(s.toString(), estadoFiltroActual);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Botón Filtros
        View btnFiltroTarget = binding.btnFiltrosContainer != null ? binding.btnFiltrosContainer : binding.btnFilter;
        if (btnFiltroTarget != null) {
            btnFiltroTarget.setOnClickListener(this::mostrarPopupMenuFiltros);
        }

        // Filtros dinámicos al hacer clic en las tarjetas de métricas
        if (binding.cardOnTime != null) {
            binding.cardOnTime.setOnClickListener(v -> {
                estadoFiltroActual = "A_TIEMPO";
                if (adapter != null) adapter.filtrar(binding.etSearchCollaborator != null ? binding.etSearchCollaborator.getText().toString() : "", estadoFiltroActual);
            });
        }
        if (binding.cardLate != null) {
            binding.cardLate.setOnClickListener(v -> {
                estadoFiltroActual = "TARDANZA";
                if (adapter != null) adapter.filtrar(binding.etSearchCollaborator != null ? binding.etSearchCollaborator.getText().toString() : "", estadoFiltroActual);
            });
        }
        if (binding.cardFaltas != null) {
            binding.cardFaltas.setOnClickListener(v -> {
                estadoFiltroActual = "FALTA";
                if (adapter != null) adapter.filtrar(binding.etSearchCollaborator != null ? binding.etSearchCollaborator.getText().toString() : "", estadoFiltroActual);
            });
        }
        if (binding.cardSinMarcar != null) {
            binding.cardSinMarcar.setOnClickListener(v -> {
                estadoFiltroActual = "SIN_REGISTRO";
                if (adapter != null) adapter.filtrar(binding.etSearchCollaborator != null ? binding.etSearchCollaborator.getText().toString() : "", estadoFiltroActual);
            });
        }
        if (binding.cardTodaySummary != null) {
            binding.cardTodaySummary.setOnClickListener(v -> {
                estadoFiltroActual = "TODOS";
                if (adapter != null) adapter.filtrar(binding.etSearchCollaborator != null ? binding.etSearchCollaborator.getText().toString() : "", estadoFiltroActual);
            });
        }
    }

    private void mostrarPopupMenuFiltros(View view) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenu().add(0, 1, 0, "Todos los estados");
        popup.getMenu().add(0, 2, 1, "A tiempo");
        popup.getMenu().add(0, 3, 2, "Tardanza");
        popup.getMenu().add(0, 4, 3, "Falta");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    estadoFiltroActual = "TODOS";
                    break;
                case 2:
                    estadoFiltroActual = "A_TIEMPO";
                    break;
                case 3:
                    estadoFiltroActual = "TARDANZA";
                    break;
                case 4:
                    estadoFiltroActual = "FALTA";
                    break;
            }

            if (adapter != null && binding.etSearchCollaborator != null) {
                adapter.filtrar(binding.etSearchCollaborator.getText().toString(), estadoFiltroActual);
            }
            return true;
        });

        popup.show();
    }

    private void cargarDatos() {
        if (viewModel != null && isAdded()) {
            viewModel.cargarColaboradoresConAsistencia(requireContext());
        }
    }

    private void verificarJustificacionesPendientes(List<RegistroAsistencia> lista) {
        if (binding == null) return;
        int countPendientes = 0;

        if (lista != null) {
            for (RegistroAsistencia item : lista) {
                boolean tieneJustificacion = (item.getJustificacion() != null && !item.getJustificacion().trim().isEmpty())
                        || (item.getMotivoJustificacion() != null && !item.getMotivoJustificacion().trim().isEmpty());
                String estJ = item.getEstadoJustificacion();
                boolean esPendiente = estJ == null || estJ.trim().isEmpty() || "PENDIENTE".equalsIgnoreCase(estJ) || "ENVIADA".equalsIgnoreCase(estJ);

                if (tieneJustificacion && esPendiente) {
                    countPendientes++;
                }
            }
        }

        if (countPendientes > 0) {
            binding.cardPendingJustificationsBanner.setVisibility(View.VISIBLE);
            binding.tvPendingJustificationsTitle.setText("Revisar Justificaciones (" + countPendientes + ")");
            binding.tvPendingJustificationsCount.setText("Tienes " + countPendientes + " colaborador(es) con justificación por revisar.");
            View.OnClickListener abrirModuloRevision = v -> {
                ColaboradoresFragment fragment = ColaboradoresFragment.newInstance("JUSTIFICACIONES");
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            };
            binding.btnFiltrarPendientes.setOnClickListener(abrirModuloRevision);
            binding.cardPendingJustificationsBanner.setOnClickListener(abrirModuloRevision);
        } else {
            binding.cardPendingJustificationsBanner.setVisibility(View.GONE);
        }
    }

    private static final TimeZone TIME_ZONE_LIMA = TimeZone.getTimeZone("America/Lima");
    private static final Locale LOCALE_PERU = new Locale("es", "PE");

    private void actualizarMetricasFaltas(List<RegistroAsistencia> lista) {
        if (binding == null || lista == null) return;

        SimpleDateFormat sdfFecha = new SimpleDateFormat("yyyy-MM-dd", LOCALE_PERU);
        sdfFecha.setTimeZone(TIME_ZONE_LIMA);
        String hoyStr = sdfFecha.format(new Date());

        String fechaReferencia = hoyStr;
        for (RegistroAsistencia item : lista) {
            String f = item.getFechaHoraServidor();
            if (f == null || f.trim().isEmpty()) f = item.getHoraMarcado();
            if (f != null && f.length() >= 10 && !f.contains("--:--")) {
                fechaReferencia = f.substring(0, 10);
                break;
            }
        }

        int countATiempo = 0;
        int countTardanzas = 0;
        int countFaltas = 0;
        int countSinMarcar = 0;

        for (RegistroAsistencia item : lista) {
            String fechaReg = item.getFechaHoraServidor();
            if (fechaReg == null || fechaReg.trim().isEmpty()) {
                fechaReg = item.getHoraMarcado();
            }

            boolean tieneFecha = (fechaReg != null && !fechaReg.trim().isEmpty() && !fechaReg.contains("--:--"));
            boolean esDeHoy = tieneFecha && (fechaReg.startsWith(fechaReferencia) || fechaReg.startsWith(hoyStr));

            if (esDeHoy) {
                String estadoReal = item.getEstado();
                if (fechaReg.length() >= 19) {
                    String horaOnly = fechaReg.substring(11, 19);
                    if (horaOnly.compareTo("10:30:00") > 0) {
                        estadoReal = "FALTA";
                    } else if (horaOnly.compareTo("09:40:00") > 0) {
                        estadoReal = "TARDANZA";
                    } else {
                        estadoReal = "A_TIEMPO";
                    }
                } else if (estadoReal == null || estadoReal.trim().isEmpty()) {
                    estadoReal = "A_TIEMPO";
                }
                item.setEstado(estadoReal);

                if ("A_TIEMPO".equalsIgnoreCase(estadoReal)) {
                    countATiempo++;
                } else if ("TARDANZA".equalsIgnoreCase(estadoReal)) {
                    countTardanzas++;
                } else if ("FALTA".equalsIgnoreCase(estadoReal)) {
                    countFaltas++;
                } else {
                    countSinMarcar++;
                }
            } else {
                item.setEstado("SIN_REGISTRO");
                countSinMarcar++;
            }
        }

        int presentes = countATiempo + countTardanzas;
        int total = Math.max(30, lista.size());
        if (countSinMarcar == 0 && total > (presentes + countFaltas)) {
            countSinMarcar = total - presentes - countFaltas;
        }

        int porcentaje = total > 0 ? ((presentes * 100) / total) : 0;
        binding.pbAttendancePercentage.setProgress(porcentaje);
        binding.tvAttendancePercentage.setText(porcentaje + "%");
        binding.tvPresentesFraction.setText(presentes + "/" + total);

        if (binding.tvOnTimeCount != null) binding.tvOnTimeCount.setText("(" + countATiempo + ")");
        if (binding.tvLateCount != null) binding.tvLateCount.setText("(" + countTardanzas + ")");
        if (binding.tvFaltasCount != null) binding.tvFaltasCount.setText("(" + countFaltas + ")");
        if (binding.tvSinMarcarCount != null) binding.tvSinMarcarCount.setText("(" + countSinMarcar + ")");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
