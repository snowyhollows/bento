package net.snowyhollows.bento.soft;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.config.Configurer;
import net.snowyhollows.bento.soft.tested.Color;
import net.snowyhollows.bento.soft.tested.Colors;
import net.snowyhollows.bento.soft.tested.ColorsFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class SoftEnumTest {

    @Test
    @DisplayName("Should create enum instances, according to configuration file")
    void createEnumInstances() throws IOException {
        // given
        Bento bento = new Configurer().loadConfigResource("/soft_enum.properties").getConfig();
        Colors colors = bento.get(ColorsFactory.IT);

        // execute
        List<String> names = colors.values().stream().map(Color::name).collect(Collectors.toList());
        List<Byte> ordinals = colors.values().stream().map(Color::ordinal).collect(Collectors.toList());
        List<Color> colorList = colors.values();
        Color white = colors.getByName("WHITE");
        Color black = colors.getByName("BLACK");

        // assert
        assertThat(colors.WHITE).isSameAs(white);
        assertThat(colors.BLACK).isSameAs(black);

        assertThat(names).containsExactly("BLACK","WHITE","RED","BROWN","GREEN","BLUE");
        assertThat(ordinals).containsExactly((byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5);
        for (int i = 0; i < colors.values().size(); i++) {
            Color color = colors.getByOrdinal(i);
            assertThat(color.ordinal()).isEqualTo((byte)i);
            assertThat(color.name()).isEqualTo(names.get(i));
        }
    }
}
