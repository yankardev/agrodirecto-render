package com.agrodirecto.catalogo.controller;

import com.agrodirecto.catalogo.dto.ProductoDto;
import com.agrodirecto.catalogo.service.ProductoService;
import com.agrodirecto.common.dto.PageResponse;
import org.springframework.web.bind.annotation.*;

/** Catálogo de productos para Cliente y Administrador (el agricultor no ve los precios de los demás). */
@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    private final ProductoService service;

    public CatalogoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<ProductoDto> listar(@RequestParam(required = false) String q,
                                            @RequestParam(required = false) Long categoriaId,
                                            @RequestParam(defaultValue = "0") int pagina,
                                            @RequestParam(defaultValue = "12") int tamano) {
        return service.catalogo(q, categoriaId, pagina, tamano);
    }

    @GetMapping("/{id}")
    public ProductoDto obtener(@PathVariable Long id) {
        return service.obtenerVisible(id);
    }
}
