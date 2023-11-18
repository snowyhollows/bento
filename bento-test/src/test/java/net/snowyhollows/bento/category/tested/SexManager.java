package net.snowyhollows.bento.category.tested;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.BentoFactory;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.category.CategoryManager;

import java.util.Arrays;
import java.util.List;

public class SexManager extends CategoryManager<Sex> {

    public final Sex MALE;
    public final Sex FEMALE;

    @WithFactory
    public SexManager(Bento bento) {
        super(bento, "additional_sexes", SexFactory.IT);
        MALE = getByOrdinal(0);
        FEMALE = getByOrdinal(1);
    }

    @Override
    protected List<Sex> getBuiltIns() {
        return Arrays.asList(new Sex(0, "MALE"), new Sex(1, "FEMALE"));
    }

    @Override
    public Sex[] emptyArray() {
        return new Sex[0];
    }
}
