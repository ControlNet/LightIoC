package space.controlnet.lightioc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static space.controlnet.lightioc.annotation.Constants.NULL;
import static space.controlnet.lightioc.annotation.Constants.Null;

/**
 * This decorator is used to annotate the classes and scala objects to be scanned by LightIoC Container in
 * singleton scope. It should be used with `Container.init`.
 *
 * Note: Cannot work on companion object, and cannot work on non-public inner class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Singleton {
    String stringId() default NULL;
    Class<?> classId() default Null.class;
    boolean isObject() default false;
}
