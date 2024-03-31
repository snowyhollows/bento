package net.snowyhollows.bento.config.parser;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

public class LookAheadReader implements Closeable {
    private final Reader reader;
    private int current;
    private int next;

    public LookAheadReader(Reader reader) {
        this.reader = reader;
        current = rawRead();
        next = rawRead();
    }

    private int  rawRead() {
        try {
            return reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void readNext() {
        current = next;
        next = rawRead();
    }

    public int peek() {
        return next;
    }

    public int current() {
        return current;
    }

    public void close() throws IOException {
        reader.close();
    }
}
