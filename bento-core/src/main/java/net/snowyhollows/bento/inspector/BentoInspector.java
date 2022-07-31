package net.snowyhollows.bento.inspector;

import net.snowyhollows.bento.Bento;

public interface BentoInspector {
    void createChild(Bento parent, Bento bento);
    void createObject(Bento creator, Object key, Object value);
    void disposeOfChild(Bento bento);
}
