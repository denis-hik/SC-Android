package sc.denishik.ru.ledApiBLU;

public class Config {
    static Client defaultClient;

    static public void setDefaultClient(Client client) {
        defaultClient = client;
    }
    static public Client getDefaultClient() {
        return defaultClient;
    }

    public static final String DMX_STRIP_CHARACTERISTIC = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static final String DMX_STRIP_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";

    public static final String ELK_STRIP_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static final String ELK_STRIP_CHARACTERISTIC = "0000fff3-0000-1000-8000-00805f9b34fb";
    public static final String ELK_STRIP_PREFIX = "ELK-";

}
