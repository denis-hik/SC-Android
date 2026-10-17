package sc.denishik.ru.kuggoApi;

import android.content.Context;
import android.content.pm.PackageInfo;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

/** Uses the crypto implementation from the installed, verified Kugoo APK.
 * Requires that APK; it does not contain a copy of its secrets or private signing key.
 * Uses an isolated APK class loader; never changes SC's SDK account.
 */
public final class KugooOemCrypto {
    private static final String PACKAGE = "com.kugoo.cn";
    private static final String CERTIFICATE =
            "ee4b49cc3fdd2abb553cf2d018c7d8ea3b2f6d3dfbe1203f7c4a9373f5419214";
    private static final Object LOCK = new Object();
    private final Context context;
    private final Method command;
    private final Class<?> network;
    private final ClassLoader loader;
    private Class<?> paramsType;

    public KugooOemCrypto(Context applicationContext) throws Exception {
        PackageInfo info = applicationContext.getPackageManager()
                .getPackageInfo(PACKAGE, android.content.pm.PackageManager.GET_SIGNATURES);
        if (info.signatures == null || info.signatures.length != 1
                || !CERTIFICATE.equals(hex(MessageDigest.getInstance("SHA-256")
                .digest(info.signatures[0].toByteArray())))) {
            throw new SecurityException("Unsupported Kugoo APK certificate");
        }
        Context packageContext = applicationContext.createPackageContext(PACKAGE,
                Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        context = new android.content.ContextWrapper(packageContext) {
            @Override public Context getApplicationContext() { return this; }
            @Override public android.content.SharedPreferences getSharedPreferences(String name, int mode) {
                return applicationContext.getSharedPreferences("kugoo_protocol_" + name, Context.MODE_PRIVATE);
            }
        };
        loader = context.getClassLoader();
        network = loader.loadClass("com.thingclips.smart.android.network.ThingSmartNetWork");
        Class<?> security = loader.loadClass("com.thingclips.sdk.network.ThingNetworkSecurity");
        command = security.getMethod("doCommandNative", Context.class, int.class,
                byte[].class, byte[].class, boolean.class);
    }

    /** Initialize only the network protocol, without starting the OEM application. */
    void initialize() throws Exception {
        synchronized (LOCK) {
            Class<?> config = loader.loadClass("com.thingclips.sample.BuildConfig");
            network.getField("mAppContext").set(null, context);
            network.getField("mAppId").set(null, config.getField("THING_SMART_APPKEY").get(null));
            network.getField("mAppSecret").set(null, config.getField("THING_SMART_SECRET").get(null));
            network.getField("mAppVersion").set(null, config.getField("VERSION_NAME").get(null));
            network.getField("mSdkVersion").set(null, "6.5.0");
            network.getField("mDeviceCoreVersion").set(null, "6.5.0");
            java.lang.reflect.Field ttid = network.getDeclaredField("mTtid");
            ttid.setAccessible(true);
            ttid.set(null, config.getField("THING_SMART_TTID").get(null));
            java.lang.reflect.Field channel = network.getDeclaredField("mChannel");
            channel.setAccessible(true);
            channel.set(null, "oem");
            Class<?> provider = network.getField("mApiUrlProvider").getType();
            network.getField("mApiUrlProvider").set(null, java.lang.reflect.Proxy.newProxyInstance(
                    loader, new Class<?>[]{provider}, (proxy, method, args) -> {
                        if (method.getName().equals("getApiUrl")) return "https://a1-eu.iot334.com/api.json";
                        throw new UnsupportedOperationException("Unsupported URL provider operation");
                    }));
            loader.loadClass("com.thingclips.sdk.network.ThingNetworkSecurity")
                    .getMethod("initJNI", Context.class).invoke(null, context);
            paramsType = loader.loadClass("com.thingclips.smart.android.network.ThingApiParams");
        }
    }

    Object request(String api, String version, org.json.JSONObject payload,
                   String sid, String ecode) throws Exception {
        synchronized (LOCK) {
            Object params = paramsType.getConstructor(String.class, String.class).newInstance(api, version);
            paramsType.getMethod("setSessionRequire", boolean.class).invoke(params, sid != null);
            paramsType.getMethod("setSession", String.class).invoke(params, sid);
            paramsType.getMethod("setEcode", String.class).invoke(params, ecode);
            java.util.Iterator<String> keys = payload.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = payload.get(key);
                if (value instanceof org.json.JSONArray || value instanceof org.json.JSONObject)
                    value = loader.loadClass("com.alibaba.fastjson.JSON").getMethod("parse", String.class)
                            .invoke(null, value.toString());
                paramsType.getMethod("putPostData", String.class, Object.class).invoke(params, key, value);
            }
            return params;
        }
    }

    @SuppressWarnings("unchecked")
    java.util.Map<String, String> body(Object params) throws Exception {
        synchronized (LOCK) {
            return (java.util.Map<String, String>) paramsType.getMethod("getRequestBody").invoke(params);
        }
    }

    String decrypt(Object params, String raw, java.util.List<java.util.Map.Entry<String, String>> headers) throws Exception {
        synchronized (LOCK) {
            Class<?> business = loader.loadClass("com.thingclips.smart.android.network.Business");
            return (String) business.getMethod("decryptResponse", paramsType, String.class, java.util.List.class)
                    .invoke(business.getConstructor().newInstance(), params, raw, headers);
        }
    }

    /** Input must already be canonicalized using the official signing rules.
     * Returns the signature; never logs input, credentials or result.
     * All instances share native state, so configuration and signing are serialized.
     */
    public String sign(String appKey, String appSecret, String canonicalRequest) throws Exception {
        if (appKey == null || appKey.isEmpty() || appSecret == null || appSecret.isEmpty()
                || canonicalRequest == null) throw new IllegalArgumentException("Missing signing input");
        synchronized (LOCK) {
            network.getField("mAppId").set(null, appKey);
            network.getField("mAppSecret").set(null, appSecret);
            byte[] secretBytes = appSecret.getBytes(StandardCharsets.UTF_8);
            byte[] keyBytes = appKey.getBytes(StandardCharsets.UTF_8);
            byte[] inputBytes = canonicalRequest.getBytes(StandardCharsets.UTF_8);
            try {
                command.invoke(null, context, 0, secretBytes, keyBytes, false);
                Object result = command.invoke(null, context, 1, inputBytes, null, false);
                if (!(result instanceof String) || !((String) result).matches("[0-9a-fA-F]{64}")) {
                    throw new IllegalStateException("OEM signing unavailable");
                }
                return (String) result;
            } finally {
                java.util.Arrays.fill(secretBytes, (byte) 0);
                java.util.Arrays.fill(keyBytes, (byte) 0);
                java.util.Arrays.fill(inputBytes, (byte) 0);
            }
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder out = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) out.append(String.format(Locale.ROOT, "%02x", value & 255));
        return out.toString();
    }
}
