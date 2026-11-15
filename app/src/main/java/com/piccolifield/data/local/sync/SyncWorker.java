package com.piccolifield.data.local.sync;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.piccolifield.MenuActivity;
import com.piccolifield.data.local.AppDatabase;
import com.piccolifield.data.local.entity.AsistenciaOfflineEntity;
import com.piccolifield.data.local.entity.IncidenciaOfflineEntity;
import com.piccolifield.data.local.entity.ReposicionOfflineEntity;
import com.piccolifield.data.local.entity.VentaOfflineEntity;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.request.MarcarAsistenciaRequest;
import com.piccolifield.data.remote.dto.request.RegistrarVentaRequest;
import com.piccolifield.data.remote.dto.request.ReportarIncidenciaRequest;
import com.piccolifield.data.remote.dto.request.ReponerStockRequest;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.sharedpreferences.SplashStorage;
import com.piccolifield.util.NotificationUtils;

import java.util.List;

import retrofit2.Response;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("SyncWorker", ">>> Iniciando sincronización de datos offline...");
        Context context = getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);

        String token = SplashStorage.obtenerToken(context);
        if (token != null && !token.isEmpty()) {
            RetrofitClient.API_TOKEN = token;
        }

        ApiService apiService = RetrofitClient.createService();
        int sincronizadosCount = 0;

        // 1. Sincronizar Ventas
        try {
            List<VentaOfflineEntity> ventas = db.ventaOfflineDao().obtenerPendientes();
            for (VentaOfflineEntity v : ventas) {
                Response<ApiResponse<Object>> res = apiService.registrarVenta(
                        new RegistrarVentaRequest(v.getCodigoBarras(), v.getCantidad())
                ).execute();

                if (res.isSuccessful()) {
                    db.ventaOfflineDao().eliminarPorId(v.getIdLocal());
                    sincronizadosCount++;
                    Log.d("SyncWorker", "Venta offline id " + v.getIdLocal() + " sincronizada.");
                }
            }
        } catch (Exception e) {
            Log.e("SyncWorker", "Error sincronizando ventas: " + e.getMessage());
        }

        // 2. Sincronizar Incidencias
        try {
            List<IncidenciaOfflineEntity> incidencias = db.incidenciaOfflineDao().obtenerPendientes();
            for (IncidenciaOfflineEntity inc : incidencias) {
                Response<ApiResponse<Object>> res = apiService.reportarIncidencia(
                        new ReportarIncidenciaRequest(inc.getTipo(), inc.getSeveridad(), inc.getDescripcion(), inc.getIdPuntosVenta(), inc.getFotoBase64())
                ).execute();

                if (res.isSuccessful()) {
                    db.incidenciaOfflineDao().eliminarPorId(inc.getIdLocal());
                    sincronizadosCount++;
                    Log.d("SyncWorker", "Incidencia offline id " + inc.getIdLocal() + " sincronizada.");
                }
            }
        } catch (Exception e) {
            Log.e("SyncWorker", "Error sincronizando incidencias: " + e.getMessage());
        }

        // 3. Sincronizar Asistencias
        try {
            List<AsistenciaOfflineEntity> asistencias = db.asistenciaOfflineDao().obtenerPendientes();
            for (AsistenciaOfflineEntity a : asistencias) {
                Response<ApiResponse<Object>> res = apiService.registrarAsistencia(
                        new MarcarAsistenciaRequest(a.getLatitud(), a.getLongitud(), "Marcaje Offline (" + a.getTipoEvento() + ")")
                ).execute();

                if (res.isSuccessful()) {
                    db.asistenciaOfflineDao().eliminarPorId(a.getIdLocal());
                    sincronizadosCount++;
                    Log.d("SyncWorker", "Asistencia offline id " + a.getIdLocal() + " sincronizada.");
                }
            }
        } catch (Exception e) {
            Log.e("SyncWorker", "Error sincronizando asistencias: " + e.getMessage());
        }

        // 4. Sincronizar Reposiciones de Stock
        try {
            List<ReposicionOfflineEntity> reposiciones = db.reposicionOfflineDao().obtenerPendientes();
            for (ReposicionOfflineEntity r : reposiciones) {
                Response<ApiResponse<Object>> res = apiService.reponerStock(
                        new ReponerStockRequest(r.getCodigoBarras(), null, r.getCantidad(), r.getObservacion())
                ).execute();

                if (res.isSuccessful()) {
                    db.reposicionOfflineDao().eliminarPorId(r.getIdLocal());
                    sincronizadosCount++;
                    Log.d("SyncWorker", "Reposición offline id " + r.getIdLocal() + " sincronizada.");
                }
            }
        } catch (Exception e) {
            Log.e("SyncWorker", "Error sincronizando reposiciones: " + e.getMessage());
        }

        // Notificar al usuario si se subieron registros a la nube
        if (sincronizadosCount > 0) {
            Intent intent = new Intent(context, MenuActivity.class);
            new NotificationUtils(context).showNotificationMessage(
                    "Sincronización Exitosa ☁️",
                    "Se han subido " + sincronizadosCount + " registros pendientes a la nube.",
                    null,
                    intent
            );
        }

        return Result.success();
    }

    public static void programarSincronizacion(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueue(syncRequest);
    }
}