package sc.denishik.ru.midwayApi.base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

public class FullAdvParams {
    private int acceleratedThrottleResponse;
    private int acceleratorBrakeResponse;
    private boolean cruiseSw;
    private int cruiseTime;
    private boolean isMetric;
    private boolean isZeroStart;
    private int lastServiceMileage;
    private int limitedSpeedValue;
    private float maxBrakingCurrent;
    private float maxDischargeCurrent;
    private int maxModulationDepth;
    private float motorDiameter;
    private int motorPolePairs;
    private int pwmFrequency;
    private int serviceMileage;
    private int shutdownTime;
    private float voltageProtection;

    public FullAdvParams() {
        this(false, false, false, 0, 0, 0, 0, 0.0f, 0.0f, 0.0f, 0, 0.0f, 0, 0, 0, 0, 0, 131071, null);
    }

    public FullAdvParams(boolean z2, boolean z3, boolean z4, int i2, int i3, int i4, int i5, float f2, float f3, float f4, int i6, float f5, int i7, int i8, int i9, int i10, int i11) {
        this.cruiseSw = z2;
        this.isMetric = z3;
        this.isZeroStart = z4;
        this.maxModulationDepth = i2;
        this.motorPolePairs = i3;
        this.acceleratedThrottleResponse = i4;
        this.acceleratorBrakeResponse = i5;
        this.maxDischargeCurrent = f2;
        this.maxBrakingCurrent = f3;
        this.voltageProtection = f4;
        this.limitedSpeedValue = i6;
        this.motorDiameter = f5;
        this.pwmFrequency = i7;
        this.cruiseTime = i8;
        this.shutdownTime = i9;
        this.serviceMileage = i10;
        this.lastServiceMileage = i11;
    }

    public final boolean component1() {
        return this.cruiseSw;
    }

    public final float component10() {
        return this.voltageProtection;
    }

    public final int component11() {
        return this.limitedSpeedValue;
    }

    public final float component12() {
        return this.motorDiameter;
    }

    public final int component13() {
        return this.pwmFrequency;
    }

    public final int component14() {
        return this.cruiseTime;
    }

    public final int component15() {
        return this.shutdownTime;
    }

    public final int component16() {
        return this.serviceMileage;
    }

    public final int component17() {
        return this.lastServiceMileage;
    }

    public final boolean component2() {
        return this.isMetric;
    }

    public final boolean component3() {
        return this.isZeroStart;
    }

    public final int component4() {
        return this.maxModulationDepth;
    }

    public final int component5() {
        return this.motorPolePairs;
    }

    public final int component6() {
        return this.acceleratedThrottleResponse;
    }

    public final int component7() {
        return this.acceleratorBrakeResponse;
    }

    public final float component8() {
        return this.maxDischargeCurrent;
    }

    public final float component9() {
        return this.maxBrakingCurrent;
    }

