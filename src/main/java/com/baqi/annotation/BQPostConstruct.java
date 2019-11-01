package com.baqi.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})       //在方法上使用
@Retention(RetentionPolicy.RUNTIME) //运行时
@Documented
public @interface BQPostConstruct {
}
