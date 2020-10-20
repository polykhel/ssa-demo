package com.polykhel.ssa.config;

import com.polykhel.ssa.utils.constants.PropertyDefaults;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.web.cors.CorsConfiguration;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Properties for SSA configured in the {@code application.yml}.
 *
 * <p>Also loads properties from the git.properties and META-INF/build-info.properties files if they
 * are found in the classpath.
 */
@ConfigurationProperties(prefix = "core", ignoreUnknownFields = false)
@PropertySources({
    @PropertySource(value = "classpath:git.properties", ignoreResourceNotFound = true),
    @PropertySource(value = "classpath:META-INF/build-info.properties", ignoreResourceNotFound = true)
})
@Getter
public class CoreProperties {

    private final Async async = new Async();

    private final Http http = new Http();

    private final Cache cache = new Cache();

    private final Mail mail = new Mail();

    private final Security security = new Security();

    private final Swagger swagger = new Swagger();

    private final Metrics metrics = new Metrics();

    private final Logging logging = new Logging();

    private final CorsConfiguration cors = new CorsConfiguration();

    private final Social social = new Social();

    private final Gateway gateway = new Gateway();

    private final Registry registry = new Registry();

    private final ClientApp clientApp = new ClientApp();

    private final AuditEvents auditEvents = new AuditEvents();

    @Getter
    @Setter
    public static class Async {
        private int corePoolSize = PropertyDefaults.Async.corePoolSize;

        private int maxPoolSize = PropertyDefaults.Async.maxPoolSize;

        private int queueCapacity = PropertyDefaults.Async.queueCapacity;
    }

    @Getter
    public static class Http {
        private final Cache cache = new Cache();

        @Getter
        @Setter
        public static class Cache {
            private int timeToLiveInDays = PropertyDefaults.Http.Cache.timeToLiveInDays;
        }
    }

    @Getter
    public static class Cache {
        private final Hazelcast hazelcast = new Hazelcast();

        private final Redis redis = new Redis();

        @Getter
        @Setter
        public static class Hazelcast {
            private final ManagementCenter managementCenter = new ManagementCenter();
            private int timeToLiveSeconds = PropertyDefaults.Cache.Hazelcast.timeToLiveSeconds;
            private int backupCount = PropertyDefaults.Cache.Hazelcast.backupCount;

            @Getter
            @Setter
            public static class ManagementCenter {
                private boolean enabled = PropertyDefaults.Cache.Hazelcast.ManagementCenter.enabled;

                private int updateInterval =
                    PropertyDefaults.Cache.Hazelcast.ManagementCenter.updateInterval;

                private String url = PropertyDefaults.Cache.Hazelcast.ManagementCenter.url;
            }
        }

        @Getter
        @Setter
        public static class Redis {
            private String[] server = PropertyDefaults.Cache.Redis.server;
            private int expiration = PropertyDefaults.Cache.Redis.expiration;
            private boolean cluster = PropertyDefaults.Cache.Redis.cluster;
        }
    }

    @Getter
    @Setter
    public static class Mail {
        private boolean enabled = PropertyDefaults.Mail.enabled;

        private String from = PropertyDefaults.Mail.from;

        private String baseUrl = PropertyDefaults.Mail.baseUrl;
    }

    @Getter
    public static class Security {
        private final ClientAuthorization clientAuthorization = new ClientAuthorization();

        private final Authentication authentication = new Authentication();

        private final RememberMe rememberMe = new RememberMe();

        private final OAuth2 oauth2 = new OAuth2();

        @Getter
        @Setter
        public static class ClientAuthorization {
            private String accessTokenUri = PropertyDefaults.Security.ClientAuthorization.accessTokenUri;

            private String tokenServiceId = PropertyDefaults.Security.ClientAuthorization.tokenServiceId;

            private String clientId = PropertyDefaults.Security.ClientAuthorization.clientId;

            private String clientSecret = PropertyDefaults.Security.ClientAuthorization.clientSecret;
        }

