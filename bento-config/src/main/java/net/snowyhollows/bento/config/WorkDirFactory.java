package net.snowyhollows.bento.config;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.BentoFactory;
import net.snowyhollows.bento.annotation.GwtIncompatible;

@GwtIncompatible
public enum WorkDirFactory implements BentoFactory<WorkDir> {
    IT;

    @Override
    public WorkDir createInContext(Bento bento) {
        throw new IllegalArgumentException("No WorkDir implementation registered");
    }
}
