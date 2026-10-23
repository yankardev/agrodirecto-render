package com.agrodirecto.catalogo.controller;

import com.agrodirecto.catalogo.dto.ProductoDto;
import com.agrodirecto.catalogo.dto.ProductoRequest;
import com.agrodirecto.catalogo.service.ProductoService;
import com.agrodirecto.common.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Productos del agricultor autenticado. */
@RestController
@RequestMapping("/api/mis-productos")
public class MisProductosController {

    private final ProductoService service;

    public MisProductosController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductoDto> listar(@AuthenticationPrincipal AuthUser usuario) {
        return service.delAgricultor(usuario.id());
    }

    @GetMapping("/{id}")
    public ProductoDto obtener(@AuthenticationPrincipal AuthUser usuario, @PathVariable Long id) {
        return service.delAgricultor(usuario.id(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoDto crear(@AuthenticationPrincipal AuthUser usuario, @Valid @RequestBody ProductoRequest r) {
        return service.crear(usuario, r);
    }

    @PutMapping("/{id}")
    public ProductoDto actualizar(@AuthenticationPrincipal AuthUser usuario, @PathVariable Long id,
                                  @Valid @RequestBody ProductoRequest r) {
        return service.actualizar(usuario, id, r);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ocultar(@AuthenticationPrincipal AuthUser usuario, @PathVariable Long id) {
        service.ocultar(usuario.id(), id);
    }
}
