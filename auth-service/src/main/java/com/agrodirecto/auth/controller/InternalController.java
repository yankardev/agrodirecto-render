package com.agrodirecto.auth.controller;

import com.agrodirecto.auth.service.UsuarioService;
import com.agrodirecto.common.contrato.EstadoAprobacion;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints para otros microservicios. No se exponen por el API Gateway. */
@RestController
@RequestMapping("/internal")
public class InternalController {

    private final UsuarioService service;

    public InternalController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping("/usuarios/{id}/aprobado")
    public EstadoAprobacion aprobado(@PathVariable Long id) {
        return new EstadoAprobacion(service.agricultorAprobado(id));
    }
}
