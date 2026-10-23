package com.agrodirecto.auth.controller;

import com.agrodirecto.auth.dto.CambioAprobacion;
import com.agrodirecto.auth.dto.CambioEstado;
import com.agrodirecto.auth.dto.UsuarioDto;
import com.agrodirecto.auth.service.UsuarioService;
import com.agrodirecto.common.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Gestión de usuarios (solo administrador, ver SecurityConfig). */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<UsuarioDto> listar(@RequestParam(required = false) String rol,
                                   @RequestParam(required = false) String q) {
        return service.listar(rol, q);
    }

    @PatchMapping("/{id}/aprobacion")
    public UsuarioDto aprobacion(@PathVariable Long id, @Valid @RequestBody CambioAprobacion cambio) {
        return service.cambiarAprobacion(id, cambio.aprobado());
    }

    @PatchMapping("/{id}/estado")
    public UsuarioDto estado(@PathVariable Long id, @Valid @RequestBody CambioEstado cambio,
                             @AuthenticationPrincipal AuthUser admin) {
        return service.cambiarEstado(id, cambio.activo(), admin.id());
    }
}
