package sc.denishik.ru.mapsApi;

import android.content.Context;
import android.content.SharedPreferences;

public final class PlacesStore {
    private final SharedPreferences prefs;
    public PlacesStore(Context context) { prefs = context.getSharedPreferences("map_places", Context.MODE_PRIVATE); }
    public SavedPlace get(String slot) { return SavedPlace.fromJson(prefs.getString(slot, "")); }
    public void save(String slot, SavedPlace place) {
        try { prefs.edit().putString(slot, place.toJson().toString()).apply(); }
        catch (org.json.JSONException error) { throw new IllegalArgumentException(error); }
    }
    public void remove(String slot) { prefs.edit().remove(slot).apply(); }
    public String provider() { return prefs.getString("provider", "yandex"); }
    public void provider(String value) { prefs.edit().putString("provider", value).apply(); }
    public TravelMode travelMode() { return TravelMode.parse(prefs.getString("travel_mode", "SCOOTER")); }
    public void travelMode(TravelMode mode) { prefs.edit().putString("travel_mode", mode.name()).apply(); }
}
