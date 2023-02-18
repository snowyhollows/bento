package net.snowyhollows.bento.wrapper;

import net.snowyhollows.bento.annotation.BentoWrapper;

import java.awt.*;

@BentoWrapper
public interface ParticleUtil {
    Particle create();
    Particle create(Color color, float speed);
    Particle create(float speed);
}
