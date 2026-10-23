package com.agrodirecto.catalogo.controller;

import com.agrodirecto.catalogo.dto.UnidadRequest;
import com.agrodirecto.catalogo.entity.UnidadMedida;
import com.agrodirecto.catalogo.repository.UnidadMedidaRepository;
import com.agrodirecto.common.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Mantenimiento de unidades de medida. Lectura: cualquier usuario autenticado. Escritura: administrador. */
@RestController
@RequestMapping("/api/unidades")
public class UnidadMedidaController {

    private final UnidadMedidaRepository repo;

    public UnidadMedidaController(UnidadMedidaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<UnidadMedida> listar() {
        return repo.findAllByOrderByNombreAsc();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UnidadMedida crear(@Valid @RequestBody UnidadRequest r) {
        if (repo.existsByNombreIgnoreCase(r.nombre().trim())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Ya existe una unidad con ese nombre.");
        }
        UnidadMedida u = new UnidadMedida(r.nombre().trim(), r.abreviatura().trim());
        u.setActivo(r.activo() == null || r.activo());
        return repo.save(u);
    }

    @PutMapping("/{id}")
    public UnidadMedida actualizar(@PathVariable Long id, @Valid @RequestBody UnidadRequest r) {
        UnidadMedida u = repo.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Unidad no encontrada."));
        u.setNombre(r.nombre().trim());
        u.setAbreviatura(r.abreviatura().trim());
        if (r.activo() != null) {
            u.setActivo(r.activo());
        }
        return repo.save(u);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        UnidadMedida u = repo.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Unidad no encontrada."));
        u.setActivo(false);
        repo.save(u);
    }
}
