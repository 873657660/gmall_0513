package com.atguigu.gmall.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jay
 * @create 2019-11-06 8:35
 */
@Target(ElementType.METHOD)  // 表示注解作用在方法上
@Retention(RetentionPolicy.RUNTIME) // 表示注解一直生存到jvm加载class文件之后
public @interface LoginRequire {
    // 默认为true，表示必须登录
    boolean autoRedirect() default true;
}
