package space.controlnet.lightioc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Cannot work on companion object
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Provider {
    String stringId() default Helpers.NULL;
    Class<?> classId() default Helpers.Null.class;
    boolean isObject() default false;
}
