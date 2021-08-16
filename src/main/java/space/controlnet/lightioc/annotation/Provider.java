package space.controlnet.lightioc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static space.controlnet.lightioc.annotation.Constants.NULL;
import static space.controlnet.lightioc.annotation.Constants.Null;


/**
 * Cannot work on companion object
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Provider {
    String stringId() default NULL;
    Class<?> classId() default Null.class;
    boolean isObject() default false;
}
