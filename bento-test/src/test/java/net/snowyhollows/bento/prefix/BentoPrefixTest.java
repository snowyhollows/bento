package net.snowyhollows.bento.prefix;

import net.snowyhollows.bento.Bento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BentoPrefixTest {

    @Test
    @DisplayName("Should read properties from root, depending on prefixes and compound prefixes")
    public void testPrefix() {
        // given
        Bento bento = Bento.createRoot();
        bento.register("name", "Xavier");
        bento.register("a.name", "Adam");
        bento.register("a.b.name", "Balin");
        bento.register("b.name", "Baltasar");

        Bento bento_a = bento.createWithPrefix("a."); // prefix: "a."
        Bento bento_b = bento.createWithPrefix("b."); // prefix "b."
        Bento bento_a_b = bento_a.createWithPrefix("b.");  // prefix "a.b"

        // execute
        String plain = bento.getString("name");
        String a = bento_a.getString("name");
        String b = bento_b.getString("name");
        String a_b = bento_a_b.getString("name");

        // assert
        assertThat(plain).isEqualTo("Xavier");
        assertThat(a).isEqualTo("Adam");
        assertThat(b).isEqualTo("Baltasar");
        assertThat(a_b).isEqualTo("Balin");
    }
}
