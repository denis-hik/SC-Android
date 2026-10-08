package sc.denishik.ru.tuya;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/** Units and DP meanings come from the imported product schema, never from a guessed DP number. */
public final class KugooTelemetry {
    private final Map<String, JSONObject> schema = new LinkedHashMap<>();
    public KugooTelemetry(JSONObject profile) { collect(profile.optJSONObject("_product"), 0); collect(profile.optJSONObject("productBean"), 0); }
    private void collect(Object value, int depth) {
        if (depth > 12 || value == null) return;
        try {
            if (value instanceof String) {
                String text = ((String) value).trim();
                if (text.startsWith("{") || text.startsWith("[")) collect(new JSONTokener(text).nextValue(), depth + 1);
            } else if (value instanceof JSONArray) {
                JSONArray list = (JSONArray) value;
                for (int i = 0; i < list.length(); i++) collect(list.get(i), depth + 1);
            } else if (value instanceof JSONObject) {
                JSONObject object = (JSONObject) value;
                if (object.has("id") && object.has("code")) schema.put(String.valueOf(object.get("id")), object);
                Iterator<String> keys = object.keys();
                while (keys.hasNext()) collect(object.get(keys.next()), depth + 1);
            }
        } catch (Exception ignored) { }
    }
    public JSONObject decoded(JSONObject dps) {
        JSONObject result = new JSONObject();
        Iterator<String> keys = dps.keys();
        while (keys.hasNext()) {
            String id = keys.next();
            JSONObject definition = schema.get(id);
            if (definition == null) continue;
            try {
                Object value = dps.get(id);
                JSONObject property = propertyOf(definition);
                if (value instanceof Number && property != null) {
                    int scale = property.optInt("scale", 0);
                    if (scale >= 0 && scale <= 6) value = ((Number) value).doubleValue() / Math.pow(10, scale);
                }
                result.put(definition.getString("code"), value);
            } catch (Exception ignored) { }
        }
        return result;
    }
    public String headlightDpId() {
        for (Map.Entry<String, JSONObject> entry : schema.entrySet()) {
            JSONObject definition = entry.getValue();
            String code = definition.optString("code");
            if (!"headlight_switch".equals(code) && !"switch_led".equals(code)) continue;
            JSONObject property = propertyOf(definition);
            if (property != null && "bool".equals(property.optString("type"))
                    && definition.optString("mode").contains("w")) return entry.getKey();
        }
        return null;
    }

    public double voltage(JSONObject dps) {
        JSONObject values = decoded(dps);
        for (String code : new String[]{"voltage", "battery_voltage", "total_voltage", "voltage_current"}) {
            Object value = values.opt(code);
            if (value instanceof Number) {
                double volts = ((Number) value).doubleValue();
                if (!Double.isNaN(volts) && !Double.isInfinite(volts) && volts > 0) return volts;
            }
        }
        return Double.NaN;
    }

    private static JSONObject propertyOf(JSONObject definition) {
        for (String key : new String[]{"property", "typeSpec"}) {
            Object value = definition.opt(key);
            if (value instanceof JSONObject) return (JSONObject) value;
            if (value instanceof String) {
                try { return new JSONObject((String) value); }
                catch (org.json.JSONException ignored) { }
            }
        }
        return null;
    }

    public JSONObject headlightCommand(boolean enabled) throws org.json.JSONException {
        String id = headlightDpId();
        if (id == null) throw new IllegalStateException("В схеме нет доступного управления фонарём");
        return new JSONObject().put(id, enabled);
    }

    public String describe(JSONObject dps) {
        StringBuilder text = new StringBuilder();
        JSONObject values = decoded(dps);
        Iterator<String> keys = dps.keys();
        while (keys.hasNext()) {
            String id = keys.next();
            JSONObject definition = schema.get(id);
            String code = definition == null ? "DP " + id : definition.optString("code", "DP " + id);
            Object value = values.has(code) ? values.opt(code) : dps.opt(id);
            text.append(code).append(": ").append(value).append('\n');
        }
        return text.toString();
    }

    public String levelDpId() {
        for (Map.Entry<String, JSONObject> entry : schema.entrySet()) {
            if ("level".equals(entry.getValue().optString("code"))) return entry.getKey();
        }
        return null;
    }

    public boolean canControlParking() {
        JSONObject definition = schema.get(levelDpId());
        if (definition == null || !definition.optString("mode").contains("w")) return false;
        JSONObject property = propertyOf(definition);
        if (property == null || !"enum".equals(property.optString("type"))) return false;
        JSONArray range = property.optJSONArray("range");
        boolean on = false, off = false;
        if (range != null) for (int i = 0; i < range.length(); i++) {
            on |= "level_1".equals(range.optString(i));
            off |= "level_4".equals(range.optString(i));
        }
        return on && off;
    }

    public JSONObject parkingCommand(boolean enabled) throws org.json.JSONException {
        if (!canControlParking()) throw new IllegalStateException("Управление паркингом недоступно");
        return new JSONObject().put(levelDpId(), enabled ? "level_1" : "level_4");
    }
}
