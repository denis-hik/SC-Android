package sc.denishik.ru.kuggoApi;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import com.thingclips.smart.android.network.ThingSmartNetWork;
import com.thingclips.smart.android.network.util.ParseHelper;
import okhttp3.*;
import org.json.*;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;
import java.util.concurrent.*;
import javax.crypto.Cipher;

/** Direct HTTP login using the installed Kugoo APK's crypto implementation.
 * No credentials, sessions or device keys are logged or persisted.
 * Does not use ThingHomeSdk or silently fall back to a different app account.
 * Currently verified with Kugoo 1.0.2 (SDK 6.5.0), country 7, EU backend.
 */
public final class Login {
    private Login() { }
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor();
    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(25, TimeUnit.SECONDS).followRedirects(false)
            .followSslRedirects(false).build();
    public interface Callback { void onResult(List<JSONObject> profiles); }
    public interface ResultCallback { void onResult(List<JSONObject> profiles, String errorCode); }
    public interface ProgressCallback { void onProgress(String stage); }

    /** Compatibility API: null on failure; an empty list means no devices. */
    public static void login(String country, String email, String password, Callback callback) {
        if (callback == null) throw new IllegalArgumentException("callback required");
        loginDetailed(country, email, password, (profiles, error) -> callback.onResult(profiles));
    }

    public static void login(Context context, String country, String email, String password, Callback callback) {
        if (callback == null) throw new IllegalArgumentException("callback required");
        loginDetailed(context, country, email, password, (profiles, error) -> callback.onResult(profiles));
    }

    /** Callback runs on main thread. Error codes never include credentials or server bodies. */
    public static void loginDetailed(String country, String email, String password, ResultCallback callback) {
        loginDetailed(ThingSmartNetWork.getAppContext(), country, email, password, callback);
    }

    public static void loginDetailed(Context context, String country, String email, String password, ResultCallback callback) {
        loginDetailed(context, country, email, password, stage -> {}, callback);
    }

    public static void loginDetailed(Context context, String country, String email, String password,
                                     ProgressCallback progress, ResultCallback callback) {
        if (callback == null) throw new IllegalArgumentException("callback required");
        Context application = context == null ? null : context.getApplicationContext();
        Handler main = new Handler(Looper.getMainLooper());
        WORKER.execute(() -> {
            List<JSONObject> profiles = null;
            String error = null;
            try {
                if (empty(country) || empty(email) || empty(password)) throw new Failure("INVALID_ARGUMENT");
                if (application == null) throw new Failure("CONTEXT_REQUIRED");
                KugooOemCrypto crypto = new KugooOemCrypto(application);
                crypto.initialize();
                NativeTransport transport = new NativeTransport(crypto);
                profiles = authenticate((api, version, payload, session) -> {
                    String stage = api.endsWith("token.get") ? "TOKEN" : api.endsWith("password.login")
                            ? "LOGIN" : api.equals("m.life.home.space.list") ? "HOMES" : "KEYS";
                    main.post(() -> progress.onProgress(stage));
                    return transport.call(api, version, payload, session);
                }, country, email, password);
            } catch (Failure failure) { error = failure.code; }
              catch (PackageManager.NameNotFoundException failure) { error = "KUGOO_APP_REQUIRED"; }
              catch (SecurityException failure) { error = "KUGOO_CERTIFICATE_UNSUPPORTED"; }
              catch (ReflectiveOperationException | LinkageError failure) { error = "KUGOO_PROTOCOL_UNAVAILABLE"; }
              catch (IOException failure) { error = "NETWORK_ERROR"; }
              catch (Exception failure) { error = "CLIENT_ERROR"; }
            final List<JSONObject> result = profiles;
            final String resultError = error;
            main.post(() -> callback.onResult(result, resultError));
        });
    }

    interface Transport {
        Object call(String api, String version, JSONObject payload, Session session) throws Exception;
    }
    static final class Session {
        final String sid, ecode;
        Session(String sid, String ecode) { this.sid = sid; this.ecode = ecode; }
    }
    static final class Failure extends IOException {
        final String code;
        Failure(String code) { super(code); this.code = code; }
    }

