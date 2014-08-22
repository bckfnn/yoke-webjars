package io.github.bckfnn.yoke.webjars;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.MimeType;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;

/**
 * A yoke middleware that serves webjars files from a mounted path.
 */
public class WebJars extends Middleware {
    private static Logger log = LoggerFactory.getLogger(WebJars.class); 

    private static String WEBJARSFOLDER = "META-INF/resources/webjars/";

    String mount;

    Map<String, byte[]> entries = new HashMap<>();

    /**
     * Constructor.
     * @param mount the mount point of all webjars.
     */
    public WebJars(String mount) {
        this.mount = mount;
        try {
            load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void load() throws IOException {
        int totalSize = 0;
        int count = 0;

        Enumeration<URL> en = getClass().getClassLoader().getResources(WEBJARSFOLDER);
        while (en.hasMoreElements()) {
            URL url = en.nextElement();
            if ("jar".equals(url.getProtocol())) {
                JarURLConnection urlcon = (JarURLConnection) (url.openConnection());

                JarFile jar = urlcon.getJarFile();
                System.out.println("jar:" + jar + " " + url);
                Enumeration<JarEntry> jarEntries = jar.entries();
                while (jarEntries.hasMoreElements()) {
                    JarEntry entry = jarEntries.nextElement();
                    String name = entry.getName();
                    //System.out.println(entry);
                    if (!entry.isDirectory() && entry.getName().startsWith(WEBJARSFOLDER)) {
                        InputStream is = jar.getInputStream(entry);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        byte[] b = new byte[4096];

                        for (int len; (len = is.read(b, 0, b.length)) > 0;) {
                            baos.write(b, 0, len);
                        }
                        is.close();
                        //System.out.println(len + " " + b.length);

                        String mountName = mount + "/" + name.substring(WEBJARSFOLDER.length());
                        entries.put(mountName, baos.toByteArray());

                        log.info("loaded webjar {} {}", mountName, baos.size());
                        totalSize += baos.size();
                        count++;
                    }
                }
            }

        }
        log.info("loaded webjars content from {} files, {} bytes", count, totalSize);
    }

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {
        String path = request.path();

        if (!path.startsWith(mount)) {
            next.handle(null);
            return;
        }
        byte[] buf = entries.get(path);

        if (buf == null) {
            next.handle(null);
        } else {
            String contentType = MimeType.getMime(path);
            String charset = "UTF-8"; // MimeType.getCharset(contentType);
            request.response().putHeader("content-type", contentType + (charset != null ? "; charset=" + charset : ""));
            request.response().putHeader("Content-Length", Long.toString(buf.length));

            request.response().end(new Buffer(buf));
        }
    }
}
