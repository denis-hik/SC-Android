package sc.denishik.ru.midwayApi.help;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KClass;

public final class Convert {
    @NotNull
    public static final Convert INSTANCE = new Convert();

    public static /* synthetic */ byte[] toBytes$default(Convert convert, short s, boolean z, boolean z2, int i, Object obj) {
        if ((i & 2) != 0) {
            z = false;
        }
        if ((i & 4) != 0) {
            z2 = false;
        }
        return convert.toBytes(s, z, z2);
    }

    public static /* synthetic */ String toHexString$default(Convert convert, byte[] bArr, boolean z, int i, int i2, int i3, Object obj) {
        if ((i3 & 2) != 0) {
            z = false;
        }
        if ((i3 & 4) != 0) {
            i = 0;
        }
        if ((i3 & 8) != 0) {
            i2 = bArr.length;
        }
        return convert.toHexString(bArr, z, i, i2);
    }

//    public final byte a(char c) {
//        return (byte) StringsKt__StringsKt.indexOf$default("0123456789ABCDEF", c, 0, false, 6, (Object) null);
//    }

//    public final double addHalf(double d) {
//        StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
//        Locale locale = Locale.ENGLISH;
//        Intrinsics.checkExpressionValueIsNotNull(locale, "Locale.ENGLISH");
//        String format = String.format(locale, "%.1f", Arrays.copyOf(new Object[]{Double.valueOf(d)}, 1));
//        Intrinsics.checkNotNullExpressionValue(format, "java.lang.String.format(locale, format, *args)");
//        int length = format.length();
//        int i = 0;
//        while (true) {
//            if (i >= length) {
//                break;
//            }
//            char charAt = format.charAt(i);
//            if (!((charAt == '.' || charAt == ',') ? false : true)) {
//                format = format.substring(0, i);
//                Intrinsics.checkNotNullExpressionValue(format, "(this as java.lang.Strin…ing(startIndex, endIndex)");
//                break;
//            }
//            i++;
//        }
//        double parseDouble = Double.parseDouble(format);
//        StringCompanionObject stringCompanionObject2 = StringCompanionObject.INSTANCE;
//        Locale locale2 = Locale.ENGLISH;
//        Intrinsics.checkExpressionValueIsNotNull(locale2, "Locale.ENGLISH");
//        String format2 = String.format(locale2, "%.1f", Arrays.copyOf(new Object[]{Double.valueOf(d)}, 1));
//        Intrinsics.checkNotNullExpressionValue(format2, "java.lang.String.format(locale, format, *args)");
//        double parseDouble2 = Double.parseDouble(String.takeLast(format2, 1));
//        return parseDouble + (parseDouble2 <= ((double) 3) ? 0 : parseDouble2 >= ((double) 7) ? 1 : 0.5d);
//    }

    @NotNull
    public final String binary(@NotNull byte[] bArr, int i) {
        Intrinsics.checkParameterIsNotNull(bArr, "bytes");
        String bigInteger = new BigInteger(1, bArr).toString(i);
        Intrinsics.checkExpressionValueIsNotNull(bigInteger, "BigInteger(1, bytes).toString(radix)");
        return bigInteger;
    }

    public final char byte2char(byte b) {
        return (char) b;
    }

    @NotNull
    public final String byteToBit(byte b) {
        StringBuilder sb = new StringBuilder();
        sb.append((b >> 7) & 1);
        sb.append((b >> 6) & 1);
        sb.append((b >> 5) & 1);
        sb.append((b >> 4) & 1);
        sb.append((b >> 3) & 1);
        sb.append((b >> 2) & 1);
        sb.append((b >> 1) & 1);
        sb.append((b >> 0) & 1);
        return sb.toString();
    }

