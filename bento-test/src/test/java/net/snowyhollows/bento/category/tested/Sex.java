package net.snowyhollows.bento.category.tested;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.category.Category;

public class Sex implements Category {
    private final byte ordinal;
    private final String name;

    @WithFactory
    public Sex(int ordinal, String name) {
        this.ordinal = (byte) ordinal;
        this.name = name;
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
