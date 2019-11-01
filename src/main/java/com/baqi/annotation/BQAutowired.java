package com.baqi.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD})        //在Filed上使用
@Retention(RetentionPolicy.RUNTIME) //运行时
@Documented
public @interface BQAutowired {
    String value() default "";
}
