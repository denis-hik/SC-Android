package sc.denishik.ru.ledApiBLU;

import kotlin.ranges.ClosedRange;
import kotlin.ranges.IntRange;
import kotlin.ranges.RangesKt;

public class Commands {
    public static int speedPercent(float speed, int target) {
        if (Float.isNaN(speed) || Float.isInfinite(speed)) return 0;
        return Math.round(Math.max(0, Math.min(speed / Math.max(30, Math.min(150, target)), 1)) * 100);
    }
    public static byte[] makeSpeedCommand(int percent) {
        return new byte[]{126, 4, 2, (byte) Math.max(0, Math.min(100, percent)), -1, -1, -1, 0, -17};
    }
    public static byte[] forDevice(byte[] command, boolean encrypted) {
        if (!encrypted || (command[2] != 1 && command[2] != 3 && command[2] != 4)) return command.clone();
        byte[] plain = command.clone(); plain[0] = (byte) 0xaa; plain[8] = 0x55;
        byte[] random = new byte[12]; new java.security.SecureRandom().nextBytes(random);
        byte[] preset = {42,127,-63,-108,51,-34,69,-32,-117,17,92,-90,9,-14,125,-72};
        byte[] output = new byte[21];
        for (int i = 0; i < 9; i++) {
            int stream = (((random[i] & 255) * 27) & 255)
                    ^ (((random[(i + 3) % 12] & 255) + 55) & 255)
                    ^ ((random[(i + 7) % 12] & 255) >> 2) ^ ((i * 85) & 255);
            output[i] = (byte) (plain[i] ^ stream);
        }
        for (int i = 0; i < 12; i++) output[9 + i] = (byte) (random[i] ^ preset[i]);
        return output;
    }
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
