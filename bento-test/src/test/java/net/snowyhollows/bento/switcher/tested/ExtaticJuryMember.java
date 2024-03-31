package net.snowyhollows.bento.switcher.tested;

import net.snowyhollows.bento.annotation.WithFactory;

public class ExtaticJuryMember implements JuryMember {
    @WithFactory
    public ExtaticJuryMember() {
    }

    @Override
    public int getNumberOfStars() {
        return 5;
    }
}
