package com.agrodirecto.catalogo.controller;

import com.agrodirecto.catalogo.service.ProductoService;
import com.agrodirecto.catalogo.service.StockService;
import com.agrodirecto.common.contrato.ItemReservado;
import com.agrodirecto.common.contrato.ReservaRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints para otros microservicios. El API Gateway no los expone. */
@RestController
@RequestMapping("/internal")
public class InternalController {

    private final StockService stock;
    private final ProductoService productos;

    public InternalController(StockService stock, ProductoService productos) {
        this.stock = stock;
        this.productos = productos;
    }

    @PostMapping("/stock/reservar")
    public List<ItemReservado> reservar(@Valid @RequestBody ReservaRequest request) {
        return stock.reservar(request.items());
    }

    @PostMapping("/stock/liberar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void liberar(@Valid @RequestBody ReservaRequest request) {
        stock.liberar(request.items());
    }

    @PutMapping("/agricultores/{id}/habilitacion")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void habilitacion(@PathVariable Long id, @RequestParam boolean habilitado) {
        productos.habilitarDelAgricultor(id, habilitado);
    }
}
