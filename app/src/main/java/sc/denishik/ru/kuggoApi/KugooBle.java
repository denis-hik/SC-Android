package sc.denishik.ru.kuggoApi;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.UUID;

import cn.wandersnail.ble.Connection;
import cn.wandersnail.ble.RequestBuilderFactory;
import cn.wandersnail.ble.WriteOptions;

public final class KugooBle {
    private static final String TAG = "KugooBle";
    public static final UUID SERVICE_UUID = UUID.fromString(Config.SERVICE_UUID);
    public static final UUID CHAR_WRITE_NO_RESPONSE = UUID.fromString(Config.CHAR_WRITE_NO_RESPONSE);
    public static final UUID CHAR_NOTIFY_WRITE = UUID.fromString(Config.CHAR_NOTIFY_WRITE);
    public static final UUID CHAR_READ = UUID.fromString(Config.CHAR_READ);
    private static final UUID CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final byte[] KEEPALIVE_CMD = hexToBytes(Config.KEEPALIVE_HEX);
    private static final SecureRandom RNG = new SecureRandom();

    private KugooBle() {
    }

    public static boolean isKugooName(String name) {
        return name != null && name.contains(Config.NAME_SCOOTER_KUGOO_BLU);
    }

    public static boolean isNotifyChar(UUID characteristic) {
        return CHAR_NOTIFY_WRITE.equals(characteristic);
    }

    public static void enableNotifications(Connection connection) {
        new RequestBuilderFactory()
                .getSetNotificationBuilder(SERVICE_UUID, CHAR_NOTIFY_WRITE, true)
                .build()
                .execute(connection);
    }

    /**
     * Build BLE_GATT_OP_PAIR_ENC_REQ packet used by official Kugoo Riding (Tuya/Telink mesh).
     * Packet format (17 bytes):
     * - [0] opcode = 0x0C
     * - [1..8] rand8
     * - [9..16] tail8 = reverse(AES_ECB(key=reverse(rand8||0), plaintext=reverse(code^password))[8..16])
     */
    public static byte[] buildTuyaPairEncReq(String code, String password) {
        byte[] codeBytes = padOrTrim(code, 16);
        byte[] passwordBytes = padOrTrim(password, 16);
        byte[] xorKey = new byte[16];
        for (int i = 0; i < 16; i++) {
            xorKey[i] = (byte) (codeBytes[i] ^ passwordBytes[i]);
        }
        byte[] rand8 = new byte[8];
        RNG.nextBytes(rand8);
        byte[] rand16 = new byte[16];
        System.arraycopy(rand8, 0, rand16, 0, 8);

        byte[] encrypted = TuyaCrypto.aesEcbNoPaddingEncrypt(reverseCopy(rand16), reverseCopy(xorKey));
        if (encrypted == null || encrypted.length < 16) {
            return null;
        }
        byte[] pkt = new byte[17];
        pkt[0] = 0x0C;
        System.arraycopy(rand8, 0, pkt, 1, 8);
        // encrypted[8..15], then reverse into pkt[9..16]
        for (int i = 0; i < 8; i++) {
            pkt[9 + i] = encrypted[15 - i];
        }
        return pkt;
    }

    private static byte[] padOrTrim(String s, int len) {
        byte[] out = new byte[len];
        if (s == null) {
            return out;
        }
        byte[] src = s.getBytes(StandardCharsets.UTF_8);
        int n = Math.min(src.length, len);
        System.arraycopy(src, 0, out, 0, n);
        return out;
    }

    private static byte[] reverseCopy(byte[] in) {
        if (in == null) {
            return null;
        }
        byte[] out = new byte[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[in.length - 1 - i];
        }
        return out;
    }

