package chy.spring.annotation.aop;

import java.lang.annotation.*;
import java.lang.reflect.Method;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ChyBefore {
    String value();
}
