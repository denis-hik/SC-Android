package sc.denishik.ru.midwayApi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Config {
    public static String PIN_CODE = "888888";
    public static String NAME_SCOOTER_BLU = "HW";

    @Nullable
    private static byte currentHeadMonitor;
    private static byte customHeadEsc;
    private static boolean isAdmin;
    private static boolean isCustomHead;
    private static boolean isLogEnabled;
    @NotNull
    public static final Config INSTANCE = new Config();
    @NotNull
    private static byte[] customHeadMonitors = {0};
    @NotNull
    private static String pwd = "";

    private Config() {
    }

    public final byte getCurrentHeadMonitor() {
        return currentHeadMonitor;
    }

    public final byte getCustomHeadEsc() {
        return customHeadEsc;
    }

    @NotNull
    public final byte[] getCustomHeadMonitors() {
        return customHeadMonitors;
    }

    @NotNull
    public final String getPwd() {
        return pwd;
    }

    public final boolean isAdmin() {
        return isAdmin;
    }

    public final boolean isCustomHead() {
        return isCustomHead;
    }

    public final boolean isLogEnabled() {
        return isLogEnabled;
    }

    public final void setAdmin(boolean z2) {
        isAdmin = z2;
    }

    public final void setCurrentHeadMonitor(byte b2) {
        currentHeadMonitor = b2;
    }

    public final void setCustomHead(boolean z2) {
        isCustomHead = z2;
    }

    public final void setCustomHeadEsc(byte b2) {
        customHeadEsc = b2;
    }

    public final void setCustomHeadMonitors(@NotNull byte[] bArr) {
        customHeadMonitors = bArr;
    }

    public final void setLogEnabled(boolean z2) {
        isLogEnabled = z2;
    }

    public final void setPwd(@NotNull String str) {
        pwd = str;
    }

}
