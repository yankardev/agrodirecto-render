package com.agrodirecto.auth.dto;

public record AuthResponse(String token, long expiraEnMinutos, UsuarioDto usuario) {
}
