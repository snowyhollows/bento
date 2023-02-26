package net.snowyhollows.bento.lazy;

import net.snowyhollows.bento.annotation.WithFactory;

public class Thing {
    public final String value;
    @WithFactory
    public Thing(String value) {
        this.value = value;
    }
}
