package sc.denishik.ru.tuya;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.AtomicFile;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Device binding is encrypted locally and excluded from Android backups. */
public final class KugooProfileStore {
    public static String lastDeviceId(Context context) {
        return context.getSharedPreferences("connection", Context.MODE_PRIVATE).getString("kugoo_dev_id", "");
    }
    public static void rememberDevice(Context context, String id) {
        context.getSharedPreferences("connection", Context.MODE_PRIVATE).edit().putString("kugoo_dev_id", id).apply();
    }
    /** Migrate the previously connected address once; never choose a different saved scooter. */
    public static String restoreDeviceId(Context context) throws Exception {
        if (context.getSharedPreferences("connection", Context.MODE_PRIVATE).contains("kugoo_dev_id")) return lastDeviceId(context);
        String address = context.getSharedPreferences("login", Context.MODE_PRIVATE).getString("address", "");
        if (!address.isEmpty()) {
            for (JSONObject profile : loadAll(context)) {
                if (address.equalsIgnoreCase(profile.optString("_bleAddress", profile.optString("mac")))) {
                    String id = profile.getString("devId");
                    rememberDevice(context, id);
                    return id;
                }
            }
        }
        return "";
    }
    public static synchronized java.util.List<JSONObject> loadAll(Context context) throws Exception {
        JSONObject data = read(context);
        java.util.List<JSONObject> result = new java.util.ArrayList<>();
        if (data == null) return result;
        org.json.JSONArray profiles = data.optJSONArray("profiles");
        if (profiles == null) { result.add(data); return result; } // Existing single-profile installation.
        String selected = data.optString("selected");
        for (int i = 0; i < profiles.length(); i++) {
            JSONObject profile = profiles.getJSONObject(i);
            if (profile.optString("devId").equals(selected)) result.add(0, profile); else result.add(profile);
        }
        return result;
    }
    public static synchronized JSONObject load(Context context) throws Exception {
        java.util.List<JSONObject> profiles = loadAll(context);
        return profiles.isEmpty() ? null : profiles.get(0);
    }
    public static synchronized JSONObject find(Context context, String devId) throws Exception {
        for (JSONObject profile : loadAll(context)) if (profile.optString("devId").equals(devId)) return profile;
        return null;
    }
    public static synchronized void saveAll(Context context, java.util.List<JSONObject> profiles, String selected) throws Exception {
        java.util.Map<String, JSONObject> merged = new java.util.LinkedHashMap<>();
        for (JSONObject profile : loadAll(context)) merged.put(profile.getString("devId"), profile);
        for (JSONObject profile : profiles) merged.put(profile.getString("devId"), profile);
        org.json.JSONArray array = new org.json.JSONArray();
        for (JSONObject profile : merged.values()) array.put(profile);
        write(context, new JSONObject().put("profiles", array).put("selected", selected));
    }
    private static final String ALIAS = "sc_kugoo_offline_profile_v1";
    private static AtomicFile file(Context context) {
        return new AtomicFile(new File(context.getNoBackupFilesDir(), "kugoo-profile.bin"));
    }
    private static SecretKey key(boolean create) throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);
        if (!store.containsAlias(ALIAS) && create) {
            KeyGenerator generator = KeyGenerator.getInstance("AES", "AndroidKeyStore");
            generator.init(new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());
            return generator.generateKey();
        }
        SecretKey key = (SecretKey) store.getKey(ALIAS, null);
        if (key == null) throw new IOException("Ключ сохранённого профиля недоступен. Повторите импорт.");
        return key;
    }
    private static void write(Context context, JSONObject profile) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key(true));
        byte[] bytes = cipher.doFinal(profile.toString().getBytes(StandardCharsets.UTF_8));
        AtomicFile file = file(context);
        FileOutputStream out = null;
        try {
            out = file.startWrite();
            out.write(cipher.getIV());
            out.write(bytes);
            file.finishWrite(out);
        } catch (Exception error) { if (out != null) file.failWrite(out); throw error; }
    }
    private static JSONObject read(Context context) throws Exception {
        AtomicFile file = file(context);
        if (!file.getBaseFile().exists()) return null;
        byte[] bytes = file.readFully();
        if (bytes.length < 28 || bytes.length > 2 * 1024 * 1024) throw new IOException("Повреждён профиль Kugoo");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key(false), new GCMParameterSpec(128, bytes, 0, 12));
        return new JSONObject(new String(cipher.doFinal(bytes, 12, bytes.length - 12), StandardCharsets.UTF_8));
    }
}
