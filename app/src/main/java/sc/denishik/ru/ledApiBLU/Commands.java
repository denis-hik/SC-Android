package sc.denishik.ru.ledApiBLU;

import kotlin.ranges.ClosedRange;
import kotlin.ranges.IntRange;
import kotlin.ranges.RangesKt;

public class Commands {
    static public byte[] makeMicPowerCommand(boolean z) {
        return new byte[]{126, 4, 7, z ? (byte) 1 : (byte) 0, -1, -1, -1, 0, -17};
    }

    public final byte[] createColorCommand(int i, int i2, int i3) {
        return new byte[]{126, 7, 5, 3, (byte) i, (byte) i2, (byte) i3, 16, -17};
    }

    static public byte[] makePowerCommand(boolean z) {
        return new byte[]{126, 4, 4, z ? (byte) 1 : (byte) 0, 0, z ? (byte) 1 : (byte) 0, -1, 0, -17};
    }


    public byte[] makeBrightnessCommand(int i) {
        return new byte[]{126, 4, 1, (byte) RangesKt.coerceIn(i, (ClosedRange<Integer>) new IntRange(0, 100)), -1, -1, -1, 0, -17};
    }


}
