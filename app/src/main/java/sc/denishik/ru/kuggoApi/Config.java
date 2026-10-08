package sc.denishik.ru.kuggoApi;

public class Config {
    public static String NAME_SCOOTER_KUGOO_BLU = "TY";
    public static final String SERVICE_UUID = "0000fd50-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_WRITE_NO_RESPONSE = "00000001-0000-1001-8001-00805f9b07d0";
    public static final String CHAR_NOTIFY_WRITE = "00000002-0000-1001-8001-00805f9b07d0";
    public static final String CHAR_READ = "00000003-0000-1001-8001-00805f9b07d0";

    /**
     * Tuya/Telink BLE Mesh login credentials (used by official Kugoo Riding app).
     * These are NOT the Bluetooth PIN. They come from the device binding (cloud) and are used to build
     * BLE_GATT_OP_PAIR_ENC_REQ.
     *
     * If you can extract these values (code/password) for your scooter, set them here (ASCII),
     * then enable pairing in {@code ServiceScooter}.
     */
    public static final String TUYA_MESH_CODE = "";
    public static final String TUYA_MESH_PASSWORD = "";
    public static final String[] HANDSHAKE_HEX = new String[] {
            "0021200eb47a6402c6a2d766221a23b793394384bdf7ce89034b266d56be8864496f21e3",
            "0071200f4f8d6f291a0b95260cca216a91b038dc9872f559b7d65f588aeb6265119af42441de1204141fbaa5d3c306659939b18c16560dc1339b55b2abcf6b6573cb5830e30f483dd5410e34557649a9e0d4cc5ae369248c492308caf1a0f8631acc5edd154ecccf00acabb07a7f12365bc9e605",
            "0021200f226f74b8e54523cc6e4f1198390a07153b7a2a5bd390cbb4dedce7e66951e042"
    };
    public static final String KEEPALIVE_HEX = "0021200e50dc535a5ab539df6ec1729a0ee6ac0279bd5faef30bced1c658458d80950f38";

    /**
     * Experimental: additional 52-byte packets observed in official app traffic.
     * Format: 00 31 20 0F + 48 bytes payload.
     * If the scooter considers the session "not fully established" (Bluetooth icon blinking),
     * try sending one of these right after receiving the first RX notification.
     */
    public static final String[] OFFICIAL_EXTRA_INIT_HEX = new String[]{
            "0031200f9f889eb45e3f773d982841656389398a3e24c120206f2b9344df3ec0dacc41d89cc1abedccf5c3018a6b60e7e200b6e8",
            "0031200f9e54c48017d0d2d72cacbfac0ea1757523322dc673d02894a99a3d5d926a5341348605f2771f5e90b28d8256df5835ec",
            "0031200f9de7be727fb8a1c270c4646ddf75e6f3dd5cea6c24682e33fe00e9b0d8eba10619de580ae96f3f4cdab3d4f967651725",
            "0031200fdab5503eeb01698fd4b82946272aca293cd27d5e4ac5b71105676ee5a71a48ae0253bebdf312c2e15e18ec3d29ae786b",
            "0031200f583d604cc09384018203dddee0462f46b3fa5069d16c3f4e28700413f853c5d41ffafcaaa89704ca56d8288edae9acd7"
    };

    public static final String[] OFFICIAL_CONNECT_INIT_HEX = new String[]{
            "0031200ff635f547af432cc095a5bedceef8c09e769da75d09f6152547825df421135083525b0ea942af51d63397096f38ed1ecb",
            "0031200f4c91a33fe55d259e835ebab4d788aecb7f456cc8d439d76edce3a1d4dd9e04ddba97e83b9b5ac31571d67e491909e6a3",
            "0031200f38f1018780d71388d121e73b18b92a7af8e0cea16780ae1acdc2a4e875e261aa801236bc9e373ffd014167b78532eaa2",
            "0031200f8329e3a62105db2cf852f5feed14cc4aefea1c2d55b48423981060f5e810b249cae3c6b9d0f379a7f970e0ed6995c736",
            "0031200f98aa5ace391e9f8b9238793bc095a3a4465e7f233aaa28e680c4ca4d56830dd408971d4aab5d22e3ee91243eeff690bf",
            "0031200fb21138fd13c2f51927788d5baf550182c249e78add1712a34743855a2ff503d3e6a71a4e3a8a06e85e328e1e4fd8071b",
            "0031200f7c7792ee45a17f7e98fa0c7dbe3348f2c2cd5481a937afe8cc136370b454b8bcdd11ab2428e6fb8ba669b169d11ca563"
    };
}
