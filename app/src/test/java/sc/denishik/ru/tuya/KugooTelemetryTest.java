package sc.denishik.ru.tuya;

import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.Test;
import static org.junit.Assert.*;

public class KugooTelemetryTest {
    private KugooTelemetry parking(String mode, String... levels) throws Exception {
        JSONObject property = new JSONObject().put("type", "enum").put("range", new JSONArray(levels));
        JSONObject definition = new JSONObject().put("id", "44").put("code", "level")
                .put("mode", mode).put("property", property.toString());
        return new KugooTelemetry(new JSONObject().put("_product",
                new JSONObject().put("schema", new JSONArray().put(definition).toString())));
    }
    @Test public void parkingUsesLevelEnumFromSchema() throws Exception {
        KugooTelemetry mapper = parking("rw", "level_1", "level_2", "level_3", "level_4");
        assertEquals("44", mapper.levelDpId());
        assertEquals("level_1", mapper.parkingCommand(true).getString("44"));
        assertEquals("level_4", mapper.parkingCommand(false).getString("44"));
        assertEquals(1, mapper.parkingCommand(true).length());
    }
    @Test public void parkingRejectsReadonlyOrIncompleteEnum() throws Exception {
        assertFalse(parking("ro", "level_1", "level_4").canControlParking());
        assertFalse(parking("rw", "level_1", "level_2").canControlParking());
        assertFalse(new KugooTelemetry(new JSONObject()).canControlParking());
    }
    @Test(expected = IllegalStateException.class)
    public void parkingDoesNotSendUnsupportedLevel() throws Exception {
        parking("rw", "level_1", "level_2").parkingCommand(false);
    }
    private KugooTelemetry telemetry() throws Exception {
        JSONObject speed = new JSONObject().put("id", 2).put("code", "speed")
                .put("property", new JSONObject().put("scale", 1));
        JSONObject lamp = new JSONObject().put("id", "8").put("code", "switch_led");
        JSONObject product = new JSONObject().put("schemaInfo", new JSONObject()
                .put("schema", new JSONArray().put(speed).put(lamp).toString()));
        return new KugooTelemetry(new JSONObject().put("_product", product));
    }
    @Test public void scalesValuesUsingImportedSchemaAndPreservesBooleans() throws Exception {
        JSONObject actual = telemetry().decoded(new JSONObject().put("2", 123).put("8", true));
        assertEquals(12.3, actual.getDouble("speed"), 0.0001);
        assertTrue(actual.getBoolean("switch_led"));
    }
    @Test public void unknownDpIsDisplayedButNeverGuessedAsSpeed() throws Exception {
        KugooTelemetry mapper = new KugooTelemetry(new JSONObject());
        JSONObject actual = mapper.decoded(new JSONObject().put("2", 100));
        assertFalse(actual.has("speed"));
        assertTrue(mapper.describe(new JSONObject().put("2", 100)).contains("DP 2: 100"));
    }
    @Test public void partialNotificationDoesNotFabricateOtherValues() throws Exception {
        JSONObject actual = telemetry().decoded(new JSONObject().put("8", false));
        assertEquals(1, actual.length());
        assertFalse(actual.has("speed"));
    }
    private KugooTelemetry lamp(String mode, String type) throws Exception {
        JSONObject definition = new JSONObject().put("id", 42).put("code", "headlight_switch")
                .put("mode", mode).put("property", new JSONObject().put("type", type));
        return new KugooTelemetry(new JSONObject().put("_product",
                new JSONObject().put("schema", new JSONArray().put(definition).toString())));
    }
    @Test public void headlightUsesWritableBooleanDpFromSchema() throws Exception {
        assertEquals("42", lamp("rw", "bool").headlightDpId());
        JSONObject on = lamp("rw", "bool").headlightCommand(true);
        assertEquals(1, on.length());
        assertTrue(on.getBoolean("42"));
        assertFalse(lamp("rw", "bool").headlightCommand(false).getBoolean("42"));
    }
    @Test public void readOnlyOrNonBooleanHeadlightCannotBeWritten() throws Exception {
        assertNull(lamp("ro", "bool").headlightDpId());
        assertNull(lamp("rw", "enum").headlightDpId());
        assertNull(new KugooTelemetry(new JSONObject()).headlightDpId());
    }
    @Test(expected = IllegalStateException.class)
    public void missingHeadlightDoesNotSendGuessedDp() throws Exception {
        new KugooTelemetry(new JSONObject()).headlightCommand(true);
    }
    @Test public void officialStringPropertySupportsLightAndScaling() throws Exception {
        JSONArray schema = new JSONArray()
                .put(new JSONObject().put("id", "8").put("code", "headlight_switch").put("mode", "rw")
                        .put("property", "{\"type\":\"bool\"}"))
                .put(new JSONObject().put("id", "2").put("code", "speed")
                        .put("property", "{\"type\":\"value\",\"scale\":1}"));
        KugooTelemetry mapper = new KugooTelemetry(new JSONObject().put("_product", new JSONObject().put("schema", schema.toString())));
        assertEquals("8", mapper.headlightDpId());
        assertEquals(12.3, mapper.decoded(new JSONObject().put("2", 123)).getDouble("speed"), 0.0001);
    }
}
