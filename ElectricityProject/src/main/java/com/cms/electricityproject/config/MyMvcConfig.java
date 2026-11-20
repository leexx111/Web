package com.cms.electricityproject.config;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
/**
 * 配置 MVC 跨域和静态资源映射。
 */
public class MyMvcConfig implements WebMvcConfigurer {
    /**
     * springboot 无法直接访问静态资源，需要放开资源访问路径。
     * 添加静态资源文件，外部可以直接访问地址
     *
     * @param registry
//     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/templates/");
        registry.addResourceHandler("/templates/**")
                .addResourceLocations("classpath:/templates/");
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
//        registry.addResourceHandler("/**")
//                .addResourceLocations("classpath:/static/");
    }

//    //配置视图组件
//    @Bean
//    public WebMvcConfigurerAdapter webMvcConfigurerAdapter(){
//        WebMvcConfigurerAdapter adapter = new WebMvcConfigurerAdapter() {
//            @Override
//            public void addViewControllers(ViewControllerRegistry registry) {
////                registry.addViewController("/").setViewName("login");
////                registry.addViewController("/templates/login.html").setViewName("login");
//                registry.addViewController("/templates/index.html").setViewName("index");
//                registry.addViewController("/templates/LengJiZu.html").setViewName("LengJiZu");
//            }
//            //注册拦截器
//            @Override
//            public void addInterceptors(InterceptorRegistry registry) {
//                /**
//                 * addPathPatterns：拦截哪些请求  .addPathPatterns("/**")
//                 * excludePathPatterns：不拦截哪些请求
//                 */
//                registry.addInterceptor(new LoginHandlerInterceptor()).
//                        excludePathPatterns("/login.html","/login","/logout","/static/**");
//            }
//        };
//        return adapter;
//    }
}
