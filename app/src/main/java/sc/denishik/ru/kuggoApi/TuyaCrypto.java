package sc.denishik.ru.kuggoApi;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

final class TuyaCrypto {
    private TuyaCrypto() {
    }

    static byte[] aesEcbNoPaddingEncrypt(byte[] key16, byte[] plaintext16) {
        if (key16 == null || plaintext16 == null || key16.length != 16 || plaintext16.length != 16) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key16, "AES"));
            return cipher.doFinal(plaintext16);
        } catch (Exception e) {
            return null;
        } finally {
            Arrays.fill(key16, (byte) 0);
            Arrays.fill(plaintext16, (byte) 0);
        }
    }
}
