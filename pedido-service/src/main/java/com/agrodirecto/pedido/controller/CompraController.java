package com.agrodirecto.pedido.controller;

import com.agrodirecto.common.security.AuthUser;
import com.agrodirecto.pedido.dto.CheckoutRequest;
import com.agrodirecto.pedido.dto.CompraDto;
import com.agrodirecto.pedido.service.CheckoutService;
import com.agrodirecto.pedido.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final CheckoutService checkout;
    private final PedidoService pedidos;

    public CompraController(CheckoutService checkout, PedidoService pedidos) {
        this.checkout = checkout;
        this.pedidos = pedidos;
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public CompraDto finalizar(@AuthenticationPrincipal AuthUser cliente, @Valid @RequestBody CheckoutRequest r) {
        return checkout.finalizar(cliente, r);
    }

    @GetMapping
    public List<CompraDto> misCompras(@AuthenticationPrincipal AuthUser cliente) {
        return pedidos.comprasDelCliente(cliente.id());
    }

    @GetMapping("/{id}")
    public CompraDto detalle(@AuthenticationPrincipal AuthUser usuario, @PathVariable Long id) {
        return pedidos.compra(id, usuario);
    }
}
