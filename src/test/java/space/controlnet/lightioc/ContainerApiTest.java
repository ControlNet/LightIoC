package space.controlnet.lightioc;

import org.junit.jupiter.api.Test;
import space.controlnet.lightioc.annotation.Provider;
import space.controlnet.lightioc.api.Container;

import static org.junit.jupiter.api.Assertions.*;


class ContainerApiTest {

    @Provider
    public static class Foo {}

    public static class Bar {
        Foo foo;
        String str;

        public Bar(Foo foo, String str) {
            this.foo = foo;
            this.str = str;
        }
    }

    @Test
    void api() {
        Container.init("space.controlnet.lightioc");
        Container.register(String.class).toValue("str").inSingletonScope();
        Container.register(Bar.class).toConstructor(Foo.class, String.class).inSingletonScope();
        String str = Container.resolve(Bar.class).str;
        assertEquals(str, "str");
    }
}