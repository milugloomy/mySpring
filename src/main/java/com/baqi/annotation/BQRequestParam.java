package com.baqi.annotation;


import java.lang.annotation.*;

@Target({ElementType.PARAMETER})    //在参数上使用
@Retention(RetentionPolicy.RUNTIME) //运行时
@Documented
public @interface BQRequestParam {
    String value() default "";
    boolean required() default true;
}
