package com.agrodirecto.auth.controller;

import com.agrodirecto.auth.dto.DistritoRequest;
import com.agrodirecto.auth.entity.Distrito;
import com.agrodirecto.auth.repository.DistritoRepository;
import com.agrodirecto.common.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Mantenimiento de distritos. El listado es público (lo usa el formulario de registro). */
@RestController
@RequestMapping("/api/distritos")
public class DistritoController {

    private final DistritoRepository repo;

    public DistritoController(DistritoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Distrito> listar() {
        return repo.findAllByOrderByNombreAsc();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Distrito crear(@Valid @RequestBody DistritoRequest r) {
        if (repo.existsByNombreIgnoreCase(r.nombre().trim())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Ese distrito ya existe.");
        }
        Distrito d = new Distrito(r.nombre().trim());
        d.setActivo(r.activo() == null || r.activo());
        return repo.save(d);
    }

    @PutMapping("/{id}")
    public Distrito actualizar(@PathVariable Long id, @Valid @RequestBody DistritoRequest r) {
        Distrito d = repo.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Distrito no encontrado."));
        d.setNombre(r.nombre().trim());
        if (r.activo() != null) {
            d.setActivo(r.activo());
        }
        return repo.save(d);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Distrito no encontrado.");
        }
        repo.deleteById(id);
    }
}
