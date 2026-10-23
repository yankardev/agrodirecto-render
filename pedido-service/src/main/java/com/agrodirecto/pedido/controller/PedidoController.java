package com.agrodirecto.pedido.controller;

import com.agrodirecto.common.security.AuthUser;
import com.agrodirecto.pedido.dto.CambioEstadoRequest;
import com.agrodirecto.pedido.dto.PedidoDto;
import com.agrodirecto.pedido.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Pedidos recibidos por el agricultor autenticado. */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @GetMapping
    public List<PedidoDto> recibidos(@AuthenticationPrincipal AuthUser agricultor) {
        return service.delAgricultor(agricultor.id());
    }

    @PatchMapping("/{id}/estado")
    public PedidoDto cambiarEstado(@AuthenticationPrincipal AuthUser agricultor, @PathVariable Long id,
                                   @Valid @RequestBody CambioEstadoRequest r) {
        return service.cambiarEstado(id, r.estado(), agricultor);
    }
}
