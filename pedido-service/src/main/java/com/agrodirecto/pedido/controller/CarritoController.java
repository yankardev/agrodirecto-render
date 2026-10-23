package com.agrodirecto.pedido.controller;

import com.agrodirecto.common.security.AuthUser;
import com.agrodirecto.pedido.dto.AgregarCarritoRequest;
import com.agrodirecto.pedido.dto.CambiarCantidadRequest;
import com.agrodirecto.pedido.dto.CarritoDto;
import com.agrodirecto.pedido.service.CarritoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    private final CarritoService service;

    public CarritoController(CarritoService service) {
        this.service = service;
    }

    @GetMapping
    public CarritoDto ver(@AuthenticationPrincipal AuthUser cliente) {
        return service.ver(cliente.id());
    }

    @PostMapping
    public CarritoDto agregar(@AuthenticationPrincipal AuthUser cliente,
                              @RequestHeader("Authorization") String authorization,
                              @Valid @RequestBody AgregarCarritoRequest r) {
        return service.agregar(cliente.id(), r.productoId(), r.cantidad(), authorization);
    }

    @PutMapping("/{productoId}")
    public CarritoDto cambiarCantidad(@AuthenticationPrincipal AuthUser cliente, @PathVariable Long productoId,
                                      @Valid @RequestBody CambiarCantidadRequest r) {
        return service.cambiarCantidad(cliente.id(), productoId, r.cantidad());
    }

    @DeleteMapping("/{productoId}")
    public CarritoDto quitar(@AuthenticationPrincipal AuthUser cliente, @PathVariable Long productoId) {
        return service.quitar(cliente.id(), productoId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void vaciar(@AuthenticationPrincipal AuthUser cliente) {
        service.vaciar(cliente.id());
    }
}
