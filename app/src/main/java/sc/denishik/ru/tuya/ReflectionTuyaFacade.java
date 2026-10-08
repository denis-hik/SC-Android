package sc.denishik.ru.tuya;

import android.app.Application;

import java.lang.reflect.Method;

import sc.denishik.ru.BuildConfig;

public class ReflectionTuyaFacade implements TuyaFacade {
    private static final String[] HOME_SDK_CANDIDATES = new String[]{
            "com.tuya.smart.home.sdk.TuyaHomeSdk",
            "com.thingclips.smart.home.sdk.ThingHomeSdk",
    };

    @Override
    public boolean isAvailable() {
        try {
            loadHomeSdk();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public void init(Application application) throws Exception {
        // Prefer TuyaHomeSdk.init(application, appKey, appSecret) if provided.
        Class<?> sdk = loadHomeSdk();

        String appKey = BuildConfig.TUYA_APP_KEY;
        String appSecret = BuildConfig.TUYA_APP_SECRET;
        boolean hasKeys = appKey != null && !appKey.trim().isEmpty()
                && appSecret != null && !appSecret.trim().isEmpty();

        if (!hasKeys) {
            throw new IllegalStateException("Tuya keys are empty. Set tuya.appKey/tuya.appSecret in local.properties and rebuild.");
        }

        try {
            Method init = sdk.getMethod("init", Application.class, String.class, String.class);
            init.invoke(null, application, appKey.trim(), appSecret.trim());
        } catch (Throwable t) {
            Throwable cause = t.getCause();
            if (cause != null) {
                throw new RuntimeException("TuyaHomeSdk.init failed: " + cause, cause);
            }
            throw new RuntimeException("TuyaHomeSdk.init failed: " + t, t);
        }

        try {
            Method setDebug = sdk.getMethod("setDebugMode", boolean.class);
            setDebug.invoke(null, true);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public boolean isLoggedIn() throws Exception {
        // TuyaHomeSdk.getUserInstance().isLogin()
        Object user = getUserInstance();
        Method isLogin = user.getClass().getMethod("isLogin");
        Object res = isLogin.invoke(user);
        return res instanceof Boolean ? (Boolean) res : false;
    }

    @Override
    public void loginWithEmail(String countryCode, String email, String password) throws Exception {
        // TuyaHomeSdk.getUserInstance().loginWithEmail(countryCode, email, password, ILoginCallback)
        // Use reflection proxy for callback.
        Object user = getUserInstance();
        Class<?> iLoginCallback = loadFirstClass(
                "com.tuya.smart.android.user.api.ILoginCallback",
                "com.tuya.smart.sdk.api.ILoginCallback",
                "com.thingclips.smart.android.user.api.ILoginCallback",
                "com.thingclips.smart.sdk.api.ILoginCallback"
        );

        Object callback = java.lang.reflect.Proxy.newProxyInstance(
                iLoginCallback.getClassLoader(),
                new Class<?>[]{iLoginCallback},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("onSuccess".equals(name)) {
                        TuyaLoginActivity.postActionResult("login", true, null);
                    } else if ("onError".equals(name) && args != null && args.length >= 2) {
                        TuyaLoginActivity.postActionResult("login", false, String.valueOf(args[1]));
                    }
                    return null;
                });

        Method login = user.getClass().getMethod("loginWithEmail", String.class, String.class, String.class, iLoginCallback);
        login.invoke(user, countryCode, email, password, callback);
    }

    @Override
    public void sendRegisterCode(String countryCode, String email) throws Exception {
        Object user = getUserInstance();
        String cc = countryCode == null ? "" : countryCode.trim();
        if (cc.startsWith("+")) cc = cc.substring(1);
        String ccPlus = cc.isEmpty() ? "" : ("+" + cc);
        String e = email == null ? "" : email.trim();

        Class<?> iResultCallback = loadFirstClass(
                "com.tuya.smart.android.user.api.IResultCallback",
                "com.tuya.smart.sdk.api.IResultCallback",
                "com.thingclips.smart.android.user.api.IResultCallback",
                "com.thingclips.smart.sdk.api.IResultCallback"
        );

        Object callback = java.lang.reflect.Proxy.newProxyInstance(
                iResultCallback.getClassLoader(),
                new Class<?>[]{iResultCallback},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("onSuccess".equals(name)) {
                        TuyaLoginActivity.postActionResult("send_code", true, null);
                    } else if ("onError".equals(name) && args != null && args.length >= 2) {
                        TuyaLoginActivity.postActionResult("send_code", false, String.valueOf(args[1]));
                    }
                    return null;
                });

        // Prefer explicit *Email* code APIs first (some tenants reject username+countryCode flow for email).
        invokeFirstMatching(user,
                new String[]{
                        "getRegisterEmailCode",
                        "getEmailValidateCode",
                        "getEmailCode",
                        "getValidateCode",
                        "sendEmailVerifyCode",
                        "sendEmailVerifyCodeWithUserName"
                },
                new Object[][]{
                        new Object[]{e, callback},
                        new Object[]{e, 1, callback},               // 1 = register
                        new Object[]{e, "1", callback},
                        new Object[]{e, cc, callback},
                        new Object[]{e, ccPlus, callback},
                        new Object[]{cc, e, callback},
                        new Object[]{ccPlus, e, callback},
                        new Object[]{cc, e, 1, callback},
                        new Object[]{ccPlus, e, 1, callback},
                        new Object[]{e, cc, 1, callback},
                        new Object[]{e, ccPlus, 1, callback},
                });

        // Fallback: username-based code APIs (may route to `smartlife.m.user.username.code.get`).
        invokeFirstMatching(user,
                new String[]{"getRegisterCodeWithUserName", "sendVerifyCodeWithUserName", "sendVerifyCodeWithUserNameAndRegion"},
                new Object[][]{
                        new Object[]{cc, e, callback},
                        new Object[]{ccPlus, e, callback},
                        new Object[]{e, cc, callback},
                        new Object[]{e, ccPlus, callback},
                        new Object[]{e, callback},
                        new Object[]{cc, e, 1, callback},
                        new Object[]{ccPlus, e, 1, callback},
                        new Object[]{e, cc, 1, callback},
                        new Object[]{e, ccPlus, 1, callback},
                        new Object[]{cc, e, "", 1, callback},
                        new Object[]{ccPlus, e, "", 1, callback},
                        new Object[]{e, cc, "", 1, callback},
                        new Object[]{e, ccPlus, "", 1, callback},
                        new Object[]{cc, e, "", callback},
                        new Object[]{ccPlus, e, "", callback},
                        new Object[]{e, cc, "", callback},
                        new Object[]{e, ccPlus, "", callback},
                });
    }

    @Override
    public void registerWithEmail(String countryCode, String email, String password, String code) throws Exception {
        Object user = getUserInstance();
        String cc = countryCode == null ? "" : countryCode.trim();
        if (cc.startsWith("+")) cc = cc.substring(1);
        String ccPlus = cc.isEmpty() ? "" : ("+" + cc);

        Class<?> iRegisterCallback = loadFirstClass(
                "com.tuya.smart.android.user.api.IRegisterCallback",
                "com.tuya.smart.sdk.api.IRegisterCallback",
                "com.thingclips.smart.android.user.api.IRegisterCallback",
                "com.thingclips.smart.sdk.api.IRegisterCallback"
        );

        Object callback = java.lang.reflect.Proxy.newProxyInstance(
                iRegisterCallback.getClassLoader(),
                new Class<?>[]{iRegisterCallback},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("onSuccess".equals(name)) {
                        TuyaLoginActivity.postActionResult("register", true, null);
                    } else if ("onError".equals(name) && args != null && args.length >= 2) {
                        TuyaLoginActivity.postActionResult("register", false, String.valueOf(args[1]));
                    }
                    return null;
                });

        invokeFirstMatching(user,
                new String[]{"registerAccountWithEmail", "registerAccountWithEmailAndCode", "registerAccountWithUserName"},
                new Object[][]{
                        // Common orders
                        new Object[]{cc, email, password, code, callback},
                        new Object[]{ccPlus, email, password, code, callback},
                        new Object[]{email, password, code, cc, callback},
                        new Object[]{email, password, code, ccPlus, callback},
                        // Some flows don't require code (rare)
                        new Object[]{cc, email, password, callback},
                        new Object[]{ccPlus, email, password, callback},
                        new Object[]{email, password, cc, callback},
                        new Object[]{email, password, ccPlus, callback},
                        // Extra param variants (region)
                        new Object[]{cc, email, password, code, "", callback},
                        new Object[]{ccPlus, email, password, code, "", callback},
                        new Object[]{email, password, code, cc, "", callback},
                        new Object[]{email, password, code, ccPlus, "", callback},
                });
    }

    @Override
    public void logout() throws Exception {
        // TuyaHomeSdk.getUserInstance().logout(ILogoutCallback)
        Object user = getUserInstance();
        Class<?> iLogoutCallback = loadFirstClass(
                "com.tuya.smart.android.user.api.ILogoutCallback",
                "com.tuya.smart.sdk.api.ILogoutCallback",
                "com.thingclips.smart.android.user.api.ILogoutCallback",
                "com.thingclips.smart.sdk.api.ILogoutCallback"
        );
        Object callback = java.lang.reflect.Proxy.newProxyInstance(
                iLogoutCallback.getClassLoader(),
                new Class<?>[]{iLogoutCallback},
                (proxy, method, args) -> null);
        Method logout = user.getClass().getMethod("logout", iLogoutCallback);
        logout.invoke(user, callback);
    }

    private static Object getUserInstance() throws Exception {
        Class<?> sdk = loadHomeSdk();
        Method getUser = sdk.getMethod("getUserInstance");
        return getUser.invoke(null);
    }

    private static Class<?> loadHomeSdk() throws ClassNotFoundException {
        return loadFirstClass(HOME_SDK_CANDIDATES);
    }

    private static void invokeFirstMatching(Object target, String[] methodNames, Object[][] candidateArgs) throws Exception {
        Throwable last = null;
        Class<?> cls = target.getClass();
        Method[] methods = cls.getMethods();
        for (String name : methodNames) {
            for (Object[] args : candidateArgs) {
                for (Method m : methods) {
                    if (!m.getName().equals(name)) continue;
                    Class<?>[] p = m.getParameterTypes();
                    if (p.length != args.length) continue;
                    boolean ok = true;
                    for (int i = 0; i < p.length; i++) {
                        Object a = args[i];
                        if (a == null) continue;
                        if (!wrap(p[i]).isAssignableFrom(a.getClass())) {
                            ok = false;
                            break;
                        }
                    }
                    if (!ok) continue;
                    try {
                        m.invoke(target, args);
                        return;
                    } catch (Throwable t) {
                        last = t.getCause() != null ? t.getCause() : t;
                    }
                }
            }
        }
        if (last instanceof Exception) throw (Exception) last;
        throw new RuntimeException(last != null ? last : new NoSuchMethodException("No matching method found"));
    }

    private static Class<?> wrap(Class<?> c) {
        if (!c.isPrimitive()) return c;
        if (c == boolean.class) return Boolean.class;
        if (c == int.class) return Integer.class;
        if (c == long.class) return Long.class;
        if (c == float.class) return Float.class;
        if (c == double.class) return Double.class;
        if (c == byte.class) return Byte.class;
        if (c == short.class) return Short.class;
        if (c == char.class) return Character.class;
        return c;
    }

    private static Class<?> loadFirstClass(String... fqns) throws ClassNotFoundException {
        ClassNotFoundException last = null;
        for (String fqn : fqns) {
            try {
                return Class.forName(fqn);
            } catch (ClassNotFoundException e) {
                last = e;
            }
        }
        throw last != null ? last : new ClassNotFoundException("No class candidates provided");
    }
}
