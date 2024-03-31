package net.snowyhollows.bento.config;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.io.ReaderProvider;
import net.snowyhollows.bento.config.parser.Parser;

import java.util.LinkedHashMap;
import java.util.Map;

public class PropertiesLoader {

    private ReaderProvider readerProvider;

    @WithFactory
    public PropertiesLoader(ReaderProvider readerProvider) {
        this.readerProvider = readerProvider;
    }

    public Map<String, String> configureFromProperties(String path, Bento bento) {
        Map<String, String> properties = loadProperties(path);
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            bento.register(entry.getKey(), entry.getValue());
        }
        return properties;
    }

    private Map<String, String> loadProperties(String path) {
        Map<String, String> properties = new LinkedHashMap<>();
        Parser propertiesParser = new Parser(readerProvider.readerForFile(path));
        while (propertiesParser.hasMore()) {
            propertiesParser.nextPropertiesLine(properties);
            propertiesParser.nextLine();
        }
        return properties;
    }
}
