package sc.denishik.ru.midwayApi.base;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashMap;

import kotlin.jvm.internal.Intrinsics;

public final class BaseParams implements Serializable, Cloneable {
    private boolean atmosphereLightSw;
    private boolean bootMode;
    private boolean cruiseControlSw;
    @Nullable
    private String displayName;
    @Nullable
    private String displayVersion;
    private int gearPosition;
    private boolean headLightSw;
    private int lastService;
    private int limitCruise;
    private int limitMode1;
    private int limitMode2;
    private int limitMode3;
    private boolean lockSw;
    private boolean metricInchSw;
    private float nextService;
    private int serviceMileage;
    private int test10;
    private int test11;
    private int test12;
    private int test13;
    private int test9;
    private float speed;
    private float power;
    @NotNull
    private String testStr;

    public BaseParams() {
        this(0, false, false, false, false, false, false, 0, 0, 0, 0, 0.0f, 0, 0, null, null, 0, 0, 0, 0, 0, null, 4194303);
    }

    public BaseParams(int i2, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7, int i3, int i4, int i5, int i6, float f2, int i7, int i8, @Nullable String str, @Nullable String str2, int i9, int i10, int i11, int i12, int i13, @NotNull String testStr) {
        Intrinsics.checkNotNullParameter(testStr, "testStr");
        this.gearPosition = i2;
        this.headLightSw = z2;
        this.atmosphereLightSw = z3;
        this.cruiseControlSw = z4;
        this.bootMode = z5;
        this.metricInchSw = z6;
        this.lockSw = z7;
        this.limitCruise = i3;
        this.limitMode1 = i4;
        this.limitMode2 = i5;
        this.limitMode3 = i6;
        this.nextService = f2;
        this.lastService = i7;
        this.serviceMileage = i8;
        this.displayName = str;
        this.displayVersion = str2;
        this.test9 = i9;
        this.test10 = i10;
        this.test11 = i11;
        this.test12 = i12;
        this.test13 = i13;
        this.testStr = testStr;
        this.speed = 0;
        this.power = 0;
    }

    public final int component1() {
        return this.gearPosition;
    }

    public final int component10() {
        return this.limitMode2;
    }

    public final int component11() {
        return this.limitMode3;
    }

    public final float component12() {
        return this.nextService;
    }

    public final int component13() {
        return this.lastService;
    }

    public final int component14() {
        return this.serviceMileage;
    }

    @Nullable
    public final String component15() {
        return this.displayName;
    }

    @Nullable
    public final String component16() {
        return this.displayVersion;
    }

    public final int component17() {
        return this.test9;
    }

    public final int component18() {
        return this.test10;
    }

    public final int component19() {
        return this.test11;
    }

    public final boolean component2() {
        return this.headLightSw;
    }

    public final int component20() {
        return this.test12;
    }

    public final int component21() {
        return this.test13;
    }

    @NotNull
    public final String component22() {
        return this.testStr;
    }

    public final boolean component3() {
        return this.atmosphereLightSw;
    }

    public final boolean component4() {
        return this.cruiseControlSw;
    }

    public final boolean component5() {
        return this.bootMode;
    }

    public final boolean component6() {
        return this.metricInchSw;
    }

    public final boolean component7() {
        return this.lockSw;
    }

    public final int component8() {
        return this.limitCruise;
    }

    public final int component9() {
        return this.limitMode1;
    }
    public final float getSpeed() {
        return this.speed;
    }

