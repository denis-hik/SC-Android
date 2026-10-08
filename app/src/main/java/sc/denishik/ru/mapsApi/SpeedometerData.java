package sc.denishik.ru.mapsApi;

public final class SpeedometerData {
    private SpeedometerData() { }
    public static String gear(String level) {
        switch (level) {
            case "level_1": return "P";
            case "level_2": return "1";
            case "level_3": return "2";
            case "level_4": return "3";
            default: return "—";
        }
    }
}
