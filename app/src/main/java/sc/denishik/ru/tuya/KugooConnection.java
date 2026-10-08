package sc.denishik.ru.tuya;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import com.google.gson.Gson;
import com.thingclips.sdk.core.PluginManager;
import com.thingclips.smart.android.ble.builder.BleConnectBuilder;
import com.thingclips.smart.android.ble.bean.BleOTABean;
import com.thingclips.smart.android.ble.api.OnBleUpgradeListener;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.interior.api.IThingDevicePlugin;
import com.thingclips.smart.interior.device.IThingDevListCacheManager;
import com.thingclips.smart.interior.device.bean.DeviceRespBean;
import com.thingclips.smart.sdk.api.IDevListener;
import com.thingclips.smart.sdk.api.IResultCallback;
import com.thingclips.smart.sdk.api.IThingDevice;
import org.json.JSONObject;
import java.util.Collections;
import sc.denishik.ru.BuildConfig;

/** Main-thread session shared by the diagnostic screen and the scooter service. */
public final class KugooConnection {
    public interface Listener {
        void onStatus(boolean online, String message);
        void onData(JSONObject dps);
        void onError(String message);
    }
    private static boolean sdkReady;
    private static KugooConnection owner;
    private final Application application;
    private final Listener listener;
    private final Handler main = new Handler(Looper.getMainLooper());
    private IThingDevice device;
    private BleConnectBuilder builder;
    private JSONObject profile;
    private JSONObject dps = new JSONObject();
    private boolean connecting, online, retryPending;
    private int attempts, generation;
    private long deadline;
    private FirmwareListener firmwareListener;
    private java.io.File firmwareFile;
    private Thread firmwarePreparation;
    private int firmwareGeneration;
    private boolean firmwareStarted;

    public interface FirmwareListener {
        void onProgress(int percent);
        /** SDK transfer succeeded; device firmware version still needs to be checked after reboot. */
        void onSuccess();
        void onError(String code, String message);
    }

    public boolean isUpdatingFirmware() { return firmwareListener != null; }

    /** Main-thread API. No update is started by connecting or by opening a screen. */
    public void startFirmwareUpdate(KugooFirmwarePackage firmware, FirmwareListener callback) {
        if (Looper.myLooper() != Looper.getMainLooper()) throw new IllegalStateException("Main thread required");
        if (firmware == null || callback == null) throw new IllegalArgumentException("Firmware and callback required");
        if (isUpdatingFirmware()) { callback.onError("BUSY", "Обновление уже выполняется"); return; }
        if (!online || builder == null || profile == null || !bluetoothEnabled()
                || !ThingHomeSdk.getBleManager().isBleLocalOnline(builder.getDevId())) {
            callback.onError("OFFLINE", "Подключите самокат перед обновлением"); return;
        }
        if (Boolean.TRUE.equals(ThingHomeSdk.getBleManager().isDeviceInOta(builder.getDevId()))) {
            callback.onError("BUSY", "Устройство уже обновляется"); return;
        }
        final int update = ++firmwareGeneration;
        final String productId = profile.optString("productId");
        final String uuid = profile.optString("uuid");
        final String devId = builder.getDevId();
        firmwareListener = callback;
        main.removeCallbacks(monitor);
        firmwarePreparation = new Thread(() -> {
            try {
                java.io.File prepared = firmware.prepare(application.getCacheDir(), productId);
                main.post(() -> {
                    if (update != firmwareGeneration || !isUpdatingFirmware()) { prepared.delete(); return; }
                    firmwarePreparation = null;
                    firmwareFile = prepared;
                    try {
                        if (!bluetoothEnabled() || !ThingHomeSdk.getBleManager().isBleLocalOnline(devId)) {
                            finishFirmware("OFFLINE", "Соединение потеряно до начала обновления"); return;
                        }
                        // Same BLE OTA transport as Kugoo Export's BleOtaUseCase.c().
                        // BLE types 0/1 are different from cloud channel IDs 1/9.
                        BleOTABean bean = new BleOTABean(uuid, firmware.target.bleType,
                                firmware.version, prepared.getAbsolutePath(), null, devId);
                        bean.setPid(productId);
                        firmwareStarted = true;
                        main.postDelayed(firmwareTimeout, 180000);
                        ThingHomeSdk.getBleManager().startBleOta(bean, new OnBleUpgradeListener() {
                            public void onUpgrade(int percent) {
                                main.post(() -> {
                                    if (update != firmwareGeneration || !isUpdatingFirmware()) return;
                                    main.removeCallbacks(firmwareTimeout);
                                    main.postDelayed(firmwareTimeout, 180000);
                                    firmwareListener.onProgress(Math.max(0, Math.min(100, percent)));
                                });
                            }
                            public void onSuccess() {
                                main.post(() -> { if (update == firmwareGeneration) finishFirmware(null, null); });
                            }
                            public void onFail(String code, String message) {
                                main.post(() -> { if (update == firmwareGeneration)
                                    finishFirmware(code == null ? "OTA_FAILED" : code, "SDK не завершил обновление прошивки"); });
                            }
                        });
                    } catch (Exception | LinkageError error) {
                        finishFirmware("OTA_START", "Не удалось запустить обновление прошивки");
                    }
                });
            } catch (Exception error) {
                main.post(() -> { if (update == firmwareGeneration)
                    finishFirmware("INVALID_PACKAGE", "Проверьте файл, Product ID, размер и MD5 прошивки"); });
            }
        }, "KugooFirmwarePrepare");
        firmwarePreparation.start();
    }

