package space.controlnet.lightioc.api;

import scala.reflect.ClassTag;
import space.controlnet.lightioc.Container$;
import space.controlnet.lightioc.enumerate.ClassId;
import space.controlnet.lightioc.enumerate.StringId;

import java.util.function.Supplier;

public class Container {

    private static final Container$ container = Container$.MODULE$;

    public static final Container api = new Container();

    public static <T> BindingSetter<T> register(String identifier) {
        return new BindingSetter<>(container.register(new StringId(identifier)));
    }

    public static <T> BindingSetter<T> register(Class<T> identifier) {
        return new BindingSetter<>(container.register(ClassTag.apply(identifier)));
    }

    public static <T> T resolve(Class<T> identifier) {
        return container.resolve(ClassTag.apply(identifier));
    }

    public static <T> T resolve(String identifier, Class<T> type) {
        return container.resolve(new StringId(identifier), ClassTag.apply(type));
    }

    public static <T> T resolveOrElse(String identifier, T default_, Class<T> type) {
        return container.resolveOrElse(new StringId(identifier), default_, ClassTag.apply(type));
    }

    public static <T> T resolveOrElse(Class<T> identifier, T default_) {
        return container.resolveOrElse(new ClassId<>(identifier, ClassTag.apply(identifier)), default_,
                ClassTag.apply(identifier)
        );
    }

    public static boolean has(String identifier) {
        return container.has(new StringId(identifier));
    }

    public static <T> boolean has(Class<T> type) {
        return container.has(ClassTag.apply(type));
    }

    public static void init(String packageName) {
        init(packageName, false, Thread.currentThread()::getContextClassLoader);
    }

    public static void init(String packageName, boolean allStringId) {
        init(packageName, allStringId, Thread.currentThread()::getContextClassLoader);
    }

    public static void init(String packageName, Supplier<ClassLoader> classLoader) {
        init(packageName, false, classLoader);
    }

    public static void init(String packageName, boolean allStringId, Supplier<ClassLoader> classLoader) {
        container.init(packageName, allStringId, classLoader::get);
    }

    public static void reset() {
        container.reset();
    }
}
