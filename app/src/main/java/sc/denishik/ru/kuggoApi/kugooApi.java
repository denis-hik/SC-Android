package sc.denishik.ru.kuggoApi;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.thingclips.smart.android.ble.api.BleScanResponse;
import com.thingclips.smart.android.ble.api.LeScanSetting;
import com.thingclips.smart.android.ble.api.ScanDeviceBean;
import com.thingclips.smart.android.ble.api.ScanType;
import com.thingclips.smart.android.user.api.ILoginCallback;
import com.thingclips.smart.android.user.bean.User;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.home.sdk.bean.HomeBean;
import com.thingclips.smart.home.sdk.callback.IThingGetHomeListCallback;
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback;
import com.thingclips.smart.sdk.api.IBleActivator;
import com.thingclips.smart.sdk.api.IBleActivatorListener;
import com.thingclips.smart.sdk.bean.BleActivatorBean;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.security.MessageDigest;

import sc.denishik.ru.BuildConfig;
import sc.denishik.ru.ServiceScooter;
import sc.denishik.ru.other.Config;

public class kugooApi {
    private static final String TAG = "kugooApi";
    private static final UUID TUYA_FD50_SERVICE = UUID.fromString("0000fd50-0000-1000-8000-00805f9b34fb");
    private static final int TUYA_MFG_COMPANY_ID = 0x07D0; // little-endian bytes: D0 07

    public interface FindScootersCallback {
        void onResult(ArrayList<Scooter> scooters);

        void onError(String error);
    }

    public interface BindCallback {
        void onSuccess(String devId);

        void onError(String error);
    }

    public interface ProductInfoCallback {
        void onResult(String productName, String iconUrl);

        void onError(String error);
    }

    private final Application application;

    public kugooApi(Application application) {
        this.application = application;
    }

