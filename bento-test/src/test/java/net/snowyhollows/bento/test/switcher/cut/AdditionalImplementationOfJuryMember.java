package net.snowyhollows.bento.test.switcher.cut;

import net.snowyhollows.bento.annotation.WithFactory;

public class AdditionalImplementationOfJuryMember implements JuryMember{

    private final int decision;

    @WithFactory
    public AdditionalImplementationOfJuryMember(int decision) {
        this.decision = decision;
    }


    @Override
    public int getNumberOfStars() {
        return decision;
    }
}
