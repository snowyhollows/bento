package net.snowyhollows.bento.parametrized;


import net.snowyhollows.bento.annotation.WithFactory;

public class NeedsParametrized {

    private final LabeledProcessor<String> processor;

    @WithFactory
    public NeedsParametrized(LabeledProcessor<String> processor) {
        this.processor = processor;
    }

    public String returnSomething() {
        return processor.process("a", "b");
    }
}
