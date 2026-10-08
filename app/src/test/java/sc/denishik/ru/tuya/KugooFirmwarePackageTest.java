package sc.denishik.ru.tuya;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static org.junit.Assert.*;

public class KugooFirmwarePackageTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();
    private static final String MD5 = "900150983cd24fb0d6963f7d28e17f72";

    private KugooFirmwarePackage firmware(String content, long size, String md5) throws Exception {
        File source = folder.newFile();
        Files.write(source.toPath(), content.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        return new KugooFirmwarePackage(source, "product", KugooFirmwarePackage.Target.MCU, "1.0.1", size, md5);
    }

    @Test public void stagesVerifiedIndependentCopy() throws Exception {
        File output = firmware("abc", 3, MD5.toUpperCase(java.util.Locale.ROOT)).prepare(folder.getRoot(), "product");
        assertArrayEquals(new byte[]{97, 98, 99}, Files.readAllBytes(output.toPath()));
        assertTrue(output.getName().startsWith("kugoo-ota-"));
    }

    @Test public void wrongProductIsRejectedBeforeStaging() throws Exception {
        KugooFirmwarePackage firmware = firmware("abc", 3, MD5);
        int before = folder.getRoot().list().length;
        assertThrows(IOException.class, () -> firmware.prepare(folder.getRoot(), "another-product"));
        assertEquals(before, folder.getRoot().list().length);
    }

    @Test public void corruptedFileIsRejectedAndTemporaryFileRemoved() throws Exception {
        KugooFirmwarePackage firmware = firmware("abd", 3, MD5);
        int before = folder.getRoot().list().length;
        assertThrows(IOException.class, () -> firmware.prepare(folder.getRoot(), "product"));
        assertEquals(before, folder.getRoot().list().length);
    }

    @Test public void truncatedFileIsRejected() throws Exception {
        KugooFirmwarePackage firmware = firmware("ab", 3, MD5);
        assertThrows(IOException.class, () -> firmware.prepare(folder.getRoot(), "product"));
    }

    @Test public void missingChecksumOrInvalidVersionIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new KugooFirmwarePackage(new File("unused"),
                "product", KugooFirmwarePackage.Target.MCU, "1.0.1", 3, ""));
        assertThrows(IllegalArgumentException.class, () -> new KugooFirmwarePackage(new File("unused"),
                "product", KugooFirmwarePackage.Target.MCU, "latest", 3, MD5));
    }
}