    public void findScooters(int timeoutSeconds, FindScootersCallback callback) {
        if (callback == null) return;
        if (application == null) {
            callback.onError("application is null");
            return;
        }

        Map<String, Scooter> seen = Collections.synchronizedMap(new LinkedHashMap<>());
        int t = Math.max(3, timeoutSeconds);
        final int[] scanSeenCount = {0};
        final int[] scanAddedCount = {0};
        final int[] nativeSeenCount = {0};
        final int[] nativeAddedCount = {0};

        // 0) Native Android BLE scan fallback (independent of Thing SDK internal scanner)
        final BluetoothLeScanner[] nativeScannerRef = {null};
        final ScanCallback[] nativeCallbackRef = {null};
        try {
            BluetoothManager bm = (BluetoothManager) application.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter adapter = bm == null ? null : bm.getAdapter();
            BluetoothLeScanner nativeScanner = adapter == null ? null : adapter.getBluetoothLeScanner();
            nativeScannerRef[0] = nativeScanner;
            if (nativeScanner != null) {
                ScanSettings settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                BluetoothLeScanner finalNativeScanner = nativeScanner;
                ScanCallback nativeCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        if (result == null || result.getDevice() == null) return;
                        nativeSeenCount[0]++;

                        String addr = safe(result.getDevice().getAddress());
                        if (addr.isEmpty()) return;

                        ScanRecord record = result.getScanRecord();
                        byte[] raw = record != null ? record.getBytes() : null;
                        boolean hasFd50 = false;
                        if (record != null) {
                            if (record.getServiceUuids() != null) {
                                for (android.os.ParcelUuid pu : record.getServiceUuids()) {
                                    if (pu != null && TUYA_FD50_SERVICE.equals(pu.getUuid())) {
                                        hasFd50 = true;
                                        break;
                                    }
                                }
                            }
                            // Some firmwares don't advertise ServiceUUID; keep raw record for later reverse.
                            if (!hasFd50) {
                                byte[] bytes = raw;
                                if (bytes != null && bytes.length > 0) {
                                    // Heuristic: look for 0x50 0xFD (little-endian 16-bit UUID) inside AD structures
                                    for (int i = 0; i < bytes.length - 1; i++) {
                                        if ((bytes[i] & 0xFF) == 0x50 && (bytes[i + 1] & 0xFF) == 0xFD) {
                                            hasFd50 = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (!hasFd50) return;

                        String name = "";
                        if (android.os.Build.VERSION.SDK_INT < 31 || application.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            try { name = safe(result.getDevice().getName()); }
                            catch (SecurityException denied) { Log.w(TAG, "Bluetooth name permission revoked"); }
                        }
                        if (name.isEmpty() && record != null) name = safe(record.getDeviceName());
                        if (name.isEmpty()) name = "TY";

                        String productId = parseTuyaProductIdFromAdv(raw);
                        String deviceUuid = parseTuyaDeviceUuidFromAdv(raw);
                        if (BuildConfig.DEBUG && !productId.isEmpty()) {
                            Log.d(TAG, "native parsed productId=" + productId + " addr=" + addr);
                        }

                        if (BuildConfig.DEBUG && record != null) {
                            Log.d(TAG, "native scan: addr=" + addr + " name=" + name + " rssi=" + result.getRssi()
                                    + " rec=" + bytesToHex(raw));
                        }

                        Scooter s = new Scooter(name + " [fd50]", productId, addr, null, false, "", 400, raw, result.getRssi());
                        s.setDeviceUuid(deviceUuid);
                        seen.put(addr, s);
                        nativeAddedCount[0]++;
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        Log.w(TAG, "native scan failed: " + errorCode);
                        try {
                            stopNativeScan(finalNativeScanner, this);
                        } catch (Throwable ignored) {
                        }
                    }
                };
                nativeCallbackRef[0] = nativeCallback;
                nativeScanner.startScan(null, settings, nativeCallback);
            }
        } catch (SecurityException denied) {
            callback.onError("Разрешите доступ к Bluetooth и устройствам поблизости");
            return;
        } catch (Throwable t0) {
            Log.w(TAG, "native BLE scan init failed: " + t0.getMessage());
        }

        // 1) Start raw scan first so we register our callback before SDK possibly starts its own scan.
        try {
            ThingHomeSdk.getBleOperator().startLeScan(t, ScanType.NORMAL, new BleScanResponse() {
                @Override
                public void onResult(ScanDeviceBean bean) {
                    if (bean == null) return;
                    scanSeenCount[0]++;
                    String name = safe(bean.getName());
                    String addr = safe(bean.getAddress());
                    if (addr.isEmpty()) {
                        try {
                            addr = safe(bean.getMac());
                        } catch (Throwable ignored) {
                        }
                    }

                    int deviceType = 0;
                    try {
                        deviceType = bean.getDeviceType();
                    } catch (Throwable ignored) {
                    }

                    boolean binded = false;
                    try {
                        binded = bean.getIsbind();
                    } catch (Throwable ignored) {
                    }
                    String uuid = safe(bean.getUuid());

                    if (BuildConfig.DEBUG) {
                        Log.d(
                                TAG,
                                "scan bean: addr=" + addr
                                        + " mac? uuid=" + uuid
                                        + " name=" + (name.isEmpty() ? "NULL" : name)
                                        + " deviceType=" + deviceType
                                        + " isBind=" + binded
                        );
                    }

                    if (addr.isEmpty()) return;

                    String displayName = name.isEmpty() ? "TY" : name;
                    Scooter s = new Scooter(displayName, uuid, addr, null, binded, "", deviceType);
                    seen.put(addr, s);
                    scanAddedCount[0]++;
                }
            });
        } catch (Throwable t2) {
            Log.w(TAG, "startLeScan failed: " + t2.getMessage());
        }

        // 2) Try Tuya/Thingclips "bind scan" as well: this is how the official app discovers devices for pairing.
        try {
            ThingHomeSdk.getBleOperator().startScanBindDevice(t, ScanType.SINGLE,
                    new com.thingclips.smart.home.sdk.callback.IThingResultCallback<String>() {
                        @Override
                        public void onSuccess(String devId) {
                            String id = safe(devId);
                            if (id.isEmpty()) return;
                            // We might not have MAC/name from this API; still surface it to the UI.
                            String key = "devId:" + id;
                            if (!seen.containsKey(key)) {
                                seen.put(key, new Scooter("TY [bind-scan]", "", "", null, false, id, 0));
                            }
                        }

                        @Override
                        public void onError(String code, String error) {
                            Log.w(TAG, "startScanBindDevice error: " + code + ": " + error);
                        }
                    });
        } catch (Throwable t1) {
            Log.w(TAG, "startScanBindDevice failed: " + t1.getMessage());
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                ThingHomeSdk.getBleOperator().stopLeScan();
            } catch (Throwable ignored) {
            }
            try {
                if (nativeScannerRef[0] != null && nativeCallbackRef[0] != null) {
                    stopNativeScan(nativeScannerRef[0], nativeCallbackRef[0]);
                }
            } catch (Throwable ignored) {
            }
            ArrayList<Scooter> out;
            synchronized (seen) {
                out = new ArrayList<>(seen.values());
            }
            Log.i(TAG, "scan finished: thingSeen=" + scanSeenCount[0] + " thingAdded=" + scanAddedCount[0]
                    + " nativeSeen=" + nativeSeenCount[0] + " nativeAdded=" + nativeAddedCount[0]
                    + " unique=" + out.size());
            callback.onResult(out);
        }, t * 1000L);
    }

    private void stopNativeScan(BluetoothLeScanner scanner, ScanCallback callback) {
        try {
            scanner.stopScan(callback);
        } catch (SecurityException denied) {
            Log.w(TAG, "Bluetooth scan permission revoked");
        }
    }

    public void enrichScootersProductInfo(List<Scooter> scooters, Runnable onAnyUpdate) {
        if (scooters == null || scooters.isEmpty()) return;
        for (Scooter s : scooters) {
            if (s == null) continue;
            if (safe(s.getIconUrl()).length() > 0 && safe(s.getProductName()).length() > 0) continue;

            byte[] raw = s.getRawAdv();
            String productId = safe(s.getProductId());
            if (productId.isEmpty() && raw != null) productId = parseTuyaProductIdFromAdv(raw);

            String deviceUuid = safe(s.getDeviceUuid());
            if (deviceUuid.isEmpty() && raw != null) deviceUuid = parseTuyaDeviceUuidFromAdv(raw);

            String mac = normalizeMac(s.getAddress());
            if (productId.isEmpty() || deviceUuid.isEmpty() || mac.isEmpty()) continue;

            fetchProductInfo(mac, productId, deviceUuid, new ProductInfoCallback() {
                @Override
                public void onResult(String productName, String iconUrl) {
                    s.setProductInfo(productName, iconUrl);
                    if (onAnyUpdate != null) {
                        new Handler(Looper.getMainLooper()).post(onAnyUpdate);
                    }
                }

                @Override
                public void onError(String error) {
                    // ignore per-device errors
                }
            });
        }
    }

    private void fetchProductInfo(String mac, String productId, String deviceUuid, ProductInfoCallback callback) {
        fetchProductInfo(mac, productId, deviceUuid, callback, 0);
    }

    private void fetchProductInfo(String mac, String productId, String deviceUuid, ProductInfoCallback callback, int attempt) {
        if (callback == null) return;
        try {
            if (!ThingHomeSdk.getUserInstance().isLogin()) {
                callback.onError("Not logged in");
                return;
            }
        } catch (Throwable t) {
            callback.onError("Login state error: " + t.getMessage());
            return;
        }

        try {
            com.thingclips.smart.android.network.ThingApiParams params =
                    new com.thingclips.smart.android.network.ThingApiParams("smartlife.m.product.info.get", "1.0");
            params.setSessionRequire(true);
            params.putPostData("mac", mac);
            params.putPostData("productId", productId);
            params.putPostData("uuid", deviceUuid);

            new com.thingclips.smart.android.network.Business().asyncRequest(params,
                    new com.thingclips.smart.android.network.Business.ResultListener<JSONObject>() {
                        @Override
                        public void onSuccess(com.thingclips.smart.android.network.http.BusinessResponse resp, JSONObject result, String raw) {
                            if (result == null) {
                                callback.onError("Empty result");
                                return;
                            }
                            JSONObject r = result.getJSONObject("result");
                            if (r == null) r = result;
                            String name = r.getString("name");
                            String icon = r.getString("icon");
                            callback.onResult(name, icon);
                        }

                        @Override
                        public void onFailure(com.thingclips.smart.android.network.http.BusinessResponse resp, JSONObject result, String raw) {
                            String code = resp != null ? resp.errorCode : "ERR";
                            String msg = resp != null ? resp.errorMsg : "Request failed";

                            // Workaround for intermittent "verify response result failed" / decrypt errors that surface
                            // as network error (e.g. errorCode=102). Retry a couple of times.
                            if (attempt < 2 && ("102".equals(code) || "50514".equals(code))) {
                                long delayMs = (attempt == 0) ? 300L : 900L;
                                new Handler(Looper.getMainLooper()).postDelayed(
                                        () -> fetchProductInfo(mac, productId, deviceUuid, callback, attempt + 1),
                                        delayMs
                                );
                                return;
                            }

                            callback.onError(code + ": " + msg);
                        }
                    });
        } catch (Throwable t) {
            callback.onError("Request error: " + t.getMessage());
        }
    }

    public void connectScooter(Context context, Scooter scooter) {
        if (context == null || scooter == null) {
            Log.w(TAG, "connectScooter: context or scooter is null");
            return;
        }
        if (scooter.isBinded()) {
            Log.w(TAG, "connectScooter: scooter is binded, skipping connect addr=" + scooter.getAddress());
            return;
        }

        // If Thingclips bind-scan returned a devId, prefer connecting by devId through SDK (official path).
        String devId = scooter.getDevId();
        if (devId != null && !devId.trim().isEmpty()) {
            try {
                ThingHomeSdk.getBleOperator().connectBleDevice(devId, (deviceId, success) ->
                        Log.d(TAG, "connectBleDevice devId=" + deviceId + " success=" + success));
                return;
            } catch (Throwable t) {
                Log.w(TAG, "connectBleDevice(devId) failed, fallback to local ServiceScooter: " + t.getMessage());
            }
        }

        Intent i = new Intent(context, ServiceScooter.class);
        i.putExtra("command", Config.SCOOTER_CONNECT_COMMAND);
        i.putExtra("uuid", scooter.getUuid());
        i.putExtra("address", scooter.getAddress());
        i.putExtra("name", scooter.getName());
        // ServiceScooter expects name_device for protocol selection (midway vs kugoo).
        i.putExtra("name_device", scooter.getName());
        try {
            context.startService(i);
        } catch (Throwable t) {
            Log.e(TAG, "connectScooter: startService failed", t);
        }
    }

    /**
     * Official-style flow: ensure Home exists, scan via Thing BLE, activate/bind to account home,
     * then connect by devId through Thing SDK.
     */
    public void bindAndConnectScooter(Context context, Scooter scooter, BindCallback callback) {
        if (callback == null) return;
        if (context == null || scooter == null) {
            callback.onError("context or scooter is null");
            return;
        }

        // Proceed only after a confirmed login.
        boolean alreadyLoggedIn = false;
        try {
            alreadyLoggedIn = ThingHomeSdk.getUserInstance().isLogin();
        } catch (Throwable t) {
            callback.onError("Thing user instance error: " + t.getMessage());
            return;
        }

        Runnable afterLogin = () -> ensureHomeId(new com.thingclips.smart.home.sdk.callback.IThingResultCallback<Long>() {
            @Override
            public void onSuccess(Long homeId) {
                if (homeId == null || homeId <= 0) {
                    callback.onError("homeId is empty");
                    return;
                }
                // Prefer using the already captured native scanRecord to build ScanDeviceBean,
                // because Thing SDK scan callback can be suppressed by internal scanner state.
                byte[] raw = scooter.getRawAdv();
                if (raw != null && raw.length > 0) {
                    try {
                        String productId = safe(scooter.getUuid());
                        if (productId.isEmpty()) {
                            productId = parseTuyaProductIdFromAdv(raw);
                        }
                        String deviceUuid = parseTuyaDeviceUuidFromAdv(raw);
                        String addr = safe(scooter.getAddress());
                        String mac = normalizeMac(addr);

                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "bind prep: addr=" + addr + " mac=" + mac
                                    + " productId=" + productId + " deviceUuid=" + deviceUuid
                                    + " rawLen=" + raw.length);
                        }

                        ScanDeviceBean scanBean = new ScanDeviceBean();
                        scanBean.setAddress(addr);
                        scanBean.setMac(mac);
                        scanBean.setName(scooter.getName());
                        scanBean.setUuid(deviceUuid);
                        scanBean.setProductId(productId);
                        scanBean.setDeviceType(scooter.getDeviceType());
                        scanBean.setRaw(raw);
                        scanBean.setRssi(scooter.getRssi());
                        startBleActivator(homeId, scanBean, callback);
                        return;
                    } catch (Throwable t) {
                        Log.w(TAG, "build ScanDeviceBean from rawAdv failed, fallback to Thing scan: " + t.getMessage());
                    }
                }

                scanAndActivateByAddress(homeId, scooter.getAddress(), callback);
            }

            @Override
            public void onError(String code, String error) {
                callback.onError("Home error: " + code + ": " + error);
            }
        });

