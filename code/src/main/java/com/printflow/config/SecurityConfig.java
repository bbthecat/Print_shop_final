package com.printflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String STAFF = "STAFF";

    private static final String[] SWAGGER_PATHS = {
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
    };

    private static final String[] PUBLIC_PAGES = {
            "/", "/login", "/register", "/services", "/css/**", "/error"
    };

    // ทุกคนดูรายการบริการได้ แต่เพิ่ม/แก้/ลบได้เฉพาะ ADMIN
    private static final String[] CATALOG_PATHS = {
            "/api/v1/services", "/api/v1/services/**",
            "/api/v1/addon-services", "/api/v1/addon-services/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API is stateless (HTTP Basic); Thymeleaf forms keep CSRF protection
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SWAGGER_PATHS).permitAll()
                        .requestMatchers(PUBLIC_PAGES).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/customers").permitAll()
                        .requestMatchers("/api/v1/customers/me").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/customers/**").hasRole(ADMIN)
                        .requestMatchers("/api/v1/customers", "/api/v1/customers/**").hasAnyRole(STAFF, ADMIN)
                        .requestMatchers(HttpMethod.GET, CATALOG_PATHS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/promotions", "/api/v1/promotions/**").authenticated()
                        .requestMatchers(CATALOG_PATHS).hasRole(ADMIN)
                        .requestMatchers("/api/v1/promotions", "/api/v1/promotions/**").hasRole(ADMIN)
                        .requestMatchers("/api/v1/reports/**", "/api/v1/admin/**", "/admin/**").hasRole(ADMIN)
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", false))
                .logout(logout -> logout.logoutSuccessUrl("/login?logout"));
        return http.build();
    }
}
