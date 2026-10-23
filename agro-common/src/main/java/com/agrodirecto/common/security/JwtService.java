package com.agrodirecto.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

/** Genera y valida tokens JWT firmados (HMAC). Todos los servicios comparten el mismo secreto. */
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMinutos;

    public JwtService(String secreto, long expiracionMinutos) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMinutos = expiracionMinutos;
    }

    public String generar(AuthUser usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(usuario.id()))
                .claim("email", usuario.email())
                .claim("nombre", usuario.nombre())
                .claim("rol", usuario.rol())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(expiracionMinutos, ChronoUnit.MINUTES)))
                .signWith(clave)
                .compact();
    }

    /** Devuelve el usuario si el token es válido y no expiró; vacío en cualquier otro caso. */
    public Optional<AuthUser> validar(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            return Optional.of(new AuthUser(
                    Long.valueOf(c.getSubject()),
                    c.get("email", String.class),
                    c.get("nombre", String.class),
                    c.get("rol", String.class)));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long getExpiracionMinutos() {
        return expiracionMinutos;
    }
}
