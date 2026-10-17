package sc.denishik.ru.ledApiBLU;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class SavedDevicesTest {
    @Test public void multipleDevicesSurviveRestartAndForget() {
        Map<String, String> devices = new LinkedHashMap<>();
        devices.put("AA:BB:CC:DD:EE:01", "ELK-front");
        devices.put("AA:BB:CC:DD:EE:02", "ELK-rear");
        Map<String, String> restored = SavedDevices.decode(SavedDevices.encode(devices));
        assertEquals(devices, restored);
        restored.remove("AA:BB:CC:DD:EE:01");
        Map<String, String> nextStart = SavedDevices.decode(SavedDevices.encode(restored));
        assertEquals(1, nextStart.size());
        assertEquals("ELK-rear", nextStart.get("AA:BB:CC:DD:EE:02"));
    }
    @Test public void invalidRecordsDoNotDiscardValidDevices() {
        assertEquals(1, SavedDevices.decode("[null,{}, {\"address\":\"invalid\"}, {\"address\":\"aa:bb:cc:dd:ee:01\"}]").size());
        assertTrue(SavedDevices.decode("broken").isEmpty());
    }
    @Test public void sameAddressIsNotConnectedTwice() {
        assertEquals(1, SavedDevices.decode("[{\"address\":\"aa:bb:cc:dd:ee:01\"},{\"address\":\"AA:BB:CC:DD:EE:01\"}]").size());
    }
}
