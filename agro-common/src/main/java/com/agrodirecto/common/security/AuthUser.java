package com.agrodirecto.common.security;

/** Usuario autenticado, extraído del token JWT. */
public record AuthUser(Long id, String email, String nombre, String rol) {
}
