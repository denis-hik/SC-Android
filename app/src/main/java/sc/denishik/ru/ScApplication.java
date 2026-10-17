package sc.denishik.ru;

import android.app.Application;
import android.util.Log;
import com.thingclips.smart.android.base.ThingSmartSdk;
import com.thingclips.smart.android.network.ThingSmartNetWork;

public class ScApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        sc.denishik.ru.tuya.SdkNoticePreferences.install(this);
        com.yandex.mapkit.MapKitFactory.setApiKey("d9c967a8-e55c-47ff-8d95-19924290e00b");
        // Configure privacy/network flags only. KugooConnection initializes the SDK
        // with this Application before using device APIs. Account access here can
        // poison the SDK user singleton before its context has been initialized.
        // Explicit account login is handled by kuggoApi.Login, not boot auto-login.
        try {
            ThingSmartNetWork.mSdk = true;
            ThingSmartNetWork.mSecurity = true;
            ThingSmartNetWork.setNeutralDomainSwitch(true);
            ThingSmartSdk.setLocationSwitch(false);
        } catch (RuntimeException | LinkageError error) {
            Log.e("ScApplication", "Thingclips configuration failed", error);
        }
    }
}
