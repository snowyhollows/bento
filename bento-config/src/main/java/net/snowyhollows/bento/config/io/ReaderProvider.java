package net.snowyhollows.bento.config.io;

import net.snowyhollows.bento.annotation.GwtIncompatible;
import net.snowyhollows.bento.annotation.WithFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

public class ReaderProvider {

    @WithFactory
    public ReaderProvider() {
    }

    public Reader readerForFile(String path) {
        return createReader(path);
    }

    @GwtIncompatible
    private Reader createReader(String path) {
        try {
            return new FileReader(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Reader createReader(Object path) {
        throw new RuntimeException("Not implemented in GWT; register GWTReaderProvider in Bento.");
    }
}
