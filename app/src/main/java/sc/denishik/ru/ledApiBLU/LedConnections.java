package sc.denishik.ru.ledApiBLU;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import org.json.*;
import java.util.*;
import sc.denishik.ru.R;

/** Owns independent LED sessions. Service lifetime controls background discovery. */
public final class LedConnections {
    public static final class Entry {
        public final String address, name;
        public Client client;
        long retryAfter;
        Entry(String address, String name) { this.address = address; this.name = name; }
        public boolean connected() { return client != null && client.isConnected(); }
    }
    private static LedConnections instance;
    public static LedConnections get(Context context) {
        if (instance == null) instance = new LedConnections(context.getApplicationContext());
        return instance;
    }
    private final Context context;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final LinkedHashMap<String, Entry> saved = new LinkedHashMap<>();
    private final Client scanner;
    private boolean running, pickerOpen;
    private long telemetryAt;
    private float lastSpeed;
    private boolean lastParking;
    private final Runnable poll = new Runnable() {
        @Override public void run() {
            if (!running) return;
            discover(); main.postDelayed(this, 30000);
        }
    };
    private LedConnections(Context context) {
        this.context = context; scanner = new Client(context);
        Map<String, String> stored = SavedDevices.decode(context.getSharedPreferences("led_ble", 0).getString("devices", "[]"));
        for (Map.Entry<String, String> item : stored.entrySet()) saved.put(item.getKey(), new Entry(item.getKey(), item.getValue()));
    }
    public List<Entry> entries() { return new ArrayList<>(saved.values()); }
    public boolean contains(String address) { return saved.containsKey(address); }
    public int connectedCount() {
        int count = 0; for (Entry entry : saved.values()) if (entry.connected()) count++; return count;
    }
    public boolean connecting() {
        for (Entry entry : saved.values())
            if (entry.client != null && entry.client.hasSelection() && !entry.connected()) return true;
        return false;
    }
    public void start() {
        if (running) return;
        running = true; main.post(poll);
    }
    public void stop() {
        running = false; main.removeCallbacks(poll); scanner.stopSearch();
        for (Entry entry : saved.values()) {
            if (entry.client != null) { entry.client.setListener(null); entry.client.unSelectDevice(); entry.client = null; }
        }
        telemetryAt = 0;
    }
    public void pickerOpen(boolean value) {
        pickerOpen = value;
        if (value) scanner.stopSearch();
    }
    public void remember(LedBluDevice device, Client client) {
        Entry entry = saved.get(device.getAddress());
        if (entry == null) { entry = new Entry(device.getAddress(), device.getName()); saved.put(entry.address, entry); }
        if (entry.client != null && entry.client != client) { entry.client.setListener(null); entry.client.unSelectDevice(); }
        entry.client = client; observe(entry); persist(); changed();
        if (SystemClock.elapsedRealtime() - telemetryAt < 3000) client.sendSpeed(lastSpeed, lastParking);
    }
    private void observe(Entry entry) {
        entry.client.setListener(() -> {
            if (!entry.client.hasSelection()) entry.retryAfter = SystemClock.elapsedRealtime() + 60000;
        });
    }
    public void forget(String address) {
        Entry entry = saved.remove(address);
        if (entry != null && entry.client != null) { entry.client.setListener(null); entry.client.unSelectDevice(); }
        persist(); changed();
    }
    private void persist() {
        Map<String, String> data = new LinkedHashMap<>();
        for (Entry entry : saved.values()) data.put(entry.address, entry.name);
        context.getSharedPreferences("led_ble", 0).edit().putString("devices", SavedDevices.encode(data)).apply();
    }
    public void sendSpeed(float speed, boolean parking) {
        main.post(() -> {
            lastSpeed = speed; lastParking = parking; telemetryAt = SystemClock.elapsedRealtime();
            for (Entry entry : saved.values()) if (entry.client != null) entry.client.sendSpeed(speed, parking);
        });
    }
    private void discover() {
        if (pickerOpen || connecting() || !Client.hasPermissions(context)) return;
        boolean missing = false;
        long now = SystemClock.elapsedRealtime();
        for (Entry entry : saved.values()) if (!entry.connected() && now >= entry.retryAfter) missing = true;
        if (!missing) return;
        scanner.onSearch(new Client.Callback() {
            @Override public void onSuccess(ArrayList<LedBluDevice> devices) {
                if (!running || pickerOpen || connecting()) return;
                for (LedBluDevice device : devices) {
                    Entry entry = saved.get(device.getAddress());
                    if (entry == null || entry.connected() || SystemClock.elapsedRealtime() < entry.retryAfter) continue;
                    scanner.stopSearch();
                    if (entry.client != null) { entry.client.setListener(null); entry.client.unSelectDevice(); }
                    entry.client = new Client(context); observe(entry);
                    entry.client.selectDevice(device);
                    if (SystemClock.elapsedRealtime() - telemetryAt < 3000) entry.client.sendSpeed(lastSpeed, lastParking);
                    return;
                }
            }
            @Override public void onError(String error) { /* Retry at the next bounded scan window. */ }
        });
    }
    public String summary() {
        StringBuilder result = new StringBuilder(context.getString(R.string.led_ble_count, connectedCount(), saved.size()));
        for (Entry entry : saved.values()) {
            result.append("\n\n").append(entry.name).append("\n");
            if (entry.client == null) result.append(context.getString(R.string.led_ble_waiting));
            else if (!entry.client.hasSelection()) result.append(entry.client.getStatus()).append("\n").append(context.getString(R.string.led_ble_waiting));
            else if (!entry.connected()) result.append(entry.client.getStatus());
            else result.append(context.getString(R.string.led_ble_values, entry.client.getSpeed(), Math.max(0, entry.client.getSentSpeed())));
        }
        return result.toString();
    }
    private void changed() { context.sendBroadcast(new Intent(Client.STATE_CHANGED).setPackage(context.getPackageName())); }
}
