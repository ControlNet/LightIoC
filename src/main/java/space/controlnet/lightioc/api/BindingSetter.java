package space.controlnet.lightioc.api;


import scala.collection.mutable.ArrayBuffer;
import scala.reflect.ClassTag;
import space.controlnet.lightioc.ConstructorScopeSetter;
import space.controlnet.lightioc.FactoryScopeSetter;
import space.controlnet.lightioc.ServiceScopeSetter;
import space.controlnet.lightioc.ValueScopeSetter;
import space.controlnet.lightioc.enumerate.ClassId;
import space.controlnet.lightioc.enumerate.Identifier;
import space.controlnet.lightioc.enumerate.StringId;

import java.util.Arrays;
import java.util.function.Function;

public class BindingSetter<T> {
    space.controlnet.lightioc.BindingSetter<T> bindingSetter;

    BindingSetter(space.controlnet.lightioc.BindingSetter<T> bindingSetter) {
        this.bindingSetter = bindingSetter;
    }

    public <R extends T> ValueScopeSetter<T> to(Class<R> constructor) {
        return bindingSetter.to(constructor);
    }

    public ValueScopeSetter<T> toSelf() {
        return bindingSetter.toSelf();
    }

    public ConstructorScopeSetter<T> toConstructor(Class<?>... types) {
        ArrayBuffer<Identifier> identifiers = new ArrayBuffer<>();
        Arrays.stream(types).forEach(type -> identifiers.addOne(new ClassId<>(type, ClassTag.apply(type))));
        return bindingSetter.toConstructor(identifiers.toSeq());
    }

    public ValueScopeSetter<T> toValue(T value) {
        return bindingSetter.toValue(value);
    }

    public FactoryScopeSetter<T> toFactory(Function<Container, T> function) {
        return bindingSetter.toFactory(c -> function.apply(Container.api));
    }

    public <R> Container toService(String targetIdentifier) {
        bindingSetter.<R>toService(new StringId(targetIdentifier));
        return Container.api;
    }

    public <R> Container toService(Class<R> targetIdentifier) {
        bindingSetter.<R>toService(new ClassId<>(targetIdentifier, ClassTag.apply(targetIdentifier)));
        return Container.api;
    }
}
