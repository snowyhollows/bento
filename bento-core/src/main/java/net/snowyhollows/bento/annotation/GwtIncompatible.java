package net.snowyhollows.bento.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Retention(value=CLASS)
@Target(value={TYPE,METHOD,CONSTRUCTOR,FIELD})
@Documented
public @interface GwtIncompatible {
}
