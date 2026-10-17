package sc.denishik.ru.tuya;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

/** Local reminder policy for the bundled security-algorithm-1.0.0-beta SDK. */
public final class SdkNoticePreferences implements Application.ActivityLifecycleCallbacks {
    private final SharedPreferences preferences;

    private SdkNoticePreferences(Application application) {
        preferences = application.getSharedPreferences("prompt", Context.MODE_PRIVATE);
    }

    public static void install(Application application) {
        SdkNoticePreferences policy = new SdkNoticePreferences(application);
        policy.refreshReminderTime();
        application.registerActivityLifecycleCallbacks(policy);
    }

    private void refreshReminderTime() {
        // The bundled SDK checks this timestamp (hours since epoch) from an idle
        // callback after Activity creation, showing its trial notice after 48h.
        // Keep only this local reminder deferred. No license, account, crypto,
        // server response or other dialog is modified. Recheck on SDK upgrades.
        long hour = System.currentTimeMillis() / 3_600_000L;
        if (preferences.getLong("thing_security_prompt_key", -1L) != hour) {
            preferences.edit().putLong("thing_security_prompt_key", hour).apply();
        }
    }

    @Override public void onActivityCreated(Activity activity, Bundle state) {
        refreshReminderTime();
    }

    @Override public void onActivityStarted(Activity activity) { }
    @Override public void onActivityResumed(Activity activity) { }
    @Override public void onActivityPaused(Activity activity) { }
    @Override public void onActivityStopped(Activity activity) { }
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) { }
    @Override public void onActivityDestroyed(Activity activity) { }
}
