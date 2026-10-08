package sc.denishik.ru.mapsApi;
import org.junit.Test;
import static org.junit.Assert.*;
public class SpeedometerDataTest {
    @Test public void kugooLevelsMatchParkingAndThreeGears() {
        assertEquals("P", SpeedometerData.gear("level_1"));
        assertEquals("1", SpeedometerData.gear("level_2"));
        assertEquals("2", SpeedometerData.gear("level_3"));
        assertEquals("3", SpeedometerData.gear("level_4"));
        assertEquals("—", SpeedometerData.gear("level_9"));
    }
    @Test public void voltageUsesSchemaScaleAndNeverBatteryPercentage() throws Exception {
        org.json.JSONObject property = new org.json.JSONObject().put("type", "value").put("scale", 1);
        org.json.JSONObject definition = new org.json.JSONObject().put("id", 55).put("code", "battery_voltage").put("property", property);
        org.json.JSONObject profile = new org.json.JSONObject().put("_product", new org.json.JSONObject().put("schema", new org.json.JSONArray().put(definition)));
        sc.denishik.ru.tuya.KugooTelemetry telemetry = new sc.denishik.ru.tuya.KugooTelemetry(profile);
        assertEquals(54.6, telemetry.voltage(new org.json.JSONObject().put("55", 546)), .001);
        assertTrue(Double.isNaN(telemetry.voltage(new org.json.JSONObject().put("battery_percentage", 41))));
        assertTrue(Double.isNaN(telemetry.voltage(new org.json.JSONObject().put("55", 0))));
    }
}
