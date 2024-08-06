package sc.denishik.ru.ledApiBLU;

import static sc.denishik.ru.ledApiBLU.Config.DMX_STRIP_CHARACTERISTIC;
import static sc.denishik.ru.ledApiBLU.Config.DMX_STRIP_SERVICE;
import static sc.denishik.ru.ledApiBLU.Config.ELK_STRIP_CHARACTERISTIC;
import static sc.denishik.ru.ledApiBLU.Config.ELK_STRIP_PREFIX;
import static sc.denishik.ru.ledApiBLU.Config.ELK_STRIP_SERVICE;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class Client {
    Context context;
    String TAG = "ClienLedBLU";
    ArrayList<LedBluDevice> devices = new ArrayList<>();
    LedBluDevice currentDevice;
    boolean isSend = false;


    public Client(Context context) {
        this.context = context;
        BluetoothManager bluetoothManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            bluetoothManager = (BluetoothManager) context.getSystemService(BluetoothManager.class);
        }
        assert bluetoothManager != null;
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
    }

    public interface Callback {
        void onSuccess(ArrayList<LedBluDevice> result);

        void onError(String err);
    }

    @SuppressLint("MissingPermission")
    public void onSearch(Callback callback) {
        try {
            final BluetoothLeScanner bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
            bluetoothLeScanner.startScan(new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    if (String.valueOf(result.getDevice().getName()).contains(ELK_STRIP_PREFIX)) {
                        if (!devicesContainsName(String.valueOf(result.getDevice().getName()))) {
                            devices.add(new LedBluDevice(result.getDevice(), result.getDevice().getName()));
                        }
                    }
                    callback.onSuccess(devices);
                }
            });
        } catch (Exception e) {
            Log.d(TAG, String.valueOf(e.getMessage()));
            callback.onError(String.valueOf(e.getMessage()));
        }
    }


    boolean devicesContainsName(String name) {
        for (LedBluDevice device : devices) {
            if (device.getName().contains(name)) {
                return true;
            }
        }
        return false;
    }

    public void selectDevice(LedBluDevice device) {
        currentDevice = device;
        sendCommand(Commands.makePowerCommand(true));
    }

    public void unSelectDevice() {
        currentDevice = null;
        sendCommand(Commands.makePowerCommand(false));
    }

    @SuppressLint("MissingPermission")
    public void sendCommand(byte[] commandBytes) {
        try {
            if (!isSend) {
                Log.d(TAG, "onSend>" + Arrays.toString(commandBytes));
                isSend = true;
                if (currentDevice != null) {
                    currentDevice.device.connectGatt(context, false, new BluetoothGattCallback() {
                        @Override
                        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                            Log.d(TAG, "onServicesDiscovered>" + status);
                            super.onServicesDiscovered(gatt, status);
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                BluetoothGattService service = gatt.getService(UUID.fromString(ELK_STRIP_SERVICE));
                                BluetoothGattCharacteristic ch = service.getCharacteristic(UUID.fromString(ELK_STRIP_CHARACTERISTIC));
                                if (Build.VERSION.SDK_INT < 33) {
                                    Log.d(TAG, "writing");
                                    ch.setWriteType(1);
                                    ch.setValue(commandBytes);
                                    if (!gatt.writeCharacteristic(ch)) {
                                        Log.e(TAG, "error write");
                                    }
                                } else {
//                                gatt.writeCharacteristic(gatt, commandBytes, 1);
                                }
                                gatt.disconnect();
                                gatt.close();
                                isSend = false;
                            }

                        }

                        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
                            Log.d(TAG, "onCharacteristicWrite> " + Arrays.toString(bluetoothGattCharacteristic.getValue()) + " " + String.valueOf(i));
                            super.onCharacteristicWrite(bluetoothGatt, bluetoothGattCharacteristic, i);
                        }


                        @Override
                        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                            Log.d(TAG, " onConnectionStateChange>" + status + " " + newState);
                            super.onConnectionStateChange(gatt, status, newState);
                            gatt.discoverServices();
                        }
                    });
                }
            }
        } catch (Exception e) {
            Log.e(TAG, String.valueOf(e.getMessage()));
        }
    }
}
