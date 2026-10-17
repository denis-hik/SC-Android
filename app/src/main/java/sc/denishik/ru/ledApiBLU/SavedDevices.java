package sc.denishik.ru.ledApiBLU;

import org.json.*;
import java.util.*;

/** Persistence contains only controller addresses and display names. */
final class SavedDevices {
    static LinkedHashMap<String, String> decode(String json) {
        LinkedHashMap<String, String> devices = new LinkedHashMap<>();
        try {
            JSONArray data = new JSONArray(json);
            for (int i = 0; i < data.length(); i++) {
                JSONObject item = data.optJSONObject(i);
                if (item == null) continue;
                String address = item.optString("address", "").toUpperCase(Locale.ROOT);
                if (address.matches("([0-9A-F]{2}:){5}[0-9A-F]{2}")) devices.put(address, item.optString("name", "LED"));
            }
        } catch (JSONException ignored) { }
        return devices;
    }
    static String encode(Map<String, String> devices) {
        JSONArray data = new JSONArray();
        for (Map.Entry<String, String> item : devices.entrySet()) {
            try { data.put(new JSONObject().put("address", item.getKey()).put("name", item.getValue())); }
            catch (JSONException ignored) { }
        }
        return data.toString();
    }
}