    private final Runnable firmwareTimeout = () -> finishFirmware("OTA_TIMEOUT", "Нет ответа при обновлении прошивки");

    /** Cancels the transfer, not a rollback of bytes already written to the device. */
    public void cancelFirmwareUpdate() {
        if (Looper.myLooper() != Looper.getMainLooper()) throw new IllegalStateException("Main thread required");
        finishFirmware("CANCELLED", "Обновление прервано");
    }

    private void finishFirmware(String code, String message) {
        FirmwareListener callback = firmwareListener;
        if (callback == null) return;
        firmwareListener = null;
        firmwareGeneration++;
        main.removeCallbacks(firmwareTimeout);
        if (firmwarePreparation != null) { firmwarePreparation.interrupt(); firmwarePreparation = null; }
        if (code != null && firmwareStarted && builder != null) {
            try { ThingHomeSdk.getBleManager().cancelBleOta(builder.getDevId()); } catch (Exception ignored) { }
        }
        firmwareStarted = false;
        if (firmwareFile != null) { firmwareFile.delete(); firmwareFile = null; }
        if (connecting) {
            online = false;
            retryPending = false;
            deadline = SystemClock.elapsedRealtime() + 20000;
            main.removeCallbacks(monitor);
            main.postDelayed(monitor, 1000);
        }
        if (code == null) callback.onSuccess(); else callback.onError(code, message);
    }
    public KugooConnection(Application application, Listener listener) {
        this.application = application;
        this.listener = listener;
    }
    public void connect(JSONObject profile) {
        if (owner != null && owner.isUpdatingFirmware()) {
            listener.onError("Дождитесь завершения обновления прошивки");
            return;
        }
        disconnect();
        if (owner != null && owner != this) {
            KugooConnection previous = owner;
            previous.disconnect();
            previous.listener.onError("Подключение передано другому экрану");
        }
        owner = this;
        this.profile = profile;
        BluetoothManager manager = (BluetoothManager) application.getSystemService(Context.BLUETOOTH_SERVICE);
        if (Build.VERSION.SDK_INT >= 31 && (application.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                || application.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)) {
            fail("Разрешите доступ к устройствам поблизости и повторите подключение"); return;
        }
        if (Build.VERSION.SDK_INT < 31 && application.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            fail("Разрешите геопозицию для BLE-поиска"); return;
        }
        if (manager == null || manager.getAdapter() == null) {
            fail("Bluetooth недоступен на телефоне"); return;
        }
        try {
            if (!sdkReady) {
                if (BuildConfig.TUYA_APP_KEY.trim().isEmpty() || BuildConfig.TUYA_APP_SECRET.trim().isEmpty()) {
                    throw new IllegalStateException("SDK keys unavailable");
                }
                ThingHomeSdk.init(application, BuildConfig.TUYA_APP_KEY, BuildConfig.TUYA_APP_SECRET);
                ThingHomeSdk.setDebugMode(false);
                sdkReady = true;
            }
            IThingDevicePlugin plugin = PluginManager.service(IThingDevicePlugin.class);
            if (plugin == null) throw new IllegalStateException("Device plugin unavailable");
            IThingDevListCacheManager cache = plugin.getDevListCacheManager();
            JSONObject product = profile.optJSONObject("_product");
            if (product == null) product = profile.optJSONObject("productBean");
            if (product != null) {
                com.thingclips.smart.sdk.bean.ProductBean productBean = new Gson().fromJson(product.toString(), com.thingclips.smart.sdk.bean.ProductBean.class);
                if (!profile.getString("productId").equals(productBean.getId())) throw new IllegalArgumentException("Product mismatch");
                plugin.getThingSmartDeviceInstance().addProductList(Collections.singletonList(productBean));
            }
            DeviceRespBean bean = new Gson().fromJson(profile.toString(), DeviceRespBean.class);
            cache.addDev(bean);
            String id = profile.getString("devId");
            if (cache.getDev(id) == null) throw new IllegalStateException("Unsupported device record");
            builder = new BleConnectBuilder().setDevId(id).setScanTimeout(15000).setDirectConnect(false);
            String address = profile.optString("_bleAddress");
            if (BluetoothAdapter.checkBluetoothAddress(address) && profile.optInt("_bleType", -1) >= 0) {
                BleConnectBuilder.ExtInfo info = new BleConnectBuilder.ExtInfo();
                info.address = address;
                info.deviceType = profile.optInt("_bleType");
                info.flag = profile.optInt("_bleFlag");
                builder.setExtInfo(info).setDirectConnect(true);
            }
            device = ThingHomeSdk.newDeviceInstance(id);

            final int session = generation;
            device.registerDevListener(new IDevListener() {
                public void onDpUpdate(String devId, String update) {
                    main.post(() -> {
                        if (session != generation || !connecting) return;
                        try {
                            JSONObject values = new JSONObject(update);
                            java.util.Iterator<String> keys = values.keys();
                            while (keys.hasNext()) { String key = keys.next(); dps.put(key, values.get(key)); }
                            listener.onData(new JSONObject(dps.toString()));
                        } catch (org.json.JSONException ignored) { }
                    });
                }
                public void onRemoved(String id) { main.post(() -> { if (session == generation) fail("Привязка удалена. Повторите импорт Kugoo"); }); }
                public void onStatusChanged(String id, boolean online) { }
                public void onNetworkStatusChanged(String id, boolean online) { }
                public void onDevInfoUpdate(String id) { }
            });
            dps = new JSONObject();
            connecting = true;
            attempts = 0;
            startAttempt();
            main.postDelayed(monitor, 500);
        } catch (Exception | LinkageError error) {
            fail("Не удалось подключиться через SDK: " + error.getClass().getSimpleName());
        }
    }
    private void startAttempt() {
        if (!bluetoothEnabled()) {
            waitingForBluetooth = true;
            listener.onStatus(false, "Включите Bluetooth — подключение восстановится автоматически");
            return;
        }
        retryPending = false;
        attempts++;
        deadline = SystemClock.elapsedRealtime() + 20000;
        listener.onStatus(false, "Подключение Kugoo: попытка " + attempts + "…");
        android.util.Log.i("KugooConnection", "BLE attempt " + attempts);
        ThingHomeSdk.getBleManager().connectBleDevice(Collections.singletonList(builder));
    }
    private boolean waitingForBluetooth;
    private boolean bluetoothEnabled() {
        BluetoothManager manager = (BluetoothManager) application.getSystemService(Context.BLUETOOTH_SERVICE);
        return manager != null && manager.getAdapter() != null && manager.getAdapter().isEnabled();
    }
    private final Runnable monitor = new Runnable() {
        public void run() {
            if (!connecting || builder == null) return;
            if (isUpdatingFirmware()) return;
            try {
                if (!bluetoothEnabled()) {
                    if (!waitingForBluetooth) {
                        online = false;
                        scheduleRetry();
                        waitingForBluetooth = true;
                        listener.onStatus(false, "Включите Bluetooth — подключение восстановится автоматически");
                    }
                    main.postDelayed(this, 1000);
                    return;
                }
                if (waitingForBluetooth) {
                    waitingForBluetooth = false;
                    attempts = 0;
                    builder.setDirectConnect(false);
                    startAttempt();
                }
                boolean current = ThingHomeSdk.getBleManager().isBleLocalOnline(builder.getDevId());
                if (current && !online) {
                    online = true;
                    attempts = 0;
                    android.util.Log.i("KugooConnection", "BLE online; requesting data");
                    listener.onStatus(true, "Kugoo подключён");
                    queryData();
                } else if (!current && online) {
                    online = false;
                    attempts = 0;
                    scheduleRetry();
                } else if (!current && SystemClock.elapsedRealtime() >= deadline) {
                    if (retryPending) startAttempt(); else scheduleRetry();
                }
            } catch (SecurityException error) {
                fail("Разрешите доступ к Bluetooth и откройте приложение снова"); return;
            } catch (Exception error) {
                online = false;
                scheduleRetry();
            } catch (LinkageError error) {
                fail("Ошибка BLE SDK: " + error.getClass().getSimpleName()); return;
            }
            main.postDelayed(this, 500);
        }
    };
    private void scheduleRetry() {
        try { ThingHomeSdk.getBleManager().disconnectBleDevice(Collections.singletonList(builder)); }
        catch (Exception ignored) { }
        retryPending = true;
        builder.setDirectConnect(false);
        deadline = SystemClock.elapsedRealtime() + Math.min(10000L, 2000L * Math.max(1, attempts));
        listener.onStatus(false, "Восстановление связи с Kugoo…");
    }
    public boolean isOnline() { return online; }
    public boolean isActive() { return connecting; }
    public void setHeadlight(boolean enabled, IResultCallback callback) {
        if (isUpdatingFirmware()) { callback.onError("OTA_BUSY", "Выполняется обновление прошивки"); return; }
        if (!online || device == null) {
            callback.onError("OFFLINE", "Самокат не подключён");
            return;
        }
        final int session = generation;
        try {
            String command = new KugooTelemetry(profile).headlightCommand(enabled).toString();
            ThingHomeSdk.getBleManager().publishDps(builder.getDevId(), command,
                    new IResultCallback() {
                        public void onSuccess() {
                            main.post(() -> {
                                if (session != generation) return;
                                queryData();
                                callback.onSuccess();
                            });
                        }
                        public void onError(String code, String error) {
                            android.util.Log.w("KugooConnection", "Headlight command failed: " + code);
                            main.post(() -> {
                                if (session == generation) callback.onError(code, "Не удалось переключить фонарь");
                            });
                        }
                    });
        } catch (Exception error) {
            callback.onError("HEADLIGHT", "Управление фонарём недоступно");
        }
    }

