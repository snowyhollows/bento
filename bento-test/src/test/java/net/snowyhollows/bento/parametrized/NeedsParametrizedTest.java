package net.snowyhollows.bento.parametrized;

import net.snowyhollows.bento.Bento;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NeedsParametrizedTest {
    @Test
    @DisplayName("Parametrized dependencies should be achievable")
    void create() {
        // given
        Bento root = Bento.createRoot();
        root.register("a", 0);

        // execute
        NeedsParametrized needsParametrized = root.get(NeedsParametrizedFactory.IT);
        String result = needsParametrized.returnSomething();

        // assert
        assertThat(result).isEqualTo("a + b + 0");
    }
}
