package space.controlnet.lightioc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static space.controlnet.lightioc.annotation.Constants.NULL;
import static space.controlnet.lightioc.annotation.Constants.Null;

/**
 * This decorator is used to annotate the fields in a class or Scala Object for autowiring.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Autowired {
    String stringId() default NULL;
    Class<?> classId() default Null.class;
}
