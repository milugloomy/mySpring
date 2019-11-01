package com.baqi.annotation;


import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD}) //在类和方法上使用
@Retention(RetentionPolicy.RUNTIME)             //运行时
@Documented
public @interface BQRequestMapping {
    String value() default "";
}
