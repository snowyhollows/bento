package net.snowyhollows.bento.switcher;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.BentoException;
import net.snowyhollows.bento.switcher.tested.JuryMember;
import net.snowyhollows.bento.switcher.tested.JuryMemberFactory;
import net.snowyhollows.bento.switcher.tested.NoImplementationsFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ImplementationSwitcherTest {
    @Test
    @DisplayName("Should throw if no implementation configured")
    public void create() {
        // given
        Bento root = Bento.createRoot();

        // execute & assert
        Assertions.assertThatThrownBy(() -> root.get(JuryMemberFactory.IT))
                .isInstanceOf(BentoException.class)
                .hasMessage("Couldn't retrieve jury_member.impl");
    }

    @Test
    @DisplayName("Should select correct implementation")
    public void create__selected() {
        // given
        Bento root = Bento.createRoot();
        root.register("jury_member.impl", "average");

        // execute
        JuryMember juryMember = root.get(JuryMemberFactory.IT);
        int numberOfStars = juryMember.getNumberOfStars();

        // assert
        Assertions.assertThat(numberOfStars).isEqualTo(3);
    }

    @Test
    @DisplayName("Should throw if wrong implementation configured")
    public void create__wrong_impl() {
        // given
        Bento root = Bento.createRoot();
        root.register("jury_member.impl", "hip");

        // execute & assert
        Assertions.assertThatThrownBy(() -> root.get(JuryMemberFactory.IT))
                .isInstanceOf(BentoException.class)
                .hasMessage("No case found for [hip]");
    }

    @Test
    @DisplayName("Should allow usage of implementation not known to the original interface, by using its qualified class name")
    public void create__by_qn()  {
        // given
        Bento root = Bento.createRoot();
        root.register("jury_member.impl", "net.snowyhollows.bento.switcher.tested.AdditionalImplementationOfJuryMember");
        root.register("decision", 999);

        // execute
        JuryMember juryMember = root.get(JuryMemberFactory.IT);
        int numberOfStars = juryMember.getNumberOfStars();

        // assert
        Assertions.assertThat(numberOfStars).isEqualTo(999);
    }

    @Test
    @DisplayName("Should throw if the interface has no automatic implementations")
    public void create__no_imp() {
        // given
        Bento root = Bento.createRoot();

        // execute & assert
        Assertions.assertThatThrownBy(() -> root.get(NoImplementationsFactory.IT))
                .isInstanceOf(BentoException.class)
                .hasMessage("Implementation of net.snowyhollows.bento.switcher.tested.NoImplementations must be registered manually, e.g. by calling bento.register(NoImplementationsFactory.IT, someImplementation)");
    }

}
