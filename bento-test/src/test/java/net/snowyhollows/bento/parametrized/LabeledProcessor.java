package net.snowyhollows.bento.parametrized;

public interface LabeledProcessor<T> {
    String process(T t, String label);
}
