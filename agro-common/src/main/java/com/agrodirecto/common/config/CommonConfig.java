package com.agrodirecto.common.config;

import com.agrodirecto.common.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {

    @Bean
    public JwtService jwtService(@Value("${app.jwt.secret}") String secreto,
                                 @Value("${app.jwt.expiracion-minutos:240}") long expiracionMinutos) {
        return new JwtService(secreto, expiracionMinutos);
    }
}
