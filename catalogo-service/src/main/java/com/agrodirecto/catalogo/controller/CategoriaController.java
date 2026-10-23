package com.agrodirecto.catalogo.controller;

import com.agrodirecto.catalogo.dto.CategoriaRequest;
import com.agrodirecto.catalogo.entity.Categoria;
import com.agrodirecto.catalogo.repository.CategoriaRepository;
import com.agrodirecto.common.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Mantenimiento de categorías. Lectura: cualquier usuario autenticado. Escritura: administrador. */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaRepository repo;

    public CategoriaController(CategoriaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Categoria> listar() {
        return repo.findAllByOrderByNombreAsc();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Categoria crear(@Valid @RequestBody CategoriaRequest r) {
        if (repo.existsByNombreIgnoreCase(r.nombre().trim())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre.");
        }
        Categoria c = new Categoria(r.nombre().trim(), r.descripcion());
        c.setActivo(r.activo() == null || r.activo());
        return repo.save(c);
    }

    @PutMapping("/{id}")
    public Categoria actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest r) {
        Categoria c = repo.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Categoría no encontrada."));
        c.setNombre(r.nombre().trim());
        c.setDescripcion(r.descripcion());
        if (r.activo() != null) {
            c.setActivo(r.activo());
        }
        return repo.save(c);
    }

    /** Las categorías en uso no se borran: se desactivan. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        Categoria c = repo.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Categoría no encontrada."));
        c.setActivo(false);
        repo.save(c);
    }
}
