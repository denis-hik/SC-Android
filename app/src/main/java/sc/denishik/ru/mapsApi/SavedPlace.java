package sc.denishik.ru.mapsApi;

import org.json.JSONObject;

public final class SavedPlace {
    public final String title, address;
    public final double latitude, longitude;
    public SavedPlace(String title, String address, double latitude, double longitude) {
        this.title = title; this.address = address; this.latitude = latitude; this.longitude = longitude;
    }
    public JSONObject toJson() throws org.json.JSONException {
        return new JSONObject().put("title", title).put("address", address)
                .put("latitude", latitude).put("longitude", longitude);
    }
    public static SavedPlace fromJson(String json) {
        try {
            JSONObject o = new JSONObject(json);
            double lat = o.getDouble("latitude"), lon = o.getDouble("longitude");
            if (!Double.isFinite(lat) || !Double.isFinite(lon) || Math.abs(lat) > 90 || Math.abs(lon) > 180) return null;
            return new SavedPlace(o.getString("title"), o.getString("address"), lat, lon);
        } catch (Exception error) { return null; }
    }
}
