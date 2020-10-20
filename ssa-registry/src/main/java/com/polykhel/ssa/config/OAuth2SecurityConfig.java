package com.polykhel.ssa.config;

import com.polykhel.ssa.utils.constants.AuthoritiesConstants;
import com.polykhel.ssa.security.oauth2.AudienceValidator;
import com.polykhel.ssa.security.oauth2.AuthorizationHeaderFilter;
import com.polykhel.ssa.security.oauth2.AuthorizationHeaderUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.polykhel.ssa.utils.constants.AuthoritiesConstants.ADMIN;
import static com.polykhel.ssa.utils.constants.ProfileConstants.PROFILE_OAUTH2;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Profile(PROFILE_OAUTH2)
public class OAuth2SecurityConfig extends WebSecurityConfigurerAdapter {
    private final String issuerUri;
    private final CoreProperties properties;

    public OAuth2SecurityConfig(
        @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}") String issuerUri,
        CoreProperties properties) {
        this.issuerUri = issuerUri;
        this.properties = properties;
    }

    /**
     * Configure users in memory.
     */
    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager(
        SecurityProperties properties, ObjectProvider<PasswordEncoder> passwordEncoder) {
        SecurityProperties.User user = properties.getUser();
        List<String> roles = user.getRoles();
        return new InMemoryUserDetailsManager(
            User.withUsername(user.getName())
                .password(getOrDeducePassword(user, passwordEncoder.getIfAvailable()))
                .roles(StringUtils.toStringArray(roles))
                .build());
    }

    private String getOrDeducePassword(SecurityProperties.User user, PasswordEncoder encoder) {
        if (encoder != null) {
            return user.getPassword();
        }
        return "{noop}" + user.getPassword();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
            .antMatchers("/app/**/*.{js,html}")
            .antMatchers("/swagger-ui/**")
            .antMatchers("/content/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors()
            .and()
            .csrf()
            .disable()
            .headers()
            .frameOptions()
            .disable()
            .and()
            .httpBasic()
            .realmName("SSA Registry")
            .and()
            .authorizeRequests()
            .antMatchers("/services/**")
            .authenticated()
            .antMatchers("/eureka/**")
            .hasAuthority(ADMIN)
            .antMatchers("/config/**")
            .hasAuthority(ADMIN)
            .antMatchers("/api/**")
            .hasAuthority(ADMIN)
            .antMatchers("/management/info/**")
            .permitAll()
            .antMatchers("/management/health/**")
            .permitAll()
            .antMatchers("/management/**")
            .hasAuthority(ADMIN)
            .antMatchers("/v2/api-docs/**")
            .permitAll()
            .antMatchers("/swagger-resources/configuration/**")
            .permitAll()
            .antMatchers("/swagger-ui/index.html")
            .hasAuthority(ADMIN)
            .and()
            .oauth2Login()
            .and()
            .oauth2ResourceServer()
            .jwt();
    }

    /**
     * Converts user authorities.
     */
    @Bean
    @SuppressWarnings("unchecked")
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(
                authority -> {
                    OidcUserInfo userInfo = null;
                    // Check for OidcUserAuthority
                    if (authority instanceof OidcUserAuthority) {
                        OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) authority;
                        userInfo = oidcUserAuthority.getUserInfo();
                    }
                    if (userInfo == null) {
                        mappedAuthorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
                    } else {
                        Map<String, Object> claims = userInfo.getClaims();
                        Collection<String> groups =
                            (Collection<String>)
                                claims.getOrDefault(
                                    "groups", claims.getOrDefault("roles", new ArrayList<>()));

                        mappedAuthorities.addAll(
                            groups.stream()
                                .filter(group -> group.startsWith("ROLE_"))
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList()));
                    }
                });

            return mappedAuthorities;
        };
    }

    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation(issuerUri);

        OAuth2TokenValidator<Jwt> audienceValidator =
            new AudienceValidator(properties.getSecurity().getOauth2().getAudience());
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withAudience =
            new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);

        return jwtDecoder;
    }

    @Bean
    public AuthorizationHeaderFilter authHeaderFilter(AuthorizationHeaderUtil headerUtil) {
        return new AuthorizationHeaderFilter(headerUtil);
    }
}
