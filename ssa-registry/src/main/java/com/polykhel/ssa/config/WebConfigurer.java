package com.polykhel.ssa.config;

import com.polykhel.ssa.web.filter.CachingHttpHeadersFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.web.server.WebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.EnumSet;

import static com.polykhel.ssa.utils.constants.ProfileConstants.SPRING_PROFILE_PRODUCTION;
import static java.net.URLDecoder.decode;

/**
 * Configuration of web application with Servlet 3.0 APIS.
 */
@Configuration
@Slf4j
public class WebConfigurer
    implements ServletContextInitializer, WebServerFactoryCustomizer<WebServerFactory> {

    private final Environment env;

    private final CoreProperties coreProperties;

    public WebConfigurer(Environment env, CoreProperties coreProperties) {
        this.env = env;
        this.coreProperties = coreProperties;
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        if (env.getActiveProfiles().length != 0) {
            log.info(
                "Web application configuration, using profiles: {}", (Object[]) env.getActiveProfiles());
        }
        EnumSet<DispatcherType> dispatcherTypes =
            EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC);
        if (env.acceptsProfiles(Profiles.of(SPRING_PROFILE_PRODUCTION))) {
            initCachingHttpHeadersFilter(servletContext, dispatcherTypes);
        }
        log.info("Web application fully configured.");
    }

    /**
     * Initializes the caching HTTP Headers Filter.
     */
    private void initCachingHttpHeadersFilter(
        ServletContext servletContext, EnumSet<DispatcherType> dispatcherTypes) {
        log.debug("Registering Caching HTTP Headers Filter");
        FilterRegistration.Dynamic cachingHttpHeadersFilter =
            servletContext.addFilter(
                "cachingHttpHeadersFilter", new CachingHttpHeadersFilter(coreProperties));

        cachingHttpHeadersFilter.addMappingForUrlPatterns(dispatcherTypes, true, "i18n/*");
        cachingHttpHeadersFilter.addMappingForUrlPatterns(dispatcherTypes, true, "content/*");
        cachingHttpHeadersFilter.addMappingForUrlPatterns(dispatcherTypes, true, "app/*");
        cachingHttpHeadersFilter.setAsyncSupported(true);
    }

    @Override
    public void customize(WebServerFactory server) {
        // When running in an IDE or with ./mvnw spring-boot:run, set location of the static web assets.
        setLocationForStaticAssets(server);
    }

    private void setLocationForStaticAssets(WebServerFactory server) {
        if (server instanceof ConfigurableServletWebServerFactory) {
            ConfigurableServletWebServerFactory servletWebServer =
                (ConfigurableServletWebServerFactory) server;
            String prefixPath = resolvePathPrefix();
            File root = new File(prefixPath + "target/classes/static");
            if (root.exists() && root.isDirectory()) {
                servletWebServer.setDocumentRoot(root);
            }
        }
    }

    /**
     * Resolve path prefix to static resources.
     */
    private String resolvePathPrefix() {
        String fullExecutablePath;
        try {
            fullExecutablePath =
                decode(this.getClass().getResource("").getPath(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            fullExecutablePath = this.getClass().getResource("").getPath();
        }
        String rootPath = Paths.get(".").toUri().normalize().getPath();
        String extractedPath = fullExecutablePath.replace(rootPath, "");
        int extractionEndIndex = extractedPath.indexOf("target/");
        if (extractionEndIndex <= 0) {
            return "";
        }
        return extractedPath.substring(0, extractionEndIndex);
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = coreProperties.getCors();
        if (ObjectUtils.isNotEmpty(config.getAllowedOrigins())) {
            log.debug("Registering CORS filter");
            source.registerCorsConfiguration("/api/**", config);
            source.registerCorsConfiguration("/management/**", config);
            source.registerCorsConfiguration("/v2/api-docs/**", config);
            source.registerCorsConfiguration("/config/**", config);
            source.registerCorsConfiguration("/eureka/**", config);
            source.registerCorsConfiguration("/*/api/**", config);
            source.registerCorsConfiguration("/services/*/api/**", config);
            source.registerCorsConfiguration("/*/management/**", config);
        }

        // default is to deny all CORS requests
        source.registerCorsConfiguration("/**", new CorsConfiguration());
        return new CorsFilter(source);
    }
}
