package sc.denishik.ru.midwayApi.help;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import kotlin.jvm.internal.Intrinsics;

public class CRC16 {
    @NotNull
    public static final CRC16 INSTANCE = new CRC16();

    private CRC16() {
    }

    public final int ARC(@NotNull byte[] data, int i2, int i3) {
        Intrinsics.checkNotNullParameter(data, "data");
        return CRC(32773, 0, data, i2, i3, true, true, 0);
    }

    public final int AUG_CCITT(@NotNull byte[] data, int i2, int i3) {
        Intrinsics.checkNotNullParameter(data, "data");
        return CRC(4129, 7439, data, i2, i3, false, false, 0);
    }

    public final int CCITT_FALSE(@NotNull byte[] data, int i2, int i3) {
        Intrinsics.checkNotNullParameter(data, "data");
        return CRC(4129, 65535, data, i2, i3, false, false, 0);
    }

    public final int CCITT_Kermit(@NotNull byte[] data, int i2, int i3) {
        Intrinsics.checkNotNullParameter(data, "data");
        return CRC(4129, 0, data, i2, i3, true, true, 0);
    }

    public final int CRC(int i2, int i3, @NonNull @NotNull byte[] data, int i4, int i5, boolean z2, boolean z3, int i6) {
        Intrinsics.checkNotNullParameter(data, "data");
        for (int i7 = i4; i7 < i4 + i5 && i7 < data.length; i7++) {
            byte b2 = data[i7];
            for (int i8 = 0; i8 < 8; i8++) {
                boolean z4 = (((b2 & 255) >> (7 - (z2 ? 7 - i8 : i8))) & 1) == 1;
                boolean z5 = ((i3 >> 15) & 1) == 1;
                i3 <<= 1;
                if (z4 ^ z5) {
                    i3 ^= i2;
                }
            }
        }
        return z3 ? (Integer.reverse(i3) >>> 16) ^ i6 : (i3 ^ i6) & 65535;
    }

    public final int MAXIM(@NotNull byte[] data, int i2, int i3) {
        Intrinsics.checkNotNullParameter(data, "data");
        return CRC(32773, 0, data, i2, i3, true, true, 65535);
    }

    public final int MCRF4XX(@NotNull byte[] data, int i2, int i3) {
        Intrinsics.checkNotNullParameter(data, "data");
        return CRC(4129, 65535, data, i2, i3, true, true, 0);
    }

    public final int MODBUS(@NotNull byte[] data, int i2, int i3) {
        Intrinsics.checkNotNullParameter(data, "data");
        return CRC(32773, 65535, data, i2, i3, true, true, 0);
    }

    public final int XMODEM(@NotNull byte[] data, int i2, int i3) {
        Intrinsics.checkNotNullParameter(data, "data");
        return CRC(4129, 0, data, i2, i3, false, false, 0);
    }

}
