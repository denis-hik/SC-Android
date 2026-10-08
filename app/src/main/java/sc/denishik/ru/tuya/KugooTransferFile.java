package sc.denishik.ru.tuya;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.io.IOException;

/** Portable JSON: preserves binding keys and product schema, without device-local encryption. */
final class KugooTransferFile {
    static String encode(List<JSONObject> profiles) throws Exception {
        JSONArray devices = new JSONArray();
        for (JSONObject profile : profiles) devices.put(profile);
        String json = new JSONObject().put("format", "sc-kugoo-connections").put("version", 1)
                .put("profiles", devices).toString(2);
        if (json.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 2 * 1024 * 1024)
            throw new IOException("Файл слишком большой");
        return json;
    }
}
