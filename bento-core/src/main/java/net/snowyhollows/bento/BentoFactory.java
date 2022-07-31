package net.snowyhollows.bento;

public interface BentoFactory<T> {
    T createInContext(Bento bento);
}
