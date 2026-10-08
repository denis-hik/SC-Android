package sc.denishik.ru;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.thingclips.smart.android.base.ThingSmartSdk;
import com.thingclips.smart.android.network.ThingSmartNetWork;
import com.thingclips.smart.android.network.request.ThingSmartNetWorkConfig;
import com.thingclips.smart.android.user.api.ILoginCallback;
import com.thingclips.smart.android.user.bean.User;
import com.thingclips.smart.home.sdk.ThingHomeSdk;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScApplication extends Application {
    private static final String TAG = "ScApplication";
    private static volatile boolean inited = false;
    private static final String PREFS = "sc_app_prefs";
    private static final String KEY_AUTO_LOGIN_DONE = "tuya_auto_login_done";

    @Override
    public void onCreate() {
        super.onCreate();
        com.yandex.mapkit.MapKitFactory.setApiKey("d9c967a8-e55c-47ff-8d95-19924290e00b");
        initThingclipsOnce();
    }

    private void initThingclipsOnce() {
        if (inited) return;
        synchronized (ScApplication.class) {
            if (inited) return;

            String appKey = safe(BuildConfig.THING_SMART_APPKEY);
            String appSecret = safe(BuildConfig.THING_SMART_SECRET);
            if (appKey.isEmpty() || appSecret.isEmpty()) {
                Log.e(TAG, "Thingclips keys are empty. Set tuya.appKey/tuya.appSecret in local.properties and rebuild.");
            }

            try {
                ThingSmartNetWork.mSdk = true;
                ThingSmartNetWork.mSecurity = true;
                ThingSmartNetWork.setNeutralDomainSwitch(true);
//                try {
//                    ExecutorService business = Executors.newFixedThreadPool(4);
//                    ExecutorService network = Executors.newCachedThreadPool();
//                    ThingSmartNetWork.setThingSmartNetWorkConfig(
//                            new ThingSmartNetWorkConfig.Builder()
//                                    .businessExecutor(business)
//                                    .netWorkExecutor(network)
//                                    .build()
//                    );
//                } catch (Throwable t) {
//                    Log.w(TAG, "ThingSmartNetWorkConfig setup failed", t);
//                }

//                ThingSmartSdk.init(this);
                ThingSmartSdk.setLocationSwitch(false);
//                tryInitThingSmartSdkWithKeys(appKey, appSecret);

//                if (!appKey.isEmpty() && !appSecret.isEmpty()) {
//                    ThingHomeSdk.init(this, appKey, appSecret);
//                    ThingHomeSdk.setDebugMode(BuildConfig.DEBUG);
//                    tryInitPreferencesUtil();
//                    tryInitThingNetworkContext();
//                    tryInitOemAppInitializer(appKey, appSecret);
//                }
            } catch (Throwable t) {
                Log.e(TAG, "Thingclips init failed", t);
            }

            inited = true;
        }

        // Auto-login after SDK is initialized.
        tryAutoLogin();
    }

    /**
     * Official OEM apps (like Kugoo Riding) usually call AppInitializer to complete API parameter setup.
     * In our project we invoke it reflectively to avoid hard dependency on optional modules.
     */
    private void tryInitOemAppInitializer(String appKey, String appSecret) {
        try {
            Class<?> appInit = Class.forName("com.thingclips.smart.initializer.AppInitializer");
            Object apiConfig;
            try {
                // ApiConfig apiConfig = AppInitializer.c(false, context)
                apiConfig = appInit.getMethod("c", boolean.class, android.content.Context.class).invoke(null, false, this);
            } catch (Throwable t) {
                Log.w(TAG, "AppInitializer.c(...) failed", t);
                apiConfig = null;
            }

            // AppInitializer.d(context, appKey, secret, apiConfig, appName, channel, false)
            String appName;
            try {
                int resId = getResources().getIdentifier("app_name", "string", getPackageName());
                appName = resId != 0 ? getString(resId) : getPackageName();
            } catch (Throwable t) {
                appName = getPackageName();
            }
            String channel = "oem";
            java.lang.reflect.Method best = null;
            for (java.lang.reflect.Method m : appInit.getMethods()) {
                if (!"d".equals(m.getName())) continue;
                Class<?>[] p = m.getParameterTypes();
                if (p.length != 7) continue;
                if (!android.content.Context.class.isAssignableFrom(p[0])) continue;
                if (!String.class.equals(p[1]) || !String.class.equals(p[2])) continue;
                if (!String.class.equals(p[4]) || !String.class.equals(p[5])) continue;
                if (!(boolean.class.equals(p[6]) || Boolean.class.equals(p[6]))) continue;
                // p[3] is ApiConfig (exact class depends on SDK); accept anything assignable from apiConfig.
                if (apiConfig != null && !p[3].isAssignableFrom(apiConfig.getClass())) continue;
                best = m;
                break;
            }

            if (best == null) {
                Log.w(TAG, "AppInitializer.d(...) not found (no compatible signature)");
                return;
            }
            best.invoke(null, this, appKey, appSecret, apiConfig, appName, channel, false);
            Log.i(TAG, "AppInitializer.d(...) ok");
        } catch (ClassNotFoundException e) {
            // Optional module not present; ignore.
        } catch (Throwable t) {
            Log.w(TAG, "OEM AppInitializer reflective init failed", t);
        }
    }

    private void tryInitPreferencesUtil() {
        try {
            Class<?> prefs = Class.forName("com.thingclips.smart.android.base.utils.PreferencesUtil");
            // Some versions: PreferencesUtil.init(Context)
            try {
                prefs.getMethod("init", android.content.Context.class).invoke(null, this);
                Log.i(TAG, "PreferencesUtil.init(...) ok");
            } catch (NoSuchMethodException ignored) {
                // ignore
            }
        } catch (Throwable ignored) {
        }
    }

    private void tryInitThingSmartSdkWithKeys(String appKey, String appSecret) {
        if (appKey == null || appSecret == null) return;
        try {
            Class<?> sdk = Class.forName("com.thingclips.smart.android.base.ThingSmartSdk");
            for (java.lang.reflect.Method m : sdk.getMethods()) {
                if (!"init".equals(m.getName())) continue;
                Class<?>[] p = m.getParameterTypes();
                if (p.length == 3
                        && android.content.Context.class.isAssignableFrom(p[0])
                        && String.class.equals(p[1])
                        && String.class.equals(p[2])) {
                    m.invoke(null, this, appKey, appSecret);
                    Log.i(TAG, "ThingSmartSdk.init(context, key, secret) ok");
                    return;
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private void tryInitThingNetworkContext() {
        // Some SDKs require setting app context into network module explicitly.
        try {
            Class<?> nw = Class.forName("com.thingclips.smart.android.network.ThingSmartNetWork");
            for (java.lang.reflect.Method m : nw.getMethods()) {
                if (!"init".equals(m.getName())) continue;
                Class<?>[] p = m.getParameterTypes();
                if (p.length == 1 && android.content.Context.class.isAssignableFrom(p[0])) {
                    m.invoke(null, this);
                    Log.i(TAG, "ThingSmartNetWork.init(...) ok");
                    return;
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private void tryAutoLogin() {
        String email = safe(BuildConfig.TUYA_APP_Login);
        String password = safe(BuildConfig.TUYA_APP_Password);
        if (email.isEmpty() || password.isEmpty()) {
            Log.i(TAG, "Auto-login skipped: TUYA_APP_Login/TUYA_APP_Password empty");
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (prefs.getBoolean(KEY_AUTO_LOGIN_DONE, false)) {
            return;
        }

        boolean isLoggedIn = false;
        try {
            isLoggedIn = ThingHomeSdk.getUserInstance().isLogin();
        } catch (Throwable t) {
            Log.w(TAG, "Auto-login: cannot read isLogin()", t);
        }
        if (isLoggedIn) {
            prefs.edit().putBoolean(KEY_AUTO_LOGIN_DONE, true).apply();
            return;
        }

        // Default cc used elsewhere in the project.
        final String countryCode = "7";
        Log.i(TAG, "Auto-login: starting loginWithEmail(cc=" + countryCode + ", email=" + email + ")");
        try {
            ThingHomeSdk.getUserInstance().loginWithEmail(countryCode, email, password, new ILoginCallback() {
                @Override
                public void onSuccess(User user) {
                    Log.i(TAG, "Auto-login: success");
                    prefs.edit().putBoolean(KEY_AUTO_LOGIN_DONE, true).apply();
                }

                @Override
                public void onError(String code, String error) {
                    Log.w(TAG, "Auto-login: error " + code + ": " + error);
                    // Mark as done to avoid infinite retry loop on every app start.
                    prefs.edit().putBoolean(KEY_AUTO_LOGIN_DONE, true).apply();
                }
            });
        } catch (Throwable t) {
            Log.e(TAG, "Auto-login: call failed", t);
            prefs.edit().putBoolean(KEY_AUTO_LOGIN_DONE, true).apply();
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