    public final int dipToPx(@NotNull Context context, float f) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Resources resources = context.getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "context.resources");
        return (int) ((resources.getDisplayMetrics().density * f) + ((f >= ((float) 0) ? 1 : -1) * 0.5f));
    }

    public final int getBit(int i, int i2) {
        return (i & (1 << i2)) >> i2;
    }

    @NotNull
    public final byte[] getBoolArray(byte b) {
        byte[] bArr = new byte[1];
        bArr[0] = 8;
        for (int i = 7; i >= 0; i--) {
            bArr[i] = (byte) (b & 1);
            b = (byte) (b >> 1);
        }
        return bArr;
    }

    public final double getNumber(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "src");
        Matcher matcher = Pattern.compile("(\\d+\\.\\d+)").matcher(str);
        String str2 = "";
        if (matcher.find()) {
            String group = matcher.group(1);
            if (group != null) {
                str2 = group;
            }
        } else {
            Matcher matcher2 = Pattern.compile("(\\d+)").matcher(str);
            if (matcher2.find()) {
                if (matcher2.group(1) != null) {
                    str2 = matcher2.group(1);
                }
                Intrinsics.checkExpressionValueIsNotNull(str2, "if (m.group(1) == null) \"\" else m.group(1)");
            }
        }
        Double valueOf = Double.valueOf(str2);
        Intrinsics.checkExpressionValueIsNotNull(valueOf, "java.lang.Double.valueOf(str)");
        return valueOf.doubleValue();
    }

    @NotNull
    public final String getPrecisionFormat(int i) {
        return "%." + i + "f";
    }

    @NotNull
    public final String getString(@NotNull String str, @Nullable Object obj) {
        Intrinsics.checkParameterIsNotNull(str, "format");
        Locale locale = Locale.getDefault();
        Intrinsics.checkExpressionValueIsNotNull(locale, "Locale.getDefault()");
        String format = String.format(locale, str, Arrays.copyOf(new Object[]{obj}, 1));
        Intrinsics.checkNotNullExpressionValue(format, "java.lang.String.format(locale, format, *args)");
        return format;
    }

//    public final int measureSize(int i, int i2) {
//        int mode = View.MeasureSpec.getMode(i);
//        int size = View.MeasureSpec.getSize(i);
//        return mode == 1073741824 ? size : mode == Integer.MIN_VALUE ? RangesKt___RangesKt.coerceAtMost(i2, size) : i2;
//    }

    public final float measureTextHeight(@NotNull Paint paint) {
        Intrinsics.checkParameterIsNotNull(paint, "paint");
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return Math.abs(fontMetrics.ascent) - fontMetrics.descent;
    }

    public final int px2sp(@NotNull Context context, float f) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Resources resources = context.getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "context.resources");
        return (int) ((f / resources.getDisplayMetrics().scaledDensity) + 0.5f);
    }

    public final int sp2px(@NotNull Context context, float f) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Resources resources = context.getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "context.resources");
        return (int) ((f * resources.getDisplayMetrics().scaledDensity) + 0.5f);
    }

    @NotNull
    public final List<byte[]> splitToList(@NotNull byte[] bArr, int i) {
        Intrinsics.checkParameterIsNotNull(bArr, "src");
        ArrayList arrayList = new ArrayList();
        int length = bArr.length / i;
        for (int i2 = 0; i2 < length; i2++) {
            byte[] bArr2 = new byte[i];
            for (int i3 = 0; i3 < i; i3++) {
                bArr2[i3] = bArr[(i2 * i) + i3];
            }
            arrayList.add(bArr2);
        }
        if (bArr.length % i != 0) {
            int length2 = (bArr.length / i) * i;
            byte[] bArr3 = new byte[bArr.length % i];
            int length3 = bArr.length % i;
            for (int i4 = 0; i4 < length3; i4++) {
                bArr3[i4] = bArr[length2 + i4];
            }
            arrayList.add(bArr3);
        }
        return arrayList;
    }

    public <T> boolean toBoolean(T t) {
        Intrinsics.reifiedOperationMarker(4, "T");
        KClass orCreateKotlinClass = Reflection.getOrCreateKotlinClass(Object.class);
        if (Intrinsics.areEqual(orCreateKotlinClass, Reflection.getOrCreateKotlinClass(Float.TYPE))) {
            if (t == null) {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
            }
            if (((int) ((Float) t).floatValue()) != 1) {
                return false;
            }
        } else if (Intrinsics.areEqual(orCreateKotlinClass, Reflection.getOrCreateKotlinClass(Double.TYPE))) {
            if (t == null) {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.Double");
            }
            if (((int) ((Double) t).doubleValue()) != 1) {
                return false;
            }
        } else if (Intrinsics.areEqual(orCreateKotlinClass, Reflection.getOrCreateKotlinClass(Short.TYPE))) {
            if (t == null) {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.Short");
            }
            if (((Short) t).shortValue() != 1) {
                return false;
            }
        } else if (Intrinsics.areEqual(orCreateKotlinClass, Reflection.getOrCreateKotlinClass(Long.TYPE))) {
            if (t == null) {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.Long");
            }
            if (((int) ((Long) t).longValue()) != 1) {
                return false;
            }
        } else if (Intrinsics.areEqual(orCreateKotlinClass, Reflection.getOrCreateKotlinClass(Byte.TYPE))) {
            if (t == null) {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.Byte");
            }
            if (((Byte) t).byteValue() != 1) {
                return false;
            }
        } else if (Intrinsics.areEqual(orCreateKotlinClass, Reflection.getOrCreateKotlinClass(Character.TYPE))) {
            if (t == null) {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.Char");
            }
            if (((Character) t).charValue() != 1) {
                return false;
            }
        } else if (!Intrinsics.areEqual(orCreateKotlinClass, Reflection.getOrCreateKotlinClass(Integer.TYPE))) {
            return false;
        } else {
            if (t == null) {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.Int");
            }
            if (((Integer) t).intValue() != 1) {
                return false;
            }
        }
        return true;
    }

