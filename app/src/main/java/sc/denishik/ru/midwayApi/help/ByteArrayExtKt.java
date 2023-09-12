package sc.denishik.ru.midwayApi.help;

import static sc.denishik.ru.midwayApi.help.other.joinToString$defaultC;
import static sc.denishik.ru.midwayApi.help.other.replace$default;

import android.annotation.SuppressLint;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.CharsKt;
import kotlin.text.StringsKt;

public class ByteArrayExtKt {
    public static final byte[] insertByteArrayBE(@NotNull byte[] bArr, @NotNull byte[] insertArray, int i2, int i3, int i4) {
        byte[] copyOfRange;
        byte[] copyOfRange2;
        byte[] copyOfRange3;
        byte[] plus;
        byte[] plus2;
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        Intrinsics.checkNotNullParameter(insertArray, "insertArray");
        copyOfRange = ArraysKt.copyOfRange(bArr, 0, i2);
        copyOfRange2 = ArraysKt.copyOfRange(bArr, i2, bArr.length);
        copyOfRange3 = ArraysKt.copyOfRange(insertArray, i3, i4 + i3);
        plus = ArraysKt.plus(copyOfRange, copyOfRange3);
        plus2 = ArraysKt.plus(plus, copyOfRange2);
        return plus2;
    }

    public static /* synthetic */ byte[] insertByteArrayBE$default(byte[] bArr, byte[] bArr2, int i2, int i3, int i4, int i5, Object obj) {
        if ((i5 & 2) != 0) {
            i2 = 0;
        }
        if ((i5 & 4) != 0) {
            i3 = 0;
        }
        if ((i5 & 8) != 0) {
            i4 = bArr2.length - i3;
        }
        return insertByteArrayBE(bArr, bArr2, i2, i3, i4);
    }

    @NotNull
    public static final byte[] insertByteArrayLE(@NotNull byte[] bArr, @NotNull byte[] insertArray, int i2, int i3, int i4) {
        byte[] copyOfRange;
        byte[] copyOfRange2;
        byte[] copyOfRange3;
        byte[] plus;
        byte[] plus2;
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        Intrinsics.checkNotNullParameter(insertArray, "insertArray");
        ArraysKt.reverse(insertArray);
        copyOfRange = ArraysKt.copyOfRange(bArr, 0, i2);
        copyOfRange2 = ArraysKt.copyOfRange(bArr, i2, bArr.length);
        copyOfRange3 = ArraysKt.copyOfRange(insertArray, i3, i4 + i3);
        plus = ArraysKt.plus(copyOfRange, copyOfRange3);
        plus2 = ArraysKt.plus(plus, copyOfRange2);
        return plus2;
    }

    public static /* synthetic */ byte[] insertByteArrayLE$default(byte[] bArr, byte[] bArr2, int i2, int i3, int i4, int i5, Object obj) {
        if ((i5 & 2) != 0) {
            i2 = 0;
        }
        if ((i5 & 4) != 0) {
            i3 = 0;
        }
        if ((i5 & 8) != 0) {
            i4 = bArr2.length - i3;
        }
        return insertByteArrayLE(bArr, bArr2, i2, i3, i4);
    }

