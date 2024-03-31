package net.snowyhollows.bento.config.io;

import net.snowyhollows.bento.annotation.GwtIncompatible;
import net.snowyhollows.bento.annotation.WithFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class WriterProvider {
    @WithFactory
    public WriterProvider() {
    }

    public Writer writerForFile(String path, int bufferSize) {
        return createWriter(path, bufferSize);
    }

    @GwtIncompatible
    private Writer createWriter(String path, int bufferSize) {
        try {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8), bufferSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Writer createWriter(Object path, int bufferSize) {
        throw new RuntimeException("Not implemented in GWT; no good means of writing files.");
    }
}
