package sc.denishik.ru.ledApiWS;

public class Config {
    public static String IP_HOST_DEFAULT = "192.168.4.1";
    public static String IP_CLIENT_DEFAULT = "192.168.4.4";
    public static String PORT_CLIENT_DEFAULT = "81";
    public static String MASK_CLIENT_DEFAULT = "255.255.255.0";
    public static String ID_BOARD = "3363b6eb";

    public static void SetBoard(String value) {
        ID_BOARD = value;
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
