package com.agrodirecto.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Protege los endpoints /internal/** (llamadas entre microservicios).
 * El API Gateway nunca enruta /internal, así que solo se alcanzan desde la red interna con la clave compartida.
 */
public class InternalKeyFilter extends OncePerRequestFilter {

    private final byte[] clave;

    public InternalKeyFilter(String clave) {
        this.clave = clave.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        if (req.getRequestURI().startsWith("/internal/")) {
            String recibida = req.getHeader("X-Internal-Key");
            if (recibida != null && MessageDigest.isEqual(clave, recibida.getBytes(StandardCharsets.UTF_8))) {
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                        "servicio-interno", null, List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))));
            }
        }
        chain.doFilter(req, res);
    }
}
