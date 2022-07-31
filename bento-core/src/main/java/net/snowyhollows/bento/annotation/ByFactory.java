package net.snowyhollows.bento.annotation;

import net.snowyhollows.bento.BentoFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface ByFactory {
    Class<? extends BentoFactory> value() default DefaultFactory.class;
}