        @Getter
        public static class Authentication {
            private final Jwt jwt = new Jwt();

            @Getter
            @Setter
            public static class Jwt {
                private String base64Secret = PropertyDefaults.Security.Authentication.Jwt.base64Secret;

                private long tokenValidityInSeconds =
                    PropertyDefaults.Security.Authentication.Jwt.tokenValidityInSeconds;

                private long tokenValidityInSecondsForRememberMe =
                    PropertyDefaults.Security.Authentication.Jwt.tokenValidityInSecondsForRememberMe;
            }
        }

        @Getter
        @Setter
        public static class RememberMe {
            @NotNull
            private String key = PropertyDefaults.Security.RememberMe.key;
        }

        @Getter
        @Setter
        public static class OAuth2 {
            private List<String> audience = new ArrayList<>();
        }
    }

    @Getter
    @Setter
    public static class Swagger {
        private String title = PropertyDefaults.Swagger.title;

        private String description = PropertyDefaults.Swagger.description;

        private String version = PropertyDefaults.Swagger.version;

        private String termsOfServiceUrl = PropertyDefaults.Swagger.termsOfServiceUrl;

        private String contactName = PropertyDefaults.Swagger.contactName;

        private String contactUrl = PropertyDefaults.Swagger.contactUrl;

        private String contactEmail = PropertyDefaults.Swagger.contactEmail;

        private String license = PropertyDefaults.Swagger.license;

        private String licenseUrl = PropertyDefaults.Swagger.licenseUrl;

        private String defaultIncludePattern = PropertyDefaults.Swagger.defaultIncludePattern;

        private String host = PropertyDefaults.Swagger.host;

        private String[] protocols = PropertyDefaults.Swagger.protocols;

        private boolean useDefaultResponseMessages =
            PropertyDefaults.Swagger.useDefaultResponseMessages;
    }

    @Getter
    public static class Metrics {
        private final Logs logs = new Logs();

        @Getter
        @Setter
        public static class Logs {
            private boolean enabled = PropertyDefaults.Metrics.Logs.enabled;

            private long reportFrequency = PropertyDefaults.Metrics.Logs.reportFrequency;
        }
    }

    @Getter
    @Setter
    public static class Logging {

        private final Logstash logstash = new Logstash();
        private boolean useJsonFormat = PropertyDefaults.Logging.useJsonFormat;

        @Getter
        @Setter
        public static class Logstash {
            private boolean enabled = PropertyDefaults.Logging.Logstash.enabled;

            private String host = PropertyDefaults.Logging.Logstash.host;

            private int port = PropertyDefaults.Logging.Logstash.port;

            private int queueSize = PropertyDefaults.Logging.Logstash.queueSize;
        }
    }

    @Getter
    @Setter
    public static class Social {
        private String redirectAfterSignIn = PropertyDefaults.Social.redirectAfterSignIn;
    }

    @Getter
    @Setter
    public static class Gateway {
        private final RateLimiting rateLimiting = new RateLimiting();

        private Map<String, List<String>> authorizedMicroservicesEndpoints =
            PropertyDefaults.Gateway.authorizedMicroservicesEndpoints;

        @Getter
        @Setter
        public static class RateLimiting {
            private boolean enabled = PropertyDefaults.Gateway.RateLimiting.enabled;

            private long limit = PropertyDefaults.Gateway.RateLimiting.limit;

            private int durationInSeconds = PropertyDefaults.Gateway.RateLimiting.durationInSeconds;
        }
    }

    @Getter
    @Setter
    public static class Registry {
        private String password = PropertyDefaults.Registry.password;
    }

    @Getter
    @Setter
    public static class ClientApp {
        private String name = PropertyDefaults.ClientApp.name;
    }

    @Getter
    @Setter
    public static class AuditEvents {
        private int retentionPeriod = PropertyDefaults.AuditEvents.retentionPeriod;
    }
}
