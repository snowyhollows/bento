package net.snowyhollows.bento.config;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.BentoFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

public class Configurer {
    private final Bento bento;
    private final WorkDir workDir;

    public Configurer() {
        this(new DefaultWorkDir());
    }

    public Configurer(WorkDir workDir) {
        this.workDir = workDir;
        this.bento = Bento.createRoot();
        this.bento.register(WorkDirFactory.IT, workDir);
    }

    public Configurer loadConfigDir(String dirPath) throws IOException {
        loadAllConfigFiles(dirPath, bento);
        return this;
    }

    public Configurer loadConfigFile(String filename) throws IOException {
        loadProperties(filename, bento);
        return this;
    }

    public Configurer loadConfigResource(String resource) throws IOException {
        try (Reader reader = new InputStreamReader(this.getClass().getResourceAsStream(resource))) {
            loadProperties(reader, bento);
            return this;
        } catch (Exception e) {
            throw new IOException("Couldn't read resource " + resource, e);
        }
    }

    public Configurer setParam(String key, Object value) {
        bento.register(key, value);
        return this;
    }

    public Configurer overrideParam(String key, Object value) {
        assertKeyDefined(key);
        setParam(key, value);
        return this;
    }

    public Configurer initialize(BentoFactory<?>... factories) {
        for (BentoFactory<?> factory : factories) {
            bento.get(factory);
        }
        return this;
    }

    public <T> Configurer use(BentoFactory<T> factory, Consumer<T> user) {
        user.accept(bento.get(factory, null));
        return this;
    }

    public <T> Configurer useWithIo(BentoFactory<T> factory, IoConsumer<T> user) throws IOException {
        user.accept(bento.get(factory, null));
        return this;
    }

    public Bento getConfig() {
        return bento;
    }

    private void assertKeyDefined(Object key) {
        checkArgument(bento.get(key, null) != null,
                "key `%s` is not undefined. Use 'param' instead of 'override'.",
                key);
    }

    private void loadProperties(String configPath, Bento config) throws IOException {
        loadProperties(new InputStreamReader(workDir.openForReading(new File(configPath)), StandardCharsets.UTF_8), config);
    }

    private void loadProperties(Reader reader, Bento config) throws IOException {
        Properties properties = new Properties();
        properties.load(reader);
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            config.register(entry.getKey(), entry.getValue());
        }
    }

    private void loadAllConfigFiles(String configDir, Bento config) throws IOException {
        for (File file : workDir.listFiles(new File(configDir), f -> !f.isDirectory())) {
            loadProperties(file.getAbsolutePath(), config);
        }
    }

    private static void checkArgument(boolean test, String errorMessageTemplate, Object... args) {
        if (!test) {
            throw new IllegalArgumentException(String.format(errorMessageTemplate, args));
        }
    }

    public interface IoConsumer<T> {
        void accept(T t) throws IOException;
    }
}
