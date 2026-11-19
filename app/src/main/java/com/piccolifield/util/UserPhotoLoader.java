package com.piccolifield.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ImageViewCompat;

import com.piccolifield.R;
import com.piccolifield.data.model.Impulsador;
import com.piccolifield.data.model.RegistroAsistencia;
import com.piccolifield.data.model.Usuario;
import com.piccolifield.data.remote.RetrofitClient;
import com.piccolifield.data.remote.api.ApiService;
import com.piccolifield.data.remote.dto.request.ActualizarFotoUsuarioRequest;
import com.piccolifield.data.remote.dto.response.ApiResponse;
import com.piccolifield.sharedpreferences.SplashStorage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserPhotoLoader {

    private static final String TAG = "UserPhotoLoader";

    private static final LruCache<String, Bitmap> MEMORY_CACHE = new LruCache<String, Bitmap>(60) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return 1;
        }
    };

    // Mapas en memoria del directorio de usuarios (código Base64 o ruta por ID, correo y nombre)
    private static final Map<Integer, String> PHOTO_BY_ID = new ConcurrentHashMap<>();
    private static final Map<String, String> PHOTO_BY_EMAIL = new ConcurrentHashMap<>();
    private static final Map<String, String> PHOTO_BY_NAME = new ConcurrentHashMap<>();
    private static final Map<String, Integer> ID_BY_NAME = new ConcurrentHashMap<>();
    private static final Map<String, Integer> ID_BY_EMAIL = new ConcurrentHashMap<>();
    private static volatile boolean directorySyncInProgress = false;
    private static volatile long lastDirectorySyncTime = 0L;

    public interface OnFotoActualizadaListener {
        void onSuccess(Bitmap nuevoBitmap, String nuevaUrlFoto, String mensaje);
        void onError(String mensajeError);
    }

    public interface OnPerfilSincronizadoListener {
        void onPerfilCargado(int idUsuario, String nombreCompleto, String rol, String correo, String urlFoto);
    }

    private static String cacheKeyForUser(int idUsuario) {
        return "user_" + idUsuario;
    }

    public static void invalidateUserPhoto(int idUsuario) {
        if (idUsuario > 0) {
            MEMORY_CACHE.remove(cacheKeyForUser(idUsuario));
            PHOTO_BY_ID.remove(idUsuario);
        }
        MEMORY_CACHE.remove("my_profile");
    }

    public static void clearCache() {
        MEMORY_CACHE.evictAll();
        PHOTO_BY_ID.clear();
        PHOTO_BY_EMAIL.clear();
        PHOTO_BY_NAME.clear();
        ID_BY_NAME.clear();
        ID_BY_EMAIL.clear();
        lastDirectorySyncTime = 0L;
    }

    private static void asegurarToken(Context context) {
        if (context != null && (RetrofitClient.API_TOKEN == null || RetrofitClient.API_TOKEN.isEmpty())) {
            RetrofitClient.API_TOKEN = SplashStorage.obtenerToken(context);
        }
    }

    /**
     * Obtiene el idUsuario del usuario autenticado desde SplashStorage o decodificando el payload del JWT.
     */
    public static int resolveMyUserId(Context context) {
        if (context == null) return 0;
        int savedId = SplashStorage.obtenerUsuarioId(context);
        if (savedId > 0) return savedId;

        String token = SplashStorage.obtenerToken(context);
        if (token == null || token.isEmpty()) token = RetrofitClient.API_TOKEN;
        if (token != null && token.contains(".")) {
            try {
                String[] parts = token.split("\\.");
                if (parts.length >= 2) {
                    byte[] payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
                    String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);
                    JSONObject jwtObj = new JSONObject(payloadJson);

                    int extractedId = extractUserIdFromJson(jwtObj);
                    if (extractedId <= 0 && jwtObj.optJSONObject("sub") != null) {
                        extractedId = extractUserIdFromJson(jwtObj.optJSONObject("sub"));
                    }
                    if (extractedId <= 0 && jwtObj.optJSONObject("identity") != null) {
                        extractedId = extractUserIdFromJson(jwtObj.optJSONObject("identity"));
                    }
                    if (extractedId <= 0) {
                        String subStr = jwtObj.optString("sub", jwtObj.optString("identity", ""));
                        if (subStr.matches("\\d+")) {
                            extractedId = Integer.parseInt(subStr);
                        }
                    }

                    if (extractedId > 0) {
                        SplashStorage.guardarUsuarioId(context, extractedId);
                        return extractedId;
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "No se pudo extraer idUsuario del JWT: " + e.getMessage());
            }
        }

        // Intentar resolver por correo o nombre si ya sincronizamos el directorio
        String email = SplashStorage.obtenerCorreoUsuario(context);
        if (email != null && !email.isEmpty()) {
            Integer byEmail = ID_BY_EMAIL.get(email.trim().toLowerCase(Locale.ROOT));
            if (byEmail != null && byEmail > 0) {
                SplashStorage.guardarUsuarioId(context, byEmail);
                return byEmail;
            }
        }
        String name = SplashStorage.obtenerNombreUsuario(context);
        if (name != null && !name.isEmpty()) {
            Integer byName = ID_BY_NAME.get(name.trim().toLowerCase(Locale.ROOT));
            if (byName != null && byName > 0) {
                SplashStorage.guardarUsuarioId(context, byName);
                return byName;
            }
        }

        return 0;
    }

    private static int extractUserIdFromJson(JSONObject obj) {
        if (obj == null) return 0;
        String[] keys = {"idUsuario", "idUsuarios", "id_usuario", "id_usuarios", "usuarioId", "user_id", "id", "idColaborador", "idImpulsador"};
        for (String k : keys) {
            int val = obj.optInt(k, 0);
            if (val > 0) return val;
        }
        return 0;
    }

    private static String extractPhotoStringFromJson(JSONObject obj) {
        if (obj == null) return "";
        String[] keys = {"url_foto", "foto_perfil", "foto", "urlFoto", "fotoPerfil", "foto_url", "imagen", "avatar", "foto_base64", "codigo_foto", "impulsador_foto", "responsable_foto", "base64"};
        for (String k : keys) {
            if (obj.has(k) && !obj.isNull(k)) {
                String val = obj.optString(k, "").trim();
                if (!val.isEmpty() && !"null".equalsIgnoreCase(val)) {
                    return val;
                }
            }
        }
        return "";
    }

    private static String extractFullNameFromJson(JSONObject obj) {
        if (obj == null) return "";
        String completo = obj.optString("nombre_completo", "").trim();
        if (!completo.isEmpty() && !"null".equalsIgnoreCase(completo)) return completo;

        String nombres = obj.optString("nombres", obj.optString("nombre", "")).trim();
        if ("null".equalsIgnoreCase(nombres)) nombres = "";
        String apePat = obj.optString("apePaterno", obj.optString("ape_paterno", obj.optString("apellidoPaterno", ""))).trim();
        if ("null".equalsIgnoreCase(apePat)) apePat = "";
        String apeMat = obj.optString("apeMaterno", obj.optString("ape_materno", obj.optString("apellidoMaterno", ""))).trim();
        if ("null".equalsIgnoreCase(apeMat)) apeMat = "";

        return (nombres + " " + apePat + " " + apeMat).replaceAll("\\s+", " ").trim();
    }

    private static void applyBitmapToImageView(ImageView imageView, Bitmap bitmap) {
        if (imageView == null || bitmap == null) return;
        imageView.post(() -> {
            ImageViewCompat.setImageTintList(imageView, null);
            imageView.setColorFilter(null);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(0, 0, 0, 0);
            imageView.setImageBitmap(bitmap);
        });
    }

    private static void applyPlaceholder(ImageView imageView, int placeholderResId) {
        if (imageView == null) return;
        imageView.post(() -> {
            int resId = placeholderResId != 0 ? placeholderResId : R.drawable.ic_avatar_placeholder;
            ImageViewCompat.setImageTintList(imageView, null);
            imageView.setColorFilter(null);
            imageView.setImageResource(resId);
        });
    }

    /**
     * Sincroniza los datos y foto del perfil desde GET /api/perfil parseando cualquier estructura JSON
     * (ya sea que venga en "data", "usuario", "user", "data.usuario" o en la raíz).
     */
    public static void sincronizarPerfilRaw(Context context, @Nullable ImageView targetImageView, int placeholderResId, @Nullable OnPerfilSincronizadoListener listener) {
        if (context == null) return;
        asegurarToken(context);

        ApiService apiService = RetrofitClient.createService();
        apiService.obtenerPerfilRaw().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                Log.d(TAG, "GET /api/perfil -> HTTP " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String raw = response.body().string();
                        Log.d(TAG, "GET /api/perfil body len=" + raw.length());
                        JSONObject root = new JSONObject(raw);

                        JSONObject userObj = root;
                        if (root.optJSONObject("data") != null) {
                            JSONObject dataObj = root.optJSONObject("data");
                            if (dataObj.optJSONObject("usuario") != null) {
                                userObj = dataObj.optJSONObject("usuario");
                            } else if (dataObj.optJSONObject("user") != null) {
                                userObj = dataObj.optJSONObject("user");
                            } else {
                                userObj = dataObj;
                            }
                        } else if (root.optJSONObject("usuario") != null) {
                            userObj = root.optJSONObject("usuario");
                        } else if (root.optJSONObject("user") != null) {
                            userObj = root.optJSONObject("user");
                        } else if (root.optJSONObject("perfil") != null) {
                            userObj = root.optJSONObject("perfil");
                        }

                        int idUsuario = extractUserIdFromJson(userObj);
                        if (idUsuario <= 0) idUsuario = extractUserIdFromJson(root);
                        if (idUsuario <= 0) idUsuario = resolveMyUserId(context);

                        String nombreCompleto = extractFullNameFromJson(userObj);
                        if (nombreCompleto.isEmpty()) nombreCompleto = extractFullNameFromJson(root);

                        String rol = userObj.optString("rol", userObj.optString("role", root.optString("rol", ""))).trim();
                        if ("null".equalsIgnoreCase(rol)) rol = "";

                        String correo = userObj.optString("correo", userObj.optString("email", root.optString("correo", ""))).trim();
                        if ("null".equalsIgnoreCase(correo)) correo = "";

                        String urlFoto = extractPhotoStringFromJson(userObj);
                        if (urlFoto.isEmpty()) urlFoto = extractPhotoStringFromJson(root);
                        Log.d(TAG, "Perfil idUsuario=" + idUsuario + ", urlFotoLen=" + urlFoto.length() + ", prefix=" + (urlFoto.length() > 30 ? urlFoto.substring(0, 30) : urlFoto));

                        if (idUsuario > 0) SplashStorage.guardarUsuarioId(context, idUsuario);
                        if (!nombreCompleto.isEmpty()) SplashStorage.guardarNombreUsuario(context, nombreCompleto);
                        if (!rol.isEmpty()) SplashStorage.guardarRol(context, rol);
                        if (!correo.isEmpty()) SplashStorage.guardarCorreoUsuario(context, correo);

                        if (!urlFoto.isEmpty()) {
                            SplashStorage.guardarFotoUsuario(context, urlFoto);
                            indexUserPhoto(idUsuario, nombreCompleto, correo, urlFoto);

                            Bitmap bmp = decodeBase64OrDataUri(urlFoto);
                            if (bmp != null) {
                                if (idUsuario > 0) {
                                    MEMORY_CACHE.put(cacheKeyForUser(idUsuario), bmp);
                                    SplashStorage.guardarFotoUsuarioBase64Cache(context, idUsuario, urlFoto);
                                }
                                MEMORY_CACHE.put("my_profile", bmp);
                                if (targetImageView != null) {
                                    applyBitmapToImageView(targetImageView, bmp);
                                }
                            } else if (targetImageView != null) {
                                loadMyProfilePhoto(context, targetImageView, placeholderResId);
                            }
                        } else if (targetImageView != null) {
                            loadMyProfilePhoto(context, targetImageView, placeholderResId);
                        }

                        if (listener != null) {
                            listener.onPerfilCargado(
                                    idUsuario,
                                    !nombreCompleto.isEmpty() ? nombreCompleto : SplashStorage.obtenerNombreUsuario(context),
                                    !rol.isEmpty() ? rol : SplashStorage.obtenerRol(context),
                                    !correo.isEmpty() ? correo : SplashStorage.obtenerCorreoUsuario(context),
                                    urlFoto
                            );
                        }
                        return;
                    } catch (Exception e) {
                        Log.e(TAG, "Error parseando obtenerPerfilRaw", e);
                    }
                }
                if (targetImageView != null) {
                    loadMyProfilePhoto(context, targetImageView, placeholderResId);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (targetImageView != null) {
                    loadMyProfilePhoto(context, targetImageView, placeholderResId);
                }
            }
        });
    }

    private static void indexUserPhoto(int idUsuario, @Nullable String nombre, @Nullable String correo, @Nullable String urlFoto) {
        String cleanName = nombre != null ? nombre.trim().toLowerCase(Locale.ROOT) : "";
        String cleanEmail = correo != null ? correo.trim().toLowerCase(Locale.ROOT) : "";

        if (idUsuario > 0) {
            if (!cleanName.isEmpty()) ID_BY_NAME.put(cleanName, idUsuario);
            if (!cleanEmail.isEmpty()) ID_BY_EMAIL.put(cleanEmail, idUsuario);
        }

        if (urlFoto != null && !urlFoto.trim().isEmpty() && !"null".equalsIgnoreCase(urlFoto.trim())) {
            String cleanFoto = urlFoto.trim();
            if (idUsuario > 0) PHOTO_BY_ID.put(idUsuario, cleanFoto);
            if (!cleanName.isEmpty()) PHOTO_BY_NAME.put(cleanName, cleanFoto);
            if (!cleanEmail.isEmpty()) PHOTO_BY_EMAIL.put(cleanEmail, cleanFoto);
        }
    }

    /**
     * Carga la foto del usuario autenticado actualmente (Mi Perfil).
     */
    public static void loadMyProfilePhoto(Context context, ImageView imageView, int placeholderResId) {
        if (context == null || imageView == null) return;
        asegurarToken(context);

        int myId = resolveMyUserId(context);
        String savedUrlFoto = SplashStorage.obtenerFotoUsuario(context);
        String tagKey = "my_profile_tag";
        imageView.setTag(tagKey);

        // 1. Verificar caché en memoria
        Bitmap cached = MEMORY_CACHE.get("my_profile");
        if (cached == null && myId > 0) {
            cached = MEMORY_CACHE.get(cacheKeyForUser(myId));
        }
        if (cached != null) {
            applyBitmapToImageView(imageView, cached);
        } else {
            // 2. Verificar Base64 guardado localmente en SharedPreferences
            Bitmap localBmp = null;
            if (myId > 0) {
                localBmp = decodeBase64OrDataUri(SplashStorage.obtenerFotoUsuarioBase64Cache(context, myId));
            }
            if (localBmp == null && savedUrlFoto != null && !savedUrlFoto.isEmpty()) {
                localBmp = decodeBase64OrDataUri(savedUrlFoto);
            }
            if (localBmp == null && myId > 0 && PHOTO_BY_ID.containsKey(myId)) {
                localBmp = decodeBase64OrDataUri(PHOTO_BY_ID.get(myId));
            }
            if (localBmp != null) {
                if (myId > 0) MEMORY_CACHE.put(cacheKeyForUser(myId), localBmp);
                MEMORY_CACHE.put("my_profile", localBmp);
                applyBitmapToImageView(imageView, localBmp);
            } else {
                applyPlaceholder(imageView, placeholderResId);
            }
        }

        // 3. Consultar GET /api/perfil/foto
        ApiService apiService = RetrofitClient.createService();
        apiService.obtenerFotoMiPerfil().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                Log.d(TAG, "GET /api/perfil/foto -> HTTP " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    String[] relativePathOut = new String[1];
                    Bitmap bmp = decodeResponseBody(response.body(), relativePathOut);
                    if (bmp != null) {
                        int uid = resolveMyUserId(context);
                        if (uid > 0) MEMORY_CACHE.put(cacheKeyForUser(uid), bmp);
                        MEMORY_CACHE.put("my_profile", bmp);
                        if (tagKey.equals(imageView.getTag())) {
                            applyBitmapToImageView(imageView, bmp);
                        }
                        return;
                    } else if (relativePathOut[0] != null && !relativePathOut[0].isEmpty()) {
                        loadFromUrlHint(context, resolveMyUserId(context), relativePathOut[0], imageView, placeholderResId, tagKey, true);
                        return;
                    }
                }
                fallbackMyProfilePhoto(context, imageView, placeholderResId, tagKey);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                fallbackMyProfilePhoto(context, imageView, placeholderResId, tagKey);
            }
        });
    }

    private static void fallbackMyProfilePhoto(Context context, ImageView imageView, int placeholderResId, String tagKey) {
        Bitmap alreadyCached = MEMORY_CACHE.get("my_profile");
        int uid = resolveMyUserId(context);
        if (alreadyCached == null && uid > 0) {
            alreadyCached = MEMORY_CACHE.get(cacheKeyForUser(uid));
        }
        if (alreadyCached != null) {
            if (tagKey.equals(imageView.getTag())) {
                applyBitmapToImageView(imageView, alreadyCached);
            }
            return;
        }
        String savedUrlFoto = SplashStorage.obtenerFotoUsuario(context);
        if (uid > 0) {
            loadUserPhotoInternal(context, uid, savedUrlFoto, SplashStorage.obtenerNombreUsuario(context), imageView, placeholderResId, tagKey, true);
        } else if (savedUrlFoto != null && !savedUrlFoto.isEmpty()) {
            loadFromUrlHint(context, 0, savedUrlFoto, imageView, placeholderResId, tagKey, true);
        } else {
            sincronizarDirectorioUsuarios(context, () -> {
                int resolvedId = resolveMyUserId(context);
                String email = SplashStorage.obtenerCorreoUsuario(context);
                String name = SplashStorage.obtenerNombreUsuario(context);
                String foto = lookupPhotoInDirectory(resolvedId, name, email);
                if (foto != null && !foto.isEmpty()) {
                    Bitmap b64 = decodeBase64OrDataUri(foto);
                    if (b64 != null) {
                        if (resolvedId > 0) MEMORY_CACHE.put(cacheKeyForUser(resolvedId), b64);
                        MEMORY_CACHE.put("my_profile", b64);
                        if (tagKey.equals(imageView.getTag())) {
                            applyBitmapToImageView(imageView, b64);
                        }
                    } else {
                        loadFromUrlHint(context, resolvedId, foto, imageView, placeholderResId, tagKey, true);
                    }
                } else if (resolvedId > 0) {
                    loadUserPhotoInternal(context, resolvedId, null, name, imageView, placeholderResId, tagKey, true);
                }
            });
        }
    }

    /**
     * Carga la foto de cualquier usuario por idUsuario y/o urlFotoHint (Base64 o ruta).
     */
    public static void loadUserPhoto(Context context, int idUsuario, @Nullable String urlFotoHint, ImageView imageView, int placeholderResId) {
        loadUserPhoto(context, idUsuario, urlFotoHint, null, imageView, placeholderResId);
    }

    public static void loadUserPhoto(Context context, int idUsuario, @Nullable String urlFotoHint, @Nullable String nombreHint, ImageView imageView, int placeholderResId) {
        if (context == null || imageView == null) return;
        asegurarToken(context);

        int resolvedId = idUsuario;
        if (resolvedId <= 0 && nombreHint != null && !nombreHint.trim().isEmpty()) {
            Integer byName = ID_BY_NAME.get(nombreHint.trim().toLowerCase(Locale.ROOT));
            if (byName != null && byName > 0) resolvedId = byName;
        }

        String effectiveFotoHint = urlFotoHint;
        if ((effectiveFotoHint == null || effectiveFotoHint.trim().isEmpty()) && resolvedId > 0) {
            effectiveFotoHint = PHOTO_BY_ID.get(resolvedId);
        }
        if ((effectiveFotoHint == null || effectiveFotoHint.trim().isEmpty()) && nombreHint != null && !nombreHint.trim().isEmpty()) {
            effectiveFotoHint = PHOTO_BY_NAME.get(nombreHint.trim().toLowerCase(Locale.ROOT));
        }

        String tagKey = "user_tag_" + resolvedId + "_" + (nombreHint != null ? nombreHint.hashCode() : 0) + "_" + (effectiveFotoHint != null ? effectiveFotoHint.hashCode() : 0);
        imageView.setTag(tagKey);

        // 1. Verificar caché en memoria
        if (resolvedId > 0) {
            Bitmap cached = MEMORY_CACHE.get(cacheKeyForUser(resolvedId));
            if (cached != null) {
                applyBitmapToImageView(imageView, cached);
                return;
            }
            String localB64 = SplashStorage.obtenerFotoUsuarioBase64Cache(context, resolvedId);
            Bitmap localBmp = decodeBase64OrDataUri(localB64);
            if (localBmp != null) {
                MEMORY_CACHE.put(cacheKeyForUser(resolvedId), localBmp);
                applyBitmapToImageView(imageView, localBmp);
                return;
            }
        }

        // 2. Si effectiveFotoHint es un código Base64 ("data:image/..." o "/9j/..."), decodificarlo directo
        if (effectiveFotoHint != null && !effectiveFotoHint.trim().isEmpty()) {
            Bitmap b64Bmp = decodeBase64OrDataUri(effectiveFotoHint);
            if (b64Bmp != null) {
                if (resolvedId > 0) MEMORY_CACHE.put(cacheKeyForUser(resolvedId), b64Bmp);
                applyBitmapToImageView(imageView, b64Bmp);
                return;
            }
        }

        applyPlaceholder(imageView, placeholderResId);

        // 3. Consultar endpoints de foto del usuario
        final int finalId = resolvedId;
        final String finalHint = effectiveFotoHint;
        if (finalId > 0) {
            loadUserPhotoInternal(context, finalId, finalHint, nombreHint, imageView, placeholderResId, tagKey, false);
        } else if (finalHint != null && !finalHint.trim().isEmpty()) {
            loadFromUrlHint(context, 0, finalHint, imageView, placeholderResId, tagKey, false);
        } else {
            sincronizarDirectorioUsuarios(context, () -> {
                int idLookup = 0;
                if (nombreHint != null && !nombreHint.trim().isEmpty()) {
                    Integer found = ID_BY_NAME.get(nombreHint.trim().toLowerCase(Locale.ROOT));
                    if (found != null) idLookup = found;
                }
                String foto = lookupPhotoInDirectory(idLookup, nombreHint, null);
                if (foto != null && !foto.isEmpty()) {
                    Bitmap b64 = decodeBase64OrDataUri(foto);
                    if (b64 != null) {
                        if (idLookup > 0) MEMORY_CACHE.put(cacheKeyForUser(idLookup), b64);
                        if (tagKey.equals(imageView.getTag())) {
                            applyBitmapToImageView(imageView, b64);
                        }
                    } else {
                        loadFromUrlHint(context, idLookup, foto, imageView, placeholderResId, tagKey, false);
                    }
                } else if (idLookup > 0) {
                    loadUserPhotoInternal(context, idLookup, null, nombreHint, imageView, placeholderResId, tagKey, false);
                }
            });
        }
    }

    private static void loadUserPhotoInternal(
            Context context,
            int idUsuario,
            @Nullable String urlFotoHint,
            @Nullable String nombreHint,
            ImageView imageView,
            int placeholderResId,
            String tagKey,
            boolean isMyProfile
    ) {
        // Primero verificar si ya está en caché de memoria
        if (idUsuario > 0 && MEMORY_CACHE.get(cacheKeyForUser(idUsuario)) != null) {
            Bitmap cachedBmp = MEMORY_CACHE.get(cacheKeyForUser(idUsuario));
            if (tagKey.equals(imageView.getTag())) {
                applyBitmapToImageView(imageView, cachedBmp);
            }
            return;
        }

        // Si urlFotoHint ya es Base64, mostrarlo de inmediato
        if (urlFotoHint != null && !urlFotoHint.trim().isEmpty()) {
            Bitmap b64 = decodeBase64OrDataUri(urlFotoHint);
            if (b64 != null) {
                MEMORY_CACHE.put(cacheKeyForUser(idUsuario), b64);
                if (isMyProfile) MEMORY_CACHE.put("my_profile", b64);
                if (tagKey.equals(imageView.getTag())) {
                    applyBitmapToImageView(imageView, b64);
                }
                return;
            }
        }

        ApiService apiService = RetrofitClient.createService();
        apiService.obtenerFotoUsuario(idUsuario).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                Log.d(TAG, "GET /api/usuarios/" + idUsuario + "/foto -> HTTP " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    String[] relativePathOut = new String[1];
                    Bitmap bmp = decodeResponseBody(response.body(), relativePathOut);
                    if (bmp != null) {
                        MEMORY_CACHE.put(cacheKeyForUser(idUsuario), bmp);
                        if (isMyProfile) MEMORY_CACHE.put("my_profile", bmp);
                        if (tagKey.equals(imageView.getTag())) {
                            applyBitmapToImageView(imageView, bmp);
                        }
                        return;
                    } else if (relativePathOut[0] != null && !relativePathOut[0].isEmpty()) {
                        loadFromUrlHint(context, idUsuario, relativePathOut[0], imageView, placeholderResId, tagKey, isMyProfile);
                        return;
                    }
                }
                if (urlFotoHint != null && !urlFotoHint.trim().isEmpty()) {
                    loadFromUrlHint(context, idUsuario, urlFotoHint, imageView, placeholderResId, tagKey, isMyProfile);
                } else {
                    fallbackFotoImpulsadorYDirectorio(context, idUsuario, nombreHint, imageView, placeholderResId, tagKey, isMyProfile);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (urlFotoHint != null && !urlFotoHint.trim().isEmpty()) {
                    loadFromUrlHint(context, idUsuario, urlFotoHint, imageView, placeholderResId, tagKey, isMyProfile);
                } else {
                    fallbackFotoImpulsadorYDirectorio(context, idUsuario, nombreHint, imageView, placeholderResId, tagKey, isMyProfile);
                }
            }
        });
    }

    private static void loadFromUrlHint(
            Context context,
            int idUsuario,
            String urlFotoHint,
            ImageView imageView,
            int placeholderResId,
            String tagKey,
            boolean isMyProfile
    ) {
        Bitmap b64 = decodeBase64OrDataUri(urlFotoHint);
        if (b64 != null) {
            if (idUsuario > 0) MEMORY_CACHE.put(cacheKeyForUser(idUsuario), b64);
            if (isMyProfile) MEMORY_CACHE.put("my_profile", b64);
            if (tagKey.equals(imageView.getTag())) {
                applyBitmapToImageView(imageView, b64);
            }
            return;
        }

        String fullUrl = buildFullImageUrl(urlFotoHint);
        if (fullUrl == null) {
            if (idUsuario > 0) {
                fallbackFotoImpulsadorYDirectorio(context, idUsuario, null, imageView, placeholderResId, tagKey, isMyProfile);
            }
            return;
        }

        ApiService apiService = RetrofitClient.createService();
        apiService.descargarArchivoPorUrl(fullUrl).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Bitmap bmp = decodeResponseBody(response.body(), null);
                    if (bmp != null) {
                        if (idUsuario > 0) MEMORY_CACHE.put(cacheKeyForUser(idUsuario), bmp);
                        if (isMyProfile) MEMORY_CACHE.put("my_profile", bmp);
                        if (tagKey.equals(imageView.getTag())) {
                            applyBitmapToImageView(imageView, bmp);
                        }
                        return;
                    }
                }
                if (idUsuario > 0) {
                    fallbackFotoImpulsadorYDirectorio(context, idUsuario, null, imageView, placeholderResId, tagKey, isMyProfile);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (idUsuario > 0) {
                    fallbackFotoImpulsadorYDirectorio(context, idUsuario, null, imageView, placeholderResId, tagKey, isMyProfile);
                }
            }
        });
    }

    private static void fallbackFotoImpulsadorYDirectorio(
            Context context,
            int idUsuario,
            @Nullable String nombreHint,
            ImageView imageView,
            int placeholderResId,
            String tagKey,
            boolean isMyProfile
    ) {
        ApiService apiService = RetrofitClient.createService();
        // 1. Intentar GET /api/supervision/impulsadores/{id}/foto
        apiService.obtenerFotoImpulsador(idUsuario).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String[] relOut = new String[1];
                    Bitmap bmp = decodeResponseBody(response.body(), relOut);
                    if (bmp != null) {
                        MEMORY_CACHE.put(cacheKeyForUser(idUsuario), bmp);
                        if (isMyProfile) MEMORY_CACHE.put("my_profile", bmp);
                        if (tagKey.equals(imageView.getTag())) {
                            applyBitmapToImageView(imageView, bmp);
                        }
                        return;
                    }
                }
                // 2. Intentar buscar url_foto Base64 en el directorio de usuarios (/api/usuarios, /api/supervision/impulsadores)
                buscarEnDirectorioYMostrar(context, idUsuario, nombreHint, imageView, placeholderResId, tagKey, isMyProfile);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                buscarEnDirectorioYMostrar(context, idUsuario, nombreHint, imageView, placeholderResId, tagKey, isMyProfile);
            }
        });
    }

    private static void buscarEnDirectorioYMostrar(
            Context context,
            int idUsuario,
            @Nullable String nombreHint,
            ImageView imageView,
            int placeholderResId,
            String tagKey,
            boolean isMyProfile
    ) {
        String existing = lookupPhotoInDirectory(idUsuario, nombreHint, isMyProfile ? SplashStorage.obtenerCorreoUsuario(context) : null);
        if (existing != null && !existing.isEmpty()) {
            Bitmap b64 = decodeBase64OrDataUri(existing);
            if (b64 != null) {
                if (idUsuario > 0) MEMORY_CACHE.put(cacheKeyForUser(idUsuario), b64);
                if (isMyProfile) MEMORY_CACHE.put("my_profile", b64);
                if (tagKey.equals(imageView.getTag())) {
                    applyBitmapToImageView(imageView, b64);
                }
                return;
            }
        }

        sincronizarDirectorioUsuarios(context, () -> {
            String foto = lookupPhotoInDirectory(idUsuario, nombreHint, isMyProfile ? SplashStorage.obtenerCorreoUsuario(context) : null);
            if (foto != null && !foto.isEmpty()) {
                Bitmap b64 = decodeBase64OrDataUri(foto);
                if (b64 != null) {
                    if (idUsuario > 0) MEMORY_CACHE.put(cacheKeyForUser(idUsuario), b64);
                    if (isMyProfile) MEMORY_CACHE.put("my_profile", b64);
                    if (tagKey.equals(imageView.getTag())) {
                        applyBitmapToImageView(imageView, b64);
                    }
                } else {
                    String fullUrl = buildFullImageUrl(foto);
                    if (fullUrl != null) {
                        RetrofitClient.createService().descargarArchivoPorUrl(fullUrl).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Bitmap bmp = decodeResponseBody(response.body(), null);
                                    if (bmp != null) {
                                        if (idUsuario > 0) MEMORY_CACHE.put(cacheKeyForUser(idUsuario), bmp);
                                        if (isMyProfile) MEMORY_CACHE.put("my_profile", bmp);
                                        if (tagKey.equals(imageView.getTag())) {
                                            applyBitmapToImageView(imageView, bmp);
                                        }
                                    }
                                }
                            }
                            @Override public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {}
                        });
                    }
                }
            }
        });
    }

    @Nullable
    private static String lookupPhotoInDirectory(int idUsuario, @Nullable String nombre, @Nullable String correo) {
        if (idUsuario > 0 && PHOTO_BY_ID.containsKey(idUsuario)) {
            return PHOTO_BY_ID.get(idUsuario);
        }
        if (correo != null && !correo.trim().isEmpty()) {
            String byEmail = PHOTO_BY_EMAIL.get(correo.trim().toLowerCase(Locale.ROOT));
            if (byEmail != null) return byEmail;
        }
        if (nombre != null && !nombre.trim().isEmpty()) {
            String clean = nombre.trim().toLowerCase(Locale.ROOT);
            String byName = PHOTO_BY_NAME.get(clean);
            if (byName != null) return byName;
            for (Map.Entry<String, String> entry : PHOTO_BY_NAME.entrySet()) {
                if (entry.getKey().contains(clean) || clean.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Sincroniza en memoria las fotos Base64 de los usuarios desde /api/usuarios y /api/supervision/impulsadores.
     */
    public static void sincronizarDirectorioUsuarios(Context context, @Nullable Runnable onComplete) {
        long now = System.currentTimeMillis();
        if (!PHOTO_BY_ID.isEmpty() && (now - lastDirectorySyncTime) < 60_000L) {
            if (onComplete != null) onComplete.run();
            return;
        }
        if (directorySyncInProgress) {
            if (onComplete != null) onComplete.run();
            return;
        }
        directorySyncInProgress = true;
        asegurarToken(context);

        ApiService apiService = RetrofitClient.createService();

        // 1. Consultar /api/supervision/impulsadores (devuelve url_foto de USUARIOS)
        apiService.listarImpulsadores().enqueue(new Callback<ApiResponse<List<Impulsador>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Impulsador>>> call, @NonNull Response<ApiResponse<List<Impulsador>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    for (Impulsador imp : response.body().getData()) {
                        if (imp != null) {
                            indexUserPhoto(imp.getIdUsuario(), imp.getNombreCompleto(), imp.getCorreo(), imp.getUrlFoto());
                        }
                    }
                }
                consultarUsuariosRaw(apiService, onComplete);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Impulsador>>> call, @NonNull Throwable t) {
                consultarUsuariosRaw(apiService, onComplete);
            }
        });
    }

    private static void consultarUsuariosRaw(ApiService apiService, @Nullable Runnable onComplete) {
        apiService.listarUsuariosRaw().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                directorySyncInProgress = false;
                lastDirectorySyncTime = System.currentTimeMillis();
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String raw = response.body().string();
                        parseAndIndexUsersJson(raw);
                    } catch (Exception e) {
                        Log.w(TAG, "Error parseando listarUsuariosRaw: " + e.getMessage());
                    }
                }
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                directorySyncInProgress = false;
                lastDirectorySyncTime = System.currentTimeMillis();
                if (onComplete != null) onComplete.run();
            }
        });
    }

    private static void parseAndIndexUsersJson(String raw) {
        if (raw == null || raw.trim().isEmpty()) return;
        try {
            String trimmed = raw.trim();
            JSONArray arr = null;
            if (trimmed.startsWith("[")) {
                arr = new JSONArray(trimmed);
            } else if (trimmed.startsWith("{")) {
                JSONObject obj = new JSONObject(trimmed);
                arr = obj.optJSONArray("data");
                if (arr == null) arr = obj.optJSONArray("usuarios");
                if (arr == null) arr = obj.optJSONArray("users");
            }
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject u = arr.optJSONObject(i);
                    if (u != null) {
                        int id = extractUserIdFromJson(u);
                        String nombre = extractFullNameFromJson(u);
                        String correo = u.optString("correo", u.optString("email", ""));
                        String foto = extractPhotoStringFromJson(u);
                        indexUserPhoto(id, nombre, correo, foto);
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "No se pudo indexar JSON de usuarios: " + e.getMessage());
        }
    }

    @Nullable
    public static String buildFullImageUrl(String urlFoto) {
        if (urlFoto == null) return null;
        String trimmed = urlFoto.trim().replace("\\", "/");
        if (trimmed.isEmpty() || trimmed.startsWith("data:") || trimmed.length() > 300) return null;

        if (trimmed.startsWith("http://localhost") || trimmed.startsWith("http://127.0.0.1") || trimmed.startsWith("http://0.0.0.0")) {
            int idx = trimmed.indexOf('/', 8);
            trimmed = idx >= 0 ? trimmed.substring(idx + 1) : "";
        } else if (trimmed.startsWith("http://piccolifield-api-smkc.onrender.com")) {
            return "https://" + trimmed.substring("http://".length());
        } else if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }

        while (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.isEmpty()) return null;
        return RetrofitClient.BASE_SERVER_URL + trimmed;
    }

    public static void guardarFotoProspectoLocal(Context context, int idProspecto, Bitmap bmp) {
        if (context == null || idProspecto <= 0 || bmp == null) return;
        try {
            File dir = new File(context.getFilesDir(), "prospecto_fotos");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "prospecto_" + idProspecto + ".jpg");
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            }
        } catch (Exception e) {
            Log.w(TAG, "No se pudo guardar foto local de prospecto " + idProspecto, e);
        }
    }

    public static void guardarFotoProspectoBytesLocal(Context context, int idProspecto, byte[] bytes) {
        if (context == null || idProspecto <= 0 || bytes == null || bytes.length == 0) return;
        try {
            File dir = new File(context.getFilesDir(), "prospecto_fotos");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "prospecto_" + idProspecto + ".jpg");
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                fos.write(bytes);
            }
        } catch (Exception e) {
            Log.w(TAG, "No se pudo guardar bytes locales de prospecto " + idProspecto, e);
        }
    }

    @Nullable
    public static Bitmap cargarFotoProspectoLocal(Context context, int idProspecto) {
        if (context == null || idProspecto <= 0) return null;
        try {
            File file = new File(new File(context.getFilesDir(), "prospecto_fotos"), "prospecto_" + idProspecto + ".jpg");
            if (!file.exists() || file.length() == 0) return null;
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Decodifica de forma ultra-tolerante cualquier código Base64 guardado en la BD:
     * - Data URI ("data:image/jpeg;base64,/9j/...", "data:image/png;base64,iVBOR...")
     * - Base64 puro sin prefijo ("/9j/4AAQ...", "iVBORw0KGgo...")
     * - Cadenas con barras escapadas JSON ("\/9j\/..."), saltos de línea, espacios o URL-safe Base64
     * - Representaciones de bytes Python ("b'data:image/...'")
     */
    @Nullable
    public static Bitmap decodeBase64OrDataUri(@Nullable String data) {
        if (data == null || data.trim().isEmpty()) return null;
        String trimmed = data.trim();

        // Quitar comillas envolventes o literal b'...' de Python si existiera
        if ((trimmed.startsWith("b'") && trimmed.endsWith("'")) || (trimmed.startsWith("b\"") && trimmed.endsWith("\""))) {
            trimmed = trimmed.substring(2, trimmed.length() - 1).trim();
        }
        while ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }

        // Si es una ruta relativa corta de archivo (ej. uploads/perfiles/user_8.jpg) o URL http, no es Base64
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.startsWith("uploads/") || lower.startsWith("/uploads/") || lower.startsWith("http://") || lower.startsWith("https://")) {
            return null;
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")) {
            if (trimmed.length() < 260) return null;
        }

        try {
            // Si está URL-encoded (ej. data%3Aimage%2Fjpeg%3Bbase64%2C...)
            if (trimmed.startsWith("data%3A") || trimmed.startsWith("data%3a")) {
                trimmed = URLDecoder.decode(trimmed, "UTF-8");
            }

            String base64Part = trimmed;
            int base64Marker = base64Part.indexOf("base64,");
            if (base64Marker != -1) {
                base64Part = base64Part.substring(base64Marker + 7);
            } else {
                int commaIndex = base64Part.indexOf(',');
                if (commaIndex != -1 && commaIndex < 80) {
                    base64Part = base64Part.substring(commaIndex + 1);
                }
            }

            // Desescapar barras JSON (\/ -> /) y limpiar saltos de línea
            base64Part = base64Part
                    .replace("\\/", "/")
                    .replace("\\n", "")
                    .replace("\\r", "")
                    .replace("\n", "")
                    .replace("\r", "")
                    .replace("\t", "")
                    .trim();

            // Si los '+' se convirtieron en espacios o si es URL-safe Base64 ('-' y '_')
            base64Part = base64Part.replace(' ', '+').replace('-', '+').replace('_', '/');

            // Eliminar cualquier caracter fuera del alfabeto Base64
            base64Part = base64Part.replaceAll("[^A-Za-z0-9+/=]", "");

            if (base64Part.length() < 40) return null;

            // Ajustar padding '=' a múltiplo de 4
            int mod = base64Part.length() % 4;
            if (mod == 2) {
                base64Part += "==";
            } else if (mod == 3) {
                base64Part += "=";
            } else if (mod == 1) {
                base64Part = base64Part.substring(0, base64Part.length() - 1);
            }

            byte[] decoded = Base64.decode(base64Part, Base64.DEFAULT);
            if (decoded == null || decoded.length == 0) return null;

            Bitmap bmp = decodeBitmapSafely(decoded);
            if (bmp != null) return bmp;

            // Por si estaba doblemente codificado en Base64
            String innerText = new String(decoded, StandardCharsets.UTF_8).trim();
            if (innerText.startsWith("data:image") || innerText.startsWith("/9j/") || innerText.startsWith("iVBOR")) {
                return decodeBase64OrDataUri(innerText);
            }
            return null;
        } catch (Throwable e) {
            Log.w(TAG, "Error decodificando Base64: " + e.getMessage());
            return null;
        }
    }

    /**
     * Decodifica bytes de imagen escalando automáticamente si supera 1024px
     * para evitar errores de textura OpenGL o OutOfMemoryError con fotos grandes de la web/BD.
     */
    @Nullable
    private static Bitmap decodeBitmapSafely(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return null;
            }

            int maxSide = 1024;
            int inSampleSize = 1;
            int w = bounds.outWidth;
            int h = bounds.outHeight;
            while ((w / inSampleSize) > maxSide || (h / inSampleSize) > maxSide) {
                inSampleSize *= 2;
            }

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = inSampleSize;
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
        } catch (Throwable t) {
            Log.e(TAG, "Error decodificando bitmap de bytes", t);
            return null;
        }
    }

    @Nullable
    public static Bitmap decodeBitmapFromResponseBody(ResponseBody body) {
        return decodeResponseBody(body, null);
    }

    @Nullable
    private static Bitmap decodeResponseBody(ResponseBody body, @Nullable String[] relativePathOut) {
        try {
            byte[] bytes = body.bytes();
            if (bytes == null || bytes.length == 0) return null;

            // 1. Intentar decodificar como imagen binaria directa (JPEG/PNG/WebP)
            Bitmap bmp = decodeBitmapSafely(bytes);
            if (bmp != null) {
                return bmp;
            }

            // 2. Si el servidor devolvió JSON o texto con código Base64 o ruta
            String text = new String(bytes, StandardCharsets.UTF_8).trim();
            if (text.startsWith("{")) {
                JSONObject json = new JSONObject(text);
                String urlFoto = extractPhotoStringFromJson(json);

                if (urlFoto.isEmpty() && json.optJSONObject("data") != null) {
                    JSONObject dataObj = json.optJSONObject("data");
                    urlFoto = extractPhotoStringFromJson(dataObj);
                    if (urlFoto.isEmpty() && dataObj.optJSONObject("usuario") != null) {
                        urlFoto = extractPhotoStringFromJson(dataObj.optJSONObject("usuario"));
                    }
                }
                if (urlFoto.isEmpty() && json.optJSONObject("usuario") != null) {
                    urlFoto = extractPhotoStringFromJson(json.optJSONObject("usuario"));
                }
                if (urlFoto.isEmpty()) {
                    Object rawData = json.opt("data");
                    if (rawData instanceof String) {
                        urlFoto = ((String) rawData).trim();
                    }
                }

                if (!urlFoto.isEmpty()) {
                    Bitmap decodedBmp = decodeBase64OrDataUri(urlFoto);
                    if (decodedBmp != null) {
                        return decodedBmp;
                    }
                    if (relativePathOut != null) {
                        relativePathOut[0] = urlFoto;
                    }
                }
                return null;
            } else {
                Bitmap decodedTextBmp = decodeBase64OrDataUri(text);
                if (decodedTextBmp != null) {
                    return decodedTextBmp;
                }
                if (relativePathOut != null && (text.startsWith("uploads/") || text.startsWith("/uploads/") || text.startsWith("http"))) {
                    relativePathOut[0] = text;
                }
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error decodificando cuerpo de respuesta de foto", e);
            return null;
        }
    }

    /**
     * Procesa un Uri de imagen (Cámara o Galería) y la sube a la API actualizando la BD:
     * - Envía el código Base64 ("data:image/jpeg;base64,...") en el JSON {"url_foto": "..."}
     * - Si esMiPerfil = true: actualiza tanto PUT /api/perfil/foto como PUT /api/usuarios/<id>/foto
     * - Si requiere Multipart, realiza fallback automático enviando el archivo físico en el campo "foto".
     */
    public static void subirFotoUsuario(
            @NonNull Context context,
            int idUsuario,
            @NonNull Uri imageUri,
            boolean esMiPerfil,
            @NonNull OnFotoActualizadaListener listener
    ) {
        asegurarToken(context);

        try {
            InputStream is = context.getContentResolver().openInputStream(imageUri);
            Bitmap original = BitmapFactory.decodeStream(is);
            if (is != null) is.close();

            if (original == null) {
                listener.onError("No se pudo leer la imagen seleccionada");
                return;
            }

            // Redimensionar a un tamaño óptimo (máx 600px) para que el código Base64 sea ligero y compatible con la BD y la Web
            int maxDim = 600;
            int w = original.getWidth();
            int h = original.getHeight();
            Bitmap scaled = original;
            if (w > maxDim || h > maxDim) {
                float ratio = Math.min((float) maxDim / w, (float) maxDim / h);
                scaled = Bitmap.createScaledBitmap(original, Math.round(w * ratio), Math.round(h * ratio), true);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] jpegBytes = baos.toByteArray();

            String base64Raw = Base64.encodeToString(jpegBytes, Base64.NO_WRAP);
            String dataUri = "data:image/jpeg;base64," + base64Raw;

            final Bitmap finalBitmap = scaled;
            final int targetUserId = idUsuario > 0 ? idUsuario : resolveMyUserId(context);

            ActualizarFotoUsuarioRequest request = new ActualizarFotoUsuarioRequest(dataUri);
            ApiService apiService = RetrofitClient.createService();

            Call<ApiResponse<Usuario>> call = esMiPerfil
                    ? apiService.actualizarFotoMiPerfil(request)
                    : apiService.actualizarFotoUsuario(targetUserId, request);

            call.enqueue(new Callback<ApiResponse<Usuario>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Usuario>> call, @NonNull Response<ApiResponse<Usuario>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                        Usuario data = response.body().getData();
                        String nuevaRuta = (data != null && data.getFotoPerfil() != null && !data.getFotoPerfil().isEmpty())
                                ? data.getFotoPerfil()
                                : dataUri;
                        int resolvedId = (data != null && data.getIdUsuario() > 0) ? data.getIdUsuario() : targetUserId;

                        guardarEnCaches(context, resolvedId, esMiPerfil, finalBitmap, dataUri, nuevaRuta);

                        if (esMiPerfil && resolvedId > 0) {
                            apiService.actualizarFotoUsuario(resolvedId, request).enqueue(new Callback<ApiResponse<Usuario>>() {
                                @Override public void onResponse(@NonNull Call<ApiResponse<Usuario>> c, @NonNull Response<ApiResponse<Usuario>> r) {}
                                @Override public void onFailure(@NonNull Call<ApiResponse<Usuario>> c, @NonNull Throwable t) {}
                            });
                        }

                        String msg = response.body().getMessage() != null
                                ? response.body().getMessage()
                                : "Foto de perfil actualizada correctamente";
                        listener.onSuccess(finalBitmap, nuevaRuta, msg);
                    } else {
                        intentarSubidaMultipart(context, targetUserId, jpegBytes, dataUri, finalBitmap, esMiPerfil, listener);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Usuario>> call, @NonNull Throwable t) {
                    intentarSubidaMultipart(context, targetUserId, jpegBytes, dataUri, finalBitmap, esMiPerfil, listener);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error preparando foto para subir", e);
            listener.onError("Error al procesar la imagen: " + e.getMessage());
        }
    }

    private static void intentarSubidaMultipart(
            Context context,
            int idUsuario,
            byte[] jpegBytes,
            String dataUri,
            Bitmap finalBitmap,
            boolean esMiPerfil,
            OnFotoActualizadaListener listener
    ) {
        try {
            File tempFile = new File(context.getCacheDir(), "upload_user_" + (idUsuario > 0 ? idUsuario : "me") + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(jpegBytes);
            }

            RequestBody reqFile = RequestBody.create(MediaType.parse("image/jpeg"), tempFile);
            MultipartBody.Part bodyPart = MultipartBody.Part.createFormData("foto", tempFile.getName(), reqFile);

            ApiService apiService = RetrofitClient.createService();
            Call<ApiResponse<Usuario>> call = (esMiPerfil && idUsuario <= 0)
                    ? apiService.subirFotoMiPerfilMultipart(bodyPart)
                    : (idUsuario > 0 ? apiService.subirFotoUsuarioMultipart(idUsuario, bodyPart) : apiService.subirFotoMiPerfilMultipart(bodyPart));

            call.enqueue(new Callback<ApiResponse<Usuario>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Usuario>> call, @NonNull Response<ApiResponse<Usuario>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Usuario data = response.body().getData();
                        String nuevaRuta = (data != null && data.getFotoPerfil() != null && !data.getFotoPerfil().isEmpty())
                                ? data.getFotoPerfil()
                                : dataUri;
                        int resolvedId = (data != null && data.getIdUsuario() > 0) ? data.getIdUsuario() : idUsuario;

                        guardarEnCaches(context, resolvedId, esMiPerfil, finalBitmap, dataUri, nuevaRuta);
                        String msg = response.body().getMessage() != null
                                ? response.body().getMessage()
                                : "Foto de perfil actualizada correctamente";
                        listener.onSuccess(finalBitmap, nuevaRuta, msg);
                    } else if (esMiPerfil && idUsuario > 0) {
                        apiService.actualizarFotoUsuario(idUsuario, new ActualizarFotoUsuarioRequest(dataUri)).enqueue(new Callback<ApiResponse<Usuario>>() {
                            @Override
                            public void onResponse(@NonNull Call<ApiResponse<Usuario>> c, @NonNull Response<ApiResponse<Usuario>> r) {
                                if (r.isSuccessful() && r.body() != null) {
                                    guardarEnCaches(context, idUsuario, true, finalBitmap, dataUri, dataUri);
                                    listener.onSuccess(finalBitmap, dataUri, "Foto de perfil actualizada correctamente");
                                } else {
                                    listener.onError("No se pudo actualizar la foto en el servidor (HTTP " + r.code() + ")");
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ApiResponse<Usuario>> c, @NonNull Throwable t) {
                                listener.onError("Error de red al actualizar la foto: " + t.getMessage());
                            }
                        });
                    } else {
                        listener.onError("No se pudo actualizar la foto en el servidor (HTTP " + response.code() + ")");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Usuario>> call, @NonNull Throwable t) {
                    listener.onError("Error de red al subir la foto: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            listener.onError("Error al enviar archivo de foto: " + e.getMessage());
        }
    }

    private static void guardarEnCaches(Context context, int idUsuario, boolean esMiPerfil, Bitmap bitmap, String dataUri, String nuevaRuta) {
        invalidateUserPhoto(idUsuario);
        if (idUsuario > 0) {
            MEMORY_CACHE.put(cacheKeyForUser(idUsuario), bitmap);
            SplashStorage.guardarFotoUsuarioBase64Cache(context, idUsuario, dataUri);
            PHOTO_BY_ID.put(idUsuario, dataUri);
        }
        if (esMiPerfil || idUsuario == SplashStorage.obtenerUsuarioId(context)) {
            MEMORY_CACHE.put("my_profile", bitmap);
            SplashStorage.guardarFotoUsuario(context, dataUri);
            if (idUsuario > 0 && SplashStorage.obtenerUsuarioId(context) <= 0) {
                SplashStorage.guardarUsuarioId(context, idUsuario);
            }
        }
    }
}
