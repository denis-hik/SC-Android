package sc.denishik.ru.midwayApi.help;

import android.view.View;

import com.google.android.material.shadow.ShadowDrawableWrapper;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;

public class ValueExtKt {
    public static final double addHalf(double d2) {
        String takeLast;
        String format = String.format(Locale.ENGLISH, "%.1f", Arrays.copyOf(new Object[]{Double.valueOf(d2)}, 1));
        Intrinsics.checkNotNullExpressionValue(format, "format(locale, format, *args)");
        int length = format.length();
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                break;
            }
            char charAt = format.charAt(i2);
            if (!((charAt == '.' || charAt == ',') ? false : true)) {
                format = format.substring(0, i2);
                Intrinsics.checkNotNullExpressionValue(format, "this as java.lang.String…ing(startIndex, endIndex)");
                break;
            }
            i2++;
        }
        double parseDouble = Double.parseDouble(format);
        String format2 = String.format(Locale.ENGLISH, "%.1f", Arrays.copyOf(new Object[]{Double.valueOf(d2)}, 1));
        Intrinsics.checkNotNullExpressionValue(format2, "format(locale, format, *args)");
        takeLast = StringsKt.takeLast(format2, 1);
        double parseDouble2 = Double.parseDouble(takeLast);
        return parseDouble + (parseDouble2 <= 3.0d ? Math.cos(Math.toRadians(45)) : parseDouble2 >= 7.0d ? 1.0d : 0.5d);
    }

    @NotNull
    public static final String ctof(int i2) {
        String df = DecimalFormat.getInstance(Locale.ENGLISH).format("###.0");
        Intrinsics.checkNotNullExpressionValue(df, "df");
        String format = String.format(df, Arrays.copyOf(new Object[]{Double.valueOf((i2 * 1.8d) + 32)}, 1));
        Intrinsics.checkNotNullExpressionValue(format, "format(this, *args)");
        return format + "°F";
    }

    @NotNull
    public static final String dataLong(int i2) {
        if (i2 >= 10) {
            return String.valueOf(i2);
        }
        return "0" + i2;
    }

    @NotNull
    public static final String df(int i2, @NotNull String format) {
        Intrinsics.checkNotNullParameter(format, "format");
        DecimalFormat decimalFormat = new DecimalFormat(format);
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
        String format2 = decimalFormat.format(Integer.valueOf(i2));
        Intrinsics.checkNotNullExpressionValue(format2, "df.format(this)");
        return format2;
    }
