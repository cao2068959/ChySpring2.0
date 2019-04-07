package chy.spring.annotation.aop;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ChyAfter {
    String value();
}
