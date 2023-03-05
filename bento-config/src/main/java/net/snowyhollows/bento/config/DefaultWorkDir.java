package net.snowyhollows.bento.config;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class DefaultWorkDir implements WorkDir {

    private final File root;

    public DefaultWorkDir() {
        this(new File("."));
    }

    public DefaultWorkDir(File root) {
        if (!root.isDirectory()) {
            throw new IllegalArgumentException(String.format("%s is not a directory", root.getAbsolutePath()));
        }
        this.root = root;
    }

    @Override
    public OutputStream openForWriting(File file) {
        try {
            return new FileOutputStream(absolutize(file));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public InputStream openForReading(File file) {
        try {
            return new FileInputStream(absolutize(file));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean exists(File file) {
        return this.absolutize(file).exists();
    }

    @Override
    public File[] listFiles(File dir, FileFilter fileFilter) {
        return absolutize(dir).listFiles(fileFilter);
    }

    private File absolutize(File file) {
        return file.isAbsolute() ? file : new File(root.getAbsolutePath(), file.getPath());
    }


}
