package com.agrodirecto.common.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Configuración de seguridad común: sin sesión, sin CSRF (API con JWT) y errores 401/403 en JSON. */
public final class CommonSecurity {

    private CommonSecurity() {
    }

    public static HttpSecurity base(HttpSecurity http, JwtService jwt, String claveInterna) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new InternalKeyFilter(claveInterna), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(jwt), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                escribir(res, 401, "No autenticado. Inicia sesión para continuar."))
                        .accessDeniedHandler((req, res, ex) ->
                                escribir(res, 403, "No tienes permiso para realizar esta acción.")));
        return http;
    }

    private static void escribir(HttpServletResponse res, int estado, String mensaje) throws IOException {
        res.setStatus(estado);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"status\":" + estado + ",\"message\":\"" + mensaje + "\"}");
    }
}
