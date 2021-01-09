package top.itning.generic.service.jar.handle.zip;

import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author itning
 * @since 2020/12/26 10:54
 */
public final class ZippedJarClassReader {

    private final static String JAR_EXTENSION = ".jar";

    public static Map<String, byte[]> loadZip(final File file) throws IOException {
        return loadZip(Files.readAllBytes(file.toPath()));
    }

    public static Map<String, byte[]> loadZip(final byte[] bytes) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(bytes);
        return unzip(baos);
    }

    public static Map<String, byte[]> loadZip(final ByteArrayOutputStream baos) {
        return unzip(baos);
    }

    public static Map<String, byte[]> loadZip(final InputStream inputStream) {
        return unzip(inputStream);
    }

    public static Map<String, byte[]> unzip(final ByteArrayOutputStream baos) {
        return unzip(new ByteArrayInputStream(baos.toByteArray()));
    }

    public static Map<String, byte[]> unzip(final InputStream inputStream) {
        final Map<String, byte[]> result = new HashMap<>();
        try (final ZipInputStream in = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                final ByteArrayOutputStream os = new ByteArrayOutputStream();
                if (!entry.isDirectory()) {
                    byte[] bytes = StreamUtils.copyToByteArray(in);
                    os.write(bytes);
                    if (entry.getName().toLowerCase().endsWith(JAR_EXTENSION)) {
                        result.putAll(unzip(os));
                    } else if (entry.getName().toLowerCase().endsWith(".class")) {
                        result.put(entry.getName().replaceAll("/", ".").substring(0, entry.getName().length() - 6), os.toByteArray());
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}