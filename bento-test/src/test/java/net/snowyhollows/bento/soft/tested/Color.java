package net.snowyhollows.bento.soft.tested;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.soft.SoftEnum;

public class Color implements SoftEnum {
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
