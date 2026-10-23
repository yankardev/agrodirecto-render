package com.agrodirecto.auth.controller;

import com.agrodirecto.auth.dto.AuthResponse;
import com.agrodirecto.auth.dto.LoginRequest;
import com.agrodirecto.auth.dto.RegistroRequest;
import com.agrodirecto.auth.dto.UsuarioDto;
import com.agrodirecto.auth.service.AuthService;
import com.agrodirecto.common.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto registro(@Valid @RequestBody RegistroRequest request) {
        return service.registrar(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }

    @GetMapping("/me")
    public UsuarioDto me(@AuthenticationPrincipal AuthUser usuario) {
        return service.perfil(usuario.id());
    }
}
