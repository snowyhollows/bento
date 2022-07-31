package net.snowyhollows.bento.config;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.io.OutputStream;

public interface WorkDir {
    OutputStream openForWriting(File file);

    InputStream openForReading(File file);

    File[] listFiles(File dir, FileFilter fileFilter);
}
