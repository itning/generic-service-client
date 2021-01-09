package top.itning.generic.service.jar.handle.zip;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

/**
 * @author itning
 * @since 2020/12/26 10:57
 */
public final class ZipClassLoader extends URLClassLoader {

    private final Map<String, byte[]> classes;

    public ZipClassLoader(URL[] urls, final File zipFile) throws IOException {
        super(urls);
        classes = ZippedJarClassReader.loadZip(zipFile);
    }

    public ZipClassLoader(final File zipFile) throws IOException {
        super(new URL[]{});
        classes = ZippedJarClassReader.loadZip(zipFile);
    }

    public ZipClassLoader(final byte[] bytes) throws IOException {
        super(new URL[]{});
        classes = ZippedJarClassReader.loadZip(bytes);
    }

    public ZipClassLoader(final ByteArrayOutputStream baos) {
        super(new URL[]{});
        classes = ZippedJarClassReader.loadZip(baos);
    }

    public ZipClassLoader(final InputStream in) throws IOException {
        super(new URL[]{});
        classes = ZippedJarClassReader.loadZip(in);
    }

    @Override
    public final Class<?> findClass(String name) throws ClassNotFoundException {
        final byte[] bytes = classes.get(name);
        if (bytes != null) return defineClass(name, bytes, 0, bytes.length);
        return super.findClass(name);
    }
}