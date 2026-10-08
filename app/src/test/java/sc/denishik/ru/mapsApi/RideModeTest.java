package sc.denishik.ru.mapsApi;
import org.junit.Test;
import static org.junit.Assert.*;
public class RideModeTest {
    @Test public void shortStopKeepsRideAndLongStopEndsIt() {
        RideMode mode = new RideMode();
        assertFalse(mode.update(0, 0));
        assertTrue(mode.update(3, 100));
        assertTrue(mode.update(0, 1000));
        assertTrue(mode.update(0, 15999));
        assertFalse(mode.update(0, 16000));
    }
    @Test public void movingAgainCancelsStopTimer() {
        RideMode mode = new RideMode();
        mode.update(8, 0); mode.update(0, 1000);
        assertTrue(mode.update(4, 14000));
        assertTrue(mode.update(0, 16000));
        assertTrue(mode.update(0, 20000));
    }
    @Test public void invalidSamplesCannotStartRide() {
        RideMode mode = new RideMode();
        assertFalse(mode.update(Double.NaN, 0));
        assertFalse(mode.update(-1, 0));
        assertFalse(mode.update(Double.POSITIVE_INFINITY, 0));
        mode.update(8, 0); mode.reset();
        assertFalse(mode.update(0, 1));
    }
}