    @NotNull
    public final BaseParams copy(int i2, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7, int i3, int i4, int i5, int i6, float f2, int i7, int i8, @Nullable String str, @Nullable String str2, int i9, int i10, int i11, int i12, int i13, @NotNull String testStr) {
        Intrinsics.checkNotNullParameter(testStr, "testStr");
        return new BaseParams(i2, z2, z3, z4, z5, z6, z7, i3, i4, i5, i6, f2, i7, i8, str, str2, i9, i10, i11, i12, i13, testStr);
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BaseParams) {
            BaseParams baseParams = (BaseParams) obj;
            return this.gearPosition == baseParams.gearPosition && this.headLightSw == baseParams.headLightSw && this.atmosphereLightSw == baseParams.atmosphereLightSw && this.cruiseControlSw == baseParams.cruiseControlSw && this.bootMode == baseParams.bootMode && this.metricInchSw == baseParams.metricInchSw && this.lockSw == baseParams.lockSw && this.limitCruise == baseParams.limitCruise && this.limitMode1 == baseParams.limitMode1 && this.limitMode2 == baseParams.limitMode2 && this.limitMode3 == baseParams.limitMode3 && Intrinsics.areEqual((Object) Float.valueOf(this.nextService), (Object) Float.valueOf(baseParams.nextService)) && this.lastService == baseParams.lastService && this.serviceMileage == baseParams.serviceMileage && Intrinsics.areEqual(this.displayName, baseParams.displayName) && Intrinsics.areEqual(this.displayVersion, baseParams.displayVersion) && this.test9 == baseParams.test9 && this.test10 == baseParams.test10 && this.test11 == baseParams.test11 && this.test12 == baseParams.test12 && this.test13 == baseParams.test13 && Intrinsics.areEqual(this.testStr, baseParams.testStr);
        }
        return false;
    }

    public final boolean getAtmosphereLightSw() {
        return this.atmosphereLightSw;
    }

    public final boolean getBootMode() {
        return this.bootMode;
    }

    public final boolean getCruiseControlSw() {
        return this.cruiseControlSw;
    }

    @Nullable
    public final String getDisplayName() {
        return this.displayName;
    }

    @Nullable
    public final String getDisplayVersion() {
        return this.displayVersion;
    }

    public final int getGearPosition() {
        return this.gearPosition;
    }

    public final boolean getHeadLightSw() {
        return this.headLightSw;
    }

    public final int getLastService() {
        return this.lastService;
    }

    public final int getLimitCruise() {
        return this.limitCruise;
    }

    public final int getLimitMode1() {
        return this.limitMode1;
    }

    public final int getLimitMode2() {
        return this.limitMode2;
    }

    public final int getLimitMode3() {
        return this.limitMode3;
    }

    public final boolean getLockSw() {
        return this.lockSw;
    }

    public final boolean getMetricInchSw() {
        return this.metricInchSw;
    }

    public final float getNextService() {
        return this.nextService;
    }

    public final int getServiceMileage() {
        return this.serviceMileage;
    }

    public final int getTest10() {
        return this.test10;
    }

    public final int getTest11() {
        return this.test11;
    }

    public final int getTest12() {
        return this.test12;
    }

    public final int getTest13() {
        return this.test13;
    }

    public final int getTest9() {
        return this.test9;
    }

    @NotNull
    public final String getTestStr() {
        return this.testStr;
    }

    public final void setAtmosphereLightSw(boolean z2) {
        this.atmosphereLightSw = z2;
    }

    public final void setBootMode(boolean z2) {
        this.bootMode = z2;
    }

    public final void setCruiseControlSw(boolean z2) {
        this.cruiseControlSw = z2;
    }

    public final void setDisplayName(@Nullable String str) {
        this.displayName = str;
    }

    public final void setDisplayVersion(@Nullable String str) {
        this.displayVersion = str;
    }

    public final void setGearPosition(int i2) {
        this.gearPosition = i2;
    }

    public final void setHeadLightSw(boolean z2) {
        this.headLightSw = z2;
    }

    public final void setLastService(int i2) {
        this.lastService = i2;
    }

    public final void setLimitCruise(int i2) {
        this.limitCruise = i2;
    }

    public final void setLimitMode1(int i2) {
        this.limitMode1 = i2;
    }

    public final void setLimitMode2(int i2) {
        this.limitMode2 = i2;
    }

    public final void setLimitMode3(int i2) {
        this.limitMode3 = i2;
    }

    public final void setLockSw(boolean z2) {
        this.lockSw = z2;
    }

    public final void setMetricInchSw(boolean z2) {
        this.metricInchSw = z2;
    }

    public final void setNextService(float f2) {
        this.nextService = f2;
    }

    public final void setServiceMileage(int i2) {
        this.serviceMileage = i2;
    }

    public final void setTest10(int i2) {
        this.test10 = i2;
    }

    public final void setTest11(int i2) {
        this.test11 = i2;
    }

    public final void setTest12(int i2) {
        this.test12 = i2;
    }

    public final void setTest13(int i2) {
        this.test13 = i2;
    }

    public final void setTest9(int i2) {
        this.test9 = i2;
    }
    public final void setSpeed(float i2) {
        this.speed = i2;
    }
    public void setPower(float i) {
        this.power = i;
    }

    public final void setTestStr(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "<set-?>");
        this.testStr = str;
    }

    @NotNull
    public BaseParams clone() {
        try {
            return (BaseParams) super.clone();
        } catch (Exception e) {
            return new BaseParams();
        }
    }


    @NotNull
    public String toString() {
        int i2 = this.gearPosition;
        boolean z2 = this.headLightSw;
        boolean z3 = this.atmosphereLightSw;
        boolean z4 = this.cruiseControlSw;
        boolean z5 = this.bootMode;
        boolean z6 = this.metricInchSw;
        boolean z7 = this.lockSw;
        int i3 = this.limitCruise;
        int i4 = this.limitMode1;
        int i5 = this.limitMode2;
        int i6 = this.limitMode3;
        float f2 = this.nextService;
        int i7 = this.lastService;
        int i8 = this.serviceMileage;
        String str = this.displayName;
        String str2 = this.displayVersion;
        int i9 = this.test9;
        int i10 = this.test10;
        int i11 = this.test11;
        int i12 = this.test12;
        int i13 = this.test13;
        String str3 = this.testStr;
        return "BaseParams(gearPosition=" + i2 + ", headLightSw=" + z2 + ", atmosphereLightSw=" + z3 + ", cruiseControlSw=" + z4 + ", bootMode=" + z5 + ", metricInchSw=" + z6 + ", lockSw=" + z7 + ", limitCruise=" + i3 + ", limitMode1=" + i4 + ", limitMode2=" + i5 + ", limitMode3=" + i6 + ", nextService=" + f2 + ", lastService=" + i7 + ", serviceMileage=" + i8 + ", displayName=" + str + ", displayVersion=" + str2 + ", test9=" + i9 + ", test10=" + i10 + ", test11=" + i11 + ", test12=" + i12 + ", test13=" + i13 + ", testStr=" + str3 + ", speed=" + this.speed + ", power=" + this.power + ")";
    }
    @NotNull
    public HashMap<String, Object> toObject() {
        HashMap<String, Object> temp = new HashMap<>();
        temp.put("gearPosition", this.gearPosition);
        temp.put("headLightSw", this.headLightSw);
        temp.put("atmosphereLightSw", atmosphereLightSw);
        temp.put("cruiseControlSw", cruiseControlSw);
        temp.put("bootMode", bootMode);
        temp.put("metricInchSw", metricInchSw);
        temp.put("lockSw", lockSw);
        temp.put("limitCruise", limitCruise);
        temp.put("limitMode1", limitMode1);
        temp.put("limitMode2", limitMode2);
        temp.put("limitMode3", limitMode3);
        temp.put("nextService", nextService);
        temp.put("lastService", lastService);
        temp.put("serviceMileage", serviceMileage);
        temp.put("displayName", displayName);
        temp.put("displayVersion", displayVersion);
        temp.put("speed", speed);
        temp.put("power", power);
        int i9 = this.test9;
        int i10 = this.test10;
        int i11 = this.test11;
        int i12 = this.test12;
        int i13 = this.test13;
        String str3 = this.testStr;

        return  temp;
    }

    public BaseParams(int i2, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7, int i3, int i4, int i5, int i6, float f2, int i7, int i8, String str, String str2, int i9, int i10, int i11, int i12, int i13, String str3, int i14) {
        this((i14 & 1) != 0 ? 1 : i2, (i14 & 2) != 0 ? false : z2, (i14 & 4) != 0 ? false : z3, (i14 & 8) != 0 ? false : z4, (i14 & 16) != 0 ? false : z5, (i14 & 32) != 0 ? false : z6, (i14 & 64) != 0 ? false : z7, (i14 & 128) != 0 ? 3 : i3, (i14 & 256) != 0 ? 6 : i4, (i14 & 512) != 0 ? 10 : i5, (i14 & 1024) != 0 ? 20 : i6, (i14 & 2048) != 0 ? 0.0f : f2, (i14 & 4096) != 0 ? 0 : i7, (i14 & 8192) != 0 ? 0 : i8, (i14 & 16384) != 0 ? null : str, (i14 & 32768) == 0 ? str2 : null, (i14 & 65536) != 0 ? 0 : i9, (i14 & 131072) != 0 ? 0 : i10, (i14 & 262144) != 0 ? 0 : i11, (i14 & 524288) != 0 ? 0 : i12, (i14 & 1048576) != 0 ? 0 : i13, (i14 & 2097152) != 0 ? "" : str3);
    }

    public BaseParams(int i2, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7, int i3, int i4, int i5, int i6, float f2, int i7, int i8, String str, String str2, int i9, int i10, int i11, int i12, int i13, String str3, int i14, Object defaultConstructorMarker) {
        this((i14 & 1) != 0 ? 1 : i2, (i14 & 2) != 0 ? false : z2, (i14 & 4) != 0 ? false : z3, (i14 & 8) != 0 ? false : z4, (i14 & 16) != 0 ? false : z5, (i14 & 32) != 0 ? false : z6, (i14 & 64) != 0 ? false : z7, (i14 & 128) != 0 ? 3 : i3, (i14 & 256) != 0 ? 6 : i4, (i14 & 512) != 0 ? 10 : i5, (i14 & 1024) != 0 ? 20 : i6, (i14 & 2048) != 0 ? 0.0f : f2, (i14 & 4096) != 0 ? 0 : i7, (i14 & 8192) != 0 ? 0 : i8, (i14 & 16384) != 0 ? null : str, (i14 & 32768) == 0 ? str2 : null, (i14 & 65536) != 0 ? 0 : i9, (i14 & 131072) != 0 ? 0 : i10, (i14 & 262144) != 0 ? 0 : i11, (i14 & 524288) != 0 ? 0 : i12, (i14 & 1048576) != 0 ? 0 : i13, (i14 & 2097152) != 0 ? "" : str3);
    }

    public BaseParams(HashMap<String, Object> obj) {
        this();
        try {
            gearPosition = (int) obj.get("gearPosition");
        } catch (Exception e) {
            Log.e("BaseParams", "from hashMap".concat(String.valueOf(e.getMessage())));
        }
        try {
            headLightSw = (boolean) obj.get("headLightSw");
        } catch (Exception e) {
            Log.e("BaseParams", "from hashMap".concat(String.valueOf(e.getMessage())));
        }
        try {

            atmosphereLightSw = (boolean) obj.get("atmosphereLightSw");
        } catch (Exception e) {
            Log.e("BaseParams", "from hashMap".concat(String.valueOf(e.getMessage())));
        }
        try {
            cruiseControlSw = (boolean) obj.get("cruiseControlSw");
        } catch (Exception e) {
            Log.e("BaseParams", "from hashMap".concat(String.valueOf(e.getMessage())));
        }
        try {
            bootMode = (boolean) obj.get("bootMode");
        } catch (Exception e) {
            Log.e("BaseParams", "from hashMap".concat(String.valueOf(e.getMessage())));
        }
        try {
            metricInchSw = (boolean) obj.get("metricInchSw");
        } catch (Exception e) {
            Log.e("BaseParams", "from hashMap".concat(String.valueOf(e.getMessage())));
        }
        try {
            lockSw = (boolean) obj.get("lockSw");
        } catch (Exception e) {
            Log.e("BaseParams", "from hashMap".concat(String.valueOf(e.getMessage())));
        }
        try {
            limitCruise = (int) obj.get("limitCruise");
        } catch (Exception e) {
            Log.e("BaseParams", "from hashMap".concat(String.valueOf(e.getMessage())));
        }
        try {
            limitMode1 = (int) obj.get(" limitMode1");
        } catch (Exception e) {
            Log.e("BaseParams", "from hashMap".concat(String.valueOf(e.getMessage())));
        }
        try {
            limitMode2 = (int) obj.get("limitMode2");
        } catch (Exception e) {
            Log.e("BaseParams", "from hashMap".concat(String.valueOf(e.getMessage())));
        }
        try {
            limitMode3 = (int) obj.get("limitMode3");
        } catch (Exception e) {
            Log.e("BaseParams", "from hashMap".concat(String.valueOf(e.getMessage())));
        }
        try {
            nextService = (int) obj.get("nextService");
        } catch (Exception e) {
            Log.e("BaseParams", "from hashMap".concat(String.valueOf(e.getMessage())));
        }
        try {
            lastService = (int) obj.get("lastService");
        } catch (Exception e) {
            Log.e("BaseParams", "from hashMap".concat(String.valueOf(e.getMessage())));
        }
        try {
            serviceMileage = (int) obj.get("serviceMileage");
        } catch (Exception e) {
            Log.e("BaseParams", "from hashMap".concat(String.valueOf(e.getMessage())));
        }
        try {
            displayName = (String) obj.get("displayName");
        } catch (Exception e) {
            Log.e("BaseParams", "from hashMap".concat(String.valueOf(e.getMessage())));
        }
        try {
            displayVersion = (String) obj.get("displayVersion");
        } catch (Exception e) {
            Log.e("BaseParams", "from hashMap".concat(String.valueOf(e.getMessage())));
        }
        try {
            speed = (float) obj.get("speed");
        } catch (Exception e) {
            Log.e("BaseParams", "from hashMap".concat(String.valueOf(e.getMessage())));
        }
        try {
            power = (float) obj.get("power");
        } catch (Exception e) {
            Log.e("BaseParams", "from hashMap".concat(String.valueOf(e.getMessage())));
        }
    }

}

