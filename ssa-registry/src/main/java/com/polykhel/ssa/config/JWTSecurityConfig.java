package com.polykhel.ssa.config;

import com.polykhel.ssa.security.UnauthorizedEntryPoint;
import com.polykhel.ssa.security.jwt.JwtConfigurer;
import com.polykhel.ssa.security.jwt.TokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import static com.polykhel.ssa.utils.constants.AuthoritiesConstants.ADMIN;
import static com.polykhel.ssa.utils.constants.ProfileConstants.PROFILE_OAUTH2;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Profile("!" + PROFILE_OAUTH2)
public class JWTSecurityConfig extends WebSecurityConfigurerAdapter {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final UnauthorizedEntryPoint authenticationEntryPoint;

    private final CoreProperties properties;

    private final String username;

    private final String password;

    private final String[] roles;

    public JWTSecurityConfig(
        @Value("${spring.security.user.name}") String username,
        @Value("${spring.security.user.password}") String password,
        @Value("${spring.security.user.roles}") String[] roles,
        AuthenticationManagerBuilder authenticationManagerBuilder,
        UnauthorizedEntryPoint authenticationEntryPoint,
        CoreProperties properties) {
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.properties = properties;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(
            User.withUsername(username)
                .password(passwordEncoder().encode(password))
                .roles(roles)
                .build());
        return manager;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
            .antMatchers("/app/**/*.{js,html}")
            .antMatchers("/swagger-ui/**")
            .antMatchers("/content/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors()
            .and()
            .exceptionHandling()
            .authenticationEntryPoint(authenticationEntryPoint)
            .and()
            .csrf()
            .disable()
            .headers()
            .frameOptions()
            .disable()
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
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
            .antMatchers("/api/authenticate")
            .permitAll()
            .antMatchers("/api/**")
            .hasAuthority(ADMIN)
            .antMatchers("/management/info")
            .permitAll()
            .antMatchers("/management/health")
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
            .apply(securityConfigurerAdapter());
    }

    private JwtConfigurer securityConfigurerAdapter() {
        return new JwtConfigurer(tokenProvider());
    }

    @Bean
    public TokenProvider tokenProvider() {
        return new TokenProvider(properties);
    }
}
