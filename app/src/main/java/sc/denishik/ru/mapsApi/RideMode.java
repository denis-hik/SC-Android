package sc.denishik.ru.mapsApi;

/** Hysteresis prevents a traffic-light stop from repeatedly switching the screen. */
public final class RideMode {
    private boolean riding;
    private long stoppedAt = -1;
    public boolean update(double speed, long now) {
        if (!Double.isFinite(speed) || speed < 0) return riding;
        if (speed > 2) { riding = true; stoppedAt = -1; }
        else if (speed < 0.5 && riding) {
            if (stoppedAt < 0) stoppedAt = now;
            if (now - stoppedAt >= 15000) { riding = false; stoppedAt = -1; }
        } else stoppedAt = -1;
        return riding;
    }
    public void reset() { riding = false; stoppedAt = -1; }
}
