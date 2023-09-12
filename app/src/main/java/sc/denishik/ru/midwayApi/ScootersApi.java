package sc.denishik.ru.midwayApi;

import static sc.denishik.ru.midwayApi.Config.NAME_SCOOTER_BLU;
import static sc.denishik.ru.midwayApi.Config.PIN_CODE;
import static sc.denishik.ru.other.Utils.isServiceRunning;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
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

    public interface getScooterCallback {
        void onGetScooter(ArrayList<Scooter> result);

        void onGetError();
    }

    public static void getScooters(AppCompatActivity activity, getScooterCallback callback) {
        Log.d(TAG, "getScooters: onCreate");
        try {
            ArrayList<BluetoothDevice> devicesTemp = new ArrayList<>();
            blu = new BluetoothConnect(activity);
            blu.getAllDevices(new BluetoothConnect.getAllDevicesCallback() {
                @SuppressLint("MissingPermission")
                @Override
                public void onScan(ArrayList<BluetoothDevice> result, ArrayList<Device> result2) {

                    ArrayList<Scooter> scootersTemp = new ArrayList<>();
                    Log.d(TAG, "onScan".concat(String.valueOf(result.toString())));
                    for (Device device : result2) {
                        Scooter s = new Scooter(device.getName(), String.valueOf(device.getOriginDevice()), "0%", String.valueOf(device.getAddress()), device);
                        if (String.valueOf(device.getName()).contains(NAME_SCOOTER_BLU)) {
                            scootersTemp.add(s);
                        }
                    }
                    for (BluetoothDevice device : result) {
                        Scooter s = new Scooter(device.getName(), String.valueOf(device.getType()), "0%", String.valueOf(device.getAddress()), device);
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