    @NotNull
    public static final byte[] readByteArrayBE(@NotNull byte[] bArr, int i2, int i3) {
        byte[] copyOfRange;
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i2, 0, 0, 12, null);
        int i4 = i3 + i2;
        if (i4 > bArr.length) {
            i4 = bArr.length;
        }
        copyOfRange = ArraysKt.copyOfRange(bArr, i2, i4);
        return copyOfRange;
    }

    @NotNull
    public static final byte[] readByteArrayLE(@NotNull byte[] bArr, int i2, int i3) {
        byte[] reversedArray;
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i2, 0, 0, 12, null);
        reversedArray = ArraysKt.reversedArray(readByteArrayBE(bArr, i2, i3));
        return reversedArray;
    }

    public static final float readFloat16BE(@NotNull byte[] bArr, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        return Float.intBitsToFloat(readInt16BE(bArr, i2));
    }

    public static /* synthetic */ float readFloat16BE$default(byte[] bArr, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 0;
        }
        return readFloat16BE(bArr, i2);
    }

    public static final float readFloat16LE(@NotNull byte[] bArr, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        return Float.intBitsToFloat(readInt16LE(bArr, i2));
    }

    public static /* synthetic */ float readFloat16LE$default(byte[] bArr, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 0;
        }
        return readFloat16LE(bArr, i2);
    }

    public static final float readFloat32BE(@NotNull byte[] bArr, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        return Float.intBitsToFloat(readInt32BE(bArr, i2));
    }

    public static /* synthetic */ float readFloat32BE$default(byte[] bArr, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 0;
        }
        return readFloat32BE(bArr, i2);
    }

    public static final float readFloat32LE(@NotNull byte[] bArr, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        return Float.intBitsToFloat(readInt32LE(bArr, i2));
    }

    public static /* synthetic */ float readFloat32LE$default(byte[] bArr, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 0;
        }
        return readFloat32LE(bArr, i2);
    }

    public static final int readInt16BE(@NotNull byte[] bArr, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i2, 2, 0, 8, null);
        return (bArr[i2] << 8) + (bArr[i2 + 1] & 255);
    }

    public static /* synthetic */ int readInt16BE$default(byte[] bArr, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 0;
        }
        return readInt16BE(bArr, i2);
    }

    public static final int readInt16LE(@NotNull byte[] bArr, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i2, 2, 0, 8, null);
        return (bArr[i2 + 1] << 8) + (bArr[i2] & 255);
    }

    public static /* synthetic */ int readInt16LE$default(byte[] bArr, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 0;
        }
        return readInt16LE(bArr, i2);
    }

    public static final int readInt32BE(@NotNull byte[] bArr, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i2, 4, 0, 8, null);
        return (bArr[i2 + 3] & 255) | (bArr[i2] << 24) | ((bArr[i2 + 1] & 255) << 16) | ((bArr[i2 + 2] & 255) << 8);
    }

    public static /* synthetic */ int readInt32BE$default(byte[] bArr, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 0;
        }
        return readInt32BE(bArr, i2);
    }

    public static final int readInt32LE(@NotNull byte[] bArr, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i2, 4, 0, 8, null);
        return (bArr[i2] & 255) | (bArr[i2 + 3] << 24) | ((bArr[i2 + 2] & 255) << 16) | ((bArr[i2 + 1] & 255) << 8);
    }

    public static /* synthetic */ int readInt32LE$default(byte[] bArr, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 0;
        }
        return readInt32LE(bArr, i2);
    }

    public static final int readInt8(@NotNull byte[] bArr, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i2, 1, 0, 8, null);
        return bArr[i2];
    }

    public static /* synthetic */ int readInt8$default(byte[] bArr, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 0;
        }
        return readInt8(bArr, i2);
    }

    public static final short readShortBE(@NotNull byte[] bArr, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i2, 2, 0, 8, null);
        return (short) ((bArr[i2] << 8) + (bArr[i2 + 1] & 255));
    }

    public static /* synthetic */ short readShortBE$default(byte[] bArr, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 0;
        }
        return readShortBE(bArr, i2);
    }

    public static final short readShortLE(@NotNull byte[] bArr, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i2, 2, 0, 8, null);
        return (short) ((bArr[i2 + 1] << 8) + (bArr[i2] & 255));
    }

    public static /* synthetic */ short readShortLE$default(byte[] bArr, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 0;
        }
        return readShortLE(bArr, i2);
    }

    @NotNull
    public static final String readStringBE(@NotNull byte[] bArr, int i2, int i3, @NotNull String encoding, boolean z2) {
        String joinToString$default;
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        Intrinsics.checkNotNullParameter(encoding, "encoding");
        Locale locale = Locale.getDefault();
        Intrinsics.checkNotNullExpressionValue(locale, "getDefault()");
        String lowerCase = encoding.toLowerCase(locale);
        Intrinsics.checkNotNullExpressionValue(lowerCase, "this as java.lang.String).toLowerCase(locale)");
        if (Intrinsics.areEqual(lowerCase, "hex")) {
            return toHexString$default(readByteArrayBE(bArr, i2, i3), z2, false, 2, null);
        }
        if (Intrinsics.areEqual(lowerCase, "ascii")) {
            byte[] readByteArrayBE = readByteArrayBE(bArr, i2, i3);
            ArrayList arrayList = new ArrayList(readByteArrayBE.length);
            for (byte b2 : readByteArrayBE) {
                arrayList.add(Character.valueOf((char) b2));
            }
            joinToString$default = joinToString$defaultC(arrayList, z2 ? " " : "", null, null, 0, null, null, 62, null);
            return joinToString$default;
        }
        return "";
    }

    public static /* synthetic */ String readStringBE$default(byte[] bArr, int i2, int i3, String str, boolean z2, int i4, Object obj) {
        if ((i4 & 4) != 0) {
            str = "hex";
        }
        if ((i4 & 8) != 0) {
            Locale locale = Locale.getDefault();
            Intrinsics.checkNotNullExpressionValue(locale, "getDefault()");
            String lowerCase = str.toLowerCase(locale);
            Intrinsics.checkNotNullExpressionValue(lowerCase, "this as java.lang.String).toLowerCase(locale)");
            z2 = Intrinsics.areEqual(lowerCase, "hex");
        }
        return readStringBE(bArr, i2, i3, str, z2);
    }

    @NotNull
    public static final String readStringLE(@NotNull byte[] bArr, int i2, int i3, @NotNull String encoding, boolean z2) {
        String joinToString$default;
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        Intrinsics.checkNotNullParameter(encoding, "encoding");
        Locale locale = Locale.getDefault();
        Intrinsics.checkNotNullExpressionValue(locale, "getDefault()");
        String lowerCase = encoding.toLowerCase(locale);
        Intrinsics.checkNotNullExpressionValue(lowerCase, "this as java.lang.String).toLowerCase(locale)");
        if (Intrinsics.areEqual(lowerCase, "hex")) {
            return toHexString$default(readByteArrayLE(bArr, i2, i3), z2, false, 2, null);
        }
        if (Intrinsics.areEqual(lowerCase, "ascii")) {
            byte[] readByteArrayLE = readByteArrayLE(bArr, i2, i3);
            ArrayList arrayList = new ArrayList(readByteArrayLE.length);
            for (byte b2 : readByteArrayLE) {
                arrayList.add(Character.valueOf((char) b2));
            }
            joinToString$default = joinToString$defaultC(arrayList, z2 ? " " : "", null, null, 0, null, null, 62, null);
            return joinToString$default;
        }
        return "";
    }

    public static /* synthetic */ String readStringLE$default(byte[] bArr, int i2, int i3, String str, boolean z2, int i4, Object obj) {
        if ((i4 & 4) != 0) {
            str = "hex";
        }
        if ((i4 & 8) != 0) {
            Locale locale = Locale.getDefault();
            Intrinsics.checkNotNullExpressionValue(locale, "getDefault()");
            String lowerCase = str.toLowerCase(locale);
            Intrinsics.checkNotNullExpressionValue(lowerCase, "this as java.lang.String).toLowerCase(locale)");
            z2 = Intrinsics.areEqual(lowerCase, "hex");
        }
        return readStringLE(bArr, i2, i3, str, z2);
    }

    @SuppressLint({"SimpleDateFormat"})
    public static final String readTimeBE(@NotNull byte[] bArr, int i2, @NotNull String pattern) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        Intrinsics.checkNotNullParameter(pattern, "pattern");
        return new SimpleDateFormat(pattern).format(Long.valueOf(readUInt32BEL(bArr, i2) * 1000));
    }

    public static /* synthetic */ String readTimeBE$default(byte[] bArr, int i2, String str, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 0;
        }
        if ((i3 & 2) != 0) {
            str = "yyyy-MM-dd HH:mm:ss";
        }
        return readTimeBE(bArr, i2, str);
    }

    @SuppressLint({"SimpleDateFormat"})
    public static final String readTimeLE(@NotNull byte[] bArr, int i2, @NotNull String pattern) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        Intrinsics.checkNotNullParameter(pattern, "pattern");
        return new SimpleDateFormat(pattern).format(Long.valueOf(readUInt32LE(bArr, i2) * 1000));
    }

    public static /* synthetic */ String readTimeLE$default(byte[] bArr, int i2, String str, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 0;
        }
        if ((i3 & 2) != 0) {
            str = "yyyy-MM-dd HH:mm:ss";
        }
        return readTimeLE(bArr, i2, str);
    }

    public static final int readUInt16BE(@NotNull byte[] bArr, int i2) {
        return (bArr[i2 + 1] & 255) | ((bArr[i2] & 255) << 8);
    }

    public static /* synthetic */ int readUInt16BE$default(byte[] bArr, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 0;
        }
        return readUInt16BE(bArr, i2);
    }

    public static final int readUInt16LE(@NotNull byte[] bArr, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i2, 2, 0, 8, null);
        return (bArr[i2] & 255) | ((bArr[i2 + 1] & 255) << 8);
    }

    public static /* synthetic */ int readUInt16LE$default(byte[] bArr, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 0;
        }
        return readUInt16LE(bArr, i2);
    }

    public static final long readUInt32BE(@NotNull byte[] bArr, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i2, 4, 0, 8, null);
        return (bArr[i2 + 3] & 255) | ((bArr[i2] & 255) << 24) | ((bArr[i2 + 1] & 255) << 16) | ((bArr[i2 + 2] & 255) << 8);
    }

    public static /* synthetic */ long readUInt32BE$default(byte[] bArr, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 0;
        }
        return readUInt32BE(bArr, i2);
    }

    public static final int readUInt32BEI(@NotNull byte[] bArr, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i2, 4, 0, 8, null);
        return (bArr[i2 + 3] & 255) | ((bArr[i2] & 255) << 24) | ((bArr[i2 + 1] & 255) << 16) | ((bArr[i2 + 2] & 255) << 8);
    }

    public static /* synthetic */ int readUInt32BEI$default(byte[] bArr, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 0;
        }
        return readUInt32BEI(bArr, i2);
    }

    public static final long readUInt32BEL(@NotNull byte[] bArr, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i2, 4, 0, 8, null);
        return (bArr[i2 + 3] & 255) | ((bArr[i2] & 255) << 24) | ((bArr[i2 + 1] & 255) << 16) | ((bArr[i2 + 2] & 255) << 8);
    }

    public static /* synthetic */ long readUInt32BEL$default(byte[] bArr, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 0;
        }
        return readUInt32BEL(bArr, i2);
    }

    public static final long readUInt32LE(@NotNull byte[] bArr, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i2, 4, 0, 8, null);
        return (bArr[i2] & 255) | ((bArr[i2 + 3] & 255) << 24) | ((bArr[i2 + 2] & 255) << 16) | ((bArr[i2 + 1] & 255) << 8);
    }

    public static /* synthetic */ long readUInt32LE$default(byte[] bArr, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 0;
        }
        return readUInt32LE(bArr, i2);
    }

    public static final int readUInt8(@NotNull byte[] bArr, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i2, 1, 0, 8, null);
        return bArr[i2] & 255;
    }

    public static /* synthetic */ int readUInt8$default(byte[] bArr, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 0;
        }
        return readUInt8(bArr, i2);
    }

    public static final long readUnsigned(@NotNull byte[] byteArray, int i2, int i3, boolean z2) {
        Intrinsics.checkNotNullParameter(byteArray, "byteArray");
        long j2 = 0;
        for (int i4 = 0; i4 < i2; i4++) {
            int i5 = (z2 ? i4 : (i2 - 1) - i4) << 3;
            j2 |= (255 << i5) & (byteArray[i3 + i4] << i5);
        }
        return j2;
    }

    private static final void throwHexError(String str) {
        if (str.length() % 2 != 0) {
            throw new IllegalArgumentException("The value of \"hex\".length is out of range. It must be an even number");
        }
    }

    private static final void throwLenError(byte[] bArr, int i2) {
        if (i2 > 0 && i2 <= 4) {
            if (i2 > bArr.length) {
                throw new IllegalArgumentException("Attempt to write outside ByteArray bounds.");
            }
            return;
        }
        throw new IllegalArgumentException("The value of \"byteLength\" is out of range. It must be >= 1 and <= 4. Received " + i2);
    }

    private static final void throwOffsetError(byte[] bArr, int i2, int i3, int i4) {
        if (i2 <= (bArr.length - i3) - i4) {
            return;
        }
        throw new IllegalArgumentException("The value of \"offset\" is out of range. It must be >= 0 and <= " + ((bArr.length - i3) - i4) + ". Received " + i2);
    }

    public static /* synthetic */ void throwOffsetError$default(byte[] bArr, int i2, int i3, int i4, int i5, Object obj) {
        if ((i5 & 4) != 0) {
            i3 = 1;
        }
        if ((i5 & 8) != 0) {
            i4 = 0;
        }
        throwOffsetError(bArr, i2, i3, i4);
    }

    @NotNull
    public static final String toAsciiString(@NotNull byte[] bArr, boolean z2) {
        String joinToString$default;
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        ArrayList arrayList = new ArrayList(bArr.length);
        for (byte b2 : bArr) {
            arrayList.add(Character.valueOf((char) b2));
        }
        joinToString$default = joinToString$defaultC(arrayList, z2 ? " " : "", null, null, 0, null, null, 62, null);
        return joinToString$default;
    }

    public static /* synthetic */ String toAsciiString$default(byte[] bArr, boolean z2, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            z2 = false;
        }
        return toAsciiString(bArr, z2);
    }

    @NotNull
    public static final String toHexString(@NotNull byte[] bArr, int i2, int i3, boolean z2, boolean z3) {
        List drop;
        List take;
        byte[] byteArray;
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        drop = ArraysKt.drop(bArr, i3);
        if (i2 >= bArr.length - i3) {
            i2 = bArr.length - i3;
        }
        take = CollectionsKt.take(drop, i2);
        byteArray = CollectionsKt.toByteArray(take);
        return toHexString(byteArray, z2, z3);
    }

    public static /* synthetic */ String toHexString$default(byte[] bArr, int i2, int i3, boolean z2, boolean z3, int i4, Object obj) {
        if ((i4 & 1) != 0) {
            i2 = 0;
        }
        if ((i4 & 2) != 0) {
            i3 = 0;
        }
        if ((i4 & 4) != 0) {
            z2 = true;
        }
        if ((i4 & 8) != 0) {
            z3 = true;
        }
        return toHexString(bArr, i2, i3, z2, z3);
    }

    @NotNull
    public static final String toString(@NotNull byte[] bArr, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        String bigInteger = new BigInteger(1, bArr).toString(i2);
        Intrinsics.checkNotNullExpressionValue(bigInteger, "BigInteger(1, this).toString(radix)");
        return bigInteger;
    }

    @NotNull
    public static final byte[] writeByteArrayBE(@NotNull byte[] bArr, @NotNull byte[] byteArray, int i2, int i3) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        Intrinsics.checkNotNullParameter(byteArray, "byteArray");
        writeStringBE$default(bArr, toHexString$default(byteArray, false, false, 3, null), i2, i3, null, 8, null);
        return bArr;
    }

    public static /* synthetic */ byte[] writeByteArrayBE$default(byte[] bArr, byte[] bArr2, int i2, int i3, int i4, Object obj) {
        if ((i4 & 2) != 0) {
            i2 = 0;
        }
        if ((i4 & 4) != 0) {
            i3 = bArr2.length;
        }
        return writeByteArrayBE(bArr, bArr2, i2, i3);
    }

    @NotNull
    public static final byte[] writeByteArrayLE(@NotNull byte[] bArr, @NotNull byte[] byteArray, int i2, int i3) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        Intrinsics.checkNotNullParameter(byteArray, "byteArray");
        writeStringLE$default(bArr, toHexString$default(byteArray, false, false, 3, null), i2, i3, null, 8, null);
        return bArr;
    }

    public static /* synthetic */ byte[] writeByteArrayLE$default(byte[] bArr, byte[] bArr2, int i2, int i3, int i4, Object obj) {
        if ((i4 & 2) != 0) {
            i2 = 0;
        }
        if ((i4 & 4) != 0) {
            i3 = bArr2.length;
        }
        return writeByteArrayLE(bArr, bArr2, i2, i3);
    }

    @NotNull
    public static final byte[] writeFloatBE(@NotNull byte[] bArr, float f2, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i2, 4, 0, 8, null);
        writeInt32BE(bArr, Float.floatToIntBits(f2), i2);
        return bArr;
    }

    public static /* synthetic */ byte[] writeFloatBE$default(byte[] bArr, float f2, int i2, int i3, Object obj) {
        if ((i3 & 2) != 0) {
            i2 = 0;
        }
        return writeFloatBE(bArr, f2, i2);
    }

    @NotNull
    public static final byte[] writeFloatLE(@NotNull byte[] bArr, float f2, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i2, 4, 0, 8, null);
        writeInt32LE(bArr, Float.floatToIntBits(f2), i2);
        return bArr;
    }

    public static /* synthetic */ byte[] writeFloatLE$default(byte[] bArr, float f2, int i2, int i3, Object obj) {
        if ((i3 & 2) != 0) {
            i2 = 0;
        }
        return writeFloatLE(bArr, f2, i2);
    }

    @NotNull
    public static final byte[] writeInt16BE(@NotNull byte[] bArr, int i2, int i3) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i3, 2, 0, 8, null);
        bArr[i3] = (byte) ((65280 & i2) >>> 8);
        bArr[i3 + 1] = (byte) (i2 & 255);
        return bArr;
    }

    public static /* synthetic */ byte[] writeInt16BE$default(byte[] bArr, int i2, int i3, int i4, Object obj) {
        if ((i4 & 2) != 0) {
            i3 = 0;
        }
        return writeInt16BE(bArr, i2, i3);
    }

    @NotNull
    public static final byte[] writeInt16LE(@NotNull byte[] bArr, int i2, int i3) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i3, 2, 0, 8, null);
        bArr[i3] = (byte) (i2 & 255);
        bArr[i3 + 1] = (byte) ((i2 & 65280) >>> 8);
        return bArr;
    }

    public static /* synthetic */ byte[] writeInt16LE$default(byte[] bArr, int i2, int i3, int i4, Object obj) {
        if ((i4 & 2) != 0) {
            i3 = 0;
        }
        return writeInt16LE(bArr, i2, i3);
    }

    @NotNull
    public static final byte[] writeInt32BE(@NotNull byte[] bArr, long j2, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i2, 4, 0, 8, null);
        bArr[i2 + 3] = (byte) (255 & j2);
        bArr[i2 + 2] = (byte) ((65280 & j2) >>> 8);
        bArr[i2 + 1] = (byte) ((16711680 & j2) >>> 16);
        bArr[i2] = (byte) ((j2 & 4278190080L) >>> 24);
        return bArr;
    }

    public static /* synthetic */ byte[] writeInt32BE$default(byte[] bArr, long j2, int i2, int i3, Object obj) {
        if ((i3 & 2) != 0) {
            i2 = 0;
        }
        return writeInt32BE(bArr, j2, i2);
    }

    @NotNull
    public static final byte[] writeInt32LE(@NotNull byte[] bArr, long j2, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i2, 4, 0, 8, null);
        bArr[i2] = (byte) (255 & j2);
        bArr[i2 + 1] = (byte) ((65280 & j2) >>> 8);
        bArr[i2 + 2] = (byte) ((16711680 & j2) >>> 16);
        bArr[i2 + 3] = (byte) ((j2 & 4278190080L) >>> 24);
        return bArr;
    }

    public static /* synthetic */ byte[] writeInt32LE$default(byte[] bArr, long j2, int i2, int i3, Object obj) {
        if ((i3 & 2) != 0) {
            i2 = 0;
        }
        return writeInt32LE(bArr, j2, i2);
    }

    @NotNull
    public static final byte[] writeInt8(@NotNull byte[] bArr, int i2, int i3) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        throwOffsetError$default(bArr, i3, 0, 0, 12, null);
        bArr[i3] = (byte) i2;
        return bArr;
    }

    public static /* synthetic */ byte[] writeInt8$default(byte[] bArr, int i2, int i3, int i4, Object obj) {
        if ((i4 & 2) != 0) {
            i3 = 0;
        }
        return writeInt8(bArr, i2, i3);
    }

    @NotNull
    public static final byte[] writeStringBE(@NotNull byte[] bArr, @NotNull String str, int i2, @NotNull String encoding) {
        String joinToString$default;
        int digitToInt;
        String replace$default;
        int checkRadix;
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        Intrinsics.checkNotNullParameter(str, "str");
        Intrinsics.checkNotNullParameter(encoding, "encoding");
        throwOffsetError$default(bArr, i2, 0, 0, 12, null);
        Locale locale = Locale.getDefault();
        Intrinsics.checkNotNullExpressionValue(locale, "getDefault()");
        String lowerCase = encoding.toLowerCase(locale);
        Intrinsics.checkNotNullExpressionValue(lowerCase, "this as java.lang.String).toLowerCase(locale)");
        int i3 = 0;
        if (Intrinsics.areEqual(lowerCase, "hex")) {
            replace$default = replace$default(str, " ", "", false, 4, (Object) null);
            throwHexError(replace$default);
            int length = replace$default.length() / 2;
            while (i3 < length) {
                int i4 = i3 + i2;
                if (i4 < bArr.length) {
                    int i5 = i3 * 2;
                    String substring = replace$default.substring(i5, i5 + 2);
                    Intrinsics.checkNotNullExpressionValue(substring, "this as java.lang.String…ing(startIndex, endIndex)");
                    checkRadix = CharsKt.checkRadix(16);
                    bArr[i4] = (byte) Integer.parseInt(substring, checkRadix);
                }
                i3++;
            }
        } else if (Intrinsics.areEqual(lowerCase, "ascii")) {
            char[] charArray = str.toCharArray();
            Intrinsics.checkNotNullExpressionValue(charArray, "this as java.lang.String).toCharArray()");
            ArrayList arrayList = new ArrayList(charArray.length);
            int length2 = charArray.length;
            while (i3 < length2) {
                digitToInt = CharsKt.digitToInt(charArray[i3]);
                arrayList.add(Integer.valueOf(digitToInt));
                i3++;
            }
            joinToString$default = sc.denishik.ru.midwayApi.help.other.joinToString$defaultC(arrayList, "", null, null, 0, null, new Function1<Integer, CharSequence>() { // from class: com.zydtech.library.ext.ByteArrayExtKt$writeStringBE$hex$2
                @NotNull
                public final CharSequence invoke(int i6) {
                    int checkRadix2;
                    checkRadix2 = CharsKt.checkRadix(16);
                    String num = Integer.toString(i6, checkRadix2);
                    Intrinsics.checkNotNullExpressionValue(num, "toString(this, checkRadix(radix))");
                    return num;
                }

                @Override // kotlin.jvm.functions.Function1
                public /* bridge */ /* synthetic */ CharSequence invoke(Integer num) {
                    return invoke(num.intValue());
                }
            }, 30, null);
            writeStringBE(bArr, joinToString$default, i2, "hex");
        }
        return bArr;
    }

    public static /* synthetic */ byte[] writeStringBE$default(byte[] bArr, String str, int i2, String str2, int i3, Object obj) {
        if ((i3 & 2) != 0) {
            i2 = 0;
        }
        if ((i3 & 4) != 0) {
            str2 = "hex";
        }
        return writeStringBE(bArr, str, i2, str2);
    }

    @NotNull
    public static final byte[] writeStringLE(@NotNull byte[] bArr, @NotNull String str, int i2, @NotNull String encoding) {
        String joinToString$default;
        int digitToInt;
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        Intrinsics.checkNotNullParameter(str, "str");
        Intrinsics.checkNotNullParameter(encoding, "encoding");
        Locale locale = Locale.getDefault();
        Intrinsics.checkNotNullExpressionValue(locale, "getDefault()");
        String lowerCase = encoding.toLowerCase(locale);
        Intrinsics.checkNotNullExpressionValue(lowerCase, "this as java.lang.String).toLowerCase(locale)");
        if (Intrinsics.areEqual(lowerCase, "hex")) {
            writeStringBE(bArr, StringExtKT.reversalEvery2Charts$default(str, false, 1, null), i2, encoding);
        } else if (Intrinsics.areEqual(lowerCase, "ascii")) {
            char[] charArray = str.toCharArray();
            Intrinsics.checkNotNullExpressionValue(charArray, "this as java.lang.String).toCharArray()");
            ArrayList arrayList = new ArrayList(charArray.length);
            for (char c2 : charArray) {
                digitToInt = CharsKt.digitToInt(c2);
                arrayList.add(Integer.valueOf(digitToInt));
            }
            joinToString$default = joinToString$defaultC(arrayList, "", null, null, 0, null, new Function1<Integer, CharSequence>() { // from class: com.zydtech.library.ext.ByteArrayExtKt$writeStringLE$hex$2
                @NotNull
                public final CharSequence invoke(int i3) {
                    int checkRadix;
                    checkRadix = CharsKt.checkRadix(16);
                    String num = Integer.toString(i3, checkRadix);
                    Intrinsics.checkNotNullExpressionValue(num, "toString(this, checkRadix(radix))");
                    return num;
                }

                @Override // kotlin.jvm.functions.Function1
                public /* bridge */ /* synthetic */ CharSequence invoke(Integer num) {
                    return invoke(num.intValue());
                }
            }, 30, null);
            writeStringLE(bArr, joinToString$default, i2, "hex");
        }
        return bArr;
    }

    public static /* synthetic */ byte[] writeStringLE$default(byte[] bArr, String str, int i2, String str2, int i3, Object obj) {
        if ((i3 & 2) != 0) {
            i2 = 0;
        }
        if ((i3 & 4) != 0) {
            str2 = "hex";
        }
        return writeStringLE(bArr, str, i2, str2);
    }

    @SuppressLint({"SimpleDateFormat"})
    @NotNull
    public static final byte[] writeTimeBE(@NotNull byte[] bArr, @NotNull String time, int i2, @NotNull String pattern) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        Intrinsics.checkNotNullParameter(time, "time");
        Intrinsics.checkNotNullParameter(pattern, "pattern");
        try{
            writeInt32BE(bArr, new SimpleDateFormat(pattern).parse(time).getTime() / 1000, i2);
        } catch (Exception e) {

        }
        return bArr;
    }

    public static /* synthetic */ byte[] writeTimeBE$default(byte[] bArr, String str, int i2, String str2, int i3, Object obj) {
        if ((i3 & 2) != 0) {
            i2 = 0;
        }
        if ((i3 & 4) != 0) {
            str2 = "yyyy-MM-dd HH:mm:ss";
        }
        return writeTimeBE(bArr, str, i2, str2);
    }

    @SuppressLint({"SimpleDateFormat"})
    @NotNull
    public static final byte[] writeTimeLE(@NotNull byte[] bArr, @NotNull String time, int i2, @NotNull String pattern) {
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        Intrinsics.checkNotNullParameter(time, "time");
        Intrinsics.checkNotNullParameter(pattern, "pattern");
        try {

            writeInt32LE(bArr, new SimpleDateFormat(pattern).parse(time).getTime() / 1000, i2);
        } catch (Exception e) {

        }
        return bArr;
    }

    public static /* synthetic */ byte[] writeTimeLE$default(byte[] bArr, String str, int i2, String str2, int i3, Object obj) {
        if ((i3 & 2) != 0) {
            i2 = 0;
        }
        if ((i3 & 4) != 0) {
            str2 = "yyyy-MM-dd HH:mm:ss";
        }
        return writeTimeLE(bArr, str, i2, str2);
    }

    @NotNull
    public static final String toHexString(@NotNull byte[] bArr, final boolean z2, final boolean z3) {
        String joinToString$default;
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        joinToString$default = sc.denishik.ru.midwayApi.help.other.joinToString$default(bArr, (CharSequence) "", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) new Function1<Byte, CharSequence>() {

            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ CharSequence invoke(Byte b2) {
                return invoke(b2.byteValue());
            }

            @NotNull
            public final CharSequence invoke(byte b2) {
                int checkRadix;
                String padStart;
                String str;
                int checkRadix2;
                String padStart2;
                if (z3) {
                    checkRadix2 = CharsKt.checkRadix(16);
                    String num = Integer.toString(b2 & 255, checkRadix2);
                    Intrinsics.checkNotNullExpressionValue(num, "toString(this, checkRadix(radix))");
                    padStart2 = StringsKt.padStart(num, 2, '0');
                    Locale locale = Locale.getDefault();
                    Intrinsics.checkNotNullExpressionValue(locale, "getDefault()");
                    String upperCase = padStart2.toUpperCase(locale);
                    Intrinsics.checkNotNullExpressionValue(upperCase, "this as java.lang.String).toUpperCase(locale)");
                    str = z2 ? " " : "";
                    return upperCase + str;
                }
                checkRadix = CharsKt.checkRadix(16);
                String num2 = Integer.toString(b2 & 255, checkRadix);
                Intrinsics.checkNotNullExpressionValue(num2, "toString(this, checkRadix(radix))");
                padStart = StringsKt.padStart(num2, 2, '0');
                Locale locale2 = Locale.getDefault();
                Intrinsics.checkNotNullExpressionValue(locale2, "getDefault()");
                String lowerCase = padStart.toLowerCase(locale2);
                Intrinsics.checkNotNullExpressionValue(lowerCase, "this as java.lang.String).toLowerCase(locale)");
                str = z2 ? " " : "";
                return lowerCase + str;
            }
        }, 30);
        return joinToString$default;
    }

    public static /* synthetic */ String toHexString$default(byte[] bArr, boolean z2, boolean z3, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            z2 = true;
        }
        if ((i2 & 2) != 0) {
            z3 = true;
        }
        return toHexString(bArr, z2, z3);
    }

    public static /* synthetic */ byte[] writeStringBE$default(byte[] bArr, String str, int i2, int i3, String str2, int i4, Object obj) {
        if ((i4 & 8) != 0) {
            str2 = "hex";
        }
        return writeStringBE(bArr, str, i2, i3, str2);
    }

    public static /* synthetic */ byte[] writeStringLE$default(byte[] bArr, String str, int i2, int i3, String str2, int i4, Object obj) {
        if ((i4 & 8) != 0) {
            str2 = "hex";
        }
        return writeStringLE(bArr, str, i2, i3, str2);
    }

    @NotNull
    public static final byte[] writeStringLE(@NotNull byte[] bArr, @NotNull String str, int i2, int i3, @NotNull String encoding) {
        String joinToString$default;
        int digitToInt;
        String padEnd;
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        Intrinsics.checkNotNullParameter(str, "str");
        Intrinsics.checkNotNullParameter(encoding, "encoding");
        Locale locale = Locale.getDefault();
        Intrinsics.checkNotNullExpressionValue(locale, "getDefault()");
        String lowerCase = encoding.toLowerCase(locale);
        Intrinsics.checkNotNullExpressionValue(lowerCase, "this as java.lang.String).toLowerCase(locale)");
        if (Intrinsics.areEqual(lowerCase, "hex")) {
            int i4 = i3 * 2;
            padEnd = StringsKt.padEnd(StringExtKT.reversalEvery2Charts$default(str, false, 1, null), i4, '0');
            String substring = padEnd.substring(0, i4);
            Intrinsics.checkNotNullExpressionValue(substring, "this as java.lang.String…ing(startIndex, endIndex)");
            writeStringBE(bArr, substring, i2, i3, encoding);
        } else if (Intrinsics.areEqual(lowerCase, "ascii")) {
            char[] charArray = str.toCharArray();
            Intrinsics.checkNotNullExpressionValue(charArray, "this as java.lang.String).toCharArray()");
            ArrayList arrayList = new ArrayList(charArray.length);
            for (char c2 : charArray) {
                digitToInt = CharsKt.digitToInt(c2);
                arrayList.add(Integer.valueOf(digitToInt));
            }
            joinToString$default = joinToString$defaultC(arrayList, "", null, null, 0, null, new Function1<Integer, CharSequence>() {
                @NotNull
                public final CharSequence invoke(int i5) {
                    int checkRadix;
                    checkRadix = CharsKt.checkRadix(16);
                    String num = Integer.toString(i5, checkRadix);
                    Intrinsics.checkNotNullExpressionValue(num, "toString(this, checkRadix(radix))");
                    return num;
                }

                @Override // kotlin.jvm.functions.Function1
                public /* bridge */ /* synthetic */ CharSequence invoke(Integer num) {
                    return invoke(num.intValue());
                }
            }, 30, null);
            writeStringLE(bArr, joinToString$default, i2, i3, "hex");
        }
        return bArr;
    }

    @NotNull
    public static final byte[] writeStringBE(@NotNull byte[] bArr, @NotNull String str, int i2, int i3, @NotNull String encoding) {
        String joinToString$default;
        int digitToInt;
        String replace$default;
        String padStart;
        Intrinsics.checkNotNullParameter(bArr, "<this>");
        Intrinsics.checkNotNullParameter(str, "str");
        Intrinsics.checkNotNullParameter(encoding, "encoding");
        throwOffsetError$default(bArr, i2, i3, 0, 8, null);
        Locale locale = Locale.getDefault();
        Intrinsics.checkNotNullExpressionValue(locale, "getDefault()");
        String lowerCase = encoding.toLowerCase(locale);
        Intrinsics.checkNotNullExpressionValue(lowerCase, "this as java.lang.String).toLowerCase(locale)");
        if (Intrinsics.areEqual(lowerCase, "hex")) {
            replace$default = replace$default(str, " ", "", false, 4, null);
            int i4 = i3 * 2;
            padStart = StringsKt.padStart(replace$default, i4, '0');
            String substring = padStart.substring(0, i4);
            Intrinsics.checkNotNullExpressionValue(substring, "this as java.lang.String…ing(startIndex, endIndex)");
            throwHexError(substring);
            writeStringBE$default(bArr, substring, i2, null, 4, null);
        } else if (Intrinsics.areEqual(lowerCase, "ascii")) {
            char[] charArray = str.toCharArray();
            Intrinsics.checkNotNullExpressionValue(charArray, "this as java.lang.String).toCharArray()");
            ArrayList arrayList = new ArrayList(charArray.length);
            for (char c2 : charArray) {
                digitToInt = CharsKt.digitToInt(c2);
                arrayList.add(Integer.valueOf(digitToInt));
            }
            joinToString$default = joinToString$defaultC(arrayList, "", null, null, 0, null, new Function1<Integer, CharSequence>() {
                @NotNull
                public final CharSequence invoke(int i5) {
                    int checkRadix;
                    checkRadix = CharsKt.checkRadix(16);
                    String num = Integer.toString(i5, checkRadix);
                    Intrinsics.checkNotNullExpressionValue(num, "toString(this, checkRadix(radix))");
                    return num;
                }

                @Override // kotlin.jvm.functions.Function1
                public /* bridge */ /* synthetic */ CharSequence invoke(Integer num) {
                    return invoke(num.intValue());
                }
            }, 30, null);
            writeStringBE(bArr, joinToString$default, i2, i3, "hex");
        }
        return bArr;
    }

}
