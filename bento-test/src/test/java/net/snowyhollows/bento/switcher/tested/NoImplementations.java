package net.snowyhollows.bento.switcher.tested;

import net.snowyhollows.bento.annotation.ImplementationSwitch;

@ImplementationSwitch()
public interface NoImplementations {
    void a();
    void b(int c);
}
