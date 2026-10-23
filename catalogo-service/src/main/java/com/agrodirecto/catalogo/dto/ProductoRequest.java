package com.agrodirecto.catalogo.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank(message = "El nombre del producto es obligatorio") @Size(max = 100) String nombre,
        @Size(max = 300, message = "La descripción admite hasta 300 caracteres") String descripcion,
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero") BigDecimal precio,
        @NotNull(message = "El stock es obligatorio") @Min(value = 0, message = "El stock no puede ser negativo") Integer stock,
        @NotNull(message = "Elige una categoría") Long categoriaId,
        @NotNull(message = "Elige una unidad de medida") Long unidadId,
        Boolean activo) {
}
