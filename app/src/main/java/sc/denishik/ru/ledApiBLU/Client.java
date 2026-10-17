package sc.denishik.ru.ledApiBLU;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import java.util.*;
import sc.denishik.ru.R;

/** One persistent ELK GATT session. All state and operations are serialized on main. */
@SuppressLint("MissingPermission")
public final class Client {
    public static final String STATE_CHANGED = "sc.denishik.ru.LED_BLE_STATE";
    public interface Callback { void onSuccess(ArrayList<LedBluDevice> devices); void onError(String error); }
    public interface Listener { void onState(); }
    private final Context context;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Map<String, LedBluDevice> devices = new LinkedHashMap<>();
    private BluetoothLeScanner scanner;
    private ScanCallback scan;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic characteristic;
    private LedBluDevice selected;
    private Listener listener;
    private boolean ready, writing, powerPending;
    private int generation, retries, pendingSpeed = -1, sentSpeed = -1, activeSpeed = -1;
    private long lastWrite, telemetryAt;
    private float speed;
    private String status = "";
    private final Runnable timeout = () -> failed(R.string.led_ble_timeout);
    private Runnable stopScanTask;
    private final Runnable pumpTask = this::pump;
    private final Runnable watchdog = new Runnable() {
        @Override public void run() {
            if (!ready) return;
            if (telemetryAt > 0 && SystemClock.elapsedRealtime() - telemetryAt > 3000) {
                pendingSpeed = 0; speed = 0; pump();
            }
            main.postDelayed(this, 1000);
        }
    };

