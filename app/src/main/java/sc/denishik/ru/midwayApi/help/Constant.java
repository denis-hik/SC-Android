package sc.denishik.ru.midwayApi.help;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Constant {
    public static final byte CMD_BAT_INFO = 8;
    public static final byte CMD_BOOT_EXIT = 83;
    public static final byte CMD_ERASE_FAIL = -46;
    public static final byte CMD_ERASE_FLASH = 82;
    public static final byte CMD_ESC_INFO = 7;
    public static final byte CMD_FAIL = -127;
    public static final byte CMD_HANDSHAKE = 81;
    public static final byte CMD_HANDSHAKE_FAIL = -48;
    public static final byte CMD_KEEP = 2;
    public static final byte CMD_PACK = 1;
    public static final byte CMD_PACK_STOP = -2;
    public static final byte CMD_READ_BAT_FAIL = -120;
    public static final byte CMD_READ_INFO_FAIL = -121;
    public static final byte CMD_READ_PARAMETER = 3;
    public static final byte CMD_READ_PARAM_FAIL = -125;
    public static final byte CMD_RW_PARAMETER = 23;
    public static final byte CMD_RW_PARAM_FAIL = -105;
    public static final byte CMD_TRAN = 0;
    public static final byte CMD_TRAN_STOP = -1;
    public static final byte CMD_UPDATE_FAIL = -47;
    public static final byte CMD_UPDATE_FM = 80;
    public static final byte CMD_WRITER_PARAMETER = 16;
    public static final byte CMD_WRITE_PARAM_FAIL = -112;
    public static final byte END_TRAN = 90;
    public static final int FULL_PACKET_SIZE = 80;
    public static final byte HEAD_ESC = 1;
    public static final byte HEAD_MONITOR = -85;
    public static final byte HEAD_TRAN = -91;
    public static final int INFO_PACKET_SIZE = 16;
    public static final int MAX_PACKET_SIZE = 65535;
    public static final int MTU_SIZE = 200;
    public static final int PACKET_SIZE = 8;
    public static final int SUB_PACKET_MAX = 130;
    public static final int SUB_PACKET_MIN = 20;
    @NotNull
    public static final Constant INSTANCE = new Constant();
    @NotNull
    private static final UUID DATA_SERVICE = StringExtKT.toUUID("F1F0");
    @NotNull
    private static final UUID DATA_TX = StringExtKT.toUUID("F1F1");
    @NotNull
    private static final UUID DATA_RX = StringExtKT.toUUID("F1F2");
    @NotNull
    private static final UUID CMD_SERVICE = StringExtKT.toUUID("F2F0");
    @NotNull
    private static final UUID CMD_TX = StringExtKT.toUUID("F2F1");
    @NotNull
    private static final UUID CMD_RX = StringExtKT.toUUID("F2F2");

    private Constant() {
    }

    @NotNull
    public final UUID getCMD_RX() {
        return CMD_RX;
    }

    @NotNull
    public final UUID getCMD_SERVICE() {
        return CMD_SERVICE;
    }

    @NotNull
    public final UUID getCMD_TX() {
        return CMD_TX;
    }

    @NotNull
    public final UUID getDATA_RX() {
        return DATA_RX;
    }

    @NotNull
    public final UUID getDATA_SERVICE() {
        return DATA_SERVICE;
    }

    @NotNull
    public final UUID getDATA_TX() {
        return DATA_TX;
    }

}
