package sc.denishik.ru.mapsApi;
import org.junit.Test;
import static org.junit.Assert.*;
public class SavedPlaceTest {
    @Test public void addressAndCoordinatesSurviveStorage() throws Exception {
        SavedPlace p = SavedPlace.fromJson(new SavedPlace("Дом", "Москва, улица", 55.7, 37.6).toJson().toString());
        assertNotNull(p); assertEquals("Дом", p.title); assertEquals(55.7, p.latitude, 0.00001);
        assertEquals(37.6, p.longitude, 0.00001);
    }
    @Test public void damagedOrOutOfRangePlaceIsRejected() {
        assertNull(SavedPlace.fromJson(""));
        assertNull(SavedPlace.fromJson("{\"title\":\"x\",\"address\":\"x\",\"latitude\":99,\"longitude\":37}"));
    }
}
