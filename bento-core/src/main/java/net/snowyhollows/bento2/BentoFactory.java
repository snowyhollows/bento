package net.snowyhollows.bento2;

public interface BentoFactory<T> {
    T createInContext(Bento bento);
}
