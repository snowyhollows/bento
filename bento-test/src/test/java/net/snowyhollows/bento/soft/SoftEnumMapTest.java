package net.snowyhollows.bento.soft;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.soft.tested.Color;
import net.snowyhollows.bento.soft.tested.Colors;
import net.snowyhollows.bento.soft.tested.ColorsFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

class SoftEnumMapTest {

    Colors colors;
    SoftEnumMap<Color, Integer> map;

    @BeforeEach
    void before() {
        Bento root = Bento.createRoot();
        root.register("color", "BLACK,WHITE,GOLD");
        colors = root.get(ColorsFactory.IT);
        map = new SoftEnumMap<>(colors);
    }

    @Test
    void size() {
        // execute
        int size = map.size();
        map.put(colors.BLACK, 12);
        map.put(colors.WHITE, 12);
        map.put(colors.WHITE, 13);
        map.put(colors.WHITE, 14);
        int sizeAfter = map.size();

        // assert
        assertThat(size).isZero();
        assertThat(sizeAfter).isEqualTo(2);
    }

    @Test
    void isEmpty() {
        // execute
        boolean empty = map.isEmpty();
        map.put(colors.BLACK, 12);
        boolean emptyAfter = map.isEmpty();

        // assert
        assertThat(empty).isTrue();
        assertThat(emptyAfter).isFalse();
    }

    @Test
    void containsKey() {
        // execute
        boolean containsKey = map.containsKey(colors.BLACK);
        map.put(colors.BLACK, 1);
        boolean containsKeyAfter = map.containsKey(colors.BLACK);

        // assert
        assertThat(containsKey).isFalse();
        assertThat(containsKeyAfter).isTrue();
    }

    @Test
    void containsValue() {
        // execute
        boolean containsVal = map.containsValue(15);
        map.put(colors.getByName("GOLD"), 15);
        boolean containsValAfter = map.containsValue(15);

        // assert
        assertThat(containsVal).isFalse();
        assertThat(containsValAfter).isTrue();
    }

    @Test
    void get_put() {
        // execute
        map.put(colors.WHITE, 1);
        map.put(colors.WHITE, 2);
        map.put(colors.WHITE, 3);
        map.put(colors.WHITE, 4);
        map.put(colors.getByName("GOLD"), 5);
        Integer white = map.get(colors.WHITE);
        Integer black = map.get(colors.BLACK);
        Integer gold = map.get(colors.getByName("GOLD"));

        // assert
        assertThat(white).isEqualTo(4);
        assertThat(gold).isEqualTo(5);
        assertThat(black).isNull();
    }

    @Test
    void remove() {
        // given
        map.put(colors.WHITE, 4);
        map.put(colors.BLACK, 5);

        // execute
        int size = map.size();
        map.remove(colors.WHITE);
        int sizeAfter = map.size();

        // assert
        assertThat(size).isEqualTo(2);
        assertThat(sizeAfter).isEqualTo(1);
    }

    @Test
    void clear() {
        // given
        map.put(colors.WHITE, 4);
        map.put(colors.BLACK, 5);

        // execute
        map.clear();
        int size = map.size();

        // assert
        assertThat(size).isEqualTo(0);
    }

    @Test
    void keySet() {
        // given
        map.put(colors.WHITE, 4);
        map.put(colors.BLACK, 5);

        // execute
        Set<Color> result = map.keySet();

        // assert
        assertThat(result).containsExactlyInAnyOrder(colors.BLACK, colors.WHITE);
    }

    @Test
    void values() {
        // given
        map.put(colors.WHITE, 4);
        map.put(colors.getByName("GOLD"), 5);

        // execute
        Collection<Integer> result = map.values();

        // assert
        assertThat(result).containsExactlyInAnyOrder(4, 5);
    }

    @Test
    void entrySet() {
        // given
        map.put(colors.WHITE, 4);
        map.put(colors.BLACK, 5);

        // execute
        Set<Map.Entry<Color, Integer>> result = map.entrySet();

        // assert
        assertThat(result).extracting(Map.Entry::getKey, Map.Entry::getValue)
                        .containsExactlyInAnyOrder(tuple(colors.WHITE, 4), tuple(colors.BLACK, 5));
    }
}