    public static void writeCccd(Connection connection, boolean enable) {
        try {
            BluetoothGatt gatt = extractGatt(connection);
            if (gatt == null) {
                return;
            }
            BluetoothGattService service = gatt.getService(SERVICE_UUID);
            if (service == null) {
                return;
            }
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHAR_NOTIFY_WRITE);
            if (characteristic == null) {
                return;
            }
            BluetoothGattDescriptor cccd = characteristic.getDescriptor(CCCD_UUID);
            if (cccd == null) {
                return;
            }
            cccd.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            boolean ok = gatt.writeDescriptor(cccd);
            Log.d(TAG, "write CCCD " + (enable ? "enable" : "disable") + " ok=" + ok);
        } catch (SecurityException denied) {
            Log.w(TAG, "Bluetooth permission revoked", denied);
        } catch (Throwable t) {
            Log.w(TAG, "write CCCD failed", t);
        }
    }

    public static void writeCccdIndication(Connection connection, boolean enable) {
        try {
            BluetoothGatt gatt = extractGatt(connection);
            if (gatt == null) {
                return;
            }
            BluetoothGattService service = gatt.getService(SERVICE_UUID);
            if (service == null) {
                return;
            }
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHAR_NOTIFY_WRITE);
            if (characteristic == null) {
                return;
            }
            BluetoothGattDescriptor cccd = characteristic.getDescriptor(CCCD_UUID);
            if (cccd == null) {
                return;
            }
            cccd.setValue(enable ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            boolean ok = gatt.writeDescriptor(cccd);
            Log.d(TAG, "write CCCD indication " + (enable ? "enable" : "disable") + " ok=" + ok);
        } catch (SecurityException denied) {
            Log.w(TAG, "Bluetooth permission revoked", denied);
        } catch (Throwable t) {
            Log.w(TAG, "write CCCD indication failed", t);
        }
    }

    public static boolean isReadyForWrite(Connection connection) {
        BluetoothGatt gatt = extractGatt(connection);
        if (gatt == null) {
            return true;
        }
        try {
            BluetoothGattService service = gatt.getService(SERVICE_UUID);
            if (service == null) {
                return true;
            }
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHAR_NOTIFY_WRITE);
            if (characteristic == null) {
                return true;
            }
            BluetoothGattDescriptor cccd = characteristic.getDescriptor(CCCD_UUID);
            if (cccd == null) {
                return true;
            }
            byte[] v = cccd.getValue();
            return v != null && v.length >= 2 && v[0] == 0x01 && v[1] == 0x00;
        } catch (Throwable t) {
            return true;
        }
    }

    private static BluetoothGatt extractGatt(Connection connection) {
        if (connection == null) {
            return null;
        }
        try {
            for (String methodName : new String[]{"getBluetoothGatt", "getGatt"}) {
                try {
                    java.lang.reflect.Method m = connection.getClass().getMethod(methodName);
                    Object v = m.invoke(connection);
                    if (v instanceof BluetoothGatt) {
                        return (BluetoothGatt) v;
                    }
                } catch (NoSuchMethodException ignored) {
                }
            }
            for (String fieldName : new String[]{"gatt", "mGatt", "bluetoothGatt"}) {
                try {
                    java.lang.reflect.Field f = connection.getClass().getDeclaredField(fieldName);
                    f.setAccessible(true);
                    Object v = f.get(connection);
                    if (v instanceof BluetoothGatt) {
                        return (BluetoothGatt) v;
                    }
                } catch (NoSuchFieldException ignored) {
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "extractGatt failed", t);
        }
        return null;
    }

    public static void requestMtu(Connection connection, int mtu) {
        new RequestBuilderFactory().getChangeMtuBuilder(mtu).build().execute(connection);
    }

    public static void writeNoResponse(Connection connection, byte[] data) {
        WriteOptions options = new WriteOptions.Builder()
                .setMtuAsPackageSize()
                .setWaitWriteResult(false)
                .setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
                .build();
        new RequestBuilderFactory()
                .getWriteCharacteristicBuilder(SERVICE_UUID, CHAR_WRITE_NO_RESPONSE, data)
                .setWriteOptions(options)
                .build()
                .execute(connection);
    }

    public static void writeWithResponse(Connection connection, byte[] data) {
        WriteOptions options = new WriteOptions.Builder()
                .setMtuAsPackageSize()
                .setWaitWriteResult(true)
                .setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                .build();
        new RequestBuilderFactory()
                .getWriteCharacteristicBuilder(SERVICE_UUID, CHAR_WRITE_NO_RESPONSE, data)
                .setWriteOptions(options)
                .build()
                .execute(connection);
    }

    public static void readInfo(Connection connection) {
        new RequestBuilderFactory()
                .getReadCharacteristicBuilder(SERVICE_UUID, CHAR_READ)
                .build()
                .execute(connection);
    }

    public static void readPairCharacteristic(Connection connection) {
        new RequestBuilderFactory()
                .getReadCharacteristicBuilder(SERVICE_UUID, CHAR_WRITE_NO_RESPONSE)
                .build()
                .execute(connection);
    }

    public static byte[][] getHandshakeCommands() {
        byte[][] cmds = new byte[Config.HANDSHAKE_HEX.length][];
        for (int i = 0; i < Config.HANDSHAKE_HEX.length; i++) {
            cmds[i] = hexToBytes(Config.HANDSHAKE_HEX[i]);
        }
        return cmds;
    }

    public static byte[][] getOfficialExtraInitCommands() {
        byte[][] cmds = new byte[Config.OFFICIAL_EXTRA_INIT_HEX.length][];
        for (int i = 0; i < Config.OFFICIAL_EXTRA_INIT_HEX.length; i++) {
            cmds[i] = hexToBytes(Config.OFFICIAL_EXTRA_INIT_HEX[i]);
        }
        return cmds;
    }

    public static byte[][] getOfficialConnectInitCommands() {
        byte[][] cmds = new byte[Config.OFFICIAL_CONNECT_INIT_HEX.length][];
        for (int i = 0; i < Config.OFFICIAL_CONNECT_INIT_HEX.length; i++) {
            cmds[i] = hexToBytes(Config.OFFICIAL_CONNECT_INIT_HEX[i]);
        }
        return cmds;
    }

    public static byte[] getKeepAliveCommand() {
        return KEEPALIVE_CMD.clone();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        if ((len & 1) != 0) {
            throw new IllegalArgumentException("Hex string length must be even.");
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
