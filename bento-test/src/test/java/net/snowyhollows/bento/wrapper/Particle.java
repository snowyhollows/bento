package net.snowyhollows.bento.wrapper;

import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;

import java.awt.*;

public class Particle {
    public final Color color;
    public final float speed;

    @WithFactory
    public Particle(@ByName Color color, float speed) {
        this.color = color;
        this.speed = speed;
    }
}