        if (alreadyLoggedIn) {
            afterLogin.run();
            return;
        }

        // Optional: allow logging in using BuildConfig credentials when not already logged in.
        // This is async; do not call isLogin() immediately after starting login.
        ThingHomeSdk.getUserInstance().loginWithEmail(
                "7",
                safe(BuildConfig.TUYA_APP_Login),
                safe(BuildConfig.TUYA_APP_Password),
                new ILoginCallback() {
                    @Override
                    public void onSuccess(User user) {
                        afterLogin.run();
                    }

                    @Override
                    public void onError(String code, String error) {
                        callback.onError("Login failed: " + code + ": " + error);
                    }
                }
        );
    }

    private void scanAndActivateByAddress(long homeId, String targetAddress, BindCallback callback) {
        String addrTarget = safe(targetAddress).toUpperCase(Locale.ROOT);
        if (addrTarget.isEmpty()) {
            callback.onError("target address is empty");
            return;
        }

        LeScanSetting setting = new LeScanSetting.Builder()
                .setUUID(TUYA_FD50_SERVICE.toString())
                .setNeedMatchUUID(false) // FD50 often not in ServiceUUID list; SDK still parses ScanDeviceBean
                .setRepeatFilter(true)
                .setTimeout(12_000L)
                .addScanType(ScanType.NORMAL)
                .build();

        final boolean[] done = {false};

        try {
            ThingHomeSdk.getBleOperator().startLeScan(setting, new BleScanResponse() {
                @Override
                public void onResult(ScanDeviceBean bean) {
                    if (done[0] || bean == null) return;

                    String addr = safe(bean.getAddress());
                    if (addr.isEmpty()) {
                        try {
                            addr = safe(bean.getMac());
                        } catch (Throwable ignored) {
                        }
                    }
                    if (addr.isEmpty()) return;
                    if (!addrTarget.equalsIgnoreCase(addr.trim())) return;

                    done[0] = true;
                    try {
                        ThingHomeSdk.getBleOperator().stopLeScan();
                    } catch (Throwable ignored) {
                    }

                    startBleActivator(homeId, bean, callback);
                }
            });

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (done[0]) return;
                done[0] = true;
                try {
                    ThingHomeSdk.getBleOperator().stopLeScan();
                } catch (Throwable ignored) {
                }
                callback.onError("Scan timeout (no ScanDeviceBean for " + addrTarget + ")");
            }, 12_500L);
        } catch (Throwable t) {
            callback.onError("Thing scan failed: " + t.getMessage());
        }
    }

    private void startBleActivator(long homeId, ScanDeviceBean scanBean, BindCallback callback) {
        try {
            IBleActivator activator = ThingHomeSdk.getActivator().newBleActivator();
            BleActivatorBean bean = new BleActivatorBean(scanBean);
            bean.homeId = homeId;
            bean.uuid = safe(scanBean.getUuid());
            bean.address = safe(scanBean.getAddress());
            bean.productId = safe(scanBean.getProductId());
            bean.deviceType = scanBean.getDeviceType();
            bean.isShare = scanBean.isShare();
            bean.timeout = 12_000L;
            bean.retryCount = 1;
            Log.d(TAG, "bean.uuid: " + String.valueOf(bean.uuid));

            activator.startActivator(bean, new IBleActivatorListener() {
                @Override
                public void onSuccess(DeviceBean deviceBean) {
                    String devId = deviceBean != null ? safe(deviceBean.getDevId()) : "";
                    if (devId.isEmpty()) {
                        callback.onError("Activator success but devId is empty");
                        return;
                    }
                    // Persist bind hint for UI.
                    try {
                        application.getSharedPreferences("bind_state", Context.MODE_PRIVATE)
                                .edit()
                                .putBoolean(safe(scanBean.getAddress()), true)
                                .apply();
                    } catch (Throwable ignored) {
                    }
                    // Persist offline snapshot (devId/localKey/mac/uuid/productId) for use without internet.
                    try {
                        android.content.SharedPreferences p = application.getSharedPreferences("offline_device_cache", Context.MODE_PRIVATE);
                        com.alibaba.fastjson.JSONObject j = new com.alibaba.fastjson.JSONObject();
                        j.put("devId", devId);
                        j.put("ts", System.currentTimeMillis());
                        try {
                            j.put("name", deviceBean != null ? safe(deviceBean.getName()) : "");
                        } catch (Throwable ignored) {
                        }
                        try {
                            j.put("uuid", deviceBean != null ? safe(deviceBean.getUuid()) : safe(scanBean.getUuid()));
                        } catch (Throwable ignored) {
                            j.put("uuid", safe(scanBean.getUuid()));
                        }
                        try {
                            j.put("productId", deviceBean != null ? safe(deviceBean.getProductId()) : safe(scanBean.getProductId()));
                        } catch (Throwable ignored) {
                            j.put("productId", safe(scanBean.getProductId()));
                        }
                        try {
                            j.put("mac", deviceBean != null ? safe(deviceBean.getMac()) : "");
                        } catch (Throwable ignored) {
                        }
                        try {
                            j.put("localKey", deviceBean != null ? safe(deviceBean.getLocalKey()) : "");
                        } catch (Throwable ignored) {
                        }
                        p.edit().putString(devId, j.toJSONString()).apply();
                    } catch (Throwable ignored) {
                    }
                    try {
                        ThingHomeSdk.getBleOperator().connectBleDevice(devId, (deviceId, success) ->
                                Log.d(TAG, "connectBleDevice(devId) deviceId=" + deviceId + " success=" + success));
                    } catch (Throwable t) {
                        Log.w(TAG, "connectBleDevice after activate failed: " + t.getMessage());
                    }
                    callback.onSuccess(devId);
                }

                @Override
                public void onFailure(int code, String msg, Object handle) {
                    callback.onError("Activator failed: " + code + ": " + msg);
                }
            });
        } catch (Throwable t) {
            callback.onError("BleActivator error: " + t.getMessage());
        }
    }

    private void ensureHomeId(com.thingclips.smart.home.sdk.callback.IThingResultCallback<Long> callback) {
        if (callback == null) return;

        Runnable work = () -> {
            try {
                boolean loggedIn = false;
                try {
                    loggedIn = ThingHomeSdk.getUserInstance().isLogin();
                } catch (Throwable ignored) {
                }
                if (!loggedIn) {
                    callback.onError("NOT_LOGIN", "User is not logged in");
                    return;
                }

                ThingHomeSdk.getHomeManagerInstance().queryHomeList(new IThingGetHomeListCallback() {
                    @Override
                    public void onSuccess(List<HomeBean> homes) {
                        if (homes != null && !homes.isEmpty()) {
                            callback.onSuccess(homes.get(0).getHomeId());
                            return;
                        }
                        ThingHomeSdk.getHomeManagerInstance().createHome(
                                "1",
                                0.0,
                                0.0,
                                "",
                                new ArrayList<>(),
                                new IThingHomeResultCallback() {
                                    @Override
                                    public void onSuccess(HomeBean bean) {
                                        callback.onSuccess(bean != null ? bean.getHomeId() : 0L);
                                    }

                                    @Override
                                    public void onError(String code, String error) {
                                        callback.onError(code, error);
                                    }
                                });
                    }

                    @Override
                    public void onError(String code, String error) {
                        callback.onError(code, error);
                    }
                });
            } catch (Throwable t) {
                Log.e(TAG, "ensureHomeId failed", t);
                callback.onError("EX", t.toString());
            }
        };

        if (Looper.myLooper() == Looper.getMainLooper()) {
            work.run();
        } else {
            new Handler(Looper.getMainLooper()).post(work);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    /**
     * Offline method (no cloud) to derive the 16-hex-char "key" used by some Thing/Tuya BLE flows
     * from a human-readable authKey/password.
     *
     * This mirrors Thing/Thingclips SDK {@code MD5Util.md5AsBase64For16(str)}:
     * md5(hexLower(authKey)).substring(8, 24)
     *
     * @param authKey raw authKey/password (usually from QR/label)
     * @return 16 hex chars (lowercase) or empty string if input invalid
     */
    public static String getKeyOfflineFromAuthKey(String authKey) {
        String s = safe(authKey);
        if (s.isEmpty()) return "";
        String md5Hex = md5HexLower(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        if (md5Hex.length() < 24) return "";
        return md5Hex.substring(8, 24);
    }

    private static String md5HexLower(byte[] data) {
        if (data == null) return "";
        try {
            byte[] digest = MessageDigest.getInstance("MD5").digest(data);
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Extract Tuya BLE productId from advertisement.
     *
     * For many Tuya BLE products this is an 8-char ASCII string inside Service Data AD structure
     * for 16-bit UUID 0xFD50 (type 0x16). It is NOT necessarily at the end; we scan for the first
     * 8 consecutive printable ASCII bytes in that service-data payload.
     */
    private static String parseTuyaProductIdFromAdv(byte[] adv) {
        byte[] payload = getTuyaFd50ServiceData(adv);
        if (payload == null || payload.length < 8) return "";

        for (int i = 0; i <= payload.length - 8; i++) {
            boolean ok = true;
            for (int j = 0; j < 8; j++) {
                int b = payload[i + j] & 0xFF;
                if (b < 0x20 || b > 0x7E) {
                    ok = false;
                    break;
                }
            }
            if (!ok) continue;
            String s = new String(payload, i, 8, java.nio.charset.StandardCharsets.US_ASCII).trim();
            // basic sanity: must include at least one letter/digit and no spaces
            if (!s.isEmpty() && s.indexOf(' ') < 0) return s;
        }
        return "";
    }

    /**
     * Extract Tuya device UUID (used for authKey/provisioning) from Manufacturer Specific Data (AD type 0xFF)
     * where company id is 0x07D0 (bytes D0 07).
     *
     * Heuristic: in many firmwares the payload contains a 16-byte device UUID after a small header.
     * We pick the first 16 bytes that look non-trivial and return as lowercase hex (32 chars).
     */
    private static String parseTuyaDeviceUuidFromAdv(byte[] adv) {
        byte[] mfg = getManufacturerData(adv, TUYA_MFG_COMPANY_ID);
        if (mfg == null || mfg.length < 16) return "";

        // Try a few offsets to account for different header lengths.
        // Prefer offset 4 for Tuya BLE (often first 4 bytes are flags/version),
        // then try other common offsets.
        int[] candidateOffsets = new int[]{4, 0, 2, 3, 5, 6};
        for (int off : candidateOffsets) {
            if (off + 16 > mfg.length) continue;
            String hex = toLowerHex(mfg, off, 16);
            if (hex.isEmpty()) continue;
            // Skip obviously empty/constant UUIDs.
            if (hex.matches("0{32}") || hex.matches("f{32}")) continue;
            return hex;
        }

        // Fallback: last 16 bytes.
        if (mfg.length >= 16) {
            return toLowerHex(mfg, mfg.length - 16, 16);
        }
        return "";
    }

    private static byte[] getTuyaFd50ServiceData(byte[] adv) {
        if (adv == null || adv.length < 6) return null;
        int i = 0;
        while (i < adv.length) {
            int len = adv[i] & 0xFF;
            if (len == 0) break;
            int start = i + 1;
            int end = start + len;
            if (end > adv.length) break;
            int type = adv[start] & 0xFF;
            if (type == 0x16 && len >= 4) { // Service Data - 16-bit UUID
                int uuidLo = adv[start + 1] & 0xFF;
                int uuidHi = adv[start + 2] & 0xFF;
                if (uuidLo == 0x50 && uuidHi == 0xFD) {
                    int payloadStart = start + 3;
                    int payloadLen = len - 3;
                    byte[] out = new byte[payloadLen];
                    System.arraycopy(adv, payloadStart, out, 0, payloadLen);
                    return out;
                }
            }
            i = end;
        }
        return null;
    }

    private static byte[] getManufacturerData(byte[] adv, int companyId) {
        if (adv == null || adv.length < 6) return null;
        int wantLo = companyId & 0xFF;
        int wantHi = (companyId >> 8) & 0xFF;

        int i = 0;
        while (i < adv.length) {
            int len = adv[i] & 0xFF;
            if (len == 0) break;
            int start = i + 1;
            int end = start + len;
            if (end > adv.length) break;
            int type = adv[start] & 0xFF;
            if (type == 0xFF && len >= 3) { // Manufacturer Specific Data
                int lo = adv[start + 1] & 0xFF;
                int hi = adv[start + 2] & 0xFF;
                if (lo == wantLo && hi == wantHi) {
                    int payloadStart = start + 3;
                    int payloadLen = len - 3;
                    if (payloadLen <= 0) return null;
                    byte[] out = new byte[payloadLen];
                    System.arraycopy(adv, payloadStart, out, 0, payloadLen);
                    return out;
                }
            }
            i = end;
        }
        return null;
    }

    private static String normalizeMac(String address) {
        String a = safe(address).toLowerCase(Locale.ROOT);
        if (a.isEmpty()) return "";
        return a.replace(":", "");
    }

    private static String toLowerHex(byte[] data, int offset, int len) {
        if (data == null || offset < 0 || len <= 0 || offset + len > data.length) return "";
        StringBuilder sb = new StringBuilder(len * 2);
        for (int i = 0; i < len; i++) {
            sb.append(String.format("%02x", data[offset + i]));
        }
        return sb.toString();
    }

    private static String bytesToAscii(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";
        StringBuilder sb = new StringBuilder(bytes.length);
        for (byte b : bytes) {
            int v = b & 0xFF;
            if (v >= 0x20 && v <= 0x7E) sb.append((char) v);
            else sb.append('.');
        }
        return sb.toString();
    }
}
