package chy.spring.annotation.aop;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ChyAspect {
    String value() default "";
}
