package com.atguigu.gmall.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


/**
 * springmvc.xml
 *  <mvc:interceptors>
 *     拦截所有请求
 *     <bean class="com.atguigu.gmall0513.config.AuthInterceptor">
 *  </mvc:interceptors>
 * <mvc:interceptors>
 *     <mvc:interceptor>
 *             拦截所有请求
 *             <bean class="com.atguigu.gmall0513.config.AuthInterceptor">
 *             <mvc:mapping path="/**">
 *     </mvc:interceptor>
 *  </mvc:interceptors>
 */
// 相当于WebMvcConfiguration.xml
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {
    // 获取自定义拦截器
    @Autowired
    private AuthInterceptor authInterceptor;

    public void addInterceptors(InterceptorRegistry registry) {
        // 设置自定义拦截器请求
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");
        // 将拦截器放入拦截器栈
        super.addInterceptors(registry);

    }


}
