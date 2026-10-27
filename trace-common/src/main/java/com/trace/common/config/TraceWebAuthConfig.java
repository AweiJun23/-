package com.trace.common.config;

import com.trace.common.web.AuthInterceptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 微服务通用 Web 配置：注册鉴权拦截器，写入 UserContext
 * <p>
 * 各服务可通过 trace.auth.exclude-paths 追加白名单路径。
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = "org.springframework.web.servlet.config.annotation.WebMvcConfigurer")
public class TraceWebAuthConfig implements WebMvcConfigurer {

    @Value("${trace.auth.exclude-paths:/error,/actuator/**,/api/auth/**,/api/trace/public/**,/api/file/public/**}")
    private String[] excludePaths;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> excludes = new ArrayList<>(Arrays.asList(excludePaths));
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(excludes);
    }
}
