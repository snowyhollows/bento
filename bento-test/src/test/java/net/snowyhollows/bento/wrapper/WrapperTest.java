package net.snowyhollows.bento.wrapper;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.config.Configurer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.assertj.core.api.Assertions.assertThat;

public class WrapperTest {
    @Test
    @DisplayName("Should produce particles using a generated, configurable factory")
    public void create() {
        // given
        Bento bento = new Configurer()
                .setParam("color", Color.RED)
                .setParam("speed", 1f)
                .getConfig();

        // execute
        ParticleUtil particleUtil = bento.get(ParticleUtilFactory.IT);
        Particle particle1 = particleUtil.create(2f);
        Particle particle2 = particleUtil.create(Color.WHITE, 3f);
        Particle particle3 = particleUtil.create();

        // assert
        assertThat(particle1.color).isEqualTo(Color.RED);
        assertThat(particle1.speed).isEqualTo(2f);
        assertThat(particle2.color).isEqualTo(Color.WHITE);
        assertThat(particle2.speed).isEqualTo(3f);
        assertThat(particle3.color).isEqualTo(Color.RED);
        assertThat(particle3.speed).isEqualTo(1f);
    }
}
