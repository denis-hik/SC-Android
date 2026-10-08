package sc.denishik.ru.tuya;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.io.IOException;
import java.util.List;
import static org.junit.Assert.*;

public class KugooOfflineImportTest {
    @Test public void exportedFilePreservesKeysAddressAndSchema() throws Exception {
        JSONObject original = device("one").put("secKey", "test-secondary")
                .put("_bleAddress", "00:11:22:33:44:55")
                .put("_product", new JSONObject().put("schema", new JSONArray()
                        .put(new JSONObject().put("id", 1).put("code", "level"))));
        List<JSONObject> restored = KugooOfflineImport.fromJson(KugooTransferFile.encode(
                java.util.Arrays.asList(original, device("two"))));
        assertEquals(2, restored.size());
        JSONObject first = restored.get(0);
        assertEquals(original.getString("localKey"), first.getString("localKey"));
        assertEquals("test-secondary", first.getString("secKey"));
        assertEquals("00:11:22:33:44:55", first.getString("_bleAddress"));
        assertEquals("level", first.getJSONObject("_product").getJSONArray("schema").getJSONObject(0).getString("code"));
    }
    private JSONObject device(String id) throws Exception {
        return new JSONObject().put("devId", id).put("uuid", "uuid-" + id)
                .put("productId", "test-product").put("localKey", "test-only-key");
    }
    @Test public void extractsNestedAndStringEncodedDeviceRecords() throws Exception {
        JSONObject source = new JSONObject().put("home", new JSONArray().put(device("one"))
                .put(new JSONObject().put("cache", device("two").toString())));
        List<JSONObject> devices = KugooOfflineImport.fromJson(source.toString());
        assertEquals(2, devices.size());
        assertEquals("one", devices.get(0).getString("devId"));
        assertEquals("two", devices.get(1).getString("devId"));
    }
    @Test public void doesNotCombineCredentialsFromDifferentDevices() throws Exception {
        JSONObject first = device("one"); first.remove("localKey");
        JSONObject second = device("two"); second.remove("uuid");
        try { KugooOfflineImport.fromJson(new JSONArray().put(first).put(second).toString()); fail(); }
        catch (IOException expected) { }
    }
    @Test public void rejectsNullAndBlankKeys() throws Exception {
        for (Object value : new Object[]{JSONObject.NULL, "", "  ", "null", 42}) {
            try { KugooOfflineImport.fromJson(device("one").put("localKey", value).toString()); fail(); }
            catch (IOException expected) { }
        }
    }
    @Test public void deduplicatesDevicesAndRetainsProtocolMetadata() throws Exception {
        JSONObject object = device("one").put("secKey", "test-secondary-key").put("dps", new JSONObject().put("101", 0));
        List<JSONObject> devices = KugooOfflineImport.fromJson(new JSONArray().put(object).put(object).toString());
        assertEquals(1, devices.size());
        assertEquals("test-secondary-key", devices.get(0).getString("secKey"));
        assertEquals(0, devices.get(0).getJSONObject("dps").getInt("101"));
    }
    @Test public void matchesProductMetadataByProductId() throws Exception {
        JSONObject product = new JSONObject().put("id", "test-product").put("capability", 1024)
                .put("schemaInfo", new JSONObject());
        JSONObject other = new JSONObject().put("id", "other-product").put("capability", 1)
                .put("schemaInfo", new JSONObject());
        JSONObject source = new JSONObject().put("devices", new JSONArray().put(device("one")))
                .put("products", new JSONArray().put(other).put(product));
        List<JSONObject> devices = KugooOfflineImport.fromJson(source.toString());
        assertEquals("test-product", devices.get(0).getJSONObject("_product").getString("id"));
        assertEquals(1024, devices.get(0).getJSONObject("_product").getInt("capability"));
    }
    @Test public void rejectsExcessiveNestingAndOversizedInput() throws Exception {
        JSONObject nested = device("one");
        for (int i = 0; i < 20; i++) nested = new JSONObject().put("next", nested);
        try { KugooOfflineImport.fromJson(nested.toString()); fail(); } catch (IOException expected) { }
        char[] huge = new char[2 * 1024 * 1024 + 1];
        java.util.Arrays.fill(huge, ' ');
        try { KugooOfflineImport.fromJson(new String(huge)); fail(); } catch (IOException expected) { }
    }
}
