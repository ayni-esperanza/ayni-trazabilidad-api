import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BootstrapLauncher {
    public static void main(String[] args) throws Exception {
        List<URL> urls = new ArrayList<>();
        for (String line : Files.readAllLines(Path.of(".tmp/runtime-classpath.txt"))) {
            if (line == null || line.isBlank()) continue;
            urls.add(Path.of(line.trim()).toUri().toURL());
        }
        try (URLClassLoader loader = new URLClassLoader(urls.toArray(URL[]::new), ClassLoader.getPlatformClassLoader())) {
            Thread.currentThread().setContextClassLoader(loader);
            Class<?> appClass = Class.forName("com.trazabilidad.ayni.AyniApplication", true, loader);
            Method main = appClass.getMethod("main", String[].class);
            main.invoke(null, (Object) args);
        }
    }
}
