package sc.denishik.ru.ledApiWS;

public class Config {
    public static String IP_HOST_DEFAULT = "192.168.4.1";
    public static String IP_CLIENT_DEFAULT = "192.168.4.4";
    public static String PORT_CLIENT_DEFAULT = "81";
    public static String MASK_CLIENT_DEFAULT = "255.255.255.0";
    public static String ID_BOARD = "3363b6eb";
    public static int TARGET_SPEED = 30;

    public static void load(android.content.Context context) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("led_ws", android.content.Context.MODE_PRIVATE);
        IP_HOST_DEFAULT = prefs.getString("host", "192.168.4.1");
        PORT_CLIENT_DEFAULT = prefs.getString("port", "81");
        ID_BOARD = prefs.getString("board", "3363b6eb");
        TARGET_SPEED = Math.max(30, Math.min(150, prefs.getInt("target_speed", 30)));
    }

    public static void save(android.content.Context context, String host, String port, String board, int targetSpeed) {
        context.getSharedPreferences("led_ws", android.content.Context.MODE_PRIVATE).edit()
                .putString("host", host).putString("port", port).putString("board", board)
                .putInt("target_speed", Math.max(30, Math.min(150, targetSpeed))).apply();
        load(context);
    }

    public static void SetBoard(String value) {
        ID_BOARD = value;
    }

    /** The chosen speed corresponds to the controller's full-scale value of 255. */
    public static double ledValue(float speed, boolean parking) {
        if (parking || Float.isNaN(speed) || Float.isInfinite(speed)) return 0;
        int target = Math.max(30, Math.min(150, TARGET_SPEED));
        return Math.min(target, Math.max(0, (int) speed)) * 255.0 / target;
    }
    public static void SetMask(String value) {
        MASK_CLIENT_DEFAULT = value;
    }
    public static void SetPort(String value) {
        PORT_CLIENT_DEFAULT = value;
    }
    public static void SetIp(String value) {
        IP_CLIENT_DEFAULT = value;
    }
}
