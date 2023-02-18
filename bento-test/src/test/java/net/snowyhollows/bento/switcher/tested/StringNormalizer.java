package net.snowyhollows.bento.switcher.tested;

import net.snowyhollows.bento.annotation.ImplementationSwitch;

@ImplementationSwitch(configKey = "stringNormalizer", cases = {
        @ImplementationSwitch.When(name = "default", implementation = DefaultStringNormalizer.class, useByDefault = true)
})
public interface StringNormalizer {
    String normalize(String string);
}
