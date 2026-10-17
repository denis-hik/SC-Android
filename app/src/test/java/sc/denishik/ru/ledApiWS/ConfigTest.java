package sc.denishik.ru.ledApiWS;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ConfigTest {
    @Test public void speedScaleAndParking() {
        int previous = Config.TARGET_SPEED;
        try {
            Config.TARGET_SPEED = 30;
            assertEquals(85.0, Config.ledValue(10, false), 0.00001);
            assertEquals(255.0, Config.ledValue(31, false), 0.00001);
            Config.TARGET_SPEED = 150;
            assertEquals(127.5, Config.ledValue(75, false), 0.00001);
            assertEquals(255.0, Config.ledValue(150, false), 0.00001);
            assertEquals(0.0, Config.ledValue(75, true), 0.00001);
            assertEquals(0.0, Config.ledValue(-1, false), 0.00001);
            assertEquals(0.0, Config.ledValue(Float.NaN, false), 0.00001);
        } finally { Config.TARGET_SPEED = previous; }
    }
}
