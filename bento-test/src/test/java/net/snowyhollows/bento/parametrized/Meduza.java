package net.snowyhollows.bento.parametrized;

import net.snowyhollows.bento.annotation.WithFactory;

public class Meduza implements LabeledProcessor<String> {

    private int a;

    @WithFactory(exactName = "LabeledProcessorOfStringFactory")
    public Meduza(int a) {
        this.a = a;
    }

    @Override
    public String process(String s, String label) {
        return s + " + " + label + " + " + a;
    }
}