    public void setParking(boolean enabled, IResultCallback callback) {
        if (isUpdatingFirmware()) { callback.onError("OTA_BUSY", "Выполняется обновление прошивки"); return; }
        if (!online || device == null) {
            callback.onError("OFFLINE", "Самокат не подключён");
            return;
        }
        final int session = generation;
        try {
            String command = new KugooTelemetry(profile).parkingCommand(enabled).toString();
            ThingHomeSdk.getBleManager().publishDps(builder.getDevId(), command,
                    new IResultCallback() {
                        public void onSuccess() {
                            main.post(() -> {
                                if (session != generation) return;
                                queryData();
                                callback.onSuccess();
                            });
                        }
                        public void onError(String code, String error) {
                            android.util.Log.w("KugooConnection", "Parking command failed: " + code);
                            main.post(() -> {
                                if (session == generation) callback.onError(code, "Не удалось переключить паркинг");
                            });
                        }
                    });
        } catch (Exception error) {
            callback.onError("PARKING", "Управление паркингом недоступно");
        }
    }

    public void queryData() {
        if (isUpdatingFirmware()) return;
        if (!online || builder == null) return;
        final int session = generation;
        ThingHomeSdk.getBleManager().queryDps(builder.getDevId(), null, new IResultCallback() {
            public void onSuccess() { }
            public void onError(String code, String error) {
                if (session == generation) android.util.Log.w("KugooConnection", "DP query failed; waiting for notifications");
            }
        });
    }
    public void disconnect() {
        generation++;
        connecting = false;
        finishFirmware("DISCONNECTED", "Соединение закрыто во время обновления");
        online = false;
        waitingForBluetooth = false;
        // Let preparation callbacks run: stale ones delete their private firmware copy.
        main.removeCallbacks(monitor);
        if (device != null) { try { device.unRegisterDevListener(); } catch (Exception ignored) { } device = null; }
        if (builder != null) { try { ThingHomeSdk.getBleManager().disconnectBleDevice(Collections.singletonList(builder)); } catch (Exception ignored) { } builder = null; }
        if (owner == this) owner = null;
    }
    private void fail(String message) { disconnect(); listener.onError(message); }
}
