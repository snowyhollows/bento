package net.snowyhollows.bento.defaults;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.config.Configurer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class MiscTest {

    @Test
    @DisplayName("Should generate proper factory")
    public void createInContext() throws IOException {
        // given
        Bento root = new Configurer()
                .loadConfigResource("/misc_all.properties")
                .getConfig();

        // execute
        Misc misc = root.get(MiscFactory.IT);

        // asssert
        assertThat(misc.floatValue).isEqualTo(1.5f);
        assertThat(misc.intValue).isEqualTo(2);
        assertThat(misc.stringValue).isEqualTo("3");
        assertThat(misc.boolValue).isTrue();
        assertThat(misc.day).isEqualTo(Day.SUNDAY);
        assertThat(misc.dayWithDefault).isEqualTo(Day.SATURDAY);
        assertThat(misc.floatValueWithDefault).isEqualTo(4f);
        assertThat(misc.intValueWithDefault).isEqualTo(5);
        assertThat(misc.stringValueWithDefault).isEqualTo("6");
        assertThat(misc.boolValueWithDefault).isFalse();
    }

    @Test
    @DisplayName("Should use defaults when values missing")
    public void createInContext__defaults() throws IOException {
        // given
        Bento root = new Configurer()
                .loadConfigResource("/misc_minimum.properties")
                .getConfig();

        // execute
        Misc misc = root.get(MiscFactory.IT);

        // asssert
        assertThat(misc.dayWithDefault).isEqualTo(Day.SUNDAY);
        assertThat(misc.floatValueWithDefault).isEqualTo(11f);
        assertThat(misc.intValueWithDefault).isEqualTo(12);
        assertThat(misc.stringValueWithDefault).isEqualTo("ssssss");
        assertThat(misc.boolValueWithDefault).isTrue();
    }

}
