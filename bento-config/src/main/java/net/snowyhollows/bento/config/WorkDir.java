package net.snowyhollows.bento.config;

import net.snowyhollows.bento.annotation.GwtIncompatible;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.io.OutputStream;

@GwtIncompatible
public interface WorkDir {
    OutputStream openForWriting(File file);

    InputStream openForReading(File file);
    boolean exists(File file);

    File[] listFiles(File dir, FileFilter fileFilter);
}
