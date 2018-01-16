package net.snowyhollows.bento2.annotation;

import net.snowyhollows.bento2.Bento;
import net.snowyhollows.bento2.BentoFactory;

public final class DefaultFactory implements BentoFactory<Object> {

    private DefaultFactory() {}

    @Override
    public Object createInContext(Bento bento) {
        throw new IllegalArgumentException();
    }
}
