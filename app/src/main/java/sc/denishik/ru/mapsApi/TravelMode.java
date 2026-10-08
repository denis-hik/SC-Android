package sc.denishik.ru.mapsApi;

public enum TravelMode {
    SCOOTER("Самокат"), BICYCLE("Велосипед"), WALK("Пешком"), CAR("Авто");
    public final String label;
    TravelMode(String label) { this.label = label; }
    public static TravelMode parse(String value) {
        try { return valueOf(value); } catch (Exception ignored) { return SCOOTER; }
    }
}
