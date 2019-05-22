package net.snowyhollows.bento2.inspector;

import net.snowyhollows.bento2.Bento;

public interface BentoInspector {
    void createChild(Bento parent, Bento bento);
    void createObject(Bento creator, Object key, Object value);
    void disposeOfChild(Bento bento);
}
