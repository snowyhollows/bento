package net.snowyhollows.bento2;

public interface BentoFactory<T> {
    public T createInContext(Bento bento);
}
