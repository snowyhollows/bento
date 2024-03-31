package net.snowyhollows.bento.config;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.io.ReaderProvider;
import net.snowyhollows.bento.config.parser.Parser;

import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

public class PropertiesLoader {

    private ReaderProvider readerProvider;
    private final Bento bento;

    @WithFactory
    public PropertiesLoader(ReaderProvider readerProvider, Bento bento) {
        this.readerProvider = readerProvider;
        this.bento = bento;
    }
    
    public static Bento loadNew(Reader reader) {
        Bento bento = Bento.createRoot();
        configure(bento, readPropertiesToMap(reader));
        return bento;
    }

    public void load(String path) {
        configure(this.bento, loadPropertiesToMap(path));
    }

    public void load(Reader reader) {
       configure(this.bento, readPropertiesToMap(reader));
    }

    private Map<String, String> loadPropertiesToMap(String path) {
        return  readPropertiesToMap(readerProvider.readerForFile(path));
    }

    private static Map<String, String> readPropertiesToMap(Reader reader) {
        Map<String, String> properties = new LinkedHashMap<>();
        Parser propertiesParser = new Parser(reader);
        while (propertiesParser.hasMore()) {
            propertiesParser.nextPropertiesLine(properties);
            propertiesParser.nextLine();
        }
        return properties;
    }

    private static void configure(Bento bento, Map<String, String> properties) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            bento.register(entry.getKey(), entry.getValue());
        }
    }


}
