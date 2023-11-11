package net.snowyhollows.bento.category.tested;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.category.CategoryManager;

public class Colors extends CategoryManager<Color> {
    public final Color WHITE;
    public final Color BLACK;

    @WithFactory
    public Colors(Bento bento) {
        super(bento, "color", ColorFactory.IT);

        WHITE = getByName("WHITE");
        BLACK = getByName("BLACK");

        if (WHITE == null || BLACK == null) {
            throw new IllegalStateException("There must be at least BLACK and WHITE color available");
        }
    }

    @Override
    public Color[] emptyArray() {
        return new Color[0];
    }
}
