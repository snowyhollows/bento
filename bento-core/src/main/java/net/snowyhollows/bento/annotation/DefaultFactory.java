package net.snowyhollows.bento.annotation;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.BentoFactory;

public final class DefaultFactory implements BentoFactory<Object> {

    private DefaultFactory() {}

    @Override
    public Object createInContext(Bento bento) {
        throw new IllegalArgumentException();
    }
}
