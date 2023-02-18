package net.snowyhollows.bento.switcher.tested;

import net.snowyhollows.bento.annotation.WithFactory;

import java.util.Locale;

public class DefaultStringNormalizer implements StringNormalizer {

    @WithFactory
    public DefaultStringNormalizer() {
    }

    @Override
    public String normalize(String string) {
        return string.trim().toUpperCase(Locale.ROOT);
    }
}
