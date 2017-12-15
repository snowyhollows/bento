package net.snowyhollows.bento2;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Created by fdreger on 6/25/2017.
 */
public class BentoTest {
    private enum Type { A, B }

    private static class Person {
        public Type type;
        public String name;
        public String lastName;
        public int height;

        public Person(Type type, String name, String lastName, int height) {
            this.type = type;
            this.name = name;
            this.lastName = lastName;
            this.height = height;
        }
    }

    final BentoFactory<Person> PERSON = new BentoFactory<Person>() {
        @Override
        public Person createInContext(Bento ctx) {
            return new Person(
                    ctx.getEnum(Type.class, "type"),
                    ctx.getString("name"),
                    ctx.getString("lastName"),
                    ctx.getInt("height")
            );

        }
    };

    Bento bento = Bento.createRoot();

    @Test(expected = BentoException.class)
    public void emptyBento() {
        bento.get("test");
    }

    @Test
    public void registerAndGet() {
        bento.register("liczba", 12);
        assertThat(bento.getInt("liczba")).isEqualTo(12);
        assertThat(bento.getFloat("liczba")).isEqualTo(12);
        assertThat(bento.getString("liczba")).isEqualTo("12");
    }

    @Test
    public void registerEnum() {
        bento.register("enum", Type.A);
        assertThat((Object)bento.get("enum")).isSameAs(Type.A);
        assertThat(bento.getString("enum")).isEqualTo("A");
    }

    @Test
    public void createChild() {
        bento.register("name", "Filip");
        bento.register("lastName", "Dreger");
        Bento child = this.bento.create();
        child.register("name", "Marta");

        assertThat(bento.getString("name")).isEqualTo("Filip");
        assertThat(child.getString("name")).isEqualTo("Marta");
        assertThat(child.getString("lastName")).isEqualTo("Dreger");
    }

    @Test
    public void inject() {
        bento.register("name", "Filip");
        bento.register("lastName", "Dreger");
        bento.register("type", Type.A);
        bento.register("height", 174);

        Person person = bento.get(PERSON);

        assertThat(person.name).isEqualTo("Filip");
        assertThat(person.lastName).isEqualTo("Dreger");
        assertThat(person.type).isSameAs(Type.A);
        assertThat(person.height).isEqualTo(174);
    }

    @Test
    public void sameInstanceWithSameFactory() {
        bento.register("name", "Filip");
        bento.register("lastName", "Dreger");
        bento.register("type", Type.A);
        bento.register("height", 174);

        Person person1 = bento.get(PERSON);
        Person person2 = bento.get(PERSON);

        assertThat(person1).isSameAs(person2);
    }

    @Test
    public void childCanOverrideInstance() {
        bento.register("name", "Filip");
        bento.register("lastName", "Dreger");
        bento.register("type", Type.A);
        bento.register("height", 174);

        Person person1 = bento.get(PERSON);
        Bento child = bento.create();
        child.registerObject(PERSON.getClass().getName(), new Person(null, null, null, 0));
        Person person2 = child.get(PERSON);

        assertThat(person1).isNotSameAs(
                person2);
    }

    @Test
    public void childUsesParentsInstanceWithGet() {
        bento.register("name", "Filip");
        bento.register("lastName", "Dreger");
        bento.register("type", Type.A);
        bento.register("height", 174);
        Person person1 = bento.get(PERSON);
        Person person2 = bento.create().get(PERSON);

        assertThat(person1).isSameAs(person2);
    }
}
