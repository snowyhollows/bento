package net.snowyhollows.bento.category.tested;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.category.Category;

public class Color implements Category {
    private final String name;
    private final byte ordinal;

    @WithFactory
    Color(String name, int ordinal) {
        this.name = name;
        this.ordinal = (byte)ordinal;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public byte ordinal() {
        return ordinal;
    }
}
