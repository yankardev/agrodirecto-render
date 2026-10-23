package com.agrodirecto.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UnidadRequest(
        @NotBlank(message = "El nombre de la unidad es obligatorio") @Size(max = 40) String nombre,
        @NotBlank(message = "La abreviatura es obligatoria") @Size(max = 10) String abreviatura,
        Boolean activo) {
}
