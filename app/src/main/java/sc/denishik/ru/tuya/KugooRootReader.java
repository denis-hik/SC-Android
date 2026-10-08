package sc.denishik.ru.tuya;

import android.system.Os;
import android.util.Base64;
import android.util.Xml;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Separate app_process entry point. Reads only the selected OEM app; never repairs its stores. */
public final class KugooRootReader {
    private static final int MAX_FILE = 8 * 1024 * 1024;
    private static int total;

    public static void main(String[] args) {
        String stage = "source";
        try {
            if (args.length != 1 || !args[0].matches("/data/user/[0-9]+/com\\.kugoo\\.cn")) {
                throw new IllegalArgumentException("Unsupported source package");
            }
            File source = new File(args[0]);
            int uid = Os.stat(source.getPath()).st_uid;
            if (uid < 10000) throw new IllegalArgumentException("Invalid application UID");
            Map<String, String> global = readXml(new File(source, "shared_prefs/global_preference.xml"));
            // Snapshot while still root. No chown/chmod, copying into the OEM app, or SDK init.
            JSONArray stores = new JSONArray();
            File directory = new File(source, "files/thingmmkv");
            if (!directory.isDirectory()) directory = new File(source, "files/mmkv");
            File[] files = directory.listFiles();
            if (files != null) for (File file : files) {
                String name = file.getName();
                if (!file.isFile() || name.endsWith(".crc") || !deviceStore(name)) continue;
                File crc = new File(directory, name + ".crc");
                if (!crc.isFile()) continue;
                stores.put(new JSONObject().put("id", name)
                        .put("data", Base64.encodeToString(read(file), Base64.NO_WRAP))
                        .put("crc", Base64.encodeToString(read(crc), Base64.NO_WRAP)));
            }
            JSONArray values = new JSONArray();
            File[] prefs = new File(source, "shared_prefs").listFiles();
            if (prefs != null) for (File file : prefs) {
                if (file.getName().endsWith(".xml") && deviceStore(file.getName())) {
                    for (String value : readXml(file).values()) {
                        if (value.contains("localKey") && value.contains("devId")) values.put(value);
                    }
                }
            }
            // AndroidKeyStore keys belong to the OEM UID. A copied preference file alone is insufficient.
            // SELinux/KeyStore may deny this on some ROMs; report failure without generating new keys.
            stage = "switch-uid";
            Os.setgid(uid);
            Os.setuid(uid);
            stage = "provider";
            if (java.security.Security.getProvider("AndroidKeyStore") == null) {
                String provider = android.os.Build.VERSION.SDK_INT >= 31
                        ? "android.security.keystore2.AndroidKeyStoreProvider"
                        : "android.security.keystore.AndroidKeyStoreProvider";
                Class.forName(provider).getMethod("install").invoke(null);
            }
            stage = "key-store";
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            String alias = global.get(hash("OverridingAlias"));
            if (alias == null) alias = "preference_alias";
            stage = "key-access";
            SecretKey key = (SecretKey) keyStore.getKey(alias + "_aes_key", null);
            if (key == null) throw new IllegalStateException("OEM KeyStore key unavailable");
            stage = "decrypt";
            String encryptedKeys = global.get(hash("__SECURITY_KEY__"));
            JSONObject keys = encryptedKeys == null ? new JSONObject() : new JSONObject(decrypt(encryptedKeys, key));
            for (int i = 0; i < stores.length(); i++) {
                JSONObject store = stores.getJSONObject(i);
                String id = store.getString("id");
                String storeKey = keys.optString(id, "");
                if (storeKey.isEmpty() && global.containsKey(hash(id))) storeKey = decrypt(global.get(hash(id)), key);
                store.put("key", storeKey);
            }
            for (String encrypted : global.values()) {
                if (!encrypted.contains("]")) continue;
                try {
                    String value = decrypt(encrypted, key);
                    if (value.contains("localKey") && value.contains("devId")) values.put(value);
                } catch (Exception ignored) { /* Metadata is not necessarily encrypted. */ }
            }
            System.out.println("SC_KUGOO:" + new JSONObject().put("stores", stores).put("values", values));
        } catch (Throwable error) {
            // Do not include exception messages: providers sometimes include secret input.
            System.out.println("SC_KUGOO_ERROR:" + stage + ":" + error.getClass().getSimpleName());
            System.exit(1);
        }
    }

    private static boolean deviceStore(String name) {
        String lower = name.toLowerCase(java.util.Locale.ROOT);
        return lower.startsWith("preferences_global_key") || lower.startsWith("common_storage") || lower.contains("device") || lower.contains("dev_list") || lower.contains("home")
                || lower.contains("ble_business") || lower.contains("dev_cache");
    }

    private static String decrypt(String value, SecretKey key) throws Exception {
        String[] parts = value.split("]", -1);
        if (parts.length != 2) throw new IllegalArgumentException("Unsupported preference format");
        byte[] iv = Base64.decode(parts[0], Base64.DEFAULT);
        if (iv.length != 12) throw new IllegalArgumentException("Unsupported IV");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
        return new String(cipher.doFinal(Base64.decode(parts[1], Base64.DEFAULT)), StandardCharsets.UTF_8);
    }

    private static String hash(String value) throws Exception {
        StringBuilder result = new StringBuilder();
        for (byte b : MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))) {
            result.append(String.format(java.util.Locale.ROOT, "%02X", b & 255));
        }
        return result.toString();
    }

    private static Map<String, String> readXml(File file) throws Exception {
        Map<String, String> result = new LinkedHashMap<>();
        if (!file.isFile()) return result;
        byte[] bytes = read(file);
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new java.io.ByteArrayInputStream(bytes), "UTF-8");
        for (int event = parser.getEventType(); event != XmlPullParser.END_DOCUMENT; event = parser.next()) {
            if (event == XmlPullParser.START_TAG && "string".equals(parser.getName())) {
                String name = parser.getAttributeValue(null, "name");
                result.put(name, parser.nextText());
            }
        }
        return result;
    }

    private static byte[] read(File file) throws Exception {
        if (file.length() > MAX_FILE) throw new IllegalStateException("Cache too large");
        try (FileInputStream input = new FileInputStream(file); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                total += count;
                if (out.size() + count > MAX_FILE || total > 24 * 1024 * 1024) throw new IllegalStateException("Cache too large");
                out.write(buffer, 0, count);
            }
            return out.toByteArray();
        }
    }
}
