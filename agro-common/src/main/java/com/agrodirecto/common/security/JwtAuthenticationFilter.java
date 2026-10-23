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
import java.util.List;

/** Lee el token "Authorization: Bearer ..." y autentica la petición con el rol del usuario. */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwt;

    public JwtAuthenticationFilter(JwtService jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String token = extraerToken(req);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            jwt.validar(token).ifPresent(u -> {
                var auth = new UsernamePasswordAuthenticationToken(
                        u, null, List.of(new SimpleGrantedAuthority("ROLE_" + u.rol())));
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }
        chain.doFilter(req, res);
    }

    private String extraerToken(HttpServletRequest req) {
        String cabecera = req.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            return cabecera.substring(7);
        }
        // EventSource (avisos en tiempo real) no permite cabeceras: acepta el token por parámetro.
        if (req.getRequestURI().endsWith("/stream")) {
            return req.getParameter("token");
        }
        return null;
    }

    /** Necesario para que los avisos SSE (peticiones asíncronas) conserven la autenticación. */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }
}
