package sc.denishik.ru.midwayApi.help;

import com.google.android.material.shadow.ShadowDrawableWrapper;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

final  class a {
    public static /* synthetic */ int a(double d2) {
        long doubleToLongBits = Double.doubleToLongBits(d2);
        return (int) (doubleToLongBits ^ (doubleToLongBits >>> 32));
    }
}
public class ParamsList {
    @NotNull
    private List<Parameter> parameter;

    public ParamsList() {
        this(null, 1, null);
    }

    public ParamsList(@NotNull List<Parameter> parameter) {
        Intrinsics.checkNotNullParameter(parameter, "parameter");
        this.parameter = parameter;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static /* synthetic */ ParamsList copy$default(ParamsList paramsList, List list, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            list = paramsList.parameter;
        }
        return paramsList.copy(list);
    }

    @NotNull
    public final List<Parameter> component1() {
        return this.parameter;
    }

    @NotNull
    public final ParamsList copy(@NotNull List<Parameter> parameter) {
        Intrinsics.checkNotNullParameter(parameter, "parameter");
        return new ParamsList(parameter);
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof ParamsList) && Intrinsics.areEqual(this.parameter, ((ParamsList) obj).parameter);
    }

    @NotNull
    public final List<Parameter> getParameter() {
        return this.parameter;
    }

    public int hashCode() {
        return this.parameter.hashCode();
    }

    public final void setParameter(@NotNull List<Parameter> list) {
        Intrinsics.checkNotNullParameter(list, "<set-?>");
        this.parameter = list;
    }

    @NotNull
    public String toString() {
        List<Parameter> list = this.parameter;
        return "ParamsList(parameter=" + list + ")";
    }

    public /* synthetic */ ParamsList(List list, int i2, DefaultConstructorMarker defaultConstructorMarker) {
        this((i2 & 1) != 0 ? CollectionsKt.emptyList() : list);
    }

    /* compiled from: ParamsList.kt */
    public static final class Parameter {
        @SerializedName("address")
        private int address;
        @SerializedName("bit")
        private int bit;
        @SerializedName("max")
        private int max;
        @SerializedName("min")
        private double min;
        @SerializedName("mode")
        private int mode;
        @SerializedName("name")
        @NotNull
        private String name;
        @SerializedName("no")

        /* renamed from: no */
        private int f1347no;
        @SerializedName("now")
        private int now;
        @SerializedName("opv")
        private double opv;
        @SerializedName("real")
        private int real;
        @SerializedName("step")
        private double step;
        @SerializedName("unit")
        @NotNull
        private String unit;
        @SerializedName("value")
        @NotNull
        private List<String> value;
        @SerializedName("values")
        @NotNull
        private List<String> values;

        public Parameter() {
            this(0, 0, 0, Math.cos(Math.toRadians(45)), 0, null, 0, 0, Math.cos(Math.toRadians(45)), 0, Math.cos(Math.toRadians(45)), null, null, null, 16383, null);
        }

        public Parameter(int i2, int i3, int i4, double d2, int i5, @NotNull String name, int i6, int i7, double d3, int i8, double d4, @NotNull String unit, @NotNull List<String> value, @NotNull List<String> values) {
            Intrinsics.checkNotNullParameter(name, "name");
            Intrinsics.checkNotNullParameter(unit, "unit");
            Intrinsics.checkNotNullParameter(value, "value");
            Intrinsics.checkNotNullParameter(values, "values");
            this.address = i2;
            this.bit = i3;
            this.max = i4;
            this.min = d2;
            this.mode = i5;
            this.name = name;
            this.f1347no = i6;
            this.now = i7;
            this.opv = d3;
            this.real = i8;
            this.step = d4;
            this.unit = unit;
            this.value = value;
            this.values = values;
        }

        public final int component1() {
            return this.address;
        }

        public final int component10() {
            return this.real;
        }

        public final double component11() {
            return this.step;
        }

        @NotNull
        public final String component12() {
            return this.unit;
        }

        @NotNull
        public final List<String> component13() {
            return this.value;
        }

        @NotNull
        public final List<String> component14() {
            return this.values;
        }

        public final int component2() {
            return this.bit;
        }

        public final int component3() {
            return this.max;
        }

        public final double component4() {
            return this.min;
        }

        public final int component5() {
            return this.mode;
        }

        @NotNull
        public final String component6() {
            return this.name;
        }

        public final int component7() {
            return this.f1347no;
        }

        public final int component8() {
            return this.now;
        }

        public final double component9() {
            return this.opv;
        }

        @NotNull
        public final Parameter copy(int i2, int i3, int i4, double d2, int i5, @NotNull String name, int i6, int i7, double d3, int i8, double d4, @NotNull String unit, @NotNull List<String> value, @NotNull List<String> values) {
            Intrinsics.checkNotNullParameter(name, "name");
            Intrinsics.checkNotNullParameter(unit, "unit");
            Intrinsics.checkNotNullParameter(value, "value");
            Intrinsics.checkNotNullParameter(values, "values");
            return new Parameter(i2, i3, i4, d2, i5, name, i6, i7, d3, i8, d4, unit, value, values);
        }

        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof Parameter) {
                Parameter parameter = (Parameter) obj;
                return this.address == parameter.address && this.bit == parameter.bit && this.max == parameter.max && Intrinsics.areEqual((Object) Double.valueOf(this.min), (Object) Double.valueOf(parameter.min)) && this.mode == parameter.mode && Intrinsics.areEqual(this.name, parameter.name) && this.f1347no == parameter.f1347no && this.now == parameter.now && Intrinsics.areEqual((Object) Double.valueOf(this.opv), (Object) Double.valueOf(parameter.opv)) && this.real == parameter.real && Intrinsics.areEqual((Object) Double.valueOf(this.step), (Object) Double.valueOf(parameter.step)) && Intrinsics.areEqual(this.unit, parameter.unit) && Intrinsics.areEqual(this.value, parameter.value) && Intrinsics.areEqual(this.values, parameter.values);
            }
            return false;
        }

        public final int getAddress() {
            return this.address;
        }

        public final int getBit() {
            return this.bit;
        }

        public final int getMax() {
            return this.max;
        }

        public final double getMin() {
            return this.min;
        }

        public final int getMode() {
            return this.mode;
        }

        @NotNull
        public final String getName() {
            return this.name;
        }

        public final int getNo() {
            return this.f1347no;
        }

        public final int getNow() {
            return this.now;
        }

        public final double getOpv() {
            return this.opv;
        }

        public final int getReal() {
            return this.real;
        }

        public final double getStep() {
            return this.step;
        }

        @NotNull
        public final String getUnit() {
            return this.unit;
        }

        @NotNull
        public final List<String> getValue() {
            return this.value;
        }

        @NotNull
        public final List<String> getValues() {
            return this.values;
        }

        public int hashCode() {
            return (((((((((((((((((((((((((this.address * 31) + this.bit) * 31) + this.max) * 31) + a.a(this.min)) * 31) + this.mode) * 31) + this.name.hashCode()) * 31) + this.f1347no) * 31) + this.now) * 31) + a.a(this.opv)) * 31) + this.real) * 31) + a.a(this.step)) * 31) + this.unit.hashCode()) * 31) + this.value.hashCode()) * 31) + this.values.hashCode();
        }

        public final void setAddress(int i2) {
            this.address = i2;
        }

        public final void setBit(int i2) {
            this.bit = i2;
        }

        public final void setMax(int i2) {
            this.max = i2;
        }

        public final void setMin(double d2) {
            this.min = d2;
        }

        public final void setMode(int i2) {
            this.mode = i2;
        }

        public final void setName(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "<set-?>");
            this.name = str;
        }

        public final void setNo(int i2) {
            this.f1347no = i2;
        }

        public final void setNow(int i2) {
            this.now = i2;
        }

        public final void setOpv(double d2) {
            this.opv = d2;
        }

        public final void setReal(int i2) {
            this.real = i2;
        }

        public final void setStep(double d2) {
            this.step = d2;
        }

        public final void setUnit(@NotNull String str) {
            Intrinsics.checkNotNullParameter(str, "<set-?>");
            this.unit = str;
        }

        public final void setValue(@NotNull List<String> list) {
            Intrinsics.checkNotNullParameter(list, "<set-?>");
            this.value = list;
        }

        public final void setValues(@NotNull List<String> list) {
            Intrinsics.checkNotNullParameter(list, "<set-?>");
            this.values = list;
        }

        @NotNull
        public String toString() {
            int i2 = this.address;
            int i3 = this.bit;
            int i4 = this.max;
            double d2 = this.min;
            int i5 = this.mode;
            String str = this.name;
            int i6 = this.f1347no;
            int i7 = this.now;
            double d3 = this.opv;
            int i8 = this.real;
            double d4 = this.step;
            String str2 = this.unit;
            List<String> list = this.value;
            List<String> list2 = this.values;
            return "Parameter(address=" + i2 + ", bit=" + i3 + ", max=" + i4 + ", min=" + d2 + ", mode=" + i5 + ", name=" + str + ", no=" + i6 + ", now=" + i7 + ", opv=" + d3 + ", real=" + i8 + ", step=" + d4 + ", unit=" + str2 + ", value=" + list + ", values=" + list2 + ")";
        }

        public /* synthetic */ Parameter(int i2, int i3, int i4, double d2, int i5, String str, int i6, int i7, double d3, int i8, double d4, String str2, List list, List list2, int i9, DefaultConstructorMarker defaultConstructorMarker) {
            this((i9 & 1) != 0 ? 0 : i2, (i9 & 2) != 0 ? 0 : i3, (i9 & 4) != 0 ? 0 : i4, (i9 & 8) != 0 ? Math.cos(Math.toRadians(45)) : d2, (i9 & 16) != 0 ? 0 : i5, (i9 & 32) != 0 ? "" : str, (i9 & 64) != 0 ? 0 : i6, (i9 & 128) != 0 ? 0 : i7, (i9 & 256) != 0 ? Math.cos(Math.toRadians(45)) : d3, (i9 & 512) != 0 ? 0 : i8, (i9 & 1024) != 0 ? Math.cos(Math.toRadians(45)) : d4, (i9 & 2048) != 0 ? "" : str2, (i9 & 4096) != 0 ? CollectionsKt.emptyList() : list, (i9 & 8192) != 0 ? new ArrayList() : list2);
        }
    }

}
