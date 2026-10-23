package com.agrodirecto.auth.config;

import com.agrodirecto.common.security.CommonSecurity;
import com.agrodirecto.common.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http, JwtService jwt,
                                       @Value("${app.internal-key}") String claveInterna) throws Exception {
        CommonSecurity.base(http, jwt, claveInterna).authorizeHttpRequests(a -> a
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/registro", "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/distritos").permitAll()
                .requestMatchers("/api/distritos/**", "/api/usuarios/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/internal/**").hasRole("INTERNAL")
                .anyRequest().authenticated());
        return http.build();
    }
}
