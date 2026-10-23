package com.agrodirecto.catalogo.dto;

import com.agrodirecto.catalogo.entity.Producto;

import java.math.BigDecimal;

public record ProductoDto(Long id, String nombre, String descripcion, BigDecimal precio, int stock,
                          Long categoriaId, String categoria, Long unidadId, String unidad, String unidadAbrev,
                          Long agricultorId, String agricultorNombre, boolean activo, boolean habilitado) {

    public static ProductoDto de(Producto p) {
        return new ProductoDto(p.getId(), p.getNombre(), p.getDescripcion(), p.getPrecio(), p.getStock(),
                p.getCategoria().getId(), p.getCategoria().getNombre(),
                p.getUnidad().getId(), p.getUnidad().getNombre(), p.getUnidad().getAbreviatura(),
                p.getAgricultorId(), p.getAgricultorNombre(), p.isActivo(), p.isHabilitado());
    }
}
