package sc.denishik.ru.tuya;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.thingclips.smart.android.user.api.ILoginCallback;
import com.thingclips.smart.android.user.api.IRegisterCallback;
import com.thingclips.smart.android.user.bean.User;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.thingclips.smart.sdk.api.IResultCallback;

import java.security.MessageDigest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import sc.denishik.ru.BuildConfig;
import sc.denishik.ru.R;
import sc.denishik.ru.kuggoApi.kugooApi;

public class TuyaLoginActivity extends AppCompatActivity {
    private static volatile PendingActionResult pendingResult;

    private EditText etCountry;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etCode;
    private Button btnLogin;
    private Button btnSendCode;
    private Button btnRegister;
    private Button btnLoadDevices;
    private Button btnCreateHome;
    private Button btnOfflineKeys;
    private TextView tvStatus;
    private ListView listDevices;

    private final List<DeviceEntry> deviceEntries = new ArrayList<>();
    private android.widget.ArrayAdapter<String> devicesAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuya_login);

        etCountry = findViewById(R.id.etCountry);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etCode = findViewById(R.id.etCode);
        btnLogin = findViewById(R.id.btnLogin);
        btnSendCode = findViewById(R.id.btnSendCode);
        btnRegister = findViewById(R.id.btnRegister);
        btnLoadDevices = findViewById(R.id.btnLoadDevices);
        btnCreateHome = findViewById(R.id.btnCreateHome);
        btnOfflineKeys = findViewById(R.id.btnOfflineKeys);
        tvStatus = findViewById(R.id.tvStatus);
        listDevices = findViewById(R.id.listDevices);

        if (BuildConfig.TUYA_APP_KEY == null || BuildConfig.TUYA_APP_KEY.trim().isEmpty()
                || BuildConfig.TUYA_APP_SECRET == null || BuildConfig.TUYA_APP_SECRET.trim().isEmpty()) {
            tvStatus.setText("Tuya keys are empty. Set tuya.appKey/tuya.appSecret in local.properties and rebuild.");
            btnLogin.setEnabled(false);
            btnSendCode.setEnabled(false);
            btnRegister.setEnabled(false);
            btnLoadDevices.setEnabled(false);
            return;
        }

        try {
            if (ThingHomeSdk.getUserInstance().isLogin()) {
                tvStatus.setText("Already logged in.");
                btnLoadDevices.setEnabled(true);
            } else {
                tvStatus.setText("Not logged in.");
                btnLoadDevices.setEnabled(false);
            }
        } catch (Throwable t) {
            tvStatus.setText("Init error: " + t);
            btnLoadDevices.setEnabled(false);
        }

        try {
            String pkg = getPackageName();
            String sha1 = getAppCertDigest("SHA-1");
            String sha256 = getAppCertDigest("SHA-256");
            tvStatus.append("\n\npkg=" + pkg);
            if (sha1 != null) tvStatus.append("\nSHA1=" + sha1);
            if (sha256 != null) tvStatus.append("\nSHA-256=" + sha256);
        } catch (Throwable ignored) {
        }

        btnLogin.setOnClickListener(v -> doLogin());
        btnSendCode.setOnClickListener(v -> doSendCode());
        btnRegister.setOnClickListener(v -> doRegister());
        btnLoadDevices.setOnClickListener(v -> loadDevices());
        btnCreateHome.setOnClickListener(v -> createHome1());
        btnOfflineKeys.setOnClickListener(v -> startActivity(new android.content.Intent(this, OfflineKeysActivity.class)));

        devicesAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listDevices.setAdapter(devicesAdapter);
        listDevices.setOnItemClickListener((parent, view, position, id) -> onDeviceClicked(position));

        tvStatus.setOnClickListener(v -> copyToClipboard("Tuya status", tvStatus.getText()));
    }

    private void createHome1() {
        tvStatus.setText("Creating Home \"1\"...");
        try {
            ThingHomeSdk.getHomeManagerInstance().createHome(
                    "1",
                    0d,
                    0d,
                    "",
                    new ArrayList<>(),
                    new com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback() {
                        @Override
                        public void onSuccess(com.thingclips.smart.home.sdk.bean.HomeBean homeBean) {
                            long homeId = homeBean == null ? 0L : homeBean.getHomeId();
                            runOnUiThread(() -> tvStatus.setText("Home created: " + homeId));
                        }

                        @Override
                        public void onError(String errorCode, String errorMsg) {
                            runOnUiThread(() -> tvStatus.setText("Create home error: " + errorCode + ": " + errorMsg));
                        }
                    }
            );
        } catch (Throwable t) {
            tvStatus.setText("Create home failed: " + t.getMessage());
        }
    }

    private void doLogin() {
        tvStatus.setText("Logging in...");
        btnLogin.setEnabled(false);
        btnSendCode.setEnabled(false);
        btnRegister.setEnabled(false);

        String country = etCountry.getText().toString().trim();
        if (country.startsWith("+")) country = country.substring(1);
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (country.isEmpty()) country = "7";

        try {
            ThingHomeSdk.getUserInstance().loginWithEmail(country, email, password, new ILoginCallback() {
                @Override
                public void onSuccess(User user) {
                    postActionResult("login", true, null);
                }

                @Override
                public void onError(String code, String error) {
                    postActionResult("login", false, code + ": " + error);
                }
            });
        } catch (Throwable t) {
            tvStatus.setText("Login call failed: " + t.getMessage());
            btnLogin.setEnabled(true);
            btnSendCode.setEnabled(true);
            btnRegister.setEnabled(true);
            return;
        }

        // Poll for async callback result posted via proxy.
        tvStatus.postDelayed(new Runnable() {
            @Override
            public void run() {
                PendingActionResult r = pendingResult;
                if (r == null || !"login".equals(r.action)) {
                    tvStatus.postDelayed(this, 200);
                    return;
                }
                pendingResult = null;
                if (r.success) {
                    tvStatus.setText("Login success.");
                    btnLoadDevices.setEnabled(true);
                    btnSendCode.setEnabled(true);
                    btnRegister.setEnabled(true);
                } else {
                    tvStatus.setText("Login error: " + r.error);
                    btnLogin.setEnabled(true);
                    btnSendCode.setEnabled(true);
                    btnRegister.setEnabled(true);
                }
            }
        }, 200);
    }

    private void doSendCode() {
        String country = etCountry.getText().toString().trim();
        if (country.startsWith("+")) country = country.substring(1);
        if (country.isEmpty()) country = "7";
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            tvStatus.setText("Email is empty");
            return;
        }

        tvStatus.setText("Sending register code...\ncc=" + country + "\nemail=" + email);
        btnSendCode.setEnabled(false);

        try {
            ThingHomeSdk.getUserInstance().getRegisterEmailValidateCode(country, email, new IResultCallback() {
                @Override
                public void onSuccess() {
                    postActionResult("send_code", true, null);
                }

                @Override
                public void onError(String code, String error) {
                    postActionResult("send_code", false, code + ": " + error);
                }
            });
        } catch (Throwable t) {
            tvStatus.setText("Send code failed: " + t.getMessage());
            btnSendCode.setEnabled(true);
            return;
        }

        tvStatus.postDelayed(new Runnable() {
            @Override
            public void run() {
                PendingActionResult r = pendingResult;
                if (r == null || !"send_code".equals(r.action)) {
                    tvStatus.postDelayed(this, 200);
                    return;
                }
                pendingResult = null;
                if (r.success) {
                    tvStatus.setText("Code sent. Check email.");
                } else {
                    tvStatus.setText("Send code error: " + r.error);
                }
                btnSendCode.setEnabled(true);
            }
        }, 200);
    }

    private void doRegister() {
        String country = etCountry.getText().toString().trim();
        if (country.startsWith("+")) country = country.substring(1);
        if (country.isEmpty()) country = "7";
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String code = etCode.getText().toString().trim();

        tvStatus.setText("Registering...\ncc=" + country + "\nemail=" + email);
        btnRegister.setEnabled(false);

        try {
            ThingHomeSdk.getUserInstance().registerAccountWithEmail(country, email, password, code, new IRegisterCallback() {
                @Override
                public void onSuccess(User user) {
                    postActionResult("register", true, null);
                }

                @Override
                public void onError(String code, String error) {
                    postActionResult("register", false, code + ": " + error);
                }
            });
        } catch (Throwable t) {
            tvStatus.setText("Register call failed: " + t.getMessage());
            btnRegister.setEnabled(true);
            return;
        }

        tvStatus.postDelayed(new Runnable() {
            @Override
            public void run() {
                PendingActionResult r = pendingResult;
                if (r == null || !"register".equals(r.action)) {
                    tvStatus.postDelayed(this, 200);
                    return;
                }
                pendingResult = null;
                if (r.success) {
                    tvStatus.setText("Register success. Now login.");
                } else {
                    tvStatus.setText("Register error: " + r.error);
                }
                btnRegister.setEnabled(true);
            }
        }, 200);
    }

    private void loadDevices() {
        tvStatus.setText("Loading homes...");
        deviceEntries.clear();
        devicesAdapter.clear();

        try {
            ThingHomeSdk.getHomeManagerInstance().queryHomeList(new com.thingclips.smart.home.sdk.callback.IThingGetHomeListCallback() {
                @Override
                public void onSuccess(List<com.thingclips.smart.home.sdk.bean.HomeBean> homeBeans) {
                    if (homeBeans == null || homeBeans.isEmpty()) {
                        runOnUiThread(() -> tvStatus.setText("No homes in account. Create a Home in Tuya/Thingclips app first."));
                        return;
                    }
                    com.thingclips.smart.home.sdk.bean.HomeBean firstHome = homeBeans.get(0);
                    long homeId = firstHome.getHomeId();
                    String homeName = firstHome.getName();
                    runOnUiThread(() -> tvStatus.setText("Home: " + homeName + " (" + homeId + "). Loading devices..."));
                    loadDevicesForHome(homeId);
                }

                @Override
                public void onError(String errorCode, String errorMsg) {
                    runOnUiThread(() -> tvStatus.setText("Home list error: " + errorCode + ": " + errorMsg));
                }
            });
        } catch (Throwable t) {
            tvStatus.setText("Load homes failed: " + t.getMessage());
        }
    }

    private void loadDevicesForHome(long homeId) {
        try {
            ThingHomeSdk.newHomeInstance(homeId).getHomeDetail(new com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback() {
                @Override
                public void onSuccess(com.thingclips.smart.home.sdk.bean.HomeBean homeBean) {
                    List<Object> devices = readHomeDevices(homeBean);
                    runOnUiThread(() -> renderDevices(homeId, devices));
                }

                @Override
                public void onError(String errorCode, String errorMsg) {
                    runOnUiThread(() -> tvStatus.setText("Home detail error: " + errorCode + ": " + errorMsg));
                }
            });
        } catch (Throwable t) {
            runOnUiThread(() -> tvStatus.setText("Load home detail failed: " + t.getMessage()));
        }
    }

    private void renderDevices(long homeId, List<Object> devices) {
        deviceEntries.clear();
        devicesAdapter.clear();

        for (Object deviceBean : devices) {
            String devId = readDeviceDevId(deviceBean);
            String name = readDeviceName(deviceBean);
            boolean online = readDeviceOnline(deviceBean);

            deviceEntries.add(new DeviceEntry(homeId, devId, name));
            devicesAdapter.add(name + "  [" + (online ? "online" : "offline") + "]\n" + devId);
        }

        devicesAdapter.notifyDataSetChanged();
        tvStatus.setText("Devices loaded: " + deviceEntries.size() + ". Tap a device to connect / unbind.");
    }

    private void onDeviceClicked(int position) {
        if (position < 0 || position >= deviceEntries.size()) return;
        DeviceEntry entry = deviceEntries.get(position);

        String[] actions = new String[]{
                "Details / Keys",
                "Connect BLE",
                "Disconnect BLE",
                "Unbind (cloud remove)",
                "Unbind (local remove)",
                "Factory reset (local)",
        };

        new AlertDialog.Builder(this)
                .setTitle(entry.name)
                .setItems(actions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showDeviceDetailsAndKeys(entry.devId);
                            break;
                        case 1:
                            connectBle(entry.devId);
                            break;
                        case 2:
                            disconnectBle(entry.devId);
                            break;
                        case 3:
                            removeDeviceCloud(entry.devId);
                            break;
                        case 4:
                            removeDeviceLocal(entry.devId);
                            break;
                        case 5:
                            resetFactoryLocal(entry.devId);
                            break;
                        default:
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeviceDetailsAndKeys(String devId) {
        String id = devId == null ? "" : devId.trim();
        if (id.isEmpty()) {
            tvStatus.setText("Empty devId");
            return;
        }

        DeviceBean d = null;
        try {
            d = ThingHomeSdk.getDataInstance().getDeviceBean(id);
        } catch (Throwable ignored) {
        }

        String title = d != null && d.getName() != null && !d.getName().trim().isEmpty()
                ? d.getName().trim()
                : id;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (getResources().getDisplayMetrics().density * 12);
        root.setPadding(pad, pad, pad, pad);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tv = new TextView(this);
        tv.setText(buildDeviceInfoText(d, id));
        tv.setTextIsSelectable(true);
        root.addView(tv);

        TextView tvAuth = new TextView(this);
        tvAuth.setText("\nAuthKey (from QR/label) → offlineKey16:");
        root.addView(tvAuth);

        EditText etAuthKey = new EditText(this);
        etAuthKey.setHint("paste authKey here");
        root.addView(etAuthKey);

        TextView tvOffline = new TextView(this);
        tvOffline.setTextIsSelectable(true);
        root.addView(tvOffline);

        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(root)
                .setPositiveButton("Copy All", (d0, w) -> copyToClipboard("Device details", tv.getText() + "\n" + tvOffline.getText()))
                .setNeutralButton("Compute", null)
                .setNegativeButton("Close", null)
                .show();

        dlg.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
            String authKey = etAuthKey.getText() == null ? "" : etAuthKey.getText().toString().trim();
            String offline = kugooApi.getKeyOfflineFromAuthKey(authKey);
            if (offline.isEmpty()) {
                tvOffline.setText("offlineKey16: (empty) — enter authKey");
            } else {
                tvOffline.setText("offlineKey16: " + offline);
            }
        });
    }

    private static String buildDeviceInfoText(DeviceBean d, String devId) {
        StringBuilder sb = new StringBuilder();
        sb.append("devId=").append(devId == null ? "" : devId).append('\n');
        if (d == null) {
            sb.append("DeviceBean: null (not in local cache). Load devices/home and retry.\n");
            return sb.toString();
        }
        tryAppend(sb, "name", d.getName());
        tryAppend(sb, "uuid", d.getUuid());
        tryAppend(sb, "productId", d.getProductId());
        tryAppend(sb, "mac", d.getMac());
        tryAppend(sb, "localKey", d.getLocalKey());
        sb.append("online=").append(readBoolean(d, new String[]{"isOnline", "getIsOnline"})).append('\n');
        sb.append("isBle=").append(readBoolean(d, new String[]{"isBluetooth", "getIsBluetooth"})).append('\n');
        sb.append("devType=").append(readString(d, new String[]{"getDevType", "getDevTypeId", "getDeviceType"})).append('\n');
        sb.append("pv=").append(readString(d, new String[]{"getPv", "getVersion"})).append('\n');
        return sb.toString();
    }

    private static void tryAppend(StringBuilder sb, String k, String v) {
        if (sb == null || k == null) return;
        if (v == null) v = "";
        sb.append(k).append('=').append(v).append('\n');
    }

    private static boolean readBoolean(Object target, String[] methodNames) {
        Object v = invokeFirst(target, methodNames);
        return v instanceof Boolean ? (Boolean) v : false;
    }

    private static String readString(Object target, String[] methodNames) {
        Object v = invokeFirst(target, methodNames);
        return v == null ? "" : String.valueOf(v);
    }

    private static Object invokeFirst(Object target, String[] methodNames) {
        if (target == null || methodNames == null) return null;
        for (String name : methodNames) {
            if (name == null || name.trim().isEmpty()) continue;
            try {
                Method m = target.getClass().getMethod(name);
                return m.invoke(target);
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    private void removeDeviceCloud(String devId) {
        tvStatus.setText("Unbinding (cloud remove): " + devId);
        try {
            ThingHomeSdk.newDeviceInstance(devId).removeDevice(new IResultCallback() {
                @Override
                public void onError(String code, String error) {
                    runOnUiThread(() -> tvStatus.setText("Cloud remove failed: " + code + ": " + error));
                }

                @Override
                public void onSuccess() {
                    runOnUiThread(() -> tvStatus.setText("Cloud remove done: " + devId));
                }
            });
        } catch (Throwable t) {
            tvStatus.setText("Cloud remove failed: " + t.getMessage());
        }
    }

    private void connectBle(String devId) {
        tvStatus.setText("Connecting BLE: " + devId);
        try {
            ThingHomeSdk.getBleOperator().connectBleDevice(devId, new com.thingclips.smart.android.ble.api.LeConnectResponse() {
                @Override
                public void onConnnectResult(String deviceId, boolean success) {
                    runOnUiThread(() -> tvStatus.setText("BLE connect result for " + deviceId + ": " + success));
                }
            });

            registerBleStatus(devId);
        } catch (Throwable t) {
            tvStatus.setText("Connect BLE failed: " + t.getMessage());
        }
    }

    private void registerBleStatus(String devId) {
        try {
            ThingHomeSdk.getBleOperator().registerBleConnectStatus(devId, new com.thingclips.smart.android.ble.api.LeConnectStatusResponse() {
                @Override
                public void onConnectStatusChanged(String deviceId, int status) {
                    runOnUiThread(() -> tvStatus.setText("BLE status " + deviceId + ": " + status));
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private void disconnectBle(String devId) {
        tvStatus.setText("Disconnecting BLE: " + devId);
        try {
            ThingHomeSdk.getBleOperator().disconnectBleDevice(devId);
            tvStatus.setText("Disconnect requested: " + devId);
        } catch (Throwable t) {
            tvStatus.setText("Disconnect failed: " + t.getMessage());
        }
    }

    private void removeDeviceLocal(String devId) {
        tvStatus.setText("Unbinding (local remove): " + devId);
        runBleManagerAction("removeDeviceLocal", devId, "Local remove done", "Local remove failed");
    }

    private void resetFactoryLocal(String devId) {
        tvStatus.setText("Factory reset (local): " + devId);
        runBleManagerAction("resetFactoryLocal", devId, "Local factory reset done", "Local factory reset failed");
    }

    private void runBleManagerAction(String methodName, String devId, String okText, String errText) {
        try {
            Object bleMgr = ThingHomeSdk.getBleManager();
            Method m = bleMgr.getClass().getMethod(methodName, String.class, IResultCallback.class);
            m.invoke(bleMgr, devId, new IResultCallback() {
                @Override
                public void onError(String code, String error) {
                    runOnUiThread(() -> tvStatus.setText(errText + ": " + code + ": " + error));
                }

                @Override
                public void onSuccess() {
                    runOnUiThread(() -> tvStatus.setText(okText + ": " + devId));
                }
            });
        } catch (Throwable t) {
            tvStatus.setText(errText + ": " + t.getMessage());
        }
    }

    private static long readHomeId(Object homeBean) {
        if (homeBean == null) return 0L;
        try {
            Method m = homeBean.getClass().getMethod("getHomeId");
            Object v = m.invoke(homeBean);
            if (v instanceof Long) return (Long) v;
            if (v instanceof Number) return ((Number) v).longValue();
        } catch (Throwable ignored) {
        }
        try {
            Method m = homeBean.getClass().getMethod("getHomeID");
            Object v = m.invoke(homeBean);
            if (v instanceof Long) return (Long) v;
            if (v instanceof Number) return ((Number) v).longValue();
        } catch (Throwable ignored) {
        }
        return 0L;
    }

    private static String readHomeName(Object homeBean) {
        if (homeBean == null) return "N/A";
        try {
            Method m = homeBean.getClass().getMethod("getName");
            Object v = m.invoke(homeBean);
            return v == null ? "N/A" : String.valueOf(v);
        } catch (Throwable ignored) {
            return "N/A";
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Object> readHomeDevices(Object homeBean) {
        if (homeBean == null) return new ArrayList<>();
        try {
            Method m = homeBean.getClass().getMethod("getDeviceList");
            Object v = m.invoke(homeBean);
            if (v instanceof List) return (List<Object>) v;
        } catch (Throwable ignored) {
        }
        return new ArrayList<>();
    }

    private static String readDeviceDevId(Object deviceBean) {
        if (deviceBean == null) return "";
        try {
            Method m = deviceBean.getClass().getMethod("getDevId");
            Object v = m.invoke(deviceBean);
            return v == null ? "" : String.valueOf(v);
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static String readDeviceName(Object deviceBean) {
        if (deviceBean == null) return "N/A";
        try {
            Method m = deviceBean.getClass().getMethod("getName");
            Object v = m.invoke(deviceBean);
            return v == null ? "N/A" : String.valueOf(v);
        } catch (Throwable ignored) {
            return "N/A";
        }
    }

    private static boolean readDeviceOnline(Object deviceBean) {
        if (deviceBean == null) return false;
        try {
            Method m = deviceBean.getClass().getMethod("isOnline");
            Object v = m.invoke(deviceBean);
            return v instanceof Boolean ? (Boolean) v : false;
        } catch (Throwable ignored) {
        }
        try {
            Method m = deviceBean.getClass().getMethod("getIsOnline");
            Object v = m.invoke(deviceBean);
            return v instanceof Boolean ? (Boolean) v : false;
        } catch (Throwable ignored) {
        }
        return false;
    }

    private String getAppCertDigest(String algorithm) throws Exception {
        android.content.pm.PackageManager pm = getPackageManager();
        String pkg = getPackageName();

        android.content.pm.PackageInfo pi;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            pi = pm.getPackageInfo(pkg, android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES);
            if (pi.signingInfo == null) return null;
            android.content.pm.Signature[] sigs = pi.signingInfo.hasMultipleSigners()
                    ? pi.signingInfo.getApkContentsSigners()
                    : pi.signingInfo.getSigningCertificateHistory();
            if (sigs == null || sigs.length == 0) return null;
            return digestToHex(MessageDigest.getInstance(algorithm).digest(sigs[0].toByteArray()));
        } else {
            // noinspection deprecation
            pi = pm.getPackageInfo(pkg, android.content.pm.PackageManager.GET_SIGNATURES);
            // noinspection deprecation
            if (pi.signatures == null || pi.signatures.length == 0) return null;
            // noinspection deprecation
            return digestToHex(MessageDigest.getInstance(algorithm).digest(pi.signatures[0].toByteArray()));
        }
    }

    private static String digestToHex(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            if (i > 0) sb.append(':');
            int b = digest[i] & 0xFF;
            String hex = Integer.toHexString(b).toUpperCase();
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }

    private void copyToClipboard(String label, CharSequence text) {
        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (cm == null) return;
            cm.setPrimaryClip(ClipData.newPlainText(label, text));
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
        } catch (Throwable ignored) {
        }
    }

    static void postActionResult(String action, boolean success, @Nullable String error) {
        pendingResult = new PendingActionResult(action, success, error);
    }

    private static class PendingActionResult {
        final String action;
        final boolean success;
        final String error;

        PendingActionResult(String action, boolean success, @Nullable String error) {
            this.action = action;
            this.success = success;
            this.error = error;
        }
    }

    private static class DeviceEntry {
        final long homeId;
        final String devId;
        final String name;

        DeviceEntry(long homeId, String devId, String name) {
            this.homeId = homeId;
            this.devId = devId;
            this.name = name;
        }
    }
}
