package net.snowyhollows.bento.test.switcher.cut;

import net.snowyhollows.bento.annotation.ImplementationSwitch;

@ImplementationSwitch()
public interface NoImplementations {
    void a();
    void b(int c);
}
