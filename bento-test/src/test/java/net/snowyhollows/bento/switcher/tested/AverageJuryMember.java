package net.snowyhollows.bento.switcher.tested;

import net.snowyhollows.bento.annotation.WithFactory;

public class AverageJuryMember implements JuryMember {
    @WithFactory
    public AverageJuryMember() {
    }

    @Override
    public int getNumberOfStars() {
        return 3;
    }
}