    @NotNull
    public final FullAdvParams copy(boolean z2, boolean z3, boolean z4, int i2, int i3, int i4, int i5, float f2, float f3, float f4, int i6, float f5, int i7, int i8, int i9, int i10, int i11) {
        return new FullAdvParams(z2, z3, z4, i2, i3, i4, i5, f2, f3, f4, i6, f5, i7, i8, i9, i10, i11);
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof FullAdvParams) {
            FullAdvParams fullAdvParams = (FullAdvParams) obj;
            return this.cruiseSw == fullAdvParams.cruiseSw && this.isMetric == fullAdvParams.isMetric && this.isZeroStart == fullAdvParams.isZeroStart && this.maxModulationDepth == fullAdvParams.maxModulationDepth && this.motorPolePairs == fullAdvParams.motorPolePairs && this.acceleratedThrottleResponse == fullAdvParams.acceleratedThrottleResponse && this.acceleratorBrakeResponse == fullAdvParams.acceleratorBrakeResponse && Intrinsics.areEqual((Object) Float.valueOf(this.maxDischargeCurrent), (Object) Float.valueOf(fullAdvParams.maxDischargeCurrent)) && Intrinsics.areEqual((Object) Float.valueOf(this.maxBrakingCurrent), (Object) Float.valueOf(fullAdvParams.maxBrakingCurrent)) && Intrinsics.areEqual((Object) Float.valueOf(this.voltageProtection), (Object) Float.valueOf(fullAdvParams.voltageProtection)) && this.limitedSpeedValue == fullAdvParams.limitedSpeedValue && Intrinsics.areEqual((Object) Float.valueOf(this.motorDiameter), (Object) Float.valueOf(fullAdvParams.motorDiameter)) && this.pwmFrequency == fullAdvParams.pwmFrequency && this.cruiseTime == fullAdvParams.cruiseTime && this.shutdownTime == fullAdvParams.shutdownTime && this.serviceMileage == fullAdvParams.serviceMileage && this.lastServiceMileage == fullAdvParams.lastServiceMileage;
        }
        return false;
    }

    public final int getAcceleratedThrottleResponse() {
        return this.acceleratedThrottleResponse;
    }

    public final int getAcceleratorBrakeResponse() {
        return this.acceleratorBrakeResponse;
    }

    public final boolean getCruiseSw() {
        return this.cruiseSw;
    }

    public final int getCruiseTime() {
        return this.cruiseTime;
    }

    public final int getLastServiceMileage() {
        return this.lastServiceMileage;
    }

    public final int getLimitedSpeedValue() {
        return this.limitedSpeedValue;
    }

    public final float getMaxBrakingCurrent() {
        return this.maxBrakingCurrent;
    }

    public final float getMaxDischargeCurrent() {
        return this.maxDischargeCurrent;
    }

    public final int getMaxModulationDepth() {
        return this.maxModulationDepth;
    }

    public final float getMotorDiameter() {
        return this.motorDiameter;
    }

    public final int getMotorPolePairs() {
        return this.motorPolePairs;
    }

    public final int getPwmFrequency() {
        return this.pwmFrequency;
    }

    public final int getServiceMileage() {
        return this.serviceMileage;
    }

    public final int getShutdownTime() {
        return this.shutdownTime;
    }

    public final float getVoltageProtection() {
        return this.voltageProtection;
    }

    public final boolean isMetric() {
        return this.isMetric;
    }

    public final boolean isZeroStart() {
        return this.isZeroStart;
    }

    public final void setAcceleratedThrottleResponse(int i2) {
        this.acceleratedThrottleResponse = i2;
    }

    public final void setAcceleratorBrakeResponse(int i2) {
        this.acceleratorBrakeResponse = i2;
    }

    public final void setCruiseSw(boolean z2) {
        this.cruiseSw = z2;
    }

    public final void setCruiseTime(int i2) {
        this.cruiseTime = i2;
    }

    public final void setLastServiceMileage(int i2) {
        this.lastServiceMileage = i2;
    }

    public final void setLimitedSpeedValue(int i2) {
        this.limitedSpeedValue = i2;
    }

    public final void setMaxBrakingCurrent(float f2) {
        this.maxBrakingCurrent = f2;
    }

    public final void setMaxDischargeCurrent(float f2) {
        this.maxDischargeCurrent = f2;
    }

    public final void setMaxModulationDepth(int i2) {
        this.maxModulationDepth = i2;
    }

    public final void setMetric(boolean z2) {
        this.isMetric = z2;
    }

    public final void setMotorDiameter(float f2) {
        this.motorDiameter = f2;
    }

    public final void setMotorPolePairs(int i2) {
        this.motorPolePairs = i2;
    }

    public final void setPwmFrequency(int i2) {
        this.pwmFrequency = i2;
    }

    public final void setServiceMileage(int i2) {
        this.serviceMileage = i2;
    }

    public final void setShutdownTime(int i2) {
        this.shutdownTime = i2;
    }

    public final void setVoltageProtection(float f2) {
        this.voltageProtection = f2;
    }

    public final void setZeroStart(boolean z2) {
        this.isZeroStart = z2;
    }

    @NotNull
    public String toString() {
        boolean z2 = this.cruiseSw;
        boolean z3 = this.isMetric;
        boolean z4 = this.isZeroStart;
        int i2 = this.maxModulationDepth;
        int i3 = this.motorPolePairs;
        int i4 = this.acceleratedThrottleResponse;
        int i5 = this.acceleratorBrakeResponse;
        float f2 = this.maxDischargeCurrent;
        float f3 = this.maxBrakingCurrent;
        float f4 = this.voltageProtection;
        int i6 = this.limitedSpeedValue;
        float f5 = this.motorDiameter;
        int i7 = this.pwmFrequency;
        int i8 = this.cruiseTime;
        int i9 = this.shutdownTime;
        int i10 = this.serviceMileage;
        int i11 = this.lastServiceMileage;
        return "FullAdvParams(cruiseSw=" + z2 + ", isMetric=" + z3 + ", isZeroStart=" + z4 + ", maxModulationDepth=" + i2 + ", motorPolePairs=" + i3 + ", acceleratedThrottleResponse=" + i4 + ", acceleratorBrakeResponse=" + i5 + ", maxDischargeCurrent=" + f2 + ", maxBrakingCurrent=" + f3 + ", voltageProtection=" + f4 + ", limitedSpeedValue=" + i6 + ", motorDiameter=" + f5 + ", pwmFrequency=" + i7 + ", cruiseTime=" + i8 + ", shutdownTime=" + i9 + ", serviceMileage=" + i10 + ", lastServiceMileage=" + i11 + ")";
    }

    public /* synthetic */ FullAdvParams(boolean z2, boolean z3, boolean z4, int i2, int i3, int i4, int i5, float f2, float f3, float f4, int i6, float f5, int i7, int i8, int i9, int i10, int i11, int i12, DefaultConstructorMarker defaultConstructorMarker) {
        this((i12 & 1) != 0 ? false : z2, (i12 & 2) != 0 ? false : z3, (i12 & 4) != 0 ? false : z4, (i12 & 8) != 0 ? 0 : i2, (i12 & 16) != 0 ? 0 : i3, (i12 & 32) != 0 ? 0 : i4, (i12 & 64) != 0 ? 0 : i5, (i12 & 128) != 0 ? 0.0f : f2, (i12 & 256) != 0 ? 0.0f : f3, (i12 & 512) != 0 ? 0.0f : f4, (i12 & 1024) != 0 ? 0 : i6, (i12 & 2048) == 0 ? f5 : 0.0f, (i12 & 4096) != 0 ? 0 : i7, (i12 & 8192) != 0 ? 0 : i8, (i12 & 16384) != 0 ? 0 : i9, (i12 & 32768) != 0 ? 0 : i10, (i12 & 65536) != 0 ? 0 : i11);
    }

}
