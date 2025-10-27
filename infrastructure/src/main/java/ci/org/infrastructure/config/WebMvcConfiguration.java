package ci.org.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration Spring MVC pour enregistrer les intercepteurs personnalisés.
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;

    public WebMvcConfiguration(LoggingInterceptor loggingInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/**");  // Logger uniquement les endpoints API
    }
}