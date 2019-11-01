package com.baqi.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})         //在类上使用
@Retention(RetentionPolicy.RUNTIME) //运行时
@Documented
public @interface BQComponentScan {
    String value() default "";
}
