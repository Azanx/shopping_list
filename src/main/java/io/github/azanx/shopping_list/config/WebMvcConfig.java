package io.github.azanx.shopping_list.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import io.github.azanx.shopping_list.controller.RedirectWhenAuthenticatedInterceptor;

/**
 * Configuration class for Spring MVC
 * @author Kamil Piwowarski
 *
 */
@Configuration
@ComponentScan("io.github.azanx.shopping_list.controller")
@EnableWebMvc
/*
 * @EnableSpringDataWebSupport // registers a DomainClassConverter to enable
 * Spring // MVC to resolve instances of repository managed // domain classes
 * from request parameters or path // variables. and
 * andlerMethodArgumentResolver // implementations to let Spring MVC resolve //
 * Pageable and Sort instances from request // parameters.
 */
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @PostConstruct
    public void init() {
        //disable setting requestAttributes (and whole model) during redirects as default behaviour
        requestMappingHandlerAdapter.setIgnoreDefaultModelOnRedirect(true);
    }

    @Bean
    public UrlBasedViewResolver urlBasedViewResolver() {
        UrlBasedViewResolver resolver = new UrlBasedViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        resolver.setViewClass(JstlView.class);
        return resolver;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/accessDenied").setStatusCode(HttpStatus.UNAUTHORIZED).setViewName("accessDenied");
        registry.addViewController("/internalError").setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR).setViewName("500_InternalError");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //redirect to home page if authenticated user tries to access login or register pages
        registry.addInterceptor(new RedirectWhenAuthenticatedInterceptor()).addPathPatterns("/login*", "/register*");
    }


}