//    @Nullable
//    public final byte[] toBytes(@Nullable String str) {
//        if (str == null || Intrinsics.areEqual(str, "")) {
//            return null;
//        }
//        String upperCase = str.toUpperCase();
//        Intrinsics.checkNotNullExpressionValue(upperCase, "(this as java.lang.String).toUpperCase()");
//        int length = upperCase.length() / 2;
//        char[] charArray = upperCase.toCharArray();
//        Intrinsics.checkNotNullExpressionValue(charArray, "(this as java.lang.String).toCharArray()");
//        byte[] bArr = new byte[length];
//        for (int i = 0; i < length; i++) {
//            int i2 = i * 2;
//            bArr[i] = (byte) (a(charArray[i2 + 1]) | (a(charArray[i2]) << 4));
//        }
//        return bArr;
//    }

    @NotNull
    public final String toHexString(@NotNull byte[] bArr, boolean z, int i, int i2) {
        Intrinsics.checkParameterIsNotNull(bArr, "src");
        StringBuilder sb = new StringBuilder("");
        if (bArr.length == 0) {
            String sb2 = sb.toString();
            Intrinsics.checkExpressionValueIsNotNull(sb2, "stringBuilder.toString()");
            return sb2;
        }
        if (z) {
            for (int i3 = i2 + i; i3 < i; i3++) {
                String hexString = Integer.toHexString(bArr[i3] & 255);
                if (hexString.length() < 2) {
                    sb.append(0);
                }
                sb.append(hexString);
                sb.append(" ");
            }
        } else {
            int i4 = i2 + i;
            while (i < i4) {
                String hexString2 = Integer.toHexString(bArr[i] & 255);
                if (hexString2.length() < 2) {
                    sb.append(0);
                }
                sb.append(hexString2);
                sb.append(" ");
                i++;
            }
        }
        String sb3 = sb.toString();
        Intrinsics.checkExpressionValueIsNotNull(sb3, "stringBuilder.toString()");
        Locale locale = Locale.getDefault();
        Intrinsics.checkExpressionValueIsNotNull(locale, "Locale.getDefault()");
        Objects.requireNonNull(sb3, "null cannot be cast to non-null type java.lang.String");
        String upperCase = sb3.toUpperCase(locale);
        Intrinsics.checkNotNullExpressionValue(upperCase, "(this as java.lang.String).toUpperCase(locale)");
        return upperCase;
    }

    public final int toSign(byte b, byte b2) {
        return (b << 8) | (b2 & 255);
    }

    public final long toSign(byte b, byte b2, byte b3, byte b4) {
        return (b << 24) | ((b2 & 255) << 16) | ((b3 & 255) << 8) | (b4 & 255);
    }

    public final int toUnSign(byte b, byte b2) {
        return ((b & 255) << 8) | (b2 & 255);
    }

    public final long toUnSign(byte b, byte b2, byte b3, byte b4) {
        return ((b & 255) << 24) | ((b2 & 255) << 16) | ((b3 & 255) << 8) | (b4 & 255);
    }

    @NotNull
    public final String getString(@NotNull String str, @NotNull Object obj, @NotNull Object obj2) {
        Intrinsics.checkParameterIsNotNull(str, "format");
        Intrinsics.checkParameterIsNotNull(obj, "value1");
        Intrinsics.checkParameterIsNotNull(obj2, "value2");
        Locale locale = Locale.getDefault();
        Intrinsics.checkExpressionValueIsNotNull(locale, "Locale.getDefault()");
        String format = String.format(locale, str, Arrays.copyOf(new Object[]{obj, obj2}, 2));
        Intrinsics.checkNotNullExpressionValue(format, "java.lang.String.format(locale, format, *args)");
        return format;
    }

    @NotNull
    public final byte[] toBytes(short s, boolean z, boolean z2) {
        byte[] bArr = new byte[z2 ? 4 : 2];
        if (z) {
            if (z2) {
                bArr[2] = (byte) s;
                bArr[3] = (byte) (s >> 8);
            } else {
                bArr[0] = (byte) s;
                bArr[1] = (byte) (s >> 8);
            }
        } else if (z2) {
            bArr[2] = (byte) (s >> 8);
            bArr[3] = (byte) s;
        } else {
            bArr[0] = (byte) (s >> 8);
            bArr[1] = (byte) s;
        }
        return bArr;
    }
}