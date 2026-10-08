package sc.denishik.ru.tuya;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/** Metadata must come from the firmware release for this product, not the scooter name. */
public final class KugooFirmwarePackage {
    public enum Target { BLUETOOTH(0), MCU(1);
        final int bleType;
        Target(int bleType) { this.bleType = bleType; }
    }
    public final String productId, version, md5;
    public final Target target;
    public final long size;
    private final File source;

    public KugooFirmwarePackage(File source, String productId, Target target,
                                String version, long size, String md5) {
        if (source == null || productId == null || productId.trim().isEmpty() || target == null
                || version == null || !version.matches("\\d+\\.\\d+\\.\\d+")
                || size <= 0 || size > 32L * 1024 * 1024
                || md5 == null || !md5.matches("(?i)[0-9a-f]{32}")) {
            throw new IllegalArgumentException("Invalid firmware metadata");
        }
        this.source = source;
        this.productId = productId;
        this.target = target;
        this.version = version;
        this.size = size;
        this.md5 = md5.toLowerCase(Locale.ROOT);
    }

    /** Validate the exact private copy that the SDK will read, off the UI thread. */
    File prepare(File directory, String connectedProductId) throws IOException {
        if (!productId.equals(connectedProductId)) throw new IOException("Firmware product mismatch");
        if (!source.isFile() || source.length() != size) throw new IOException("Firmware size mismatch");
        File copy = File.createTempFile("kugoo-ota-", ".bin", directory);
        boolean valid = false;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            long total = 0;
            try (FileInputStream input = new FileInputStream(source);
                 FileOutputStream output = new FileOutputStream(copy)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    if (Thread.currentThread().isInterrupted()) throw new IOException("Preparation cancelled");
                    total += count;
                    if (total > size) throw new IOException("Firmware size mismatch");
                    digest.update(buffer, 0, count);
                    output.write(buffer, 0, count);
                }
            }
            StringBuilder actual = new StringBuilder();
            for (byte value : digest.digest()) actual.append(String.format(Locale.ROOT, "%02x", value & 255));
            if (total != size || !md5.equals(actual.toString())) throw new IOException("Firmware checksum mismatch");
            valid = true;
            return copy;
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        } finally {
            if (!valid) copy.delete();
        }
    }
}
