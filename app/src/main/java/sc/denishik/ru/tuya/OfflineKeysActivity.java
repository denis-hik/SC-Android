package sc.denishik.ru.tuya;

import android.Manifest;
import android.bluetooth.BluetoothManager;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import sc.denishik.ru.R;

/** An isolated SDK connection screen; it does not replay captured encrypted GATT packets. */
public class OfflineKeysActivity extends AppCompatActivity {
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private TextView result, profileView, statusView, connectionState;
    private EditText input;
    private Button rootButton, importButton, connectButton;
    private JSONObject profile;
    private KugooConnection session;
    private List<JSONObject> importedProfiles;
    private boolean busy;
    private static final int IMPORT_FILE = 71, EXPORT_FILE = 72;

    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_offline_keys);
        input = findViewById(R.id.global_preference_table);
        result = findViewById(R.id.result);
        profileView = findViewById(R.id.kugoo_profile);
        statusView = findViewById(R.id.offline_status);
        connectionState = findViewById(R.id.offline_connection_state);
        ((com.google.android.material.appbar.MaterialToolbar) findViewById(R.id.offline_toolbar))
                .setNavigationOnClickListener(v -> finish());
        findViewById(R.id.offline_manual_toggle).setOnClickListener(v -> toggleSection(
                R.id.offline_manual_content, R.id.offline_manual_toggle, R.string.offline_manual, R.string.offline_manual_hide));
        findViewById(R.id.offline_diagnostics_toggle).setOnClickListener(v -> toggleSection(
                R.id.result, R.id.offline_diagnostics_toggle, R.string.offline_diagnostics, R.string.offline_diagnostics_hide));
        findViewById(R.id.offline_choose_profile).setOnClickListener(v -> loadProfiles());
        rootButton = findViewById(R.id.go);
        importButton = findViewById(R.id.import_kugoo_json);
        connectButton = findViewById(R.id.connect_kugoo_offline);
        updateProfile();
        statusView.setVisibility(View.GONE);
        if (state != null) {
            if (state.getBoolean("manual_open")) toggleSection(R.id.offline_manual_content,
                    R.id.offline_manual_toggle, R.string.offline_manual, R.string.offline_manual_hide);
            if (state.getBoolean("diagnostics_open")) toggleSection(R.id.result,
                    R.id.offline_diagnostics_toggle, R.string.offline_diagnostics, R.string.offline_diagnostics_hide);
        }
        findViewById(R.id.import_kugoo_file).setOnClickListener(v -> startActivityForResult(
                new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT)
                        .addCategory(android.content.Intent.CATEGORY_OPENABLE).setType("*/*"), IMPORT_FILE));
        findViewById(R.id.export_kugoo_file).setOnClickListener(v -> startActivityForResult(
                new android.content.Intent(android.content.Intent.ACTION_CREATE_DOCUMENT)
                        .addCategory(android.content.Intent.CATEGORY_OPENABLE).setType("application/json")
                        .putExtra(android.content.Intent.EXTRA_TITLE, "kugoo-connections.json"), EXPORT_FILE));
        findViewById(R.id.share_kugoo_file).setOnClickListener(v -> shareConnections());
        rootButton.setOnClickListener(v -> importData(true));
        importButton.setOnClickListener(v -> importData(false));
        connectButton.setOnClickListener(v -> connect());
        findViewById(R.id.disconnect_kugoo_offline).setOnClickListener(v -> { disconnect(); show("Отключено"); });
        busy(true);
        worker.execute(() -> {
            try {
                JSONObject saved = KugooProfileStore.load(getApplicationContext());
                main.post(() -> { if (!isDestroyed()) { profile = saved; updateProfile(); busy(false); } });
            } catch (Exception error) { postError("Не удалось прочитать сохранённый профиль. Повторите импорт."); }
        });
    }

    private void importData(boolean root) {
        disconnect();
        String json = input.getText().toString();
        busy(true);
        show(root ? "Чтение Kugoo… Разрешите root для SC-Android (до 40 секунд)." : "Проверка JSON…");
        worker.execute(() -> {
            try {
                List<JSONObject> devices = root ? KugooOfflineImport.fromRoot(getApplicationContext()) : KugooOfflineImport.fromJson(json);
                main.post(() -> {
                    if (isDestroyed()) return;
                    busy(false);
                    chooseProfiles(devices);
                });
            } catch (Exception error) {
                postError(error instanceof java.io.IOException ? error.getMessage() : "Импорт не выполнен: " + error.getClass().getSimpleName());
            }
        });
    }

    private void chooseProfiles(List<JSONObject> devices) {
        if (devices.isEmpty()) { show("Сохранённых самокатов нет. Добавьте подключение."); return; }
        importedProfiles = devices;
        sc.denishik.ru.other.Modals.showPrimaryKugoo(this, devices,
                profile == null ? "" : profile.optString("devId"), this::saveProfile);
    }

    @Override protected void onActivityResult(int request, int resultCode, android.content.Intent data) {
        super.onActivityResult(request, resultCode, data);
        if ((request != IMPORT_FILE && request != EXPORT_FILE) || resultCode != RESULT_OK
                || data == null || data.getData() == null) return;
        android.net.Uri uri = data.getData();
        if (request == IMPORT_FILE) disconnect();
        busy(true);
        show(request == IMPORT_FILE ? "Чтение файла…" : "Экспорт подключений…");
        worker.execute(() -> {
            try {
                if (request == EXPORT_FILE) {
                    List<JSONObject> profiles = KugooProfileStore.loadAll(getApplicationContext());
                    if (profiles.isEmpty()) throw new java.io.IOException("Нет сохранённых подключений");
                    byte[] bytes = KugooTransferFile.encode(profiles).getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    try (java.io.OutputStream out = getContentResolver().openOutputStream(uri, "wt")) {
                        if (out == null) throw new java.io.IOException("Файл недоступен");
                        out.write(bytes);
                    }
                    main.post(() -> { if (!isDestroyed()) { busy(false); show("Подключения экспортированы. На другом телефоне выберите «Импортировать из файла»."); } });
                } else {
                    String json;
                    try (java.io.InputStream in = getContentResolver().openInputStream(uri)) {
                        if (in == null) throw new java.io.IOException("Файл недоступен");
                        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                        byte[] buffer = new byte[8192];
                        int n;
                        while ((n = in.read(buffer)) != -1) {
                            if (out.size() + n > 2 * 1024 * 1024) throw new java.io.IOException("Файл слишком большой (максимум 2 МБ)");
                            out.write(buffer, 0, n);
                        }
                        json = out.toString("UTF-8");
                    }
                    List<JSONObject> devices = KugooOfflineImport.fromJson(json);
                    main.post(() -> { if (!isDestroyed()) { busy(false); chooseProfiles(devices); } });
                }
            } catch (Exception error) {
                postError(request == IMPORT_FILE ? "Не удалось импортировать файл. Проверьте формат JSON подключения Kugoo."
                        : "Не удалось записать файл. Проверьте место сохранения и повторите экспорт.");
            }
        });
    }

    private void shareConnections() {
        if (busy || profile == null) return;
        busy(true);
        show(getString(R.string.offline_share_preparing));
        worker.execute(() -> {
            try {
                List<JSONObject> profiles = KugooProfileStore.loadAll(getApplicationContext());
                if (profiles.isEmpty()) throw new java.io.IOException("No saved connections");
                byte[] bytes = KugooTransferFile.encode(profiles).getBytes(java.nio.charset.StandardCharsets.UTF_8);
                // A separate cache file keeps an earlier share intact while its recipient reads it.
                java.io.File directory = new java.io.File(getCacheDir(),
                        "kugoo-transfer/" + java.util.UUID.randomUUID());
                if (!directory.mkdirs()) throw new java.io.IOException("Cannot create transfer directory");
                java.io.File file = new java.io.File(directory, "kugoo-connections.json");
                try (java.io.OutputStream out = new java.io.FileOutputStream(file)) {
                    out.write(bytes);
                }
                android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                        this, getPackageName() + ".kugoo.transfer", file);
                main.post(() -> {
                    if (isDestroyed() || isFinishing()) return;
                    busy(false);
                    statusView.setVisibility(View.GONE);
                    android.content.Intent send = new android.content.Intent(android.content.Intent.ACTION_SEND)
                            .setType("application/json")
                            .putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            .addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    send.setClipData(android.content.ClipData.newRawUri("kugoo-connections.json", uri));
                    try {
                        startActivity(android.content.Intent.createChooser(send, getString(R.string.offline_share_title)));
                    } catch (android.content.ActivityNotFoundException error) {
                        show(getString(R.string.offline_share_unavailable));
                    }
                });
            } catch (Exception error) {
                postError(getString(R.string.offline_share_error));
            }
        });
    }

    private void saveProfile(JSONObject selected) {
        disconnect();
        busy(true);
        worker.execute(() -> {
            try {
                KugooProfileStore.saveAll(getApplicationContext(), importedProfiles == null ? Collections.singletonList(selected) : importedProfiles, selected.getString("devId"));
                main.post(() -> {
                    if (isDestroyed()) return;
                    profile = selected;
                    input.setText("");
                    updateProfile();
                    busy(false);
                    show("Привязка сохранена. Включите самокат рядом с телефоном и нажмите «Подключить». Закройте Kugoo Riding, чтобы освободить BLE-соединение.");
                });
            } catch (Exception error) { android.util.Log.e("KugooProfile", "Profile save failed", error); postError("Не удалось сохранить профиль: " + error.getClass().getSimpleName()); }
        });
    }

    private void updateProfile() {
        findViewById(R.id.offline_scooter_panel).setVisibility(profile == null ? View.GONE : View.VISIBLE);
        findViewById(R.id.offline_scooter_background).setVisibility(profile == null ? View.GONE : View.VISIBLE);
        profileView.setText(profile == null ? getString(R.string.offline_empty) : label(profile));
        ((TextView) findViewById(R.id.offline_profile_hint)).setText(profile == null
                ? R.string.offline_empty_hint : R.string.offline_ready_hint);
        updateActions();
    }

    private void toggleSection(int content, int toggle, int collapsed, int expanded) {
        View section = findViewById(content);
        boolean open = section.getVisibility() != View.VISIBLE;
        section.setVisibility(open ? View.VISIBLE : View.GONE);
        ((Button) findViewById(toggle)).setText(open ? expanded : collapsed);
        if (!open && content == R.id.offline_manual_content) {
            input.clearFocus();
            ((android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(input.getWindowToken(), 0);
        }
    }

    private void loadProfiles() {
        if (busy) return;
        busy(true);
        worker.execute(() -> {
            try {
                List<JSONObject> saved = KugooProfileStore.loadAll(getApplicationContext());
                main.post(() -> { if (!isDestroyed()) { busy(false); chooseProfiles(saved); } });
            } catch (Exception error) { postError("Не удалось прочитать список самокатов."); }
        });
    }

    private static String label(JSONObject object) {
        String id = object.optString("devId");
        return object.optString("name", "Kugoo") + " · …" + id.substring(Math.max(0, id.length() - 6));
    }

    private void connect() {
        if (profile == null || busy) return;
        String[] permissions = Build.VERSION.SDK_INT >= 31
                ? new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}
                : new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, 41);
                show("Разрешите доступ к Bluetooth/геопозиции и повторите подключение.");
                return;
            }
        }
        BluetoothManager bluetooth = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        if (bluetooth == null || bluetooth.getAdapter() == null || !bluetooth.getAdapter().isEnabled()) {
            show("Включите Bluetooth и повторите подключение."); return;
        }
        if (Build.VERSION.SDK_INT < 31) {
            LocationManager location = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (location != null && !location.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    && !location.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                show("Включите геопозицию: она требуется этой версии Android для BLE-поиска."); return;
            }
        }
        disconnect();
        session = new KugooConnection(getApplication(), new KugooConnection.Listener() {
            public void onStatus(boolean online, String message) {
                connectionState.setText(online ? R.string.offline_online : R.string.offline_linking);
                show(message);
                updateActions();
            }
            public void onData(JSONObject dps) {
                try { result.setText(dps.toString(2)); } catch (Exception ignored) { }
            }
            public void onError(String message) {
                connectionState.setText(R.string.offline_error);
                show(message);
                updateActions();
            }
        });
        session.connect(profile);
    }
    private void disconnect() {
        if (session != null) { session.disconnect(); session = null; }
        if (connectionState != null) {
            connectionState.setText(R.string.offline_idle);
            result.setText(R.string.offline_data_empty);
            updateActions();
        }
    }

    private void busy(boolean value) {
        busy = value;
        findViewById(R.id.offline_progress).setVisibility(value ? View.VISIBLE : View.GONE);
        rootButton.setEnabled(!value);
        findViewById(R.id.import_kugoo_file).setEnabled(!value);
        findViewById(R.id.export_kugoo_file).setEnabled(!value && profile != null);
        findViewById(R.id.share_kugoo_file).setEnabled(!value && profile != null);
        importButton.setEnabled(!value);
        input.setEnabled(!value);
        updateActions();
    }
    private void updateActions() {
        boolean active = session != null && session.isActive();
        connectButton.setEnabled(!busy && profile != null && !active);
        connectButton.setText(active ? (session.isOnline() ? R.string.offline_online : R.string.offline_linking) : R.string.offline_connect);
        findViewById(R.id.disconnect_kugoo_offline).setVisibility(active ? View.VISIBLE : View.GONE);
        findViewById(R.id.offline_choose_profile).setVisibility(profile == null ? View.GONE : View.VISIBLE);
        findViewById(R.id.offline_choose_profile).setEnabled(!busy);
    }
    private void show(String text) {
        if (!isDestroyed()) { statusView.setVisibility(View.VISIBLE); statusView.setText(text); }
    }
    private void postError(String text) { main.post(() -> { if (!isDestroyed()) { busy(false); show(text); } }); }

    @Override protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("manual_open", findViewById(R.id.offline_manual_content).getVisibility() == View.VISIBLE);
        outState.putBoolean("diagnostics_open", result.getVisibility() == View.VISIBLE);
        super.onSaveInstanceState(outState);
    }

    @Override protected void onDestroy() {
        disconnect();
        main.removeCallbacksAndMessages(null);
        worker.shutdownNow();
        super.onDestroy();
    }
}
