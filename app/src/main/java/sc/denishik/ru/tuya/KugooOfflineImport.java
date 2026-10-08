package sc.denishik.ru.tuya;

import android.content.Context;
import android.util.Base64;
import com.thingclips.smart.mmkv.MMKV;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/** Imports device records from an isolated, disposable snapshot; never opens OEM MMKV files in place. */
final class KugooOfflineImport {
    static List<JSONObject> fromRoot(Context context) throws Exception {
        String source = "/data/user/" + (android.os.Process.myUid() / 100000) + "/com.kugoo.cn";
        String command = "CLASSPATH=" + quote(context.getApplicationInfo().sourceDir)
                + " /system/bin/app_process /system/bin sc.denishik.ru.tuya.KugooRootReader " + quote(source);
        Process process = new ProcessBuilder("su", "--mount-master", "-c", command).redirectErrorStream(true).start();
        ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
        timer.schedule(() -> { process.destroy(); try { process.getInputStream().close(); } catch (IOException ignored) {} }, 40, TimeUnit.SECONDS);
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            while ((n = process.getInputStream().read(buffer)) != -1) {
                if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
                if (output.size() + n > 40 * 1024 * 1024) throw new IOException("Слишком большой кеш Kugoo");
                output.write(buffer, 0, n);
            }
            String text = output.toString("UTF-8");
            int marker = text.indexOf("SC_KUGOO:");
            int exitCode = process.waitFor();
            String diagnostic = "exit=" + exitCode;
            java.util.regex.Matcher failure = java.util.regex.Pattern.compile("SC_KUGOO_ERROR:([a-z-]+:[A-Za-z]+)").matcher(text);
            if (failure.find()) diagnostic += ", " + failure.group(1);
            if (text.contains("Permission denied")) diagnostic += ", permission denied";
            if (text.contains("ClassNotFoundException")) diagnostic += ", helper class not found";
            if (exitCode != 0 || marker < 0) throw new IOException("[" + diagnostic + "] " +
                    "Root-чтение недоступно. Разрешите su для SC-Android. Проверьте com.kugoo.cn и откройте в нём самокат. "
                    + "KeyStore/SELinux этой прошивки может запрещать чтение ключа; данные оригинала не изменены.");
            String payload = text.substring(marker + "SC_KUGOO:".length()).split("\n", 2)[0];
            return snapshot(context, new JSONObject(payload));
        } finally {
            process.destroy();
            timer.shutdownNow();
        }
    }

    private static List<JSONObject> snapshot(Context context, JSONObject payload) throws Exception {
        Map<String, JSONObject> devices = new LinkedHashMap<>();
        Map<String, JSONObject> products = new LinkedHashMap<>();
        collect(payload.optJSONArray("values"), devices, products, 0);
        MMKV.initialize(context);
        File folder = new File(context.getCacheDir(), "kugoo-import-" + UUID.randomUUID());
        if (!folder.mkdir()) throw new IOException("Не удалось создать временный каталог");
        Map<String, String> addresses = new HashMap<>();
        try {
            JSONArray stores = payload.getJSONArray("stores");
            for (int i = 0; i < stores.length(); i++) {
                JSONObject store = stores.getJSONObject(i);
                String id = store.getString("id");
                if (!id.matches("[a-zA-Z0-9_.-]+") || id.equals(".") || id.equals("..")) continue;
                // Unknown encryption keys must never be treated as unencrypted MMKV.
                String key = store.optString("key");
                if (key.isEmpty()) continue;
                File storeFolder = new File(folder, id);
                if (!storeFolder.mkdir()) throw new IOException("Не удалось создать копию хранилища");
                // This SDK hashes filenames when a custom root is used. Ask it to create the
                // isolated file pair, close it, then replace that pair with the OEM snapshot.
                MMKV empty = MMKV.mmkvWithID(id, 1, key, storeFolder.getAbsolutePath());
                if (empty == null) continue;
                empty.close();
                File[] generated = storeFolder.listFiles((dir, name) -> name.endsWith(".crc"));
                if (generated == null || generated.length != 1) throw new IOException("Неизвестный формат MMKV");
                File crcFile = generated[0];
                File dataFile = new File(storeFolder, crcFile.getName().substring(0, crcFile.getName().length() - 4));
                write(dataFile, Base64.decode(store.getString("data"), Base64.DEFAULT));
                write(crcFile, Base64.decode(store.getString("crc"), Base64.DEFAULT));
                MMKV kv = MMKV.mmkvWithID(id, 1, key, storeFolder.getAbsolutePath());
                if (kv == null) continue;
                try {
                    String[] keys = kv.allKeys();
                    if (keys == null) continue;
                    for (String entry : keys) {
                        if (entry.endsWith("_time") || entry.equals("migrated")) continue;
                        String value = kv.getString(entry, null);
                        if (value == null) continue;
                        if (id.equals("ble_business_data")) addresses.put(entry, value);
                        collect(value, devices, products, 0);
                    }
                } finally { kv.close(); }
            }
        } finally {
            File[] files = folder.listFiles();
            if (files != null) for (File file : files) {
                File[] children = file.listFiles();
                if (children != null) for (File child : children) child.delete();
                file.delete();
            }
            folder.delete();
        }
        for (JSONObject device : devices.values()) {
            String record = addresses.get(device.optString("uuid"));
            if (record == null) continue;
            String[] fields = record.split("#");
            if (fields.length == 3 && android.bluetooth.BluetoothAdapter.checkBluetoothAddress(fields[1])) {
                try {
                    device.put("_bleAddress", fields[1]);
                    device.put("_bleType", Integer.parseInt(fields[0]));
                    device.put("_bleFlag", Integer.parseInt(fields[2]));
                } catch (NumberFormatException ignored) { }
            }
        }
        if (devices.isEmpty()) throw new IOException("В поддерживаемых XML/MMKV-кешах не найдена запись devId + uuid + productId + localKey. "
                + "Откройте самокат в Kugoo и повторите. Для другой версии кеша используйте импорт JSON записи устройства.");
        attachProducts(devices, products);
        return new ArrayList<>(devices.values());
    }

    static List<JSONObject> fromJson(String text) throws Exception {
        if (text.length() > 2 * 1024 * 1024) throw new IOException("JSON слишком большой");
        Map<String, JSONObject> devices = new LinkedHashMap<>();
        Map<String, JSONObject> products = new LinkedHashMap<>();
        collect(new org.json.JSONTokener(text).nextValue(), devices, products, 0);
        if (devices.isEmpty()) throw new IOException("Нужны devId, uuid, productId и localKey в одной записи устройства");
        attachProducts(devices, products);
        return new ArrayList<>(devices.values());
    }

    private static void collect(Object value, Map<String, JSONObject> devices, Map<String, JSONObject> products, int depth) throws Exception {
        if (depth > 16 || value == null || value == JSONObject.NULL) return;
        if (value instanceof String) {
            String text = ((String) value).trim();
            if (text.startsWith("{") || text.startsWith("[")) {
                try { collect(new org.json.JSONTokener(text).nextValue(), devices, products, depth + 1); }
                catch (org.json.JSONException ignored) { }
            }
        } else if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            for (int i = 0; i < array.length(); i++) collect(array.get(i), devices, products, depth + 1);
        } else if (value instanceof JSONObject) {
            JSONObject object = (JSONObject) value;
            if (valid(object, "id") && object.has("capability") && (object.has("schemaInfo") || object.has("schema"))) {
                products.put(object.getString("id"), new JSONObject(object.toString()));
            }
            if (valid(object, "devId") && valid(object, "uuid") && valid(object, "productId") && valid(object, "localKey")) {
                devices.put(object.getString("devId"), new JSONObject(object.toString()));
                return;
            }
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) collect(object.get(keys.next()), devices, products, depth + 1);
        }
    }

    private static void attachProducts(Map<String, JSONObject> devices, Map<String, JSONObject> products) throws Exception {
        for (JSONObject device : devices.values()) {
            JSONObject product = products.get(device.optString("productId"));
            if (product != null) device.put("_product", product);
        }
    }

    private static boolean valid(JSONObject object, String key) {
        Object value = object.opt(key);
        return value instanceof String && !((String) value).trim().isEmpty() && !"null".equals(value);
    }

    private static void write(File file, byte[] bytes) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) { out.write(bytes); }
    }

    private static String quote(String value) { return "'" + value.replace("'", "'\"'\"'") + "'"; }
}
