package sc.denishik.ru.midwayApi;

import static sc.denishik.ru.kuggoApi.Config.NAME_SCOOTER_KUGOO_BLU;
import static sc.denishik.ru.midwayApi.Config.NAME_SCOOTER_BLU;
import static sc.denishik.ru.midwayApi.Config.PIN_CODE;
import static sc.denishik.ru.other.Utils.isServiceRunning;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.util.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import cn.wandersnail.ble.ConnectionConfiguration;
import cn.wandersnail.ble.Device;
import cn.wandersnail.ble.EasyBLE;
import cn.wandersnail.ble.EventObserver;
import cn.wandersnail.ble.Request;
import sc.denishik.ru.ServiceScooter;
import sc.denishik.ru.other.BluetoothConnect;

public class ScootersApi {
    private static BluetoothConnect blu;
    private static BluetoothConnect.BluetoothConnectionListener _blu_bluetooth_connection_listener;
    private static String TAG = "ScootersApi";
    private static final Set<String> kugooScanLogged = new HashSet<>();

    public interface getScooterCallback {
        void onGetScooter(ArrayList<Scooter> result);

        void onGetError();
    }

    public static void getScooters(AppCompatActivity activity, getScooterCallback callback) {
        Log.d(TAG, "getScooters: onCreate");
        try {
            final android.content.SharedPreferences bindPrefs =
                    activity.getSharedPreferences("bind_state", Context.MODE_PRIVATE);
            ArrayList<BluetoothDevice> devicesTemp = new ArrayList<>();
            blu = new BluetoothConnect(activity);
            blu.getAllDevices(new BluetoothConnect.getAllDevicesCallback() {
                @SuppressLint("MissingPermission")
                @Override
                public void onScan(ArrayList<BluetoothDevice> result, ArrayList<Device> result2) {
                    ArrayList<Scooter> scootersTemp = new ArrayList<>();
                    for (Device device : result2) {
                        Scooter s = new Scooter(device.getName(), String.valueOf(device.getOriginDevice()), "0%", String.valueOf(device.getAddress()), device);
                        s.setIsBinded(bindPrefs.getBoolean(s.getAddress(), false));
                        if (String.valueOf(device.getName()).contains(NAME_SCOOTER_BLU)) {
                            scootersTemp.add(s);
                        }
                    }
                    for (BluetoothDevice device : result) {
                        Scooter s = new Scooter(device.getName(), String.valueOf(device.getType()), "0%", String.valueOf(device.getAddress()), device);
                        s.setIsBinded(bindPrefs.getBoolean(s.getAddress(), false));
                        if (String.valueOf(device.getName()).contains(NAME_SCOOTER_BLU)) {
                            scootersTemp.add(s);
                        }
                    }
                    callback.onGetScooter(scootersTemp);
                }

                @Override
                public void onError() {
                    callback.onGetError();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "getScooters: onErr>".concat(String.valueOf(e.getMessage())));
            callback.onGetError();
        }

    }

    private static void logKugooScanRecord(Device device) {
        try {
            String addr = device.getAddress();
            if (addr != null && kugooScanLogged.contains(addr)) return;
            if (addr != null) kugooScanLogged.add(addr);

            byte[] scanBytes = tryGetScanRecordBytes(device);
            if (scanBytes == null || scanBytes.length == 0) return;

            StringBuilder sb = new StringBuilder(scanBytes.length * 3);
            for (int i = 0; i < scanBytes.length; i++) {
                sb.append(String.format("%02X", scanBytes[i]));
                if (i + 1 < scanBytes.length) sb.append(' ');
            }
            Log.d("KugooScan", "addr=" + device.getAddress() + " name=" + device.getName() + " scanRecord=" + sb);

            int index = 0;
            while (index < scanBytes.length) {
                int len = scanBytes[index] & 0xFF;
                if (len == 0) break;
                int typeIndex = index + 1;
                int valueIndex = index + 2;
                int valueLen = len - 1;
                if (typeIndex >= scanBytes.length) break;
                int type = scanBytes[typeIndex] & 0xFF;

                if (valueIndex + valueLen > scanBytes.length) break;

                if (type == 0x16 && valueLen >= 2) {
                    int uuid = (scanBytes[valueIndex] & 0xFF) | ((scanBytes[valueIndex + 1] & 0xFF) << 8);
                    if (uuid == 0xFD50) {
                        StringBuilder sd = new StringBuilder();
                        for (int i = valueIndex + 2; i < valueIndex + valueLen; i++) {
                            sd.append(String.format("%02X", scanBytes[i]));
                            if (i + 1 < valueIndex + valueLen) sd.append(' ');
                        }
                        Log.d("KugooScan", "addr=" + device.getAddress() + " FD50 serviceData=" + sd);
                    }
                } else if (type == 0xFF && valueLen >= 2) {
                    int companyId = (scanBytes[valueIndex] & 0xFF) | ((scanBytes[valueIndex + 1] & 0xFF) << 8);
                    StringBuilder md = new StringBuilder();
                    for (int i = valueIndex + 2; i < valueIndex + valueLen; i++) {
                        md.append(String.format("%02X", scanBytes[i]));
                        if (i + 1 < valueIndex + valueLen) md.append(' ');
                    }
                    Log.d("KugooScan", "addr=" + device.getAddress() + " mfgData companyId=0x" + String.format("%04X", companyId) + " data=" + md);
                } else if (type == 0x09 || type == 0x08) {
                    try {
                        String name = new String(scanBytes, valueIndex, valueLen, java.nio.charset.StandardCharsets.UTF_8);
                        Log.d("KugooScan", "addr=" + device.getAddress() + " advName=" + name);
                    } catch (Throwable ignored) {
                    }
                }
                index += (len + 1);
            }
        } catch (Throwable t) {
            Log.w("KugooScan", "Failed to read scanRecord for " + device.getAddress(), t);
        }
    }

    @Nullable
    private static byte[] tryGetScanRecordBytes(Device device) {
        try {
            java.lang.reflect.Method m = device.getClass().getMethod("getScanResult");
            Object r = m.invoke(device);
            if (r instanceof ScanResult) {
                ScanRecord sr = ((ScanResult) r).getScanRecord();
                if (sr != null) return sr.getBytes();
            }
        } catch (Throwable ignored) {
        }
        try {
            java.lang.reflect.Method m = device.getClass().getMethod("getScanRecord");
            Object r = m.invoke(device);
            if (r instanceof byte[]) return (byte[]) r;
            if (r instanceof ScanRecord) return ((ScanRecord) r).getBytes();
        } catch (Throwable ignored) {
        }
        return null;
    }

    public interface connectScooterCallback {
            void onSuccess(Scooter scooter);
            void onError(String err);
    }
    public static void connectScooter(AppCompatActivity activity, Scooter scooter, connectScooterCallback callback) {
        if (!scooter.getIsBluDevice()) {
            EasyBLE ble = EasyBLE.getInstance();
            Application application = activity.getApplication();
            Log.d("EasyBLE", "initialize> ".concat(String.valueOf(application)) );
            ble.initialize(application);
            Log.d("EasyBLE", "connect> ".concat(String.valueOf(scooter)) );
            Device d = scooter.getDevice();
            ConnectionConfiguration configuration = new ConnectionConfiguration();
            configuration.setConnectTimeoutMillis(5000)
                    .setRequestTimeoutMillis(1000)
                    .setAutoReconnect(true)
                    .setScanIntervalPairsInAutoReconnection(Arrays.asList(new Pair[]{new Pair(0, 100), new Pair(10, 500), new Pair(20, 1000), new Pair(30, 3000)}))
                    .setDiscoverServicesDelayMillis(500)
                    .setReconnectImmediatelyMaxTimes(100)
                    .setTryReconnectMaxTimes(50);
            ble.disconnectAllConnections();
            ble.setLogEnabled(true);
            ble.connect(scooter.getDevice().getAddress(), configuration, new EventObserver() {
                @Override
                public void onBluetoothAdapterStateChanged(int state) {
                    Log.d("onBluetoothAdapterStateChanged", String.valueOf(state));
                }

                @Override
                public void onCharacteristicRead(@NonNull Request request, @NonNull byte[] value) {
                    Log.d("onCharacteristicRead", String.valueOf(request).concat(String.valueOf(value)));
                }

                @Override
                public void onCharacteristicChanged(@NonNull Device device, @NonNull UUID service, @NonNull UUID characteristic, @NonNull byte[] value) {
                    Log.d("onCharacteristicChanged", String.valueOf(device).concat(String.valueOf(service).concat(String.valueOf(value))));
                }

                @Override
                public void onCharacteristicWrite(@NonNull Request request, @NonNull byte[] value) {
                    Log.d("onCharacteristicWrite", String.valueOf(request).concat(String.valueOf(value)));
                }

                @Override
                public void onRssiRead(@NonNull Request request, int rssi) {
                    Log.d("onRssiRead", String.valueOf(request).concat(String.valueOf(rssi)));
                }

                @Override
                public void onDescriptorRead(@NonNull Request request, @NonNull byte[] value) {
                    Log.d("onDescriptorRead", String.valueOf(request).concat(String.valueOf(value)));
                }

                @Override
                public void onNotificationChanged(@NonNull Request request, boolean isEnabled) {
                    Log.d("onNotificationChanged", String.valueOf(request).concat(String.valueOf(isEnabled)));
                }

                @Override
                public void onIndicationChanged(@NonNull Request request, boolean isEnabled) {
                    Log.d("onIndicationChanged", String.valueOf(request).concat(String.valueOf(isEnabled)));
                }

                @Override
                public void onMtuChanged(@NonNull Request request, int mtu) {
                    Log.d("onIndicationChanged", String.valueOf(request).concat(String.valueOf(mtu)));
                }

                @Override
                public void onPhyChange(@NonNull Request request, int txPhy, int rxPhy) {
                    EventObserver.super.onPhyChange(request, txPhy, rxPhy);
                }

                @Override
                public void onRequestFailed(@NonNull Request request, int failType, int gattStatus, @Nullable Object value) {
                    EventObserver.super.onRequestFailed(request, failType, gattStatus, value);
                }

                @Override
                public void onConnectionStateChanged(@NonNull Device device) {
                    if (scooter.getDevice().getName().equals(device.getName()) && device.isConnected()) {
                        if (!isServiceRunning(ServiceScooter.class, activity)) {
                            Log.d(TAG, "start service");
                            Intent service = new Intent(activity, ServiceScooter.class);
                            service.putExtra("siid", scooter.getAddress());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                activity.startForegroundService(service);
                            } else {
                                activity.startService(service);

                            }
                        }
                        callback.onSuccess(scooter);
                    }
                }

                @Override
                public void onConnectFailed(@NonNull Device device, int failType) {
                    EventObserver.super.onConnectFailed(device, failType);
                }

                @Override
                public void onConnectTimeout(@NonNull Device device, int type) {
                    EventObserver.super.onConnectTimeout(device, type);
                }

                @Override
                public void onConnectionError(@NonNull Device device, int status) {
                    EventObserver.super.onConnectionError(device, status);
                }
            });
        }
    }
    public static void connectScooter(Scooter scooter, EasyBLE ble, EventObserver observer, connectScooterCallback callback) {
        try {
            Log.d("EasyBLE", "connect> ".concat(String.valueOf(scooter)) );
            ConnectionConfiguration configuration = new ConnectionConfiguration();
            configuration.setConnectTimeoutMillis(5000)
                    .setRequestTimeoutMillis(1000)
                    .setAutoReconnect(true)
                    .setScanIntervalPairsInAutoReconnection(Arrays.asList(new Pair[]{new Pair(0, 100), new Pair(10, 500), new Pair(20, 1000), new Pair(30, 3000)}))
                    .setDiscoverServicesDelayMillis(500)
                    .setReconnectImmediatelyMaxTimes(100)
                    .setTryReconnectMaxTimes(50);
            ble.disconnectAllConnections();
            ble.setLogEnabled(true);
            ble.connect(scooter.getAddress(), configuration, observer);
        } catch (Exception err) {
            Log.e(TAG, "connectScooter: ".concat(String.valueOf(err.getMessage())));
            callback.onError(String.valueOf(err.getMessage()));
        }

    }
}