//    public static final String error(int i2) {
//        if (i2 != -100) {
//            switch (i2) {
//                case FailCallback.REASON_CANCELLED /* -7 */:
//                    return "REASON_CANCELLED";
//                case FailCallback.REASON_VALIDATION /* -6 */:
//                    return "REASON_VALIDATION";
//                case FailCallback.REASON_TIMEOUT /* -5 */:
//                    return "REASON_TIMEOUT";
//                case -4:
//                    return "REASON_REQUEST_FAILED";
//                case -3:
//                    return "REASON_NULL_ATTRIBUTE";
//                case -2:
//                    return "REASON_DEVICE_NOT_SUPPORTED";
//                case -1:
//                    return "REASON_DEVICE_DISCONNECTED";
//                default:
//                    return "OTHER ERROR : " + i2;
//            }
//        }
//        return "REASON_BLUETOOTH_DISABLED";
//    }

    public static final int getBit(int i2, int i3) {
        return (i2 & (1 << i3)) >> i3;
    }

    public static final boolean isInRange(float f2, float f3, float f4) {
        if (f4 > f3) {
            if (f3 <= f2 && f2 <= f4) {
                return true;
            }
        } else if (f4 <= f2 && f2 <= f3) {
            return true;
        }
        return false;
    }

    public static final boolean isInRange(int i2, int i3, int i4) {
        if (i4 > i3) {
            if (i3 <= i2 && i2 <= i4) {
                return true;
            }
        } else if (i4 <= i2 && i2 <= i3) {
            return true;
        }
        return false;
    }

    public static final float maxDecimal(float f2, int i2) {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
        numberFormat.setGroupingUsed(false);
        numberFormat.setMaximumFractionDigits(i2);
        String format = numberFormat.format(Float.valueOf(f2));
        Intrinsics.checkNotNullExpressionValue(format, "df.format(this)");
        return Float.parseFloat(format);
    }

    public static final int measureSize(int i2, int i3) {
        int mode = View.MeasureSpec.getMode(i2);
        int size = View.MeasureSpec.getSize(i2);
        if (mode != Integer.MIN_VALUE) {
            return mode != 1073741824 ? i3 : size;
        }
        return Math.min(i3, size);
    }

    @NotNull
    public static final String precisionFormat(int i2) {
        return "%." + i2 + "f";
    }

    public static final boolean toBool(int i2) {
        return i2 == 1;
    }

    @NotNull
    public static final byte[] toBytes16(int i2, boolean z2) {
        byte[] bArr = new byte[2];
        if (z2) {
            bArr[0] = (byte) i2;
            bArr[1] = (byte) (i2 >> 8);
        } else {
            bArr[0] = (byte) (i2 >> 8);
            bArr[1] = (byte) i2;
        }
        return bArr;
    }

    public static /* synthetic */ byte[] toBytes16$default(int i2, boolean z2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            z2 = false;
        }
        return toBytes16(i2, z2);
    }

    @NotNull
    public static final byte[] toBytes32(int i2, boolean z2) {
        byte[] bArr = new byte[4];
        if (z2) {
            bArr[0] = (byte) i2;
            bArr[1] = (byte) (i2 >> 8);
            bArr[2] = (byte) (i2 >> 16);
            bArr[3] = (byte) (i2 >> 24);
        } else {
            bArr[0] = (byte) (i2 >> 24);
            bArr[1] = (byte) (i2 >> 16);
            bArr[2] = (byte) (i2 >> 8);
            bArr[3] = (byte) i2;
        }
        return bArr;
    }

    @NotNull
    public static final byte[] toBytes32(long j2, boolean z2) {
        if (j2 <= 2147483647L && j2 >= -2147483648L) {
            if (j2 <= 32767 && j2 >= -32768) {
                return toBytes16((int) j2, z2);
            }
            return toBytes32((int) j2, z2);
        }
        byte[] bArr = new byte[8];
        if (z2) {
            bArr[0] = (byte) j2;
            bArr[1] = (byte) (j2 >> 8);
            bArr[2] = (byte) (j2 >> 16);
            bArr[3] = (byte) (j2 >> 24);
            bArr[4] = (byte) (j2 >> 32);
            bArr[5] = (byte) (j2 >> 40);
        } else {
            bArr[0] = (byte) (j2 >> 40);
            bArr[1] = (byte) (j2 >> 32);
            bArr[2] = (byte) (j2 >> 24);
            bArr[3] = (byte) (j2 >> 16);
            bArr[4] = (byte) (j2 >> 8);
            bArr[5] = (byte) j2;
        }
        return bArr;
    }

    @NotNull
    public static final byte[] toBytes32(short s2, boolean z2) {
        byte[] bArr = new byte[2];
        if (z2) {
            bArr[0] = (byte) s2;
            bArr[1] = (byte) (s2 >> 8);
        } else {
            bArr[0] = (byte) (s2 >> 8);
            bArr[1] = (byte) s2;
        }
        return bArr;
    }

    public static /* synthetic */ byte[] toBytes32$default(short s2, boolean z2, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            z2 = false;
        }
        return toBytes32(s2, z2);
    }

    @NotNull
    public static final byte[] toBytes64(long j2, boolean z2) {
        if (j2 <= 2147483647L && j2 >= -2147483648L) {
            if (j2 <= 32767 && j2 >= -32768) {
                return toBytes16((int) j2, z2);
            }
            return toBytes32((int) j2, z2);
        }
        byte[] bArr = new byte[8];
        if (z2) {
            bArr[0] = (byte) j2;
            bArr[1] = (byte) (j2 >> 8);
            bArr[2] = (byte) (j2 >> 16);
            bArr[3] = (byte) (j2 >> 24);
            bArr[4] = (byte) (j2 >> 32);
            bArr[5] = (byte) (j2 >> 40);
            bArr[6] = (byte) (j2 >> 48);
            bArr[7] = (byte) (j2 >> 56);
        } else {
            bArr[0] = (byte) (j2 >> 56);
            bArr[1] = (byte) (j2 >> 48);
            bArr[2] = (byte) (j2 >> 40);
            bArr[3] = (byte) (j2 >> 32);
            bArr[4] = (byte) (j2 >> 24);
            bArr[5] = (byte) (j2 >> 16);
            bArr[6] = (byte) (j2 >> 8);
            bArr[7] = (byte) j2;
        }
        return bArr;
    }

    public static /* synthetic */ byte[] toBytes64$default(long j2, boolean z2, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            z2 = false;
        }
        return toBytes64(j2, z2);
    }

    @NotNull
    public static final String toHexString(short s2, boolean z2, boolean z3, boolean z4) {
        return ByteArrayExtKt.toHexString(toBytes32(s2, z2), z3, z4);
    }

    public static /* synthetic */ String toHexString$default(short s2, boolean z2, boolean z3, boolean z4, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            z2 = false;
        }
        if ((i2 & 2) != 0) {
            z3 = true;
        }
        if ((i2 & 4) != 0) {
            z4 = true;
        }
        return toHexString(s2, z2, z3, z4);
    }

    public static /* synthetic */ byte[] toBytes32$default(int i2, boolean z2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            z2 = false;
        }
        return toBytes32(i2, z2);
    }

    @NotNull
    public static final String toHexString(int i2, boolean z2, boolean z3, boolean z4) {
        return ByteArrayExtKt.toHexString(toBytes32(i2, z2), z3, z4);
    }

    public static /* synthetic */ String toHexString$default(int i2, boolean z2, boolean z3, boolean z4, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            z2 = false;
        }
        if ((i3 & 2) != 0) {
            z3 = true;
        }
        if ((i3 & 4) != 0) {
            z4 = true;
        }
        return toHexString(i2, z2, z3, z4);
    }

    public static /* synthetic */ byte[] toBytes32$default(long j2, boolean z2, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            z2 = false;
        }
        return toBytes32(j2, z2);
    }

    @NotNull
    public static final String toHexString(long j2, boolean z2, boolean z3, boolean z4) {
        return ByteArrayExtKt.toHexString(toBytes64(j2, z2), z3, z4);
    }

    public static /* synthetic */ String toHexString$default(long j2, boolean z2, boolean z3, boolean z4, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            z2 = false;
        }
        if ((i2 & 2) != 0) {
            z3 = true;
        }
        if ((i2 & 4) != 0) {
            z4 = true;
        }
        return toHexString(j2, z2, z3, z4);
    }

    public static final int measureSize(int i2, int i3, int i4, int i5) {
        int mode = View.MeasureSpec.getMode(i2);
        int size = View.MeasureSpec.getSize(i2);
        if (mode != Integer.MIN_VALUE) {
            return mode != 1073741824 ? i3 : size;
        }
        return Math.min(i3 + i4 + i5, size);
    }

    @NotNull
    public static final String df(long j2, @NotNull String format) {
        Intrinsics.checkNotNullParameter(format, "format");
        DecimalFormat decimalFormat = new DecimalFormat(format);
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
        String format2 = decimalFormat.format(j2);
        Intrinsics.checkNotNullExpressionValue(format2, "df.format(this)");
        return format2;
    }

    public static final double maxDecimal(double d2, int i2) {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
        numberFormat.setGroupingUsed(false);
        numberFormat.setMaximumFractionDigits(i2);
        String format = numberFormat.format(d2);
        Intrinsics.checkNotNullExpressionValue(format, "df.format(this)");
        return Double.parseDouble(format);
    }

    @NotNull
    public static final String df(float f2, @NotNull String format) {
        Intrinsics.checkNotNullParameter(format, "format");
        DecimalFormat decimalFormat = new DecimalFormat(format);
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
        String format2 = decimalFormat.format(Float.valueOf(f2));
        Intrinsics.checkNotNullExpressionValue(format2, "df.format(this)");
        return format2;
    }

    @NotNull
    public static final String df(short s2, @NotNull String format) {
        Intrinsics.checkNotNullParameter(format, "format");
        DecimalFormat decimalFormat = new DecimalFormat(format);
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
        String format2 = decimalFormat.format(Short.valueOf(s2));
        Intrinsics.checkNotNullExpressionValue(format2, "df.format(this)");
        return format2;
    }

    @NotNull
    public static final String df(double d2, @NotNull String format) {
        Intrinsics.checkNotNullParameter(format, "format");
        DecimalFormat decimalFormat = new DecimalFormat(format);
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
        String format2 = decimalFormat.format(d2);
        Intrinsics.checkNotNullExpressionValue(format2, "df.format(this)");
        return format2;
    }

}