    static List<JSONObject> authenticate(Transport transport, String country, String email, String password) throws Exception {
        JSONObject token = object(transport.call("smartlife.m.user.username.token.get", "2.0",
                new JSONObject().put("countryCode", country).put("username", email).put("isUid", false), null));
        String encrypted = encryptPassword(password, required(token, "publicKey"), required(token, "exponent"));
        JSONObject user = object(transport.call("smartlife.m.user.email.password.login", "3.0",
                new JSONObject().put("countryCode", country).put("email", email).put("passwd", encrypted)
                        .put("options", "{\"group\":1}").put("token", required(token, "token")).put("ifencrypt", 1), null));
        Session session = new Session(required(user, "sid"), required(user, "ecode"));
        JSONArray homes = array(transport.call("m.life.home.space.list", "1.0", new JSONObject(), session));
        Map<String, JSONObject> profiles = new LinkedHashMap<>();
        for (int i = 0; i < homes.length(); i++) {
            JSONObject home = homes.getJSONObject(i);
            Object gid = home.opt("gid");
            if (gid == null || gid == JSONObject.NULL) gid = home.opt("homeId");
            if (gid == null || gid == JSONObject.NULL) throw new Failure("HOME_SCHEMA_UNSUPPORTED");
            JSONArray devices = array(transport.call("m.life.my.group.device.list", "2.2",
                    new JSONObject().put("gid", gid), session));
            for (int j = 0; j < devices.length(); j++) {
                JSONObject device = devices.getJSONObject(j);
                // Keep complete device records (schema/product metadata may be required by BLE).
                if (!empty(device.optString("devId")) && !empty(device.optString("uuid"))
                        && !empty(device.optString("productId")) && !empty(device.optString("localKey"))) {
                    profiles.put(device.getString("devId"), device);
                } else {
                    throw new Failure("DEVICE_PROFILE_INCOMPLETE");
                }
            }
        }
        if (!profiles.isEmpty()) {
            java.util.Set<String> ids = new java.util.LinkedHashSet<>();
            for (JSONObject device : profiles.values()) ids.add(device.getString("productId"));
            JSONArray products = array(transport.call("smartlife.m.device.ref.info.list", "5.4",
                    new JSONObject().put("productIds", new JSONArray(ids)).put("zigbeeGroup", true), session));
            Map<String, JSONObject> byId = new LinkedHashMap<>();
            for (int i = 0; i < products.length(); i++) {
                JSONObject product = products.getJSONObject(i);
                byId.put(required(product, "id"), product);
            }
            for (JSONObject device : profiles.values()) {
                JSONObject product = byId.get(device.getString("productId"));
                if (product == null) throw new Failure("PRODUCT_PROFILE_INCOMPLETE");
                device.put("_product", product);
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(profiles.values()));
    }

    private static final class NativeTransport implements Transport {
        private final KugooOemCrypto crypto;
        NativeTransport(KugooOemCrypto crypto) { this.crypto = crypto; }
        @Override public Object call(String api, String version, JSONObject payload, Session session) throws Exception {
            Object params = crypto.request(api, version, payload, session == null ? null : session.sid,
                    session == null ? null : session.ecode);
            Map<String, String> body = crypto.body(params);
            String signature = body.get("sign");
            if (empty(signature) || !signature.matches("[0-9a-fA-F]{64}")) throw new Failure("OEM_SIGNING_UNAVAILABLE");
            FormBody.Builder form = new FormBody.Builder();
            for (Map.Entry<String, String> item : body.entrySet()) {
                if (item.getValue() != null) form.add(item.getKey(), item.getValue());
            }
            Request request = new Request.Builder().url("https://a1-eu.iot334.com/api.json")
                    .header("User-Agent", "Thing-UA=APP/Android/1.0.2/SDK/6.5.0").post(form.build()).build();
            try (Response response = HTTP.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new Failure("HTTP_" + response.code());
                if (response.body() == null) throw new Failure("EMPTY_RESPONSE");
                String raw = response.body().string();
                JSONObject envelope = new JSONObject(raw);
                String decoded = raw;
                if (envelope.has("sign") && envelope.opt("result") instanceof String) {
                    decoded = crypto.decrypt(params, raw, ParseHelper.swapToMapList(response.headers()));
                }
                return unwrap(new JSONObject(decoded));
            }
        }
    }

    static Object unwrap(JSONObject response) throws Exception {
        if (!Boolean.TRUE.equals(response.opt("success"))) {
            String code = response.optString("errorCode", "BACKEND_ERROR");
            throw new Failure(code.matches("[A-Z0-9_]{1,80}") ? code : "BACKEND_ERROR");
        }
        Object result = response.opt("result");
        if (result == null || result == JSONObject.NULL) throw new Failure("EMPTY_RESULT");
        return result;
    }
    private static JSONObject object(Object value) throws Failure {
        if (!(value instanceof JSONObject)) throw new Failure("RESPONSE_SCHEMA_UNSUPPORTED");
        return (JSONObject) value;
    }
    private static JSONArray array(Object value) throws Failure {
        if (!(value instanceof JSONArray)) throw new Failure("RESPONSE_SCHEMA_UNSUPPORTED");
        return (JSONArray) value;
    }
    private static String required(JSONObject value, String key) throws Failure {
        Object field = value.opt(key);
        if (!(field instanceof String) || empty((String) field)) throw new Failure("RESPONSE_FIELD_MISSING");
        return (String) field;
    }
    static String encryptPassword(String password, String modulus, String exponent) throws Exception {
        byte[] digest = MessageDigest.getInstance("MD5").digest(password.getBytes(StandardCharsets.UTF_8));
        PublicKey key = KeyFactory.getInstance("RSA").generatePublic(
                new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(exponent)));
        Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsa.init(Cipher.ENCRYPT_MODE, key);
        return hex(rsa.doFinal(hex(digest).getBytes(StandardCharsets.UTF_8)));
    }
    private static String hex(byte[] value) {
        StringBuilder out = new StringBuilder(value.length * 2);
        for (byte b : value) out.append(String.format(Locale.ROOT, "%02x", b & 255));
        return out.toString();
    }
    private static boolean empty(String value) { return value == null || value.trim().isEmpty(); }
}
