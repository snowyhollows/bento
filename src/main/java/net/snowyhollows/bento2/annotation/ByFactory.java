package net.snowyhollows.bento2.annotation;

import net.snowyhollows.bento2.BentoFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface ByFactory {
    Class<? extends BentoFactory> value() default DefaultFactory.class;
}
