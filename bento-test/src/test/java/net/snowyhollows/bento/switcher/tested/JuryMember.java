package net.snowyhollows.bento.switcher.tested;

import net.snowyhollows.bento.annotation.ImplementationSwitch;

@ImplementationSwitch(configKey = "jury_member.impl", cases = {
        @ImplementationSwitch.When(name = "super", implementation = ExtaticJuryMember.class),
        @ImplementationSwitch.When(name = "average", implementation = AverageJuryMember.class),
        @ImplementationSwitch.When(name = "poor", implementation = BlazeJuryMember.class),
})
public interface JuryMember {
    int getNumberOfStars();
}

