package net.snowyhollows.bento.switcher.tested;

import net.snowyhollows.bento.annotation.WithFactory;

public class BlazeJuryMember implements JuryMember {
    @WithFactory
    public BlazeJuryMember() {
    }

    @Override
    public int getNumberOfStars() {
        return 1;
    }
}
