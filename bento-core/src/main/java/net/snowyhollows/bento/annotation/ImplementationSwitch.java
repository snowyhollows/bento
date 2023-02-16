package net.snowyhollows.bento.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ImplementationSwitch {
    String NO_CONFIG_KEY_DEFINED = "@@@ no config key defined @@@";

    String configKey() default NO_CONFIG_KEY_DEFINED;
    When[] cases() default {};

    @interface When {
        String name();
        @SuppressWarnings("rawtypes")
        Class<?> implementation();
    }
}
