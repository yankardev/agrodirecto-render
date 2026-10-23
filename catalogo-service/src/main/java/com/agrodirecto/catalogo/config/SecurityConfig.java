package com.agrodirecto.catalogo.config;

import com.agrodirecto.common.security.CommonSecurity;
import com.agrodirecto.common.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http, JwtService jwt,
                                       @Value("${app.internal-key}") String claveInterna) throws Exception {
        CommonSecurity.base(http, jwt, claveInterna).authorizeHttpRequests(a -> a
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/internal/**").hasRole("INTERNAL")
                .requestMatchers(HttpMethod.GET, "/api/categorias", "/api/unidades").authenticated()
                .requestMatchers("/api/categorias/**", "/api/unidades/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/catalogo/**").hasAnyRole("CLIENTE", "ADMINISTRADOR")
                .requestMatchers("/api/mis-productos/**").hasRole("AGRICULTOR")
                .anyRequest().authenticated());
        return http.build();
    }
}
