package com.fastcampus.minischeduler.manager.login;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoggerInterceptor())
                .excludePathPatterns("/css/**", "/images/**", "/js/**");

        registry.addInterceptor(new LoginCheckInterceptor())
                .addPathPatterns("/**/logout")
                .excludePathPatterns("/log*");

        WebMvcConfigurer.super.addInterceptors(registry);
    }
}
