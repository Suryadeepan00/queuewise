package com.example.queuewise.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.Customizer;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers(request ->
                        "POST".equals(request.getMethod())
                                && (
                                "/api/tokens".equals(request.getServletPath())
                                        || "/api/tokens/call-next".equals(request.getServletPath())
                                        || request.getServletPath().matches(
                                        "^/api/tokens/[^/]+/(complete|skip)$"
                                )
                        )
                ))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tokens/waiting").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tokens/events").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tokens/{tokenNumber}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/tokens").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/tokens/call-next").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/tokens/{tokenNumber}/complete").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/tokens/{tokenNumber}/skip").hasRole("ADMIN")
                        .anyRequest().denyAll()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder encoder,
            @org.springframework.beans.factory.annotation.Value("${queuewise.admin.password}")
            String adminPassword) {
        return new InMemoryUserDetailsManager(
                User.withUsername("admin")
                        .password(encoder.encode(adminPassword))
                        .roles("ADMIN")
                        .build()
        );
    }
}