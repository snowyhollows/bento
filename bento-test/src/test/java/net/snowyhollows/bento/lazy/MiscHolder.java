package net.snowyhollows.bento.lazy;

import net.snowyhollows.bento.annotation.BentoWrapper;
import net.snowyhollows.bento.annotation.LazyProvider;

@LazyProvider
public interface MiscHolder {

    Thing thing();

    Vegetable vegetable();
}
