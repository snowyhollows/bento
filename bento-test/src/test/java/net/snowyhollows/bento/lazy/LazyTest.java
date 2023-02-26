package net.snowyhollows.bento.lazy;

import net.snowyhollows.bento.Bento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LazyTest {

    @Test
    @DisplayName("Should create objects and return same copies")
    void create() {
        // given
        Bento bento = Bento.createRoot();
        bento.register("value", "xxx");
        MiscHolder miscHolder = bento.get(MiscHolderFactory.IT);

        // execute
        Thing thing1 = miscHolder.thing();
        Thing thing2 = miscHolder.thing();
        Vegetable vegetable1 = miscHolder.vegetable();
        Vegetable vegetable2 = miscHolder.vegetable();

        // assert
        assertThat(thing1).isSameAs(thing2);
        assertThat(vegetable1).isSameAs(vegetable2);
    }

    @Test
    @DisplayName("The object should be created only after miscHolder is created")
    void create__lazy() {
        // given
        Bento bento = Bento.createRoot();
        bento.register("value", "xxx");

        // execute
        MiscHolder miscHolder = bento.get(MiscHolderFactory.IT);
        bento.register("value", "something else");
        Thing thing1 = miscHolder.thing();
        bento.register("value", "xxx");

        // assert
        assertThat(thing1.value).isEqualTo("something else");
    }

}