    public Client(Context context) { this.context = context.getApplicationContext(); }
    public boolean isConnected() { return ready; }
    public boolean hasSelection() { return selected != null; }
    public String getName() { return selected == null ? "" : selected.getName(); }
    public String getStatus() { return status; }
    public float getSpeed() { return speed; }
    public int getSentSpeed() { return sentSpeed; }
    public void setListener(Listener value) { listener = value; }
    private void publish(int text) { status = context.getString(text); publish(); }
    private void publish() {
        context.sendBroadcast(new Intent(STATE_CHANGED).setPackage(context.getPackageName()));
        if (listener != null) listener.onState();
    }
    public static boolean hasPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= 31) return context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                && context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    public void onSearch(Callback callback) {
        stopSearch(); devices.clear();
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager == null ? null : manager.getAdapter();
        if (!hasPermissions(context)) { callback.onError(context.getString(R.string.led_ble_permissions)); return; }
        if (adapter == null || !adapter.isEnabled()) { callback.onError(context.getString(R.string.led_ble_enable)); return; }
        scanner = adapter.getBluetoothLeScanner();
        if (scanner == null) { callback.onError(context.getString(R.string.led_ble_enable)); return; }
        scan = new ScanCallback() {
            @Override public void onScanResult(int type, ScanResult result) {
                main.post(() -> {
                    if (scan != this) return;
                    String name = result.getScanRecord() == null ? null : result.getScanRecord().getDeviceName();
                    if (name == null) name = result.getDevice().getName();
                    boolean compatible = name != null && name.startsWith(Config.ELK_STRIP_PREFIX);
                    List<ParcelUuid> services = result.getScanRecord() == null ? null : result.getScanRecord().getServiceUuids();
                    compatible |= services != null && services.contains(ParcelUuid.fromString(Config.ELK_STRIP_SERVICE));
                    if (!compatible) return;
                    String address = result.getDevice().getAddress();
                    devices.put(address, new LedBluDevice(result.getDevice(), name == null ? "LED " + address : name));
                    callback.onSuccess(new ArrayList<>(devices.values()));
                });
            }
            @Override public void onScanFailed(int code) {
                main.post(() -> { if (scan == this) { stopSearch(); callback.onError(context.getString(R.string.led_ble_scan_error) + " (" + code + ")"); } });
            }
        };
        try {
            scanner.startScan(null, new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), scan);
            stopScanTask = () -> { stopSearch(); callback.onSuccess(new ArrayList<>(devices.values())); };
            main.postDelayed(stopScanTask, 12000);
        } catch (RuntimeException error) { stopSearch(); callback.onError(context.getString(R.string.led_ble_scan_error)); }
    }
    public void stopSearch() {
        if (stopScanTask != null) main.removeCallbacks(stopScanTask);
        ScanCallback previous = scan; scan = null;
        try { if (scanner != null && previous != null) scanner.stopScan(previous); } catch (RuntimeException ignored) { }
    }
    public void selectDevice(LedBluDevice device) {
        stopSearch(); closeGatt(); selected = device; retries = 0; pendingSpeed = -1; telemetryAt = 0; connect();
    }
    public void unSelectDevice() {
        selected = null; stopSearch(); closeGatt(); publish(R.string.led_ble_disconnected);
    }
    private void arm(long ms) { main.removeCallbacks(timeout); main.postDelayed(timeout, ms); }
    private void connect() {
        if (selected == null) return;
        final int session = ++generation;
        publish(R.string.led_ble_connecting);
        arm(10000);
        try {
            gatt = selected.device.connectGatt(context, false, new BluetoothGattCallback() {
                private void event(BluetoothGatt source, Runnable task) {
                    main.post(() -> { if (session == generation && source == gatt) task.run(); });
                }
                @Override public void onConnectionStateChange(BluetoothGatt source, int result, int state) {
                    event(source, () -> {
                        if (result != BluetoothGatt.GATT_SUCCESS || state == BluetoothProfile.STATE_DISCONNECTED) { failed(R.string.led_ble_connection_error); return; }
                        if (state == BluetoothProfile.STATE_CONNECTED) {
                            arm(5000);
                            if (!source.discoverServices()) failed(R.string.led_ble_connection_error);
                        }
                    });
                }
                @Override public void onServicesDiscovered(BluetoothGatt source, int result) {
                    event(source, () -> {
                        BluetoothGattService service = source.getService(UUID.fromString(Config.ELK_STRIP_SERVICE));
                        characteristic = service == null ? null : service.getCharacteristic(UUID.fromString(Config.ELK_STRIP_CHARACTERISTIC));
                        if (result != 0 || characteristic == null || (characteristic.getProperties() & 12) == 0
                                || (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) == 0) { failed(R.string.led_ble_unsupported); return; }
                        arm(5000);
                        if (!source.requestMtu(63)) {
                            if (selected.getName().contains("ELK-*")) failed(R.string.led_ble_unsupported);
                            else confirm(session);
                        }
                    });
                }
                @Override public void onMtuChanged(BluetoothGatt source, int mtu, int result) {
                    event(source, () -> {
                        if (selected.getName().contains("ELK-*") && (result != 0 || mtu < 24)) { failed(R.string.led_ble_unsupported); return; }
                        confirm(session);
                    });
                }
                @Override public void onCharacteristicRead(BluetoothGatt source, BluetoothGattCharacteristic ch, int result) {
                    if (Build.VERSION.SDK_INT < 33) onCharacteristicRead(source, ch, ch.getValue(), result);
                }
                @Override public void onCharacteristicRead(BluetoothGatt source, BluetoothGattCharacteristic ch, byte[] value, int result) {
                    event(source, () -> {
                        if (ch != characteristic || ready) return;
                        if (result != 0 || value == null || value.length == 0) { failed(R.string.led_ble_connection_error); return; }
                        main.removeCallbacks(timeout); ready = true; retries = 0; powerPending = true;
                        publish(R.string.led_ble_ready); main.post(watchdog); pump();
                    });
                }
                @Override public void onCharacteristicWrite(BluetoothGatt source, BluetoothGattCharacteristic ch, int result) {
                    event(source, () -> {
                        if (!writing || ch != characteristic) return;
                        main.removeCallbacks(timeout); writing = false;
                        if (result != 0) { failed(R.string.led_ble_write_error); return; }
                        if (activeSpeed >= 0) { sentSpeed = activeSpeed; publish(); }
                        main.postDelayed(pumpTask, 150);
                    });
                }
            }, BluetoothDevice.TRANSPORT_LE);
            if (gatt == null) failed(R.string.led_ble_connection_error);
        } catch (RuntimeException error) { failed(R.string.led_ble_connection_error); }
    }
    private void confirm(int session) {
        arm(5000);
        main.postDelayed(() -> {
            if (session != generation || gatt == null) return;
            try { if (!gatt.readCharacteristic(characteristic)) failed(R.string.led_ble_connection_error); }
            catch (RuntimeException error) { failed(R.string.led_ble_connection_error); }
        }, 500);
    }
    public void sendSpeed(float value, boolean parking) {
        main.post(() -> {
            if (selected == null) return;
            speed = parking || Float.isNaN(value) || Float.isInfinite(value) ? 0 : Math.max(0, value);
            telemetryAt = SystemClock.elapsedRealtime();
            pendingSpeed = Commands.speedPercent(speed, sc.denishik.ru.ledApiWS.Config.TARGET_SPEED);
            pump();
        });
    }
    private void pump() {
        if (!ready || writing || gatt == null) return;
        if (telemetryAt > 0 && SystemClock.elapsedRealtime() - telemetryAt > 3000) {
            pendingSpeed = 0; speed = 0;
        }
        if (!powerPending && (pendingSpeed < 0 || pendingSpeed == sentSpeed)) return;
        long delay = 150 - (SystemClock.elapsedRealtime() - lastWrite);
        if (delay > 0) { main.removeCallbacks(pumpTask); main.postDelayed(pumpTask, delay); return; }
        byte[] packet;
        activeSpeed = powerPending ? -1 : pendingSpeed;
        if (powerPending) { packet = Commands.forDevice(Commands.makePowerCommand(true), selected.getName().contains("ELK-*")); powerPending = false; }
        else packet = Commands.makeSpeedCommand(activeSpeed);
        int type = (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0
                ? BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE : BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
        writing = true; lastWrite = SystemClock.elapsedRealtime(); arm(5000);
        try {
            boolean accepted;
            if (Build.VERSION.SDK_INT >= 33) accepted = gatt.writeCharacteristic(characteristic, packet, type) == BluetoothStatusCodes.SUCCESS;
            else { characteristic.setWriteType(type); characteristic.setValue(packet); accepted = gatt.writeCharacteristic(characteristic); }
            if (!accepted) failed(R.string.led_ble_write_error);
        } catch (RuntimeException error) { failed(R.string.led_ble_write_error); }
    }
    private void failed(int message) {
        closeGatt();
        boolean retry = selected != null && ++retries <= 3;
        if (!retry) selected = null;
        publish(message);
        if (retry) {
            final int expected = generation;
            main.postDelayed(() -> { if (expected == generation && selected != null) connect(); }, 2000L * retries);
        }
    }
    private void closeGatt() {
        generation++; ready = false; writing = false; sentSpeed = -1;
        main.removeCallbacks(timeout); main.removeCallbacks(pumpTask); main.removeCallbacks(watchdog);
        BluetoothGatt previous = gatt; gatt = null; characteristic = null;
        if (previous != null) {
            try { previous.disconnect(); } catch (RuntimeException ignored) { }
            try { previous.close(); } catch (RuntimeException ignored) { }
        }
    }
}
