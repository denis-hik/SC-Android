package sc.denishik.ru.ledApiBLU;

import org.junit.Test;
import static org.junit.Assert.*;

public class CommandsTest {
    @Test public void mapsSpeedToEffectRange() {
        assertEquals(50, Commands.speedPercent(15, 30));
        assertEquals(100, Commands.speedPercent(180, 150));
        assertEquals(0, Commands.speedPercent(-5, 30));
        assertEquals(0, Commands.speedPercent(Float.NaN, 30));
        assertEquals(50, Commands.speedPercent(75, 150));
        assertArrayEquals(new byte[]{126,4,2,100,-1,-1,-1,0,-17}, Commands.makeSpeedCommand(255));
    }
    @Test public void encryptedControllersKeepSpeedPacketPlain() {
        byte[] speed = Commands.makeSpeedCommand(42);
        assertArrayEquals(speed, Commands.forDevice(speed, true));
        assertArrayEquals(Commands.makePowerCommand(true), Commands.forDevice(Commands.makePowerCommand(true), false));
    }
    @Test public void encryptedPowerCanBeDecodedWithEmbeddedNonce() {
        byte[] original = Commands.makePowerCommand(true);
        byte[] encrypted = Commands.forDevice(original, true);
        assertEquals(21, encrypted.length);
        byte[] preset = {42,127,-63,-108,51,-34,69,-32,-117,17,92,-90};
        int[] nonce = new int[12];
        for (int i = 0; i < 12; i++) nonce[i] = (encrypted[9+i] ^ preset[i]) & 255;
        byte[] decoded = new byte[9];
        for (int i = 0; i < 9; i++) decoded[i] = (byte) (encrypted[i]
                ^ ((nonce[i]*27)&255) ^ ((nonce[(i+3)%12]+55)&255)
                ^ (nonce[(i+7)%12]>>2) ^ ((i*85)&255));
        assertArrayEquals(new byte[]{(byte)170,4,4,1,0,1,-1,0,85}, decoded);
        assertEquals(126, original[0]);
    }
}